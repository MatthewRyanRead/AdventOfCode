package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.util.Coords
import java.io.File
import java.util.ArrayDeque
import java.util.Scanner
import kotlin.math.max
import kotlin.math.min

private data class Brick(val id: Int, val start: Coords<Int>, val end: Coords<Int>) {
    val allCoords = sequence {
        for (x in start.first..end.first) {
            for (y in start.second..end.second) {
                for (z in start[2]..end[2]) {
                    yield(Coords(x, y, z))
                }
            }
        }
    }.toSet()

    val supports = mutableSetOf<Brick>()
    val supportedBy = mutableSetOf<Brick>()

    fun drop(): Brick {
        return Brick(
            id,
            Coords(start.first, start.second, start.third - 1),
            Coords(end.first, end.second, end.third - 1),
        )
    }

    fun intersects(coords: Coords<Int>): Boolean {
        return allCoords.any { it == coords }
    }

    fun intersects(other: Brick): Boolean {
        return allCoords.any { other.intersects(it) }
    }
}

fun main() {
    var id = 1
    val bricks =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day22.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.toList()
                .map {
                    val (startStr, endStr) = it.split('~')
                    val start = startStr.split(',')
                    val end = endStr.split(',')
                    val startCoords = Coords(start[0].toInt(), start[1].toInt(), start[2].toInt())
                    val endCoords = Coords(end[0].toInt(), end[1].toInt(), end[2].toInt())
                    if (startCoords.third <= endCoords.third) {
                        Brick(id++, startCoords, endCoords)
                    } else {
                        Brick(id++, endCoords, startCoords)
                    }
                }
        }

    println("Part 1: ${part1(bricks)}")
    println("Part 2: ${part2(bricks)}")
}

private fun solve(bricks: List<Brick>): List<Brick> {
    val sortedBricks = ArrayDeque(bricks.sortedBy { it.start.third })
    val finalBricksByMaxZ = mutableMapOf<Int, MutableSet<Brick>>()
    while (sortedBricks.isNotEmpty()) {
        var brick = sortedBricks.remove()
        var dropped = brick.drop()
        while (dropped.start.third > 0 && finalBricksByMaxZ.getOrDefault(
                dropped.start.third, mutableSetOf()
            ).filter { it.intersects(dropped) }
             .onEach {
                 it.supports.add(brick)
                 brick.supportedBy.add(it)
            }.isEmpty()
        ) {
            brick = dropped
            dropped = brick.drop()
        }

        finalBricksByMaxZ.computeIfAbsent(brick.end.third) { mutableSetOf() }.add(brick)
    }

    return finalBricksByMaxZ.values.flatten()
}

private fun part1(bricks: List<Brick>): Int {
    return solve(bricks).count { brick -> brick.supports.all { it.supportedBy.size > 1 } }
}

private fun part2(bricks: List<Brick>): Int {
    val brickById = solve(bricks).associateBy { it.id }
    return brickById.values.sumOf { brick ->
        // if a brick is not equal to the "original" brick, then it moved. very brute force
        solve(brickById.values.filter { it != brick }).count { brickById[it.id] != it }
    }
}

@Suppress("unused")
private fun printBricks(bricks: Collection<Brick>) {
    val minX = bricks.minOf { min(it.start.first, it.end.first) }
    val maxX = bricks.maxOf { max(it.start.first, it.end.first) }
    val minY = bricks.minOf { min(it.start.second, it.end.second) }
    val maxY = bricks.maxOf { max(it.start.second, it.end.second) }
    val maxZ = bricks.maxOf { max(it.start.third, it.end.third) }

    val zx = (0..maxZ).map { (minX..maxX).map { '.' }.toMutableList() }
    val zy = (0..maxZ).map { (minY..maxY).map { '.' }.toMutableList() }
    bricks.forEach { brick ->
        brick.allCoords.forEach {
            val c = (brick.id % 10).toString()[0]
            val chars = setOf('.', c)
            if (zx[it.third][it.first] !in chars) {
                zx[it.third][it.first] = '?'
            } else {
                zx[it.third][it.first] = c
            }
            if (zy[it.third][it.second] !in chars) {
                zy[it.third][it.second] = '?'
            } else {
                zy[it.third][it.second] = c
            }
        }
    }

    println()
    zx.reversed().forEach { println(it) }
    println()
    zy.reversed().forEach { println(it) }
    println()
}
