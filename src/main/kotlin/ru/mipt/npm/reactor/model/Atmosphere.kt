package ru.mipt.npm.reactor.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator
import kotlin.streams.toList


//length in meters
//energy in MeV
//field in MeV/m

/**
 * @param origin - point of birth
 * @param direction
 * @param energy
 */
sealed class Particle(val origin: Vector3D, val direction: Vector3D, val energy: Double = 0.0, val generation: Int = 0)

class Photon(origin: Vector3D, direction: Vector3D, energy: Double, generation: Int = 0) :
    Particle(origin, direction, energy, generation)

class Electron(origin: Vector3D, direction: Vector3D, energy: Double, generation: Int = 0) :
    Particle(origin, direction, energy, generation)


val defaultGenerator = JDKRandomGenerator()

interface Field {
    operator fun invoke(point: Vector3D): Vector3D

    /**
     * True if the point is inside tracking volume
     */
    fun inField(point: Vector3D): Boolean = (invoke(point) != Vector3D.ZERO)
}

interface Atmosphere {
    val field: Field

    fun Photon.interactionPoint(rng: RandomGenerator = defaultGenerator): Vector3D

    /**
     * Simulate acceleration of an electron inside the cell
     * @param point optional point of interaction of electron with cell. By default use point of origin
     */
    fun Electron.accelerate(rng: RandomGenerator, point: Vector3D = origin): Collection<Particle>

    /**
     * Convert a photon into electron via photo-effect, compton or other mechanism
     * TODO consider multiple electrons
     */
    fun Photon.convert(rng: RandomGenerator, point: Vector3D): Electron

    fun Photon.ignite(rng: RandomGenerator, point: Vector3D = interactionPoint(rng)): Collection<Particle> {
        val electron = convert(rng, point)
        return if (field.inField(electron.origin)) {
            electron.accelerate(rng = rng)
        } else {
            emptyList()
        }
    }
}


/**
 * Get next generation of photons.
 */
fun Atmosphere.nextGeneration(
    rng: RandomGenerator,
    particles: Collection<Particle>,
): Collection<Particle> {
    return particles.stream().parallel().flatMap { particle ->
        when (particle) {
            is Photon -> particle.ignite(rng).stream()
            is Electron -> particle.accelerate(rng).stream()
        }
    }.toList()
}

data class Generation(val index: Int, val particles: Collection<Particle>)

/**
 * Generate a flow of particle generations
 */
fun Atmosphere.generate(rng: RandomGenerator, seed: Collection<Particle>): Flow<Generation> = flow {
    var currentGeneration: Collection<Particle> = seed
    var index = 0
    while (!currentGeneration.isEmpty()) {
        emit(Generation(index++, currentGeneration))
        currentGeneration = nextGeneration(rng, currentGeneration)
    }
}

/**
 * Generate a flow of particle generations
 */
fun Atmosphere.generate(rng: RandomGenerator, seed: Particle) = this.generate(rng, listOf(seed))