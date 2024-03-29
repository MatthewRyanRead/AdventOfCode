﻿package tech.readonly.aoc.aoc2023

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import tech.readonly.aoc.aoc2023.CaveElem.BACK_MIRROR
import tech.readonly.aoc.aoc2023.CaveElem.BEAM_E
import tech.readonly.aoc.aoc2023.CaveElem.BEAM_N
import tech.readonly.aoc.aoc2023.CaveElem.BEAM_S
import tech.readonly.aoc.aoc2023.CaveElem.BEAM_W
import tech.readonly.aoc.aoc2023.CaveElem.EMPTY
import tech.readonly.aoc.aoc2023.CaveElem.FRONT_MIRROR
import tech.readonly.aoc.aoc2023.CaveElem.HORI_SPLIT
import tech.readonly.aoc.aoc2023.CaveElem.VERT_SPLIT
import tech.readonly.aoc.aoc2023.Day16.ALTERING_ELEMS
import tech.readonly.aoc.aoc2023.Day16.BEAMS
import tech.readonly.aoc.aoc2023.Day16.BEAM_BY_DIR
import tech.readonly.aoc.aoc2023.Day16.DIRS_BY_DIR_AND_ELEM
import tech.readonly.aoc.aoc2023.Day16.DIR_BY_BEAM
import tech.readonly.aoc.aoc2023.Day16.ELEM_BY_CHAR
import tech.readonly.aoc.aoc2023.util.Coords
import tech.readonly.aoc.aoc2023.util.Direction.Constants.TRANSLATION_BY_DIR
import tech.readonly.aoc.aoc2023.util.Direction.EAST
import tech.readonly.aoc.aoc2023.util.Direction.NORTH
import tech.readonly.aoc.aoc2023.util.Direction.SOUTH
import tech.readonly.aoc.aoc2023.util.Direction.WEST
import java.io.File
import java.util.Scanner

enum class CaveElem(val c: Char, val v: Int) {
    EMPTY('.', 0),
    VERT_SPLIT('|', 1),
    HORI_SPLIT('-', 2),
    BACK_MIRROR('\\', 4),
    FRONT_MIRROR('/', 8),
    BEAM_N('^', 16),
    BEAM_E('>', 32),
    BEAM_S('v', 64),
    BEAM_W('<', 128),
    ;
}

object Day16 {
    val ELEM_BY_CHAR = CaveElem.entries.associateBy { it.c }
    val BEAMS = setOf(BEAM_N, BEAM_E, BEAM_S, BEAM_W)
    val ALTERING_ELEMS = setOf(VERT_SPLIT, HORI_SPLIT, BACK_MIRROR, FRONT_MIRROR)
    val DIR_BY_BEAM =
        mapOf(Pair(BEAM_N, NORTH), Pair(BEAM_E, EAST), Pair(BEAM_S, SOUTH), Pair(BEAM_W, WEST))
    val BEAM_BY_DIR = DIR_BY_BEAM.map { (k, v) -> v to k }.toMap()
    val DIRS_BY_DIR_AND_ELEM = mapOf(
        Pair(Pair(NORTH, EMPTY), listOf(NORTH)),
        Pair(Pair(EAST, EMPTY), listOf(EAST)),
        Pair(Pair(SOUTH, EMPTY), listOf(SOUTH)),
        Pair(Pair(WEST, EMPTY), listOf(WEST)),
        Pair(Pair(NORTH, VERT_SPLIT), listOf(NORTH)),
        Pair(Pair(EAST, VERT_SPLIT), listOf(NORTH, SOUTH)),
        Pair(Pair(SOUTH, VERT_SPLIT), listOf(SOUTH)),
        Pair(Pair(WEST, VERT_SPLIT), listOf(NORTH, SOUTH)),
        Pair(Pair(NORTH, HORI_SPLIT), listOf(EAST, WEST)),
        Pair(Pair(EAST, HORI_SPLIT), listOf(EAST)),
        Pair(Pair(SOUTH, HORI_SPLIT), listOf(EAST, WEST)),
        Pair(Pair(WEST, HORI_SPLIT), listOf(WEST)),
        Pair(Pair(NORTH, BACK_MIRROR), listOf(WEST)),
        Pair(Pair(EAST, BACK_MIRROR), listOf(SOUTH)),
        Pair(Pair(SOUTH, BACK_MIRROR), listOf(EAST)),
        Pair(Pair(WEST, BACK_MIRROR), listOf(NORTH)),
        Pair(Pair(NORTH, FRONT_MIRROR), listOf(EAST)),
        Pair(Pair(EAST, FRONT_MIRROR), listOf(NORTH)),
        Pair(Pair(SOUTH, FRONT_MIRROR), listOf(WEST)),
        Pair(Pair(WEST, FRONT_MIRROR), listOf(SOUTH)),
    )
}

fun main() {
    val cave = Scanner(File(ClassLoader.getSystemResource("inputs/Day16.txt").file)).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.map {
            it.map { c -> ELEM_BY_CHAR[c]!!.v }.toMutableList()
        }.toList()
    }

    runBlocking {
        println("Part 1: ${solve(copyCave(cave), Coords(0, 0), BEAM_E)}")
        println("Part 2: ${part2(cave)}")
    }
}

private fun solve(
    cave: List<MutableList<Int>>,
    startCoords: Coords<Int>,
    startBeam: CaveElem,
): Int {
    val beamCoords = ArrayDeque<Coords<Int>>()
    beamCoords.addLast(startCoords)
    val energized = mutableSetOf(startCoords)
    cave[startCoords.first][startCoords.second] = cave[startCoords.first][startCoords.second] or startBeam.v

    val seen = mutableSetOf<Pair<Coords<Int>, String>>()

    while (beamCoords.isNotEmpty()) {
        val coords = beamCoords.removeFirst()
        val str = caveToStr(cave)
        if (seen.contains(Pair(coords, str))) {
            break
        }
        seen.add(Pair(coords, str))

        val spot = cave[coords.first][coords.second]
        val beams = BEAMS.filter { spot and it.v == it.v }
        if (beams.isEmpty()) {
            error("No beams found at $coords")
        }

        if (ALTERING_ELEMS.any { spot and it.v == it.v }) {
            ALTERING_ELEMS.filter { spot and it.v == it.v }.forEach { elem ->
                beams.forEach { beam ->
                    val resultCoords = move(cave, coords, beam, elem)
                    resultCoords.forEach { beamCoords.addLast(it) }
                    energized.addAll(resultCoords)
                }
            }
        } else {
            beams.forEach { beam ->
                val resultCoords = move(cave, coords, beam, EMPTY)
                resultCoords.forEach { beamCoords.addLast(it) }
                energized.addAll(resultCoords)
            }
        }
    }

    return energized.size
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun part2(cave: List<MutableList<Int>>): Int {
    val maxRow = cave.lastIndex
    val maxCol = cave[0].lastIndex

    val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(10))
    val results = mutableListOf<Deferred<Int>>()
    cave.indices.forEach { row ->
        results.add(scope.async { solve(copyCave(cave), Coords(row, 0), BEAM_E) })
        results.add(scope.async { solve(copyCave(cave), Coords(row, maxCol), BEAM_W) })
    }
    cave[0].indices.forEach { col ->
        results.add(scope.async { solve(copyCave(cave), Coords(maxRow, col), BEAM_N) })
        results.add(scope.async { solve(copyCave(cave), Coords(0, col), BEAM_S) })
    }

    return results.awaitAll().max()
}

private fun move(
    cave: List<MutableList<Int>>,
    coords: Coords<Int>,
    beam: CaveElem,
    elem: CaveElem,
): List<Coords<Int>> {
    val beamDir = DIR_BY_BEAM[beam]!!
    val newDirs = DIRS_BY_DIR_AND_ELEM[Pair(beamDir, elem)]!!

    return newDirs.mapNotNull { dir ->
        val translation = TRANSLATION_BY_DIR[dir]!!
        val newCoords = coords + translation
        if (newCoords.inbounds(cave)) {
            val newSpot = cave[newCoords.first][newCoords.second]
            val newBeam = BEAM_BY_DIR[dir]!!.v

            if (newSpot and newBeam == newBeam) {
                null
            } else {
                cave[newCoords.first][newCoords.second] = newSpot or newBeam
                newCoords
            }
        } else {
            null
        }
    }
}

private fun caveToStr(cave: List<MutableList<Int>>): String {
    return cave.joinToString(separator = System.lineSeparator()) { row ->
        row.joinToString(separator = "") { v ->
            val beams = BEAMS.filter { v and it.v == it.v }

            @Suppress("kotlin:S6511")
            if (v and VERT_SPLIT.v == VERT_SPLIT.v) {
                VERT_SPLIT.c.toString()
            } else if (v and HORI_SPLIT.v == HORI_SPLIT.v) {
                HORI_SPLIT.c.toString()
            } else if (v and BACK_MIRROR.v == BACK_MIRROR.v) {
                BACK_MIRROR.c.toString()
            } else if (v and FRONT_MIRROR.v == FRONT_MIRROR.v) {
                FRONT_MIRROR.c.toString()
            } else {
                when (beams.size) {
                    0 -> EMPTY.c.toString()
                    1 -> beams[0].c.toString()
                    else -> beams.size.toString()
                }
            }
        }
    }
}

private fun copyCave(cave: List<MutableList<Int>>): List<MutableList<Int>> {
    return cave.map { row ->
        row.map { v -> v }.toMutableList()
    }
}
