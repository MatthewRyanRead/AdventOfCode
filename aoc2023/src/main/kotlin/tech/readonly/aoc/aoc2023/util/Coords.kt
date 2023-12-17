package tech.readonly.aoc.aoc2023.util

import kotlin.math.abs

data class Coords(var row: Int, var col: Int) : Comparable<Coords> {
    companion object Constants {
        val COMPARATOR = Comparator.comparing(Coords::row)
            .thenComparing(Coords::col)!!
    }

    override fun compareTo(other: Coords): Int {
        return COMPARATOR.compare(this, other)
    }

    operator fun plus(other: Coords): Coords {
        return Coords(row + other.row, col + other.col)
    }

    fun <U> inbounds(grid: List<List<U>>): Boolean {
        if (grid.isEmpty() || grid[0].isEmpty()) {
            error("Grid is not valid")
        }

        return row in grid.indices && col in grid[0].indices
    }

    fun manhattanDist(other: Coords): Int {
        return abs(row - other.row) + abs(col - other.col)
    }
}