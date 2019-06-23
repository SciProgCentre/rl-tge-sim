package ru.mipt.npm.sky

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

fun main() {
    val atmosphere = SimpleAtmosphere()
    val seed = Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0)

    val flow = atmosphere.generate(defaultGenerator, seed)
    runBlocking {
        var i = 1;
        flow.takeWhile { it.size < 10000 }.collect { generation ->
            if (generation.isEmpty()) {
                println("No photons it the generation. Terminating.")
                cancel()
            }
            val height = generation.stream().mapToDouble { it.origin.z }.average().orElse(Double.NaN)
            println("There are ${generation.size} photons in generation $i. Average height is $height")
            i++
        }
    }
}