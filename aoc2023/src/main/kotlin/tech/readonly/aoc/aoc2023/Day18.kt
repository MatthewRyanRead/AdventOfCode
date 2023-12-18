package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Instruction.Constants.DIR_BY_CMD
import tech.readonly.aoc.aoc2023.Instruction.Constants.DIR_BY_HEX
import tech.readonly.aoc.aoc2023.util.Coords
import tech.readonly.aoc.aoc2023.util.Direction
import tech.readonly.aoc.aoc2023.util.Direction.Constants.TRANSLATION_BY_DIR
import tech.readonly.aoc.aoc2023.util.Direction.EAST
import tech.readonly.aoc.aoc2023.util.Direction.NORTH
import tech.readonly.aoc.aoc2023.util.Direction.SOUTH
import tech.readonly.aoc.aoc2023.util.Direction.WEST
import java.io.File
import java.util.Scanner
import kotlin.math.abs
import kotlin.math.roundToLong

private data class Instruction(val dir: Direction, val steps: Long, val colour: String) {
    companion object Constants {
        val DIR_BY_CMD = mapOf(
            Pair('U', NORTH),
            Pair('R', EAST),
            Pair('D', SOUTH),
            Pair('L', WEST),
        )
        val DIR_BY_HEX = mapOf(
            Pair('3', NORTH),
            Pair('0', EAST),
            Pair('1', SOUTH),
            Pair('2', WEST),
        )
    }
}

fun main() {
    val instructions =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day18.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.map {
                val (dir, steps, colour) = it.split(' ')
                Instruction(
                    DIR_BY_CMD[dir[0]]!!,
                    steps.toLong(),
                    colour.substring(2, colour.lastIndex)
                )
            }.toList()
        }

    println("Part 1: ${solve(instructions)}")
    println("Part 2: ${part2(instructions)}")
}

private fun solve(instructions: List<Instruction>): Long {
    val longTranslationByDir = TRANSLATION_BY_DIR.entries.associate { e ->
        e.key to Coords(e.value.row.toLong(), e.value.col.toLong())
    }
    var currCoords = Coords(0L, 0L)
    var shoelaceSum = 0.0
    var boundingPoints = 1L
    instructions.forEach { instr ->
        val newCoords = currCoords + (longTranslationByDir[instr.dir]!! * instr.steps)
        shoelaceSum =
            shoelaceSum + (currCoords.row * newCoords.col) - (currCoords.col * newCoords.row)
        boundingPoints += instr.steps
        currCoords = newCoords
    }

    val area = (abs(shoelaceSum) + boundingPoints) / 2.0

    return area.roundToLong()
}

@OptIn(ExperimentalStdlibApi::class)
private fun part2(instructions: List<Instruction>): Long {
    val realInstructions = instructions.map { instr ->
        Instruction(
            DIR_BY_HEX[instr.colour.last()]!!,
            instr.colour.substring(0, instr.colour.lastIndex).lowercase().hexToLong(),
            ""
        )
    }

    return solve(realInstructions)
}
