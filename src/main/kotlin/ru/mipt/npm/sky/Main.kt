package ru.mipt.npm.sky

import hep.dataforge.meta.buildMeta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import scientifik.plotly.Plot2D
import scientifik.plotly.Plotly
import scientifik.plotly.makeFile
import scientifik.plotly.models.Trace
import scientifik.plotly.trace
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Paths
import kotlin.math.PI
import kotlin.math.sin

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
        application(cmd)

    } catch (exp: ParseException) {
        println(exp)
        println("Bad CLI apruments")
        println("Use \"$programmName --help\" for additional information")
    }

}

fun startComputation(
    flow: Flow<Collection<Particle>>,
    processors: List<(index: Int, generation: Collection<Particle>) -> Unit>,
    limit: Int
) {
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

fun application(cmd: CommandLine) {
    val generator = getRandomGenerator(cmd)
    val atmosphere = getAtmosphere(cmd, generator)
    val limit = getLimitOfPhotons(cmd)
    val seed = getSeed(cmd, atmosphere)

    val flow = atmosphere.generate(generator, seed)
    val collector: (index: Int, generation: Collection<Particle>) -> Unit
    val writer: BufferedWriter
    if (cmd.hasOption("o")) {
//        try{
        val path = Paths.get(cmd.getOptionValue("o")).toFile()
        writer = path.bufferedWriter()
        writer.write("%10s %7s %7s".format("generation", "number", "height\n"))

//
//        }
//        catch (exp: FileNotFoundException){
//            println("Filename ${cmd.getOptionValue("o")} is bad")
//        }
        collector = { indx, generation ->
            val height = generation.map { it.origin.z }.average()
            writer.write("%10d %7d %7.2f\n".format(indx, generation.size, height))
        }
    } else {
        writer = System.out.bufferedWriter()
        collector = { indx, generation ->
            val height = generation.map { it.origin.z }.average()
            writer.write("There are ${generation.size} photons in generation $indx. Average height is $height")
            writer.newLine()
        }
    }
    val proccesors = mutableListOf<(index: Int, generation: Collection<Particle>) -> Unit>()

    proccesors.add(collector)
    var plot: Plot2D? = null
    val x = mutableListOf<Double>()
    val y = mutableListOf<Double>()
    if (cmd.hasOption("save-plot")) {
        plot = Plotly.plot2D {
            //        trace(x, y) {
//            name = "for a single trace in graph its name would be hidden"
//        }
            layout {
                title = "Graph name"
                xaxis {
                    title = "x axis"
                }
                yaxis {
                    title = "y axis"
                }
            }
        }
        proccesors.add { index: Int, generation: Collection<Particle> ->
            x.add(index.toDouble())
            y.add(generation.size.toDouble())
        }
    }


    val server : PlotlyServer? = null
    if (cmd.hasOption("dynamic-plot")) {
        val serverMeta = buildMeta {
            "update" to {
                "enabled" to true
                //"interval" to 20
            }
        }

        val server = Plotly.serve(serverMeta) {
            val x = (0..100).map { it.toDouble() / 100.0 }
            val y = x.map { sin(2.0 * PI * it) }

            val trace = Trace.build(x = x, y = y) { name = "sin" }


            //root level plots go to default page

            plot {
                trace(trace)
                layout {
                    title = "Dynamic plot"
                    xaxis { title = "x axis name" }
                    yaxis { title = "y axis name" }
                }
            }

            launch {
                var time: Long = 0
                while (isActive) {
                    delay(10)
                    time += 10
                    val dynamicY = x.map { sin(2.0 * PI * (it + time.toDouble() / 1000.0)) }
                    trace.y = dynamicY
                }
            }
        }
        println("Press Enter to close server")
        readLine()

        server.stop()

    }

    startComputation(flow, proccesors, limit)
    if (plot != null) {
        val file = File(cmd.getOptionValue("save-plot"))
        plot.trace(x, y)
        plot.makeFile(file, show = false)
        TODO("filename analisys")
    }
    writer.close()
}
