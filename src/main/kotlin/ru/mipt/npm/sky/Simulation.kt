package ru.mipt.npm.sky

import org.apache.commons.math3.distribution.ExponentialDistribution
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator
import java.util.stream.Stream
import kotlin.streams.toList


val rnd: RandomGenerator = SynchronizedRandomGenerator(JDKRandomGenerator())


//length in meters

/**
 * @param origin - point of birth
 * @param direction
 * @param energy
 */
data class Photon(val origin: Vector3D, val direction: Vector3D, val energy: Double = 0.0)

interface Field {
    fun fieldAt(point: Vector3D): Vector3D
}

/**
 * Generate random direction field of the same magnitude
 */
class RandomField(val magnitude: Double) : Field {
    override fun fieldAt(point: Vector3D): Vector3D {
        return Vector3D(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5).scalarMultiply(2 * magnitude);
    }
}

val randomField = RandomField(2.0);

/**
 * Mean free path = 100 m
 */
val exponential = ExponentialDistribution(100.0)

internal fun generateInteractionPoint(photon: Photon): Vector3D {
    val path = exponential.sample();
    return photon.origin.add(photon.direction.scalarMultiply(path));
}

//private fun getAngleDistribution(file: File): RealDistribution {
//    val points = ArrayList<Double>();
//    val probs = ArrayList<Double>();
//    file.forEachLine{ line->
//        val split = line.split("\\s+");
//        points += split[0].trim().toDouble()
//        probs += split[1].trim().toDouble()
//    }
//    return EnumeratedRealDistribution(rnd,points.toDoubleArray(),probs.toDoubleArray())
//}
//
//val angleDistribution = getAngleDistribution(File("bremsstahlung.dat"));

val numPhotons = 2

internal fun generatePhotons(point: Vector3D): List<Photon> {
    val field = randomField.fieldAt(point)
    return (1..numPhotons).map {
        Photon(point, field.normalize())
    }
}

val volumeSize = 1000.0;

/**
 * Check that interaction point is inside the interacting volume
 */
internal fun inVolume(point: Vector3D): Boolean {
    return Math.abs(point.x) <= volumeSize / 2 && Math.abs(point.y) <= volumeSize / 2 && Math.abs(point.z) <= volumeSize / 2;
}

/**
 * Get next generation of photons.
 */
internal fun nextGeneration(photons: List<Photon>): List<Photon> {
    return photons.stream().parallel().flatMap {
        val interactionPoint = generateInteractionPoint(it);
        if (inVolume(interactionPoint)) {
            generatePhotons(interactionPoint).stream()
        } else {
            Stream.empty()
        }
    }.toList();
}

fun main(vararg args: String) {
    var generation: List<Photon> = listOf(Photon(Vector3D(0.0, 0.0, volumeSize / 2), Vector3D(0.0, 0.0, -1.0)))

    (1..15).forEach {
        generation = nextGeneration(generation);
        println("There are ${generation.size} photons in generation $it")
    }
}
