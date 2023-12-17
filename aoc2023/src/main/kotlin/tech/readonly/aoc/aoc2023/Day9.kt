package tech.readonly.aoc.aoc2023

import java.io.File
import java.util.Scanner

fun main() {
    val input = mutableListOf<List<Long>>()
    Scanner(File("inputs/Day9.txt")).use {
        while (it.hasNextLine()) {
            input.add(it.nextLine().trim().split(' ').map { s -> s.toLong() })
        }
    }

    println("Part 1: ${solve(input, false)}")
    println("Part 2: ${solve(input, true)}")
}

fun solve(input: List<List<Long>>, isPart2: Boolean): Long {
    val answers = mutableListOf<Long>()
    for (reading in input) {
        val differences = mutableListOf<List<Long>>()
        differences.add(reading)

        var last = reading
        while (!last.all { it == 0L }) {
            val newDifferences = mutableListOf<Long>()
            for (i in 0..<(last.size - 1)) {
                newDifferences.add(last[i + 1] - last[i])
            }

            differences.add(newDifferences)
            last = newDifferences
        }

        // technically not needed, but conforming to the example
        last.addLast(0L)

        for (i in differences.size - 1 downTo 1) {
            if (isPart2) {
                // just sticking it at the end to avoid array resizing
                differences[i - 1].addLast(differences[i - 1].first() - differences[i].last())
            } else {
                differences[i - 1].addLast(differences[i - 1].last() + differences[i].last())
            }
        }

        answers.add(differences[0].last())
    }

    return answers.sum()
}
