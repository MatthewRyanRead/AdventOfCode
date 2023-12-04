import java.io.File
import java.util.Scanner


fun main() {
    val cards = mutableListOf<Pair<Int, Pair<Set<Int>, List<Int>>>>()
    Scanner(File("inputs/Day4.txt")).use {
        while (it.hasNextLine()) {
            val (card, data) = it.nextLine().trim().split(":")
            val cardId = card.split(" ").filter { s -> s.isNotEmpty() } [1].toInt()
            val (winners, actual) = data.split(" | ")
            val winnerSet = mutableSetOf<Int>()
            winners.split(" ")
                    .filter { s -> s.isNotEmpty() }
                    .map { s -> s.toInt() }
                    .toCollection(winnerSet)
            val actualList = mutableListOf<Int>()
            actual.split(" ")
                    .filter { s -> s.isNotEmpty() }
                    .map { s -> s.toInt() }
                    .toCollection(actualList)
            cards.add(Pair(cardId, Pair(winnerSet, actualList)))
        }
    }

    part1(cards)
    part2(cards)
}

fun part1(cards : List<Pair<Int, Pair<Set<Int>, List<Int>>>>) {
    var score = 0L
    for ((_, data) in cards) {
        val (winners, actual) = data
        val winCount = actual.filter { a -> winners.contains(a) }.size
        if (winCount > 0) {
            score += 1L shl (winCount - 1)
        }
    }

    println("Part 1: $score")
}

fun part2(cards : List<Pair<Int, Pair<Set<Int>, List<Int>>>>) {
    val countByCardId = cards.associate { c -> Pair(c.first, 1L) }.toMutableMap()
    for ((id, data) in cards) {
        val (winners, actual) = data
        val winCount = actual.filter { a -> winners.contains(a) }.size
        val cardCount = countByCardId[id]!!

        for (i in 1..winCount) {
            countByCardId.merge(id + i, cardCount) { a, b -> a + b }
        }
    }

    println("Part 2: ${cards.mapNotNull { c -> countByCardId[c.first] }.sum()}")
}
