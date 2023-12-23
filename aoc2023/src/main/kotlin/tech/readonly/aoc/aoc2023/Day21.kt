package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.util.Coords
import java.io.File
import java.util.Scanner

fun main() {
    val grid =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day21.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.toList()
                .map { it.toCharArray() }
        }

    var startCoords: Coords<Int> = Coords(-1, -1)
    outer@ for (row in grid.indices) {
        for (col in grid[row].indices) {
            if (grid[row][col] == 'S') {
                startCoords = Coords(row, col)
                grid[row][col] = '.'
                break@outer
            }
        }
    }

    println("Part 1: ${solve(duplicateGrid(grid), startCoords, 64)}")
    println("Part 2: ${part2(grid, startCoords)}")
}

private fun solve(grid: List<CharArray>, startCoords: Coords<Int>, numIters: Int): Long {
    val visited = mutableSetOf<Coords<Int>>()
    val toVisit = mutableSetOf<Coords<Int>>()
    val nextToVisit = mutableSetOf<Coords<Int>>()
    toVisit.add(startCoords)

    var numVisited = 0
    for (i in 0..numIters) {
        while (toVisit.isNotEmpty()) {
            val coords = toVisit.first()
            toVisit.remove(coords)
            grid[coords.row][coords.col] = (i % 10).toString()[0]
            visited.add(coords)
            nextToVisit.addAll(getNeighbours(grid, coords).filter { !visited.contains(it) })
        }

        toVisit.addAll(nextToVisit)
        nextToVisit.clear()

        if (visited.size == numVisited) {
            break
        }
        numVisited = visited.size
    }

    val dstDigits = ((if (numIters % 2 == 0) 0..8 else 1..9) step 2).toSet()
    return grid.sumOf { row ->
        row.filter { it.isDigit() }.count { dstDigits.contains(it.digitToInt()) }
    }.toLong()
}

private fun part2(grid: List<CharArray>, startCoords: Coords<Int>): Long {
    // "conveniently": 26501365 = 202300 * 131 + 65, and grid size is 131x131
    val offset = 65
    val itCount = (26501365 - offset) / grid.size

    // the number of points has quadratic growth due to nature of input (no rocks on same row/col
    // as the start coords), so we just need to solve for 3 points and then interpolate
    val numPoints = 3
    val gridOffset = Coords(grid.size, grid[0].size)
    val tripleGrid = duplicateGrid(grid, 2)
    val quintupleGrid = duplicateGrid(grid, 4)
    val answer1 = solve(grid, startCoords, offset)
    val answer2 = solve(tripleGrid, startCoords + gridOffset, offset + grid.size)
    val answer3 = solve(quintupleGrid, startCoords + gridOffset * 2, offset + grid.size * 2)

    // Day 9 was an implementation of linear regression; thus, can be used iteratively to solve this
    val answers = mutableListOf(answer1, answer2, answer3)
    for (i in numPoints..itCount) {
        day9Solve(listOf(answers.subList(answers.size - numPoints, answers.size)), false)
    }

    return answers.last()
}

private fun getNeighbours(grid: List<CharArray>, coords: Coords<Int>): List<Coords<Int>> {
    return listOf(
        Coords(coords.row - 1, coords.col),
        Coords(coords.row + 1, coords.col),
        Coords(coords.row, coords.col - 1),
        Coords(coords.row, coords.col + 1)
    ).filter { it.row in grid.indices && it.col in grid[0].indices && grid[it.row][it.col] == '.' }
}

private fun duplicateGrid(grid: List<CharArray>, numTimes: Int = 0): List<CharArray> {
    val newGrid = mutableListOf<CharArray>()
    for (row in 0..<((numTimes + 1) * grid.size)) {
        val origRow = row % grid.size
        var count = 0
        newGrid.add(sequence { while (count++ <= numTimes) yield(grid[origRow].clone()) }.toList()
            .flatMap { it.asIterable() }.toCharArray()
        )
    }

    return newGrid
}
