package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Day17.destination
import tech.readonly.aoc.aoc2023.util.Coords
import tech.readonly.aoc.aoc2023.util.Direction
import tech.readonly.aoc.aoc2023.util.Direction.Constants.OPPOSITE_BY_DIR
import tech.readonly.aoc.aoc2023.util.Direction.Constants.TRANSLATION_BY_DIR
import java.io.File
import java.util.PriorityQueue
import java.util.Scanner

object Day17 {
    var destination = Coords(Int.MAX_VALUE, Int.MAX_VALUE)
}

data class Day17Key(
    val coords: Coords<Int> = Coords(0, 0),
    var totalCost: Int = 0,
    val straightMoves: Int = 0,
    val lastDir: Direction? = null,
) : Comparable<Day17Key> {
    fun heuristicCost(): Int {
        return totalCost + coords.manhattanDist(destination)
    }

    override fun compareTo(other: Day17Key): Int {
        return heuristicCost() - other.heuristicCost()
    }
}

fun main() {
    val city =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day17.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.map {
                it.map { c -> c.digitToInt() }
            }.toList()
        }
    destination = Coords(city.lastIndex, city[city.lastIndex].lastIndex)

    println("Part 1: ${solve(city, 0, 3)}")
    println("Part 2: ${solve(city, 4, 10)}")
}

private fun solve(city: List<List<Int>>, minStraight: Int, maxStraight: Int): Int {
    // I originally just used a diagonal path as a max bound here, which was valid for P1 and just
    // happened to work for P2. Technically invalid for P2 though, and not needed, so just use max
    var bestSoFar = Day17Key(totalCost = Int.MAX_VALUE)
    val bestForCoordsDirStep = mutableMapOf<Triple<Coords<Int>, Direction?, Int>, Int>()
    val queue = PriorityQueue(setOf(Day17Key()))

    while (queue.isNotEmpty()) {
        val key = queue.poll()

        // Java's PQ doesn't support mutation / sorting on demand, and remove() is O(n), so I'm
        // managing my own map of best-so-far and just skipping when a worse dupe is encountered
        val coordsDirStep = Triple(key.coords, key.lastDir, key.straightMoves)
        val personalBest = bestForCoordsDirStep.getOrDefault(coordsDirStep, Int.MAX_VALUE)
        if (key.totalCost < personalBest) {
            bestForCoordsDirStep[coordsDirStep] = key.totalCost
        } else {
            // these coords were already reachable via the same dir/straightMoves, for less/equal
            continue
        }

        if (key.coords == destination) {
            if (key.totalCost < bestSoFar.totalCost) {
                bestSoFar = key
            }
        } else {
            queue.addAll(getNeighbourCosts(city, key, minStraight, maxStraight).filter {
                // don't bother if we can definitely do better
                it.totalCost <= 9 * (it.coords.first + it.coords.second)
                        && it.heuristicCost() < bestSoFar.totalCost
            })
        }
    }

    return bestSoFar.totalCost
}

private fun getNeighbourCosts(
    city: List<List<Int>>,
    key: Day17Key,
    minStraight: Int,
    maxStraight: Int,
): List<Day17Key> {
    return Direction.entries.map {
        Day17Key(
            key.coords + TRANSLATION_BY_DIR[it]!!,
            Int.MAX_VALUE,
            if (it == key.lastDir) key.straightMoves + 1 else 1,
            it
        )
    }.filter {
        it.straightMoves <= maxStraight
                && (key.straightMoves >= minStraight || key.lastDir == null || key.lastDir == it.lastDir)
                && OPPOSITE_BY_DIR[it.lastDir]!! != key.lastDir
                && it.coords.inbounds(city)
    }.onEach {
        it.totalCost = key.totalCost + city[it.coords.first][it.coords.second]
    }
}
