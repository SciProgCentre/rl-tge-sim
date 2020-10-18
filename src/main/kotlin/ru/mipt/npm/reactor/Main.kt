package ru.mipt.npm.reactor


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kscience.plotly.makeFile
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import ru.mipt.npm.reactor.model.generate
import java.io.File


const val programmName = "skysim"
const val version = "1.0"

fun main(args: Array<String>) {
    try {
        val options = createOptins()
        val cmd = DefaultParser().parse(options, args)
        val formatter = HelpFormatter()
        if (cmd.hasOption("help")) {
            formatter.printHelp(programmName, options)
            return
        }
        if (cmd.hasOption("version")) {
            println("Current version: $version")
            return
        }

        val generator = getRandomGenerator(cmd)
        val atmosphere = getAtmosphere(cmd, generator)
        val limit = getLimitOfPhotons(cmd)
        val seed = getSeed(cmd, atmosphere)

        val plot = GlobalScope.async {
            val plot = atmosphere.generate(generator, seed).onEach { generation ->
                if (generation.particles.isEmpty()) {
                    println("No photons it the generation. Finishing.")
                } else if (generation.particles.size > limit) {
                    println("Generation size is too large. Finishing")
                }
            }.takeWhile { it.particles.size in (1..limit) }
                .withLogging(cmd)
                .plotGenerations(this)


            if (cmd.hasOption("dynamic-plot")) {
                val engine = plot.showDynamic(this)
                launch(Dispatchers.IO) {
                    //stop condition for plotly server
                    readLine()
                    engine.stop(1000, 1000)
                }
            }
            plot
        }

        if (cmd.hasOption("save-plot")) {
            val file = File(cmd.getOptionValue("save-plot"))
            runBlocking {
                plot.await().makeFile(file.toPath())
            }
        }

    } catch (exp: ParseException) {
        println(exp)
        println("Malformed CLI arguments")
        println("Use \"$programmName --help\" for additional information")
    }

}


