package tech.readonly.aoc.aoc2023.util

enum class Direction(v: Int) {
    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(4),
    ;

    companion object {
        val OPPOSITE_BY_DIR = mapOf(
            Pair(NORTH, SOUTH),
            Pair(EAST, WEST),
            Pair(SOUTH, NORTH),
            Pair(NORTH, SOUTH),
        )
    }
}