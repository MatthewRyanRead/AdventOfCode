package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Constants.ALL_TRANSFORMS
import java.io.File
import java.util.Scanner

data class Pipe(val row: Int, val col: Int, val char: Char, var dist: Int = Int.MAX_VALUE)

object Constants {
    val ALL_TRANSFORMS = arrayOf(
        Pair(-1, 0),
        Pair(1, 0),
        Pair(0, -1),
        Pair(0, 1),
        Pair(-1, -1),
        Pair(-1, 1),
        Pair(1, -1),
        Pair(1, 1)
    )
}

fun main() {
    val input = mutableListOf<String>()
    Scanner(File("inputs/Day10.txt")).use {
        while (it.hasNextLine()) {
            input.add("." + it.nextLine().trim() + ".")
        }

        input.addFirst(".".repeat(input[0].length))
        input.addLast(input[0])
    }

    val (start, pipeland) = buildPipeland(input)

    println("Part 1: ${part1(start, pipeland)}")

    val expandedPipeland = expandPipeland(pipeland)
    println("Part 2: ${part2(expandedPipeland)}")
}

fun buildPipeland(input: List<String>): Pair<Pipe, List<List<Pipe>>> {
    var start = Pipe(-1, -1, '_', -1)
    val pipeland = mutableListOf<List<Pipe>>()

    outer@ for (row in input.indices) {
        pipeland.add(mutableListOf())

        for (col in input[row].indices) {
            val pipe = Pipe(row, col, input[row][col])
            pipeland[row].addLast(pipe)

            if (pipe.char == 'S') {
                start = pipe
            }
        }
    }

    if (start.row == -1) {
        error("Could not find start coords in input")
    }

    start.dist = 0

    return Pair(start, pipeland)
}

fun part1(start: Pipe, pipeland: List<List<Pipe>>): Int {
    val toVisit = mutableListOf<Pair<Pipe, List<Pipe>>>(Pair(start, neighbours(start, pipeland)))
    while (toVisit.isNotEmpty()) {
        val (pipe, neighbours) = toVisit.removeLast()
        for (neighbour in neighbours) {
            val newDist = pipe.dist + 1
            if (neighbour.dist < newDist) {
                continue
            }

            neighbour.dist = newDist
            toVisit.add(Pair(neighbour, neighbours(neighbour, pipeland)))
        }
    }

    val maxDist = pipeland.maxOf { row ->
        row.maxOf { pipe ->
            if (pipe.dist == Int.MAX_VALUE) -1 else pipe.dist
        }
    }
    return maxDist
}

fun part2(pipeland: List<List<Pipe>>): Int {
    val colorByPipe = mutableMapOf<Pipe, Int>()
    val uncolored = mutableSetOf<Pipe>()

    for (row in pipeland) {
        for (pipe in row) {
            if (pipe.dist != Int.MAX_VALUE) {
                colorByPipe[pipe] = 0
            } else {
                uncolored.add(pipe)
            }
        }
    }

    var currColor = 1
    val currPipes = mutableSetOf(pipeland[0][0])
    while (uncolored.isNotEmpty()) {
        val pipe: Pipe
        if (currPipes.isNotEmpty()) {
            pipe = currPipes.first()
            currPipes.remove(pipe)
        } else {
            pipe = uncolored.first()
            currColor += 1
        }

        colorByPipe[pipe] = currColor
        uncolored.remove(pipe)

        currPipes.addAll(
            neighbours(
            pipe, pipeland, true, transforms = ALL_TRANSFORMS
        ).filter { p -> !colorByPipe.contains(p) })
    }

    currColor += 1
    val enclosedPipes = mutableSetOf<Pipe>()
    val pipesByColor =
        colorByPipe.keys.groupBy { colorByPipe[it]!! }.map { e -> Pair(e.key, e.value.toSet()) }
            .toMap().toMutableMap()
    pipesByColor.remove(0)
    pipesByColor.remove(1)
    pipesByColor[currColor] = enclosedPipes

    for (group in pipesByColor.values.toList()) {
        if (group.isEmpty()) {
            continue
        }

        val color = colorByPipe[group.first()]

        val boundingNeighbours = mutableSetOf<Pipe>()
        for (pipe in group) {
            boundingNeighbours.addAll(
                neighbours(
                pipe, pipeland, true, transforms = ALL_TRANSFORMS
            ).filter { colorByPipe[it] != color })
        }

        if (boundingNeighbours.all { colorByPipe[it] == 0 }) {
            pipesByColor.remove(color)
            group.forEach {
                colorByPipe[it] = currColor
            }
            enclosedPipes.addAll(group)
        }
    }

    //printUnexpanded(pipeland, colorByPipe, currColor)

    return enclosedPipes.filter { p -> p.char != '#' }.size
}

