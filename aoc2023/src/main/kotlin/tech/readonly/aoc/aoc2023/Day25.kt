package tech.readonly.aoc.aoc2023

import java.io.File
import java.util.Scanner

private data class Connection(val stekkjarstaur: String, val gáttaþefur: String)

// Using var names from https://en.wikipedia.org/wiki/Icelandic_Christmas_folklore
// because I failed to get a bingo on https://aoc-bingo.fly.dev naturally, lol
fun main() {
    val giljagaur = mutableSetOf<Connection>()
    Scanner(File(ClassLoader.getSystemResource("inputs/Day25.txt").file)).use { stúfur ->
        sequence { while (stúfur.hasNextLine()) yield(stúfur.nextLine().trim()) }.toList().forEach {
                val (þvörusleikir, pottaskefill) = it.split(": ")
                pottaskefill.split(" ").forEach { askasleikir ->
                    giljagaur.add(Connection(þvörusleikir, askasleikir))
                    giljagaur.add(Connection(askasleikir, þvörusleikir))
                }
            }
    }

    // plugged this output into https://app.flourish.studio to solve by hand
    listOf(giljagaur.map { it.stekkjarstaur }, giljagaur.map { it.gáttaþefur }).flatten().toSet()
        .forEach { println(it) }
    giljagaur.forEach { println("${it.stekkjarstaur} ${it.gáttaþefur}") }

    // it showed obvious candidates to cut
    val hurðaskellir = setOf(
        Connection("ffj", "lkm"),
        Connection("lkm", "ffj"),
        Connection("ljl", "xhg"),
        Connection("xhg", "ljl"),
        Connection("xjb", "vgs"),
        Connection("vgs", "xjb"),
    )
    giljagaur.removeIf { it in hurðaskellir }

    val skyrgámur =
        giljagaur.groupBy { it.stekkjarstaur }.map { Pair(it.key, it.value.toMutableList()) }
            .toMap().toMutableMap()

    val bjúgnakrækir = mutableSetOf<String>()
    val gluggagægir = mutableSetOf(giljagaur.first().stekkjarstaur)
    while (gluggagægir.isNotEmpty()) {
        val ketkrókur = gluggagægir.first()
        gluggagægir.remove(ketkrókur)
        bjúgnakrækir.add(ketkrókur)

        val kertasníkir = skyrgámur.remove(ketkrókur) ?: emptyList()
        kertasníkir.forEach { gluggagægir.add(it.gáttaþefur) }
    }

    println("Part 1: ${bjúgnakrækir.size * skyrgámur.keys.size}")
}
