package ru.mipt.npm.reactor


import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kscience.plotly.Plotly
import kscience.plotly.layout
import kscience.plotly.makeFile
import kscience.plotly.models.Scatter
import kscience.plotly.plot
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

        val trace = Scatter {
            name = "Generation size"
        }

        val plot = Plotly.plot {
            traces(trace)
            layout {
                title = "Reactor like TGE"
                xaxis {
                    title = "Generation number"
                }
                yaxis {
                    title = "Number of photons"
                }
            }
        }

        runBlocking {
            atmosphere.generate(generator, seed)
                .limit(limit)
                .withLogging(cmd)
                .plotGenerationSizeIn(trace)
                .onCompletion {
                    if (cmd.hasOption("save-plot")) {
                        val file = File(cmd.getOptionValue("save-plot"))
                        plot.makeFile(file.toPath())
                    }
                }
                .launchIn(this)


            if (cmd.hasOption("dynamic-plot")) {
                Plotly.show(this) { renderer ->
                    plot(plot, renderer = renderer)
                }
            }
        }

    } catch (exp: ParseException) {
        println(exp)
        println("Malformed CLI arguments")
        println("Use \"$programmName --help\" for additional information")
    }

}


