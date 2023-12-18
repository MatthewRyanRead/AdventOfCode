package tech.readonly.aoc.aoc2023.util

import kotlin.math.abs

// Kotlin's generics are somehow even worse than Java's
@Suppress("kotlin:S6530", "UNCHECKED_CAST")
data class Coords<T>(var row: T, var col: T) : Comparable<Coords<T>> where T : Number {
    companion object Constants {
        val COMPARATOR = Comparator.comparing<Coords<out Number>, Double> { c -> c.row.toDouble() }
            .thenComparing { c -> c.col.toDouble() }!!
    }

    override fun compareTo(other: Coords<T>): Int {
        return COMPARATOR.compare(this, other)
    }

    inline operator fun <reified T> plus(other: Coords<T>): Coords<T> where T : Number {
        return when (T::class) {
            Double::class -> Coords(
                row as Double + other.row as Double,
                col as Double + other.col as Double
            ) as Coords<T>
            Float::class -> Coords(
                row as Float + other.row as Float,
                col as Float + other.col as Float
            ) as Coords<T>
            Long::class -> Coords(
                row as Long + other.row as Long,
                col as Long + other.col as Long
            ) as Coords<T>
            Int::class -> Coords(
                row as Int + other.row as Int,
                col as Int + other.col as Int
            ) as Coords<T>
            Short::class -> Coords(
                row as Short + other.row as Short,
                col as Short + other.col as Short
            ) as Coords<T>
            Byte::class -> Coords(
                row as Byte + other.row as Byte,
                col as Byte + other.col as Byte
            ) as Coords<T>
            else -> error("Unknown/unsupported Number subtype: ${T::class}")
        }
    }

    inline operator fun <reified T> times(multiplier: T): Coords<T> where T : Number {
        return when (T::class) {
            Double::class -> Coords(
                row as Double * multiplier as Double,
                col as Double * multiplier as Double
            ) as Coords<T>
            Float::class -> Coords(
                row as Float * multiplier as Float,
                col as Float * multiplier as Float
            ) as Coords<T>
            Long::class -> Coords(
                row as Long * multiplier as Long,
                col as Long * multiplier as Long
            ) as Coords<T>
            Int::class -> Coords(
                row as Int * multiplier as Int,
                col as Int * multiplier as Int
            ) as Coords<T>
            Short::class -> Coords(
                row as Short * multiplier as Short,
                col as Short * multiplier as Short
            ) as Coords<T>
            Byte::class -> Coords(
                row as Byte * multiplier as Byte,
                col as Byte * multiplier as Byte
            ) as Coords<T>
            else -> error("Unknown/unsupported Number subtype: ${T::class}")
        }
    }

    inline fun <reified T> manhattanDist(other: Coords<T>): T where T : Number {
        return when (T::class) {
            Double::class -> ((this.row as Double to this.col as Double) to (other.row as Double to other.col as Double)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Float::class -> ((this.row as Float to this.col as Float) to (other.row as Float to other.col as Float)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Long::class -> ((this.row as Long to this.col as Long) to (other.row as Long to other.col as Long)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Int::class -> ((this.row as Int to this.col as Int) to (other.row as Int to other.col as Int)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Short::class -> ((this.row as Short to this.col as Short) to (other.row as Short to other.col as Short)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Byte::class -> ((this.row as Byte to this.col as Byte) to (other.row as Byte to other.col as Byte)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            else -> error("Unknown/unsupported Number subtype: ${T::class}")
        }
    }

    fun <U> inbounds(grid: List<List<U>>): Boolean {
        if (grid.isEmpty() || grid[0].isEmpty()) {
            error("Grid is not valid")
        }

        return row.toInt() in grid.indices && col.toInt() in grid[0].indices
    }

    fun inbounds(grid: Array<CharArray>): Boolean {
        if (grid.isEmpty() || grid[0].isEmpty()) {
            error("Grid is not valid")
        }

        return row.toInt() in grid.indices && col.toInt() in grid[0].indices
    }
}