package tech.readonly.aoc.aoc2023

import java.io.File
import java.util.Scanner
import java.util.function.Predicate
import kotlin.math.max

data class Node(val id: String, val next: MutableList<Node>) {
    override fun toString(): String {
        return "Node(id='$id', next=${next.map { it.id }})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

fun main() {
    var instructions: String
    val nodesById: MutableMap<String, Node> = mutableMapOf()
    Scanner(File("inputs/Day8.txt")).use {
        instructions = it.nextLine()
        it.nextLine()

        while (it.hasNextLine()) {
            val (id, neighbours) = it.nextLine().trim().split(" = ")
            val (n1, n2) = neighbours.trim().substring(1, neighbours.length - 1).split(", ")

            val node = nodesById.computeIfAbsent(id.trim()) { Node(id.trim(), mutableListOf()) }
            val node1 = nodesById.computeIfAbsent(n1.trim()) { Node(n1.trim(), mutableListOf()) }
            val node2 = nodesById.computeIfAbsent(n2.trim()) { Node(n2.trim(), mutableListOf()) }

            node.next.add(node1)
            node.next.add(node2)
        }
    }

    part1(instructions, nodesById)
    part2(instructions, nodesById)
}

fun part1(instructions: String, nodesById: Map<String, Node> = mutableMapOf()) {
    println("Part 1: ${dist(instructions, nodesById, "AAA") { it == "ZZZ" }}")
}

fun part2(instructions: String, nodesById: Map<String, Node> = mutableMapOf()) {
    val dists = nodesById.keys.filter { it.endsWith('A') }
        .associateWith { k -> dist(instructions, nodesById, k) { it.endsWith('Z') }.toLong() }

    var lcm = 1L
    for (dist in dists.values) {
        val larger = max(dist, lcm)
        val max = dist * lcm
        var newLcm = larger
        while (newLcm <= max) {
            if (newLcm % dist == 0L && newLcm % lcm == 0L) {
                lcm = newLcm
                break
            }

            newLcm += larger
        }
    }

    println("Part 2: $lcm")
}

fun dist(
    instructions: String,
    nodesById: Map<String, Node> = mutableMapOf(),
    start: String,
    endMatcher: Predicate<String>,
): Int {
    var currNode = nodesById.get(start)!!
    var i = 0
    while (!endMatcher.test(currNode.id)) {
        val nextNode = when (val instr = instructions[i % instructions.length]) {
            'L' -> nodesById.get(currNode.next[0].id)!!
            'R' -> nodesById.get(currNode.next[1].id)!!
            else -> error("Bad instruction $instr")
        }

        if (currNode == nextNode) {
            error("Cycle found on node $currNode")
        }

        currNode = nextNode
        i++
    }

    return i
}
