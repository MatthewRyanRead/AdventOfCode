package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Face.JACK
import tech.readonly.aoc.aoc2023.Face.JOKER
import tech.readonly.aoc.aoc2023.HandType.FIVE_OF_A_KIND
import tech.readonly.aoc.aoc2023.HandType.FOUR_OF_A_KIND
import tech.readonly.aoc.aoc2023.HandType.FULL_HOUSE
import tech.readonly.aoc.aoc2023.HandType.HIGH_CARD
import tech.readonly.aoc.aoc2023.HandType.ONE_PAIR
import tech.readonly.aoc.aoc2023.HandType.THREE_OF_A_KIND
import tech.readonly.aoc.aoc2023.HandType.TWO_PAIR
import java.io.File
import java.util.Scanner

enum class Face(val char: Char, val rank: Int) {
    JOKER('?', 1),
    TWO('2', 2),
    THREE('3', 3),
    FOUR('4', 4),
    FIVE('5', 5),
    SIX('6', 6),
    SEVEN('7', 7),
    EIGHT('8', 8),
    NINE('9', 9),
    TEN('T', 10),
    JACK('J', 11),
    QUEEN('Q', 12),
    KING('K', 13),
    ACE('A', 14),
    ;

    companion object ThisReallySeemsUnnecessaryKotlin {
        val FACE_BY_CHAR = entries.associateBy { it.char }
    }
}

enum class HandType(val rank: Int) {
    FIVE_OF_A_KIND(7),
    FOUR_OF_A_KIND(6),
    FULL_HOUSE(5),
    THREE_OF_A_KIND(4),
    TWO_PAIR(3),
    ONE_PAIR(2),
    HIGH_CARD(1),
    ;
}

data class Hand(private val hand: String, val wager: Long) : Comparable<Hand> {
    private val type = typeOf()
    private val score = score()

    override fun compareTo(other: Hand) = compareValuesBy(this, other) { it.score }

    fun jokerize(): Hand {
        return Hand(this.hand.replace(JACK.char, JOKER.char), wager)
    }

    @Suppress("kotlin:S3776")
    private fun typeOf(): HandType {
        val counts = this.hand.toCharArray().toTypedArray().groupingBy { it }.eachCount()
        return when (counts.values.max()) {
            5 -> FIVE_OF_A_KIND

            4 -> {
                if (counts.getOrDefault(JOKER.char, 0) != 0) {
                    return FIVE_OF_A_KIND
                }
                return FOUR_OF_A_KIND
            }

            3 -> {
                if (counts.getOrDefault(JOKER.char, 0) == 3) {
                    if (counts.values.min() == 2) {
                        return FIVE_OF_A_KIND
                    }
                    return FOUR_OF_A_KIND
                }

                if (counts.getOrDefault(JOKER.char, 0) == 2) {
                    return FIVE_OF_A_KIND
                }

                if (counts.getOrDefault(JOKER.char, 0) == 1) {
                    return FOUR_OF_A_KIND
                }

                if (counts.values.min() == 2) {
                    return FULL_HOUSE
                }

                return THREE_OF_A_KIND
            }

            2 -> {
                if (counts.getOrDefault(JOKER.char, 0) == 2) {
                    if (counts.size == 3) {
                        return FOUR_OF_A_KIND
                    }
                    return THREE_OF_A_KIND
                }

                if (counts.getOrDefault(JOKER.char, 0) == 1) {
                    if (counts.size == 3) {
                        return FULL_HOUSE
                    }
                    return THREE_OF_A_KIND
                }

                if (counts.size == 3) {
                    return TWO_PAIR
                }

                return ONE_PAIR
            }

            else -> {
                if (counts.getOrDefault(JOKER.char, 0) == 1) {
                    return ONE_PAIR
                }
                return HIGH_CARD
            }
        }
    }

    private fun score(): Long {
        val cardsRank = hand.map { Face.FACE_BY_CHAR[it]?.rank.toString().padStart(2, '0') }
            .joinToString(" ") { s -> s }

        return (type.rank.toString() + cardsRank.replace(" ", "")).toLong()
    }
}

fun main() {
    val hands = mutableListOf<Hand>()
    Scanner(File("inputs/Day7.txt")).use {
        while (it.hasNextLine()) {
            val (hand, wagerStr) = it.nextLine().trim().split(" ")
            hands.add(Hand(hand, wagerStr.toLong()))
        }
    }

    part1(hands)
    part2(hands)
}

fun part1(hands: List<Hand>) {
    val score = hands.sorted().mapIndexed { index, hand -> (index + 1) * hand.wager }.sum()
    println("Part 1: $score")
}

@Suppress("kotlin:S125")
fun part2(hands: List<Hand>) {
    val maybeJokerizedHands = hands.map {
        // problem says J cards ""can"" pretend to be whatever card is best, but it means ""must""
        it.jokerize()
        //val joker = it.jokerize()
        //if (joker > it) {
        //    joker
        //} else {
        //    it
        //}
    }

    val score =
        maybeJokerizedHands.sorted().mapIndexed { index, hand -> (index + 1) * hand.wager }.sum()
    println("Part 2: $score")
}
