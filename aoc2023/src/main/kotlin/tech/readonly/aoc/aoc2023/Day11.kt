package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.util.Coords
import java.io.File
import java.util.Scanner
import kotlin.math.max

fun main() {
    val galaxies = mutableListOf<Coords<Int>>()
    Scanner(File(ClassLoader.getSystemResource("inputs/Day11.txt").file)).use {
        var row = 0
        while (it.hasNextLine()) {
            val line = it.nextLine().trim()
            for (col in line.indices) {
                if (line[col] == '#') {
                    galaxies.add(Coords(row, col))
                }
            }

            row++
        }
    }

    println("Part 1: ${solve(expand(galaxies))}")
    println("Part 2: ${solve(expand(galaxies, 1000000 - 1))}")
}

private fun solve(galaxies: List<Coords<Int>>): Long {
    var totalDistance = 0L
    for (galaxy1 in galaxies) {
        for (galaxy2 in galaxies) {
            if (galaxy1 <= galaxy2) {
                continue
            }

            totalDistance += galaxy1.manhattanDist(galaxy2)
        }
    }

    return totalDistance
}

fun expand(input: List<Coords<Int>>, byAdditional: Int = 1): List<Coords<Int>> {
    val galaxies = input.map { Coords(it.row, it.col) }

    val galaxiesSortedByCol = galaxies.sortedBy { it.col }
    val colOffsets = mutableListOf(0)
    for (x in 1..<galaxiesSortedByCol.size) {
        val next = galaxiesSortedByCol[x]
        val prev = galaxiesSortedByCol[x - 1]
        colOffsets.add(max(0, next.col - prev.col - 1) * byAdditional)
    }
    var totalColOffset = 0
    for (col in 1..<galaxiesSortedByCol.size) {
        totalColOffset += colOffsets[col]
        galaxiesSortedByCol[col].col += totalColOffset
    }

    val galaxiesSortedByRow = galaxies.sortedBy { it.row }
    val rowOffsets = mutableListOf(0)
    for (row in 1..<galaxiesSortedByRow.size) {
        val next = galaxiesSortedByRow[row]
        val prev = galaxiesSortedByRow[row - 1]
        rowOffsets.add(max(0, next.row - prev.row - 1) * byAdditional)
    }
    var totalRowOffset = 0
    for (row in 1..<galaxiesSortedByRow.size) {
        totalRowOffset += rowOffsets[row]
        galaxiesSortedByRow[row].row += totalRowOffset
    }

    return galaxies
}
