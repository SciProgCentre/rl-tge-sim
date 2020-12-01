package ru.mipt.npm.reactor.model

import org.apache.commons.math3.distribution.ExponentialDistribution
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.UnitSphereRandomVectorGenerator
import kotlin.math.abs


/**
 * Generate random direction field of the same magnitude
 */
class RandomField(generator: RandomGenerator, val magnitude: Double, val size: Double) : Field {
    private val sph = UnitSphereRandomVectorGenerator(3, generator)
    override fun invoke(point: Vector3D): Vector3D {
        return if (point.toArray().all { abs(it) < size / 2 }) {
            Vector3D(sph.nextVector()).scalarMultiply(magnitude)
        } else {
            Vector3D.ZERO
        }
    }
}


class SimpleAtmosphere(
    val multiplication: Double = 2.0,
    val photonFreePath: Double = 100.0,
    val cellLength: Double = 300.0,
    val cloudSize: Double = 1000.0,
    val fieldMagnitude: Double = 0.2,
    rng: RandomGenerator = defaultGenerator
) : Atmosphere {
    override val field: Field = RandomField(rng, fieldMagnitude, cloudSize)

    override fun Photon.interactionPoint(rng: RandomGenerator): Vector3D {
        val exponential = ExponentialDistribution(rng, photonFreePath)
        val path = exponential.sample()
        return origin.add(direction.scalarMultiply(path));
    }

    /**
     * Specifies if an electron produced in specific point with given direction and energy ignites the cell
     */
    private fun Electron.ignites(): Boolean {
        return true
        //return Vector3D.angle(direction, field(origin)) < 2 * Math.PI / 3;
    }


    override fun Photon.convert(rng: RandomGenerator, point: Vector3D): Electron {
        //keep the same generation as for Photon
        return Electron(point, direction, energy, generation)
    }

    override fun Electron.accelerate(rng: RandomGenerator, point: Vector3D): List<Particle> {
        return if (!ignites()) {
            emptyList()
        } else {
            val fieldValue = field(point)
            // offset the photon birth point by cell length
            val photonOrigin = point.add(fieldValue.normalize().scalarMultiply(cellLength))

            val numPhotons =
                PoissonDistribution(
                    rng,
                    multiplication,
                    PoissonDistribution.DEFAULT_EPSILON,
                    PoissonDistribution.DEFAULT_MAX_ITERATIONS
                ).sample()

            (1..numPhotons).map {
                //increment the generation
                Photon(photonOrigin, fieldValue.normalize(), 1.0, generation + 1)
            }
        }
    }


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
