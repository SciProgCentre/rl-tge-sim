package ru.mipt.npm.sky

import org.apache.commons.math3.distribution.ExponentialDistribution
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.distribution.PoissonDistribution.DEFAULT_EPSILON
import org.apache.commons.math3.distribution.PoissonDistribution.DEFAULT_MAX_ITERATIONS
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator
import java.util.stream.Stream
import kotlin.streams.toList


val rnd: RandomGenerator = SynchronizedRandomGenerator(JDKRandomGenerator())

/**
 * Average multiplication factor per cell
 */
val multiplication = 2.0

/**
 * Mean free path
 */
val freePath = 100.0

/**
 * The side of the volume cube
 */
val volumeSize = 1000.0;

/**
 * Offset of photon birth point relative to electron interaction point
 */
val originOffset = 100.0


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
val exponential = ExponentialDistribution(freePath)

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


val poisson = PoissonDistribution(rnd, multiplication, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS)

internal fun generatePhotons(point: Vector3D, field: Vector3D): List<Photon> {
    val numPhotons = poisson.sample();
    /**
     * Propagating origin point along the field
     */
    val originPoint = point.add(field.normalize().scalarMultiply(originOffset))

    return (1..numPhotons).map {
        Photon(originPoint, field.normalize())
    }
}

/**
 * Check that interaction point is inside the interacting volume
 */
internal fun inVolume(point: Vector3D): Boolean {
    return Math.abs(point.x) <= volumeSize / 2 && Math.abs(point.y) <= volumeSize / 2 && Math.abs(point.z) <= volumeSize / 2;
}

internal fun isAllowedAngle(photon: Photon, field: Vector3D): Boolean {
    return Vector3D.angle(photon.direction, field) < 2 * Math.PI / 3;
}

/**
 * Get next generation of photons.
 */
internal fun nextGeneration(photons: List<Photon>): List<Photon> {
    return photons.stream().parallel().flatMap {
        val interactionPoint = generateInteractionPoint(it);
        val field = randomField.fieldAt(interactionPoint)
        if (inVolume(interactionPoint) && isAllowedAngle(it, field)) {
            generatePhotons(interactionPoint, field).stream()
        } else {
            Stream.empty()
        }
    }.toList();
}

fun main(vararg args: String) {
    var generation: List<Photon> = listOf(Photon(Vector3D(0.0, 0.0, volumeSize / 2), Vector3D(0.0, 0.0, -1.0)))

    var i = 1;
    var size = 0;

    while (size < 1000){
        generation = nextGeneration(generation);
        size = generation.size

        if (generation.isEmpty()) {
            println("No photons it the generation. Terminating.")
            return
        }
        val height = generation.stream().mapToDouble { it.origin.z }.average().orElse(Double.NaN)
        println("There are ${size} photons in generation $i. Average height is $height")
        i++
    }
}
