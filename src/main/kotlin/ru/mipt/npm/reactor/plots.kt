package ru.mipt.npm.reactor

import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kscience.plotly.*
import kscience.plotly.Plotly.plot
import kscience.plotly.models.*
import kscience.plotly.server.serve
import ru.mipt.npm.reactor.model.Generation


fun Flow<Generation>.plotGenerations(scope: CoroutineScope): Plot {
    val trace = Scatter {
        name = "Generation size"
    }
    val plot = plot {
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
    onEach { generation ->
        trace.x.numbers += generation.index
        trace.y.numbers += generation.particles.size
        //TODO add custom visualisation logic here
    }.launchIn(scope)

    return plot
}

fun Plot.showDynamic(scope: CoroutineScope): ApplicationEngine = Plotly.serve(scope) {
    page { renderer ->
        plot(this@showDynamic, renderer = renderer)
    }
}
