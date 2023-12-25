package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Day23.EDGES_BY_COORDS
import tech.readonly.aoc.aoc2023.Day23.END_COORDS
import tech.readonly.aoc.aoc2023.Day23.GRID
import tech.readonly.aoc.aoc2023.Day23.START_COORDS
import tech.readonly.aoc.aoc2023.util.Coords
import tech.readonly.aoc.aoc2023.util.Direction.Constants.TRANSLATION_BY_DIR
import tech.readonly.aoc.aoc2023.util.Direction.EAST
import tech.readonly.aoc.aoc2023.util.Direction.NORTH
import tech.readonly.aoc.aoc2023.util.Direction.SOUTH
import tech.readonly.aoc.aoc2023.util.Direction.WEST
import java.io.File
import java.util.Scanner

private data class Edge(val start: Coords<Int>, val end: Coords<Int>, val cost: Int)

private data class State(val coords: Coords<Int>, val path: Set<Coords<Int>>, val steps: Int)

private object Day23 {
    var GRID = emptyList<CharArray>()
    val START_COORDS = Coords(0, 1)
    var END_COORDS = START_COORDS
    val EDGES_BY_COORDS = mutableMapOf<Coords<Int>, List<Edge>>()
}

fun main() {
    GRID = Scanner(File(ClassLoader.getSystemResource("inputs/Day23.txt").file)).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.toList()
            .map { it.toCharArray() }
    }
    END_COORDS = Coords(GRID.lastIndex, GRID[GRID.lastIndex].lastIndex - 1)

    createEdges(false)
    println("Part 1: ${solve()}")

    createEdges(true)
    println("Part 2: ${solve()}")
}

fun solve(): Int {
    var longestHike = State(Coords(-1, -1), emptySet(), -1)
    val statesToCheck = mutableListOf(State(START_COORDS, mutableSetOf(START_COORDS), 0))
    while (statesToCheck.isNotEmpty()) {
        val state = statesToCheck.removeLast()
        if (state.coords != END_COORDS) {
            statesToCheck.addAll(neighbours(state))
        } else if (state.steps > longestHike.steps) {
            longestHike = state
        }
    }

    return longestHike.steps
}

private fun neighbours1(coords: Coords<Int>): List<Coords<Int>> {
    return when (GRID[coords.first][coords.second]) {
        '#' -> error("Looking up neighbours for a wall! $coords")
        '^' -> listOf(coords + TRANSLATION_BY_DIR[NORTH]!!).filter { it.arrInbounds(GRID) }
        '>' -> listOf(coords + TRANSLATION_BY_DIR[EAST]!!).filter { it.arrInbounds(GRID) }
        'v' -> listOf(coords + TRANSLATION_BY_DIR[SOUTH]!!).filter { it.arrInbounds(GRID) }
        '<' -> listOf(coords + TRANSLATION_BY_DIR[WEST]!!).filter { it.arrInbounds(GRID) }
        else -> coords.neighbours().filter {
            it.arrInbounds(GRID) && when (GRID[it.first][it.second]) {
                '#' -> false
                else -> true
            }
        }
    }
}

private fun neighbours2(coords: Coords<Int>): List<Coords<Int>> {
    return coords.neighbours().filter {
        it.arrInbounds(GRID) && GRID[it.first][it.second] != '#'
    }
}

private fun neighbours(state: State): List<State> {
    return EDGES_BY_COORDS[state.coords]!!.filter { !state.path.contains(it.end) }.map {
        State(it.end, listOf(listOf(it.end), state.path).flatten().toSet(), state.steps + it.cost)
    }
}

private fun createEdges(isPart2: Boolean) {
    EDGES_BY_COORDS.clear()
    val neighboursByCoords = mutableMapOf<Coords<Int>, List<Coords<Int>>>()
    GRID.forEachIndexed { y, row ->
        row.indices.forEach {
            if (row[it] == '#') {
                return@forEach
            }

            val c = Coords(y, it)
            neighboursByCoords[c] = if (isPart2) neighbours2(c) else neighbours1(c)
        }
    }

    val coordsToProcess = mutableListOf(START_COORDS)
    while (coordsToProcess.isNotEmpty()) {
        val coords = coordsToProcess.removeLast()
        if (coords == END_COORDS) {
            continue
        }

        val edges = createEdges(neighboursByCoords, coords, isPart2)
        EDGES_BY_COORDS[coords] = edges
        coordsToProcess.addAll(edges.map { it.end }.filter { !EDGES_BY_COORDS.containsKey(it) })
    }
}

private fun createEdges(
    neighboursByCoords: Map<Coords<Int>, List<Coords<Int>>>,
    coords: Coords<Int>,
    isPart2: Boolean,
): List<Edge> {
    return neighboursByCoords[coords]!!.mapNotNull { neighbour ->
        if (neighbour == coords) {
            return@mapNotNull null
        }

        var cost = 1
        var prevCoords = coords
        var nextCoords = neighbour
        var nextNeighbours = neighboursByCoords[nextCoords]!!.filter { c ->
            c != prevCoords && (isPart2 || avoidsHill(nextCoords, c))
        }
        while (nextNeighbours.size < 2 && nextCoords != END_COORDS) {
            if (nextNeighbours.isEmpty()) {
                // can't go up a hill
                return@mapNotNull null
            }

            cost++
            prevCoords = nextCoords
            nextCoords = nextNeighbours[0]
            nextNeighbours = neighboursByCoords[nextCoords]!!.filter { c ->
                c != prevCoords && (isPart2 || avoidsHill(nextCoords, c))
            }
        }

        Edge(coords, nextCoords, cost)
    }
}

private fun avoidsHill(coords: Coords<Int>, neighbour: Coords<Int>): Boolean {
    return when (GRID[neighbour.first][neighbour.second]) {
        '^' -> coords + TRANSLATION_BY_DIR[SOUTH]!! != neighbour
        '>' -> coords + TRANSLATION_BY_DIR[WEST]!! != neighbour
        'v' -> coords + TRANSLATION_BY_DIR[NORTH]!! != neighbour
        '<' -> coords + TRANSLATION_BY_DIR[EAST]!! != neighbour
        else -> true
    }
}
