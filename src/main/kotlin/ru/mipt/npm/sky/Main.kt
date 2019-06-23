package ru.mipt.npm.sky


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
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
        val app = App.fromCmd(cmd)
        app.processors.add(getWriter(cmd))

        if (cmd.hasOption("save-plot")) {
            app.processors.add(Plotter.plotter)
        }

        if (cmd.hasOption("dynamic-plot")){
            app.processors.add(DynamicServer.plotter)
            DynamicServer.draw()
        }

        app.start()

        if (cmd.hasOption("save-plot")) {
            val file = File(cmd.getOptionValue("save-plot"))
            Plotter.save(file)
        }

        if (cmd.hasOption("dynamic-plot")){
            DynamicServer.stop()
        }


    } catch (exp: ParseException) {
        println(exp)
        println("Bad CLI apruments")
        println("Use \"$programmName --help\" for additional information")
    }

}



class App(
    val flow : Flow<Collection<Particle>>,
    val limit: Int
){
    val processors: MutableList<(index: Int, generation: Collection<Particle>)-> Unit> = mutableListOf()

    fun start() {
        runBlocking {
            var i = 1;
            flow.onEach { generation ->
                if (generation.isEmpty()) {
                    println("No photons it the generation. Finishing.")
                } else if (generation.size > limit) {
                    println("Generation size is too large. Finishing")
                }
            }.takeWhile { it.size in (1..limit) }.collect { generation ->
                processors.forEach { it.invoke(i, generation) }
                i++
            }
        }
    }

    companion object{
        fun fromCmd(cmd : CommandLine) : App{
            val generator = getRandomGenerator(cmd)
            val atmosphere = getAtmosphere(cmd, generator)
            val limit = getLimitOfPhotons(cmd)
            val seed = getSeed(cmd, atmosphere)
            return App( atmosphere.generate(generator, seed), limit)
        }
    }
}



