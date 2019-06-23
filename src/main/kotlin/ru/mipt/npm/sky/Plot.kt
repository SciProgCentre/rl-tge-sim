package ru.mipt.npm.sky

import hep.dataforge.meta.buildMeta
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.commons.cli.CommandLine
import scientifik.plotly.Plotly
import scientifik.plotly.makeFile
import scientifik.plotly.models.Trace
import scientifik.plotly.server.serve
import scientifik.plotly.trace
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

val titleTGE = "Reactor like TGE"
val xlabel = "Number of generation"
val ylabel = "Number of gamma-quanta"

object Plotter{
    val x = mutableListOf<Double>()
    val y = mutableListOf<Double>()

    val plot = Plotly.plot2D {
        //        trace(x, y) {
//            name = "for a single trace in graph its name would be hidden"
//        }
        layout {
            title = titleTGE
            xaxis {
                title = xlabel
            }
            yaxis {
                title = ylabel
            }
        }
    }

    val plotter = {
            index: Int, generation: Collection<Particle> ->
        x.add(index.toDouble())
        y.add(generation.size.toDouble())
        Unit
    }

    fun save(file: File){
        plot.trace(x, y) {
            name = "for a single trace in graph its name would be hidden"
        }
        plot.makeFile(file, show = false)
    }
}

object DynamicServer{

    val serverMeta = buildMeta {
        "update" to {
            "enabled" to true
            //"interval" to 20
        }
    }

    private val gens = mutableListOf<Double>(0.0)
    private val numbers = mutableListOf<Double>(0.0)

    private val x = Channel<Double>()
    private val y = Channel<Double>()

    val trace = Trace.build(x = gens, y = numbers) { name = "sin" }

    val server = Plotly.serve(serverMeta) {



        //root level plots go to default page
        plot {
            trace(trace)
            layout {
                title = titleTGE
                xaxis { title = xlabel}
                yaxis { title = ylabel }
            }
        }

    }

    val plotter = {
        index: Int, generation: Collection<Particle> ->

        server.apply{
            launch{


                x.send(index.toDouble())
                y.send(generation.size.toDouble())
            }

        }
        Unit

    }

    fun draw(){
        server.apply{
            launch{
                while(isActive){
                    kotlinx.coroutines.delay(10)
                    gens.add(x.receive())
                    numbers.add(y.receive())
                    trace.x = gens
                    trace.y = numbers
                }

            }
        }
    }

    fun stop(){
        println("Press Enter to close server")
        readLine()
        server.stop()
    }
}
