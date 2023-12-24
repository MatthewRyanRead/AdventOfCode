package tech.readonly.aoc.aoc2023.util

import kotlin.math.abs

// Kotlin's generics are somehow even worse than Java's
@Suppress("kotlin:S6530", "UNCHECKED_CAST")
class Coords<T>(vararg var points: T) : Comparable<Coords<T>> where T : Number {
    private val pts
        get() = points as Array<T>
    var first
        get() = pts[0]
        set(value) = setAt(0, value)
    var second
        get() = points[1]
        set(value) = setAt(1, value)
    var third
        get() = points[2]
        set(value) = setAt(2, value)

    private fun setAt(index: Int, value: T) {
        pts[index] = value
    }

    // region overrides

    override fun compareTo(other: Coords<T>): Int {
        if (first::class != other.first::class || points.size != other.points.size) {
            error("Cannot compare coordinates of different type or dimension: $this, $other")
        }

        for (i in points.indices) {
            val p1 = points[i].toDouble()
            val p2 = other.points[i].toDouble()
            if (p1 < p2) {
                return -1
            } else if (p1 > p2) {
                return 1
            }
        }

        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Coords<*>
        if (first::class != other.first::class) {
            return false
        }
        if (points.size != other.points.size) {
            return false
        }

        other as Coords<T>
        return pts.contentEquals(other.pts)
    }

    override fun hashCode(): Int {
        return pts.contentHashCode()
    }

    override fun toString(): String {
        return "(${pts.joinToString(separator = ", ")})"
    }

    // endregion overrides

    // region operators

    operator fun plus(other: Coords<T>): Coords<T> {
        if (first::class != other.first::class || points.size != other.points.size) {
            error("Cannot add coordinates of different type or dimension: $this, $other")
        }

        return when (first::class) {
            Double::class -> {
                val arr = points.mapIndexed { i, v -> v.toDouble() + other.points[i].toDouble() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Float::class -> {
                val arr = points.mapIndexed { i, v -> v.toFloat() + other.points[i].toFloat() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Long::class -> {
                val arr = points.mapIndexed { i, v -> v.toLong() + other.points[i].toLong() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Int::class -> {
                val arr = points.mapIndexed { i, v -> v.toInt() + other.points[i].toInt() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Short::class -> {
                val arr = points.mapIndexed { i, v -> v.toShort() + other.points[i].toShort() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Byte::class -> {
                val arr = points.mapIndexed { i, v -> v.toByte() + other.points[i].toByte() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            else -> error("Unknown/unsupported Number subtype: ${first::class}")
        }
    }

    inline operator fun <reified T> times(multiplier: T): Coords<T> where T : Number {
        return when (T::class) {
            Double::class -> {
                val arr = points.map { v -> v.toDouble() * multiplier.toDouble() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Float::class -> {
                val arr = points.map { v -> v.toFloat() * multiplier.toFloat() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Long::class -> {
                val arr = points.map { v -> v.toLong() * multiplier.toLong() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Int::class -> {
                val arr = points.map { v -> v.toInt() * multiplier.toInt() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Short::class -> {
                val arr = points.map { v -> v.toShort() * multiplier.toShort() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            Byte::class -> {
                val arr = points.map { v -> v.toByte() * multiplier.toByte() }
                    .toTypedArray()
                Coords(*arr) as Coords<T>
            }

            else -> error("Unknown/unsupported Number subtype: ${first::class}")
        }
    }

    inline fun <reified T> manhattanDist(other: Coords<T>): T where T : Number {
        if (points.size != 2) {
            error("Cannot compute the Manhattan distance for non-2D points")
        }
        if (first::class != other.first::class || points.size != other.points.size) {
            error("Cannot compare coordinates of different type or dimension: $this, $other")
        }

        return when (T::class) {
            Double::class -> ((first as Double to second as Double) to (other.first as Double to other.second as Double)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Float::class -> ((first as Float to second as Float) to (other.first as Float to other.second as Float)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Long::class -> ((first as Long to second as Long) to (other.first as Long to other.second as Long)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Int::class -> ((first as Int to second as Int) to (other.first as Int to other.second as Int)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Short::class -> ((first as Short to second as Short) to (other.first as Short to other.second as Short)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            Byte::class -> ((first as Byte to second as Byte) to (other.first as Byte to other.second as Byte)).let {
                abs(it.first.first - it.second.first) + abs(it.first.second - it.second.second)
            } as T

            else -> error("Unknown/unsupported Number subtype: ${T::class}")
        }
    }

    // endregion operators

    // region helpers

    fun <U> inbounds(grid: List<Collection<U>>): Boolean {
        if (points.size != 2) {
            error("Cannot check 2D bounds for non-2D coordinates: $this")
        }
        if (grid.isEmpty() || grid[0].isEmpty()) {
            error("Grid is not valid")
        }

        return first.toInt() in grid.indices && second.toInt() in grid[0].indices
    }

    fun arrInbounds(grid: List<CharArray>): Boolean {
        if (points.size != 2) {
            error("Cannot check 2D bounds for non-2D coordinates: $this")
        }
        if (grid.isEmpty() || grid[0].isEmpty()) {
            error("Grid is not valid")
        }

        return first.toInt() in grid.indices && second.toInt() in grid[0].indices
    }

    // endregion helpers
}