package ru.mipt.npm.sky

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import java.io.FileNotFoundException
import java.nio.file.Path
import java.nio.file.Paths

const val programmName = "skysim"
const val version = "1.0"

fun main(args: Array<String>) {
    try {
        val options = createOptins()
        val cmd = DefaultParser().parse(options, args)
        val formatter = HelpFormatter()
        if (cmd.hasOption("help")){
            formatter.printHelp(programmName, options)
            return
        }
        if (cmd.hasOption("version")){
            println("Current version: $version")
            return
        }

        temp2(cmd)

    } catch (exp: ParseException) {
        println(exp)
        println("Bad CLI apruments")
        println("Use \"$programmName --help\" for additional information")
    }

}

fun runToFile(flow: Flow<Collection<Particle>>, outPath : Path, limit : Int){



}

fun starComputatuon(flow: Flow<Collection<Particle>>,collector : ( index: Int, generation: Collection<Particle>) -> Unit, limit: Int){
    runBlocking {
        var i = 1;
        flow.onEach {generation->
            if (generation.isEmpty()) {
                println("No photons it the generation. Finishing.")
            } else if (generation.size > limit) {
                println("Generation size is too large. Finishing")
            }
        }.takeWhile { it.size in (1..limit) }.collect { generation ->
            collector(i, generation)
            i++
        }
    }
}

fun temp2(cmd : CommandLine){
    val atmosphere = getAtmosphere(cmd)
    val limit = getLimitOfPhotons(cmd)
    val generator = getRandomGenerator(cmd)

    val seed = Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0)
    val flow = atmosphere.generate(generator, seed)

    val collector : ( index: Int, generation: Collection<Particle>) -> Unit

    if (cmd.hasOption("o")){
        try{
            val path = Paths.get(cmd.getOptionValue("o"))
            val writer  = path.toFile().printWriter()
            writer.println("%10s %7s %7s".format("generation", "number", "height"))
            collector = {indx, generation ->
                val height = generation.map { it.origin.z }.average()
                writer.println("%10d %7d %7.2f".format(indx, generation.size, height))
            }
            starComputatuon(flow,collector,limit)
            writer.close()
        }
        catch (exp: FileNotFoundException){
            println("Filename ${cmd.getOptionValue("o")} is bad")
        }
    }
    else{
        collector = {indx, generation ->
            val height = generation.map { it.origin.z }.average()
            println("There are ${generation.size} photons in generation $indx. Average height is $height")
        }
        starComputatuon(flow,collector,limit)
    }



}

fun temp() {
    val atmosphere = SimpleAtmosphere()
    val seed = Photon(Vector3D(0.0, 0.0, atmosphere.cloudSize / 2), Vector3D(0.0, 0.0, -1.0), 1.0)

    val flow = atmosphere.generate(defaultGenerator, seed)
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