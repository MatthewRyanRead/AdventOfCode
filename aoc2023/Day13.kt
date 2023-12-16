import Day13.BYTE_BY_ROW
import Day13.CHAR_SWAP
import Day13.LOG2
import java.io.File
import java.util.Scanner

object Day13 {
    // longest input happens to be 17 rows/cols
    val LOG2 = (0..16).associateBy { 1 shl it }
    val BYTE_BY_ROW = mutableMapOf<String, Int>()
    val CHAR_SWAP = mapOf(Pair('.', "#"), Pair('#', "."))
}

fun main() {
    val mirrors = mutableListOf<MutableList<String>>()
    mirrors.add(mutableListOf())
    Scanner(File("inputs/Day13.txt")).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.forEach {
            if (it.isEmpty()) {
                mirrors.add(mutableListOf())
            } else {
                mirrors.last().add(it)
            }
        }
    }
    mirrors.last().isEmpty().takeIf { it }?.let { mirrors.removeLast() }

    println("Part 1: ${solve(mirrors) { solve(it) }}")
    println("Part 2: ${solve(mirrors) { part2(it) }}")
}

private fun solve(mirrors: List<List<String>>, solver: ((List<String>) -> Int)): Int {
    return mirrors.sumOf { mirror ->
        // rotate so the columns can just be solved row-wise also
        val colScore = mirror[0].indices.map { i ->
            mirror.joinToString(separator = "") { it[i].toString() }
        }.let { solver(it) }
        val rowScore = solver(mirror) * 100

        (rowScore + colScore).takeUnless { (rowScore == 0) == (colScore == 0) }
            ?: error("${"Zero".takeIf { rowScore == 0 } ?: "Two"} solutions found")
    }
}

private fun solve(mirror: List<String>): Int {
    (mirror.lastIndex downTo 1).forEach { i ->
        var rows1 = mirror.subList(0, i).reversed()
        var rows2 = mirror.subList(i, mirror.size)
        if (rows1.size > rows2.size) {
            rows1 = rows1.subList(0, rows2.size)
        } else if (rows2.size > rows1.size) {
            rows2 = rows2.subList(0, rows1.size)
        }

        i.takeIf { rows1 == rows2 }?.let { return it }
    }

    return 0
}

private fun part2(mirror: List<String>): Int {
    (0..<mirror.lastIndex).forEach { i ->
        // step by 2 because a row can't mirror itself
        ((i + 1)..mirror.lastIndex step 2).forEach { j ->
            // if bitDiff is a power of 2, then they are only 1 char different
            (convertToByte(mirror[i]) xor convertToByte(mirror[j])).takeIf { LOG2.containsKey(it) }
                ?.let { bitDiff ->
                    part2Check(deSmudge(mirror, i, bitDiff), solve(mirror)).takeIf { it != 0 }
                        ?.let {
                            return it
                        }
                }
        }
    }

    return 0
}

private fun part2Check(mirror: List<String>, origScore: Int): Int {
    solve(mirror).takeIf { it != origScore && it != 0 }?.let { return it }
    // reversing is an easy away to account for the encounter order of solve()
    solve(mirror.reversed()).let { mirror.size - it }
        .takeIf { it != origScore && it != mirror.size }.let { return it ?: 0 }
}

private fun convertToByte(row: String): Int {
    return BYTE_BY_ROW.getOrPut(row) {
        row.mapIndexed { i, c -> if (c == '#') 1 shl i else 0 }.reduce { acc, i -> acc or i }
    }
}

private fun deSmudge(mirror: List<String>, index: Int, diff: Int): List<String> {
    val charIndex = LOG2[diff]
    return mirror.mapIndexed { i, row ->
        row.takeIf { i != index } ?: row.mapIndexed { j, char ->
            char.takeIf { j != charIndex } ?: CHAR_SWAP[char]
        }.joinToString(separator = "")
    }
}
