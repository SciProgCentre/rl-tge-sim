package ru.mipt.npm.reactor.demo

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.runBlocking
import kotlinx.html.hr
import kscience.plotly.Plotly
import kscience.plotly.layout
import kscience.plotly.models.Scatter
import kscience.plotly.models.ScatterMode
import kscience.plotly.plot
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator
import ru.mipt.npm.reactor.limit
import ru.mipt.npm.reactor.model.Particle
import ru.mipt.npm.reactor.model.Photon
import ru.mipt.npm.reactor.model.SimpleAtmosphere
import ru.mipt.npm.reactor.model.generate
import ru.mipt.npm.reactor.plotGenerationSizeIn
import ru.mipt.npm.reactor.plotXZIn
import ru.mipt.npm.reactor.show
import java.util.*

fun main() {
    val generator = SynchronizedRandomGenerator(JDKRandomGenerator(11123))

    val multiplication: Double = 1.5
    val photonFreePath: Double = 100.0
    val cellLength: Double = 300.0
    val cloudSize: Double = 1200.0
    val fieldMagnitude: Double = 0.2
    val initialParticles = 100

    val atmosphere = SimpleAtmosphere(
        multiplication,
        photonFreePath,
        cellLength,
        cloudSize,
        fieldMagnitude,
        generator
    )

    val limit = 1e6.toInt()
    val seed: List<Particle> = Collections.nCopies(initialParticles, Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0))

    val generationTrace = Scatter {
        mode = ScatterMode.`lines+markers`
    }

    val distributionTrace = Scatter{
        mode = ScatterMode.markers
    }


    runBlocking {
        atmosphere.generate(generator, seed)
            .limit(limit)
            .plotGenerationSizeIn(generationTrace)
            .plotXZIn(distributionTrace)
            .launchIn(this)

        Plotly.show(this) { renderer ->
            plot(renderer = renderer){
                traces(generationTrace)
                layout {
                    title = "Generation size"
                    xaxis {
                        title = "Generation number"
                    }
                    yaxis {
                        title = "Number of photons"
                    }
                }
            }
            hr()
            plot(renderer = renderer){
                traces(distributionTrace)
                layout {
                    title = "XZ distribution"
                    xaxis {
                        title = "X"
                        range = -cloudSize/2 .. cloudSize/2
                    }
                    yaxis {
                        title = "Z"
                        range = -cloudSize/2 .. cloudSize/2
                    }
                }
            }
        }
    }
}
