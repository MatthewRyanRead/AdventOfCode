package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.util.Coords
import java.io.File
import java.util.Scanner
import java.util.stream.IntStream

data class VertLine(val row: Int, val startCol: Int, val endCol: Int)

fun main() {
    val board = mutableListOf<String>()
    Scanner(File(ClassLoader.getSystemResource("inputs/Day3.txt").file)).use {
        while (it.hasNextLine()) {
            board.add(it.nextLine().trim())
        }
    }

    val (numCoords, symbolCoords) = getNumCoords(board)

    part1(board, numCoords, symbolCoords)
    part2(board, numCoords, symbolCoords)
}

fun getNumCoords(board: List<String>): Pair<List<VertLine>, Set<Coords<Int>>> {
    val lines = mutableListOf<VertLine>()
    val symbolCoords = mutableSetOf<Coords<Int>>()

    board.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            if (row[x].isDigit()) {
                val startCol = x
                var endCol = x
                while (x < row.length && row[x].isDigit()) {
                    endCol = x
                    x++
                }

                lines.add(VertLine(y, startCol, endCol))
            } else if (row[x] != '.') {
                symbolCoords.add(Coords(y, x))
                x++
            } else {
                x++
            }
        }
    }

    return Pair(lines, symbolCoords)
}

fun part1(
    board: List<String>,
    lines: List<VertLine>,
    symbolCoords: Set<Coords<Int>>,
) {
    val sum = lines.filter {
        for (y in (it.row - 1)..(it.row + 1)) {
            for (x in (it.startCol - 1)..(it.endCol + 1)) {
                if (Coords(y, x) in symbolCoords) {
                    return@filter true
                }
            }
        }
        return@filter false
    }.sumOf {
        board[it.row].substring(it.startCol, it.endCol + 1).toLong()
    }

    println("Part 1: $sum")
}

fun part2(
    board: List<String>,
    lines: List<VertLine>,
    symbolCoords: Set<Coords<Int>>,
) {
    val numById = lines.withIndex().associateBy(
        { it.index },
        { board[it.value.row].substring(it.value.startCol, it.value.endCol + 1).toLong() },
    )
    val numIdByCoord = mutableMapOf<Coords<Int>, Int>()
    lines.forEachIndexed { id, coord ->
        IntStream.rangeClosed(coord.startCol, coord.endCol).boxed().map { x -> Coords(coord.row, x) }
            .forEach { numIdByCoord[it] = id }
    }

    var ratioSum = 0L
    for (symbolCoord in symbolCoords) {
        if (board[symbolCoord.first][symbolCoord.second] != '*') {
            continue
        }

        val nearbyNumIds = mutableSetOf<Int>()
        for (y in (symbolCoord.first - 1)..(symbolCoord.first + 1)) {
            for (x in (symbolCoord.second - 1)..(symbolCoord.second + 1)) {
                val nearbyCoord = Coords(y, x)
                if (nearbyCoord in numIdByCoord.keys) {
                    nearbyNumIds.add(numIdByCoord[nearbyCoord]!!)
                }
            }
        }

        if (nearbyNumIds.size == 2) {
            val numIds = nearbyNumIds.toList()
            ratioSum += numById[numIds[0]]!! * numById[numIds[1]]!!
        }
    }

    println("Part 2: $ratioSum")
}
