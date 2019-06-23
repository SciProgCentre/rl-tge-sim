package ru.mipt.npm.sky

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.SynchronizedRandomGenerator
import java.lang.Exception
import kotlin.system.exitProcess

fun createOptins(): Options {
    val options = Options()

    options.addOption(
        Option
            .builder("h")
            .longOpt("help")
            .desc("print this message and exit")
            .required(false)
            .build()

    )
    options.addOption(
        Option
            .builder("v")
            .longOpt("version")
            .desc("print information about version and exit")
            .required(false)
            .build()
    )

    options.addOption(
        Option
            .builder("o")
            .longOpt("output")
            .hasArg(true)
            .desc("print simulation result in the file with given name")
            .required(false)
            .argName("FILENAME")
            .build()

    )

    options.addOption(
        Option
            .builder("g")
            .longOpt("gain")
            .hasArg(true)
            .desc("set the local coefficient of gamma multiplication")
            .required(false)
            .argName("NUMBER")
            .build()

    )

    options.addOption(
        Option
            .builder()
            .longOpt("free-path")
            .hasArg(true)
            .desc("set the photon free mean path")
            .required(false)
            .argName("NUMBER")
            .build()

    )
    options.addOption(
        Option
            .builder()
            .longOpt("cell-length")
            .hasArg(true)
            .desc("set the length of acceleration cell (influence on birth point of new photon)")
            .required(false)
            .argName("NUMBER")
            .build()

    )
    options.addOption(
        Option
            .builder()
            .longOpt("cloud-size")
            .hasArg(true)
            .desc("set the cloud size")
            .required(false)
            .argName("NUMBER")
            .build()

    )
    options.addOption(
        Option
            .builder()
            .longOpt("field-magnitude")
            .hasArg(true)
            .desc("set the field magnitude")
            .required(false)
            .argName("NUMBER")
            .build()

    )
    options.addOption(
        Option
            .builder("l")
            .longOpt("particle-limit")
            .hasArg(true)
            .desc("set the upper limit of number of particle")
            .required(false)
            .argName("NUMBER")
            .build()


    )
    options.addOption(
        Option
            .builder("s")
            .longOpt("seed")
            .hasArg(true)
            .desc("set the random generator seed")
            .required(false)
            .argName("NUMBER")
            .build()


    )
    return options
}

fun parseError(exp: Exception, messege: String = "") {
    println(exp)
    print(messege)
    println("Bad CLI apruments")
    println("Use \"$programmName --help\" for additional information")
    exitProcess(1)
}

fun getAtmosphere(cmd: CommandLine): SimpleAtmosphere {
    val init = object {
        var multiplication: Double = 2.0
        var photonFreePath: Double = 100.0
        var cellLength: Double = 100.0
        var cloudSize: Double = 1000.0
        var fieldMagnitude: Double = 0.2
    }

    try {
        when {
            cmd.hasOption("gain") -> init.multiplication = cmd.getOptionValue("gain").toDouble()
            cmd.hasOption("free-path") -> init.photonFreePath = cmd.getOptionValue("gain").toDouble()
            cmd.hasOption("cell-length") -> init.cellLength = cmd.getOptionValue("gain").toDouble()
            cmd.hasOption("cloud-size") -> init.cloudSize = cmd.getOptionValue("gain").toDouble()
            cmd.hasOption("field-magnitude") -> init.fieldMagnitude = cmd.getOptionValue("gain").toDouble()
        }
    } catch (exp: Exception) {
        parseError(exp)
    }
    return SimpleAtmosphere(
        init.multiplication,
        init.photonFreePath,
        init.cellLength,
        init.cloudSize,
        init.fieldMagnitude
    )
}

fun getLimitOfPhotons(cmd: CommandLine): Int {
    if (cmd.hasOption("particle-limit")) {
        try {
            return cmd.getOptionValue("particle-limit").toInt()
        } catch (exp: Exception) {
            parseError(exp, "Options particle-limit can't be equal ${cmd.getOptionValue("particle-limit")}\n")
        }
    }
    return 10000
}

fun getRandomGenerator(cmd: CommandLine): RandomGenerator {
    if (cmd.hasOption("seed")) {
        try {
            val seed = cmd.getOptionValue("particle-limit").toInt()
            return SynchronizedRandomGenerator(JDKRandomGenerator(seed))
        } catch (exp: Exception) {
            parseError(exp, "Options particle-limit can't be equal ${cmd.getOptionValue("particle-limit")}\n")
        }
    }
    return SynchronizedRandomGenerator(JDKRandomGenerator())
}