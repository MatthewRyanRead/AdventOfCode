package tech.readonly.aoc.aoc2023

import java.io.File
import java.util.Scanner

data class Range(val start: Long, val end: Long)

data class RangeMap(val src: Range, val dst: Range)

fun main() {
    var seedNums = listOf<Long>()
    val allRangeMaps = mutableListOf<List<RangeMap>>()

    Scanner(File(ClassLoader.getSystemResource("inputs/Day5.txt").file)).use {
        seedNums = it.nextLine().trim().split(": ")[1].split(" ").map { s -> s.toLong() }
        it.nextLine()

        while (it.hasNextLine()) {
            it.nextLine()
            allRangeMaps.add(readRanges(it))
        }
    }

    println("Part 1: ${part1(seedNums, allRangeMaps)}")

    val seedRanges = mutableListOf<Range>()
    var i = 0
    while (i < seedNums.size) {
        val seedNum = seedNums[i]
        seedRanges.add(Range(seedNum, seedNum + seedNums[i + 1] - 1))
        i += 2
    }

    println("Part 2: ${part2(seedRanges, allRangeMaps)}")
}

fun readRanges(input: Scanner): List<RangeMap> {
    val ranges: MutableList<RangeMap> = mutableListOf()
    while (input.hasNextLine()) {
        val line = input.nextLine().trim()
        if (line.isEmpty()) {
            break
        }

        val (dst, src, size) = line.split(" ").map { s -> s.toLong() }

        ranges.add(RangeMap(src = Range(src, src + size - 1L), dst = Range(dst, dst + size - 1L)))
    }

    return ranges
}

fun part1(
    seedNums: List<Long>,
    allRangeMaps: List<List<RangeMap>>,
): Long {
    val locations = mutableListOf<Long>()

    for (seedNum in seedNums) {
        var value = seedNum
        for (rangesOfType in allRangeMaps) {
            for (rangeMap in rangesOfType) {
                if (value in rangeMap.src.start..rangeMap.src.end) {
                    value += rangeMap.dst.start - rangeMap.src.start
                    break
                }
            }
        }

        locations.add(value)
    }

    return locations.min()
}

fun part2(
    seedRanges: List<Range>,
    allRangeMaps: List<List<RangeMap>>,
): Long {
    val reverseRangeMaps = allRangeMaps.reversed().map { maps ->
        maps.reversed().map { map ->
            RangeMap(src = map.dst, dst = map.src)
        }.sortedBy { it.src.start }
    }

    // brute force baybeeee
    // better way is to build a tree of range mappings, but I am lazy
    for (candidate in 0..Long.MAX_VALUE) {
        var value = candidate

        // I tried extracting this common code out to a function.
        // That was almost 8x slower ... took an extra 2 min!
        for (rangesOfType in reverseRangeMaps) {
            for (rangeMap in rangesOfType) {
                if (value in rangeMap.src.start..rangeMap.src.end) {
                    value += rangeMap.dst.start - rangeMap.src.start
                    break
                }
            }
        }

        if (seedRanges.any { r -> value in r.start..r.end }) {
            return candidate
        }
    }

    error("No solution found")
}
