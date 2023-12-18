package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Instruction.Constants.INPUT_DIR_TO_DIRECTION
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
        val INPUT_DIR_TO_DIRECTION = mapOf(
            Pair('U', NORTH),
            Pair('R', EAST),
            Pair('D', SOUTH),
            Pair('L', WEST),
        )
    }
}

fun main() {
    val instructions =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day18.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.map {
                val (dir, steps, colour) = it.split(' ')
                Instruction(
                    INPUT_DIR_TO_DIRECTION[dir[0]]!!,
                    steps.toLong(),
                    colour.substring(2, colour.lastIndex)
                )
            }.toList()
        }

    println("Part 1: ${solve(instructions)}")
    println("Part 2: ${part2(instructions)}")
}

private fun solve(instructions: List<Instruction>): Long {
    val longTranslationByDir = TRANSLATION_BY_DIR.entries.map {
        e -> e.key to Coords(e.value.row.toLong(), e.value.col.toLong())
    }.toMap()
    var currCoords = Coords(0L, 0L)
    var shoelaceSum = 0.0
    var shoelaceOffset = 0.0
    instructions.forEach { instr ->
        val newCoords = currCoords + (longTranslationByDir[instr.dir]!! * instr.steps)
        shoelaceSum =
            shoelaceSum + (currCoords.row * newCoords.col) - (currCoords.col * newCoords.row)
        shoelaceOffset += (instr.steps - 1) / 2.0
        currCoords = newCoords
    }

    val area = (abs(shoelaceSum) / 2) + shoelaceOffset + 1 + (instructions.size / 2.0)

    return area.roundToLong()
}

@OptIn(ExperimentalStdlibApi::class)
private fun part2(instructions: List<Instruction>): Long {
    val realInstructions = instructions.map { instr ->
        Instruction(
            when (instr.colour.last()) {
                '0' -> EAST
                '1' -> SOUTH
                '2' -> WEST
                '3' -> NORTH
                else -> error("Unknown direction ${instr.colour.last()}")
            },
            instr.colour.substring(0, instr.colour.lastIndex).lowercase().hexToLong(),
            ""
        )
    }

    return solve(realInstructions)
}
