import java.io.File
import java.util.Scanner
import kotlin.math.abs
import kotlin.math.max

data class Galaxy(var x: Long, var y: Long) : Comparable<Galaxy> {
    companion object Constants {
        val COMPARATOR = Comparator.comparing(Galaxy::x).thenComparing(Galaxy::y)!!
    }

    override fun compareTo(other: Galaxy): Int {
        return COMPARATOR.compare(this, other)
    }
}

fun main() {
    val galaxies = mutableListOf<Galaxy>()
    Scanner(File("inputs/Day11.txt")).use {
        var y = 0L
        while (it.hasNextLine()) {
            val line = it.nextLine().trim()
            for (x in line.indices) {
                if (line[x] == '#') {
                    galaxies.add(Galaxy(y, x.toLong()))
                }
            }

            y++
        }
    }

    println("Part 1: ${part1(galaxies)}")
    //println("Part 2: ${part2(galaxies)}")
}

fun part1(galaxies: List<Galaxy>): Long {
    expand(galaxies)

    var totalDistance = 0L
    for (galaxy1 in galaxies) {
        for (galaxy2 in galaxies) {
            if (galaxy1 <= galaxy2) {
                continue
            }

            totalDistance += abs(galaxy1.x - galaxy2.x) + abs(galaxy1.y - galaxy2.y)
        }
    }

    return totalDistance
}

fun expand(galaxies: List<Galaxy>, byAmount: Long = 1L) {
    val galaxiesSortedByX = galaxies.sortedBy { it.x }
    val xOffsets = mutableListOf(0L)
    for (x in 1..<galaxiesSortedByX.size) {
        val next = galaxiesSortedByX[x]
        val prev = galaxiesSortedByX[x - 1]
        xOffsets.add(max(0, next.x - prev.x - 1))
    }
    var totalXOffset = 0L
    for (x in 1..<galaxiesSortedByX.size) {
        totalXOffset += xOffsets[x]
        galaxiesSortedByX[x].x += totalXOffset
    }

    val galaxiesSortedByY = galaxies.sortedBy { it.y }
    val yOffsets = mutableListOf(0L)
    for (y in 1..<galaxiesSortedByY.size) {
        val neyt = galaxiesSortedByY[y]
        val prev = galaxiesSortedByY[y - 1]
        yOffsets.add(max(0, neyt.y - prev.y - 1))
    }
    var totalYOffset = 0L
    for (y in 1..<galaxiesSortedByY.size) {
        totalYOffset += yOffsets[y]
        galaxiesSortedByY[y].y += totalYOffset
    }
}
