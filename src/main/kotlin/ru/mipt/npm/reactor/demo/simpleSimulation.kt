package ru.mipt.npm.reactor.demo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator
import ru.mipt.npm.reactor.model.Photon
import ru.mipt.npm.reactor.model.SimpleAtmosphere
import ru.mipt.npm.reactor.model.generate
import ru.mipt.npm.reactor.plotGenerations
import ru.mipt.npm.reactor.showDynamic

fun main() {
    val generator = SynchronizedRandomGenerator(JDKRandomGenerator(11123))

    val multiplication: Double = 2.0
    val photonFreePath: Double = 100.0
    val cellLength: Double = 100.0
    val cloudSize: Double = 1000.0
    val fieldMagnitude: Double = 0.2

    val atmosphere = SimpleAtmosphere(
        multiplication,
        photonFreePath,
        cellLength,
        cloudSize,
        fieldMagnitude,
        generator
    )

    val limit = 1e6.toInt()
    val seed = listOf(Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0))

    runBlocking {
        val engine = atmosphere.generate(generator, seed).onEach { generation ->
            println("Finished generation ${generation.index} with ${generation.particles.size} particles")
            if (generation.particles.isEmpty()) {
                println("No photons it the generation. Finishing.")
            } else if (generation.particles.size > limit) {
                println("Generation size is too large. Finishing")
            }
        }.takeWhile { it.particles.size in (1..limit) }.plotGenerations(this).showDynamic(this)

        launch(Dispatchers.IO) {
            //stop condition for plotly server
            readLine()
            engine.stop(1000, 1000)
        }
    }
}
