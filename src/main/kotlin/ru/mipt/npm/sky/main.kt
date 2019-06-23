package ru.mipt.npm.sky

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator

fun main() {
    val generator = SynchronizedRandomGenerator(JDKRandomGenerator(1122))

    val atmosphere = SimpleAtmosphere(1.6,rng = generator)
    val seed = (1..100).map {
        Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0)
    }

    val flow = atmosphere.generate(generator, seed)
    runBlocking {
        var i = 1;
        flow.onEach {generation->
            if (generation.isEmpty()) {
                println("No photons it the generation. Terminating.")
            } else if (generation.size > 10000) {
                println("Generation size is too large. Terminating")
            }
        }.takeWhile { it.size in (1..10000) }.collect { generation ->
            val height = generation.map { it.origin.z }.average()
            println("There are ${generation.size} photons in generation $i. Average height is $height")
            i++
        }
    }
}