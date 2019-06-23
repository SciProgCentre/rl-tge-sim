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
//plot()
        application(cmd)

    } catch (exp: ParseException) {
        println(exp)
        println("Bad CLI apruments")
        println("Use \"$programmName --help\" for additional information")
    }

}

fun startComputation(flow: Flow<Collection<Particle>>, collector : (index: Int, generation: Collection<Particle>) -> Unit, limit: Int){
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

fun application(cmd : CommandLine){
    val generator = getRandomGenerator(cmd)
    val atmosphere = getAtmosphere(cmd, generator)
    val limit = getLimitOfPhotons(cmd)
    val seed = getSeed(cmd, atmosphere)

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
            startComputation(flow,collector,limit)
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
        startComputation(flow,collector,limit)
    }



}
