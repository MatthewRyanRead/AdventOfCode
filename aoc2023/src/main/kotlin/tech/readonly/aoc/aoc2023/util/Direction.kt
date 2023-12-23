package tech.readonly.aoc.aoc2023.util

enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    ;

    companion object Constants {
        val OPPOSITE_BY_DIR = mapOf(
            Pair(NORTH, SOUTH),
            Pair(EAST, WEST),
            Pair(SOUTH, NORTH),
            Pair(WEST, EAST),
        )

        val TRANSLATION_BY_DIR = mapOf(
            Pair(NORTH, Coords(-1, 0)),
            Pair(EAST, Coords(0, 1)),
            Pair(SOUTH, Coords(1, 0)),
            Pair(WEST, Coords(0, -1)),
        )
    }
}