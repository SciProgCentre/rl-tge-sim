package ru.mipt.npm.reactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.html.FlowContent
import kscience.plotly.Plot
import kscience.plotly.Plotly
import kscience.plotly.PlotlyRenderer
import kscience.plotly.layout
import kscience.plotly.models.Scatter
import kscience.plotly.models.Trace
import kscience.plotly.server.PlotlyUpdateMode
import kscience.plotly.server.serve
import kscience.plotly.server.show
import ru.mipt.npm.reactor.model.Generation
import java.lang.Integer.min
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds


fun Flow<Generation>.plotGenerations(scope: CoroutineScope): Plot {
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
    onEach { generation ->
        trace.x.numbers += generation.index
        trace.y.numbers += generation.particles.size
        //TODO add custom visualisation logic here
    }.launchIn(scope)

    return plot
}

fun Flow<Generation>.plotGenerationSizeIn(trace: Trace): Flow<Generation> = onEach { generation ->
    trace.x.numbers += generation.index
    trace.y.numbers += generation.particles.size
}

@OptIn(ExperimentalTime::class)
fun Flow<Generation>.plotXZIn(
    scatter: Scatter,
    maxParticles: Int = 500,
    delay: Duration = 300.milliseconds,
): Flow<Generation> = onEach { generation ->
    if (!generation.particles.isEmpty()) {
        val selectedParticles = List(min(generation.particles.size, maxParticles)) { generation.particles.random() }
        scatter.x.set(selectedParticles.map { it.origin.x })
        scatter.y.set(selectedParticles.map { it.origin.z })
        delay(delay)
    }
}

internal fun Plotly.show(scope: CoroutineScope, content: FlowContent.(renderer: PlotlyRenderer) -> Unit) {
    val engine = Plotly.serve(scope) {
        updateMode = PlotlyUpdateMode.PUSH

        page(content = content)
    }

    engine.show()

    scope.launch(Dispatchers.IO) {
        //stop condition for plotly server
        readLine()
        engine.stop(1000, 1000)
    }
}
