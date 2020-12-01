package ru.mipt.npm.reactor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformWhile
import ru.mipt.npm.reactor.model.Generation


@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<Generation>.limit(limit: Int): Flow<Generation> = transformWhile { generation ->
    when {
        generation.particles.isEmpty() -> {
            println("No photons it the generation. Finishing.")
            false
        }
        generation.particles.size > limit -> {
            println("Generation size is too large. Finishing")
            false
        }
        else -> {
            emit(generation)
            true
        }
    }
}