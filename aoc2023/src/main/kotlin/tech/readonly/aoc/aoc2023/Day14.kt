package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Day14.ELEM_BY_CHAR
import tech.readonly.aoc.aoc2023.Day14.Elem
import tech.readonly.aoc.aoc2023.Day14.GRID_BY_ITERATION_SEEN
import java.io.File
import java.util.Scanner
import java.util.function.Consumer

object Day14 {
    enum class Elem(val c: Char) {
        EMPTY('.'),
        CUBE_ROCK('#'),
        ROUND_ROCK('O'),
        ;
    }

    val ELEM_BY_CHAR = Elem.entries.associateBy { it.c }

    val GRID_BY_ITERATION_SEEN = mutableMapOf<String, Int>()
}

data class Grid(val content: List<MutableList<Elem>>) : Iterable<MutableList<Elem>> {
    val indices: IntRange = content.indices
    operator fun get(index: Int) = content[index]
    override fun iterator() = content.iterator()
    override fun forEach(action: Consumer<in MutableList<Elem>>?) = content.forEach(action)

    override fun toString(): String {
        return content.joinToString(separator = System.lineSeparator()) { row ->
            row.map { it.c }.joinToString(separator = "")
        }
    }

    fun score(): Int {
        val reversedGrid = Grid(content.reversed())
        return reversedGrid.indices.sumOf {
            (it + 1) * reversedGrid[it].count { v -> v == Elem.ROUND_ROCK }
        }
    }
}

fun main() {
    val grid = Grid(Scanner(File(ClassLoader.getSystemResource("inputs/Day14.txt").file)).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.map {
            it.map { c -> ELEM_BY_CHAR[c]!! }.toMutableList()
        }.toMutableList()
    })

    GRID_BY_ITERATION_SEEN[grid.toString()] = 0

    println("Part 1: ${part1(grid)}")
    println("Part 2: ${part2(grid)}")
}

private fun part1(grid: Grid): Int {
    move(grid)
    return grid.score()
}

private fun part2(grid: Grid): Int {
    var part2Grid = grid
    // already moved once for part 1
    repeat(3) { part2Grid = rotateAndMove(part2Grid) }

    GRID_BY_ITERATION_SEEN[part2Grid.toString()] = 1

    for (i in (2..1_000_000_000)) {
        repeat(4) { part2Grid = rotateAndMove(part2Grid) }
        val str = part2Grid.toString()

        if (str in GRID_BY_ITERATION_SEEN) {
            val cycleLength = i - GRID_BY_ITERATION_SEEN[str]!!
            val iterationsLeft = 1_000_000_000 - i
            val effectiveIterationsLeft = iterationsLeft % cycleLength

            for (j in 1..effectiveIterationsLeft) {
                repeat(4) { part2Grid = rotateAndMove(part2Grid) }
            }

            break
        }

        GRID_BY_ITERATION_SEEN[str] = i
    }

    // return to face north
    return rotateClockwise(part2Grid).score()
}

private fun move(grid: Grid) {
    grid.indices.forEach { row ->
        grid[row].indices.forEach inner@{ col ->
            doMove(grid, row, col)
        }
    }
}

private fun doMove(grid: Grid, row: Int, col: Int) {
    if (row == 0) {
        // first row doesn't move
        return
    }

    var dstRow = row - 1
    if (grid[row][col] != Elem.ROUND_ROCK) {
        return
    }

    if (grid[dstRow][col] != Elem.EMPTY) {
        // rock can't move
        return
    }

    while (dstRow > 0 && grid[--dstRow][col] == Elem.EMPTY) {
        // nothing to do
    }
    if (grid[dstRow][col] != Elem.EMPTY) {
        // the above overshoots unless it hits the edge
        dstRow++
    }

    grid[row][col] = Elem.EMPTY
    grid[dstRow][col] = Elem.ROUND_ROCK
}

private fun rotateClockwise(grid: Grid): Grid {
    return Grid(grid[0].indices.map { i ->
        // translation instead of rotation is actually fine (and faster),
        // but using reversed() to get a true rotation makes it much easier to visualize
        grid.map { it[i] }.reversed().toMutableList()
    })
}

private fun rotateAndMove(grid: Grid) : Grid {
    val rotated = rotateClockwise(grid)
    move(rotated)
    return rotated
}