fun neighbours(pipe: Pipe, pipeland: List<List<Pipe>>): MutableList<Pipe> {
    return when (pipe.char) {
        '|' -> neighbours(pipe, pipeland, false, Pair(-1, 0), Pair(1, 0))
        '-' -> neighbours(pipe, pipeland, false, Pair(0, -1), Pair(0, 1))
        'L' -> neighbours(pipe, pipeland, false, Pair(-1, 0), Pair(0, 1))
        'J' -> neighbours(pipe, pipeland, false, Pair(-1, 0), Pair(0, -1))
        '7' -> neighbours(pipe, pipeland, false, Pair(1, 0), Pair(0, -1))
        'F' -> neighbours(pipe, pipeland, false, Pair(1, 0), Pair(0, 1))
        'S' -> {
            val proposed =
                neighbours(pipe, pipeland, false, Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
            val it = proposed.iterator()
            while (it.hasNext()) {
                if (pipe !in neighbours(it.next(), pipeland)) {
                    it.remove()
                }
            }

            return proposed
        }

        else -> error("Unknown pipe ${pipe.char}")
    }
}

fun neighbours(
    pipe: Pipe,
    pipeland: List<List<Pipe>>,
    includeEmpty: Boolean,
    vararg transforms: Pair<Int, Int>,
): MutableList<Pipe> {
    val neighbours = mutableListOf<Pipe>()
    for (transform in transforms) {
        val row = pipe.row + transform.first
        val col = pipe.col + transform.second
        if (row in pipeland.indices && col in pipeland[row].indices) {
            val neighbour = pipeland[row][col]
            if (includeEmpty || (neighbour.char != '.' && neighbour.char != '#')) {
                neighbours.add(pipeland[row][col])
            }
        }
    }

    return neighbours
}

fun expandPipeland(pipeland: List<List<Pipe>>): List<List<Pipe>> {
    val expandedPipeland = mutableListOf<MutableList<Pipe>>()

    for (row in pipeland.indices) {
        expandedPipeland.add(mutableListOf())
        expandedPipeland.add(mutableListOf())
        val newRow = 2*row
        val nextRow = newRow + 1

        for (col in pipeland[row].indices) {
            val pipe = pipeland[row][col]
            val newCol = 2*col
            val nextCol = newCol + 1

            expandedPipeland[newRow].addLast(Pipe(newRow, newCol, pipe.char, pipe.dist))

            if (pipe.char == '-' || pipe.char == 'F' || pipe.char == 'L') {
                expandedPipeland[newRow].addLast(Pipe(newRow, nextCol, '-', -1))
            } else {
                expandedPipeland[newRow].addLast(Pipe(newRow, nextCol, '#', Int.MAX_VALUE))
            }

            if (pipe.char == '|' || pipe.char == 'F' || pipe.char == '7') {
                expandedPipeland[nextRow].addLast(Pipe(nextRow, newCol, '|', -1))
            } else {
                expandedPipeland[nextRow].addLast(Pipe(nextRow, newCol, '#', Int.MAX_VALUE))
            }

            expandedPipeland[nextRow].addLast(Pipe(nextRow, nextCol, '#', Int.MAX_VALUE))
        }
    }

    for (row in expandedPipeland.indices) {
        for (col in expandedPipeland[row].indices) {
            if (expandedPipeland[row][col].char == 'S') {
                if (expandedPipeland[row - 2][col].char in arrayOf('|', '7', 'F')) {
                    expandedPipeland[row - 1][col] = Pipe(row - 1, col, '|', 1)
                }
                if (expandedPipeland[row + 2][col].char in arrayOf('|', 'J', 'L')) {
                    expandedPipeland[row + 1][col] = Pipe(row + 1, col, '|', 1)
                }
                if (expandedPipeland[row][col - 2].char in arrayOf('-', 'L', 'F')) {
                    expandedPipeland[row][col - 1] = Pipe(row, col - 1, '-', 1)
                }
                if (expandedPipeland[row][col + 2].char in arrayOf('-', 'J', '7')) {
                    expandedPipeland[row][col + 1] = Pipe(row, col + 1, '-', 1)
                }
            }
        }
    }

    return expandedPipeland
}

fun printUnexpanded(pipeland: List<List<Pipe>>, colorByPipe: Map<Pipe, Int>, interiorColor: Int) {
    for (row in pipeland.indices) {
        if (row % 2 == 1) {
            continue
        }

        for (col in pipeland[row].indices) {
            if (col % 2 == 1) {
                continue
            }

            val pipe = pipeland[row][col]
            val color = colorByPipe[pipe]
            when (color) {
                0 -> print(pipe.char)
                interiorColor -> print('I')
                else -> print('O')
            }
        }
        println()
    }
}
