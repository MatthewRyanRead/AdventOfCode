package tech.readonly.aoc.aoc2023

import tech.readonly.aoc.aoc2023.Operator.GT
import tech.readonly.aoc.aoc2023.Operator.LT
import java.io.File
import java.util.Scanner
import kotlin.math.max
import kotlin.math.min

data class Part(val x: Int, val m: Int, val a: Int, val s: Int) {
    val value = x + m + a + s

    fun getProp(prop: Char): Int {
        return when (prop) {
            'x' -> x
            'm' -> m
            'a' -> a
            's' -> s
            else -> error("Unknown property: $prop")
        }
    }
}

enum class Operator(val char: Char) {
    LT('<'),
    GT('>'),
    ;
}

data class Condition(val prop: Char, val op: Operator, val value: Int, val matchesDst: String)

data class Rule(val name: String, val conditions: List<Condition>, val finalDst: String)

private data class TargetRanges(val name: String) {
    val listOfRangeByProp = mutableListOf<Map<Char, IntRange>>()
}

fun main() {
    val rules = mutableListOf<Rule>()
    val parts = mutableListOf<Part>()

    var isRule = true
    Scanner(File(ClassLoader.getSystemResource("inputs/Day19.txt").file)).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.forEach {
            if (it.isEmpty()) {
                isRule = false
                return@forEach
            }

            if (isRule) {
                rules.add(parseRule(it))
            } else {
                parts.add(parsePart(it))
            }
        }
    }

    val ruleByName = rules.associateBy { it.name }

    println("Part 1: ${part1(ruleByName, parts)}")
    println("Part 2: ${part2(ruleByName)}")
}

private fun part1(ruleByName: Map<String, Rule>, parts: List<Part>): Int {
    val startRule = ruleByName["in"]!!

    var sum = 0
    parts.forEach { part ->
        var rule = startRule
        while (true) {
            var dstToCheck = rule.finalDst

            for (cond in rule.conditions) {
                val partVal = part.getProp(cond.prop)
                if ((cond.op == LT && partVal < cond.value) || (cond.op == GT && partVal > cond.value)) {
                    dstToCheck = cond.matchesDst
                    break
                }
            }

            when (dstToCheck) {
                "A" -> {
                    sum += part.value
                    break
                }
                "R" -> break
                else -> rule = ruleByName[dstToCheck]!!
            }
        }
    }

    return sum
}

private fun part2(ruleByName: Map<String, Rule>): Long {
    val targetRanges = ruleByName.keys.map { TargetRanges(it) }.toMutableList()
    targetRanges.add(TargetRanges("A"))
    targetRanges.add(TargetRanges("R"))
    val targetByName = targetRanges.associateBy { it.name }

    computeAcceptedRanges(
        ruleByName, targetByName, "in", mutableMapOf(
            Pair('x', 1..4000),
            Pair('m', 1..4000),
            Pair('a', 1..4000),
            Pair('s', 1..4000),
        )
    )

    return targetByName["A"]!!.listOfRangeByProp.sumOf { rangeByProp ->
        rangeByProp.values.map { range -> (range.last - range.first + 1).toLong() }
            .reduce { product, size -> product * size }
    }
}

private fun computeAcceptedRanges(
    ruleByName: Map<String, Rule>,
    targetRangesByName: Map<String, TargetRanges>,
    targetName: String,
    startRanges: Map<Char, IntRange>,
) {
    val targetRange = targetRangesByName[targetName]!!
    targetRange.listOfRangeByProp.add(startRanges)

    if (targetName == "A" || targetRange.name == "R") {
        // final destinations have no rules/conditions associated
        return
    }
    val rule = ruleByName[targetName]!!

    val currRanges = startRanges.toMutableMap()
    for (cond in rule.conditions) {
        // find the subrange that matches the rule
        val startRange = currRanges[cond.prop]!!
        val matchRange = if (cond.op == LT) 1..<cond.value else (cond.value + 1)..4000
        val matchIntersection =
            max(startRange.first, matchRange.first)..min(startRange.last, matchRange.last)
        if (matchIntersection.first < matchIntersection.last) {
            // further break down the ranges when a new subset matches for this condition
            val newRanges = currRanges.toMutableMap()
            newRanges[cond.prop] = matchIntersection
            computeAcceptedRanges(ruleByName, targetRangesByName, cond.matchesDst, newRanges)
        }

        // for the next condition in the rule, set the range to what is left over from the match
        val missRange = if (cond.op == GT) 1..cond.value else (cond.value)..4000
        val missIntersection =
            max(startRange.first, missRange.first)..min(startRange.last, missRange.last)
        currRanges[cond.prop] = missIntersection
    }

    computeAcceptedRanges(ruleByName, targetRangesByName, rule.finalDst, currRanges)
}

private fun parseRule(str: String): Rule {
    val (name, ruleStr) = str.split('{')
    val conditionStrs = ruleStr.substring(0, ruleStr.lastIndex).split(',')

    val conditions = mutableListOf<Condition>()
    var finalDst = ""
    conditionStrs.forEachIndexed { i, cond ->
        if (i == conditionStrs.lastIndex) {
            finalDst = cond
        } else {
            val (check, dst) = cond.split(':')
            val op = if (check.contains('<')) LT else GT
            val (prop, valueStr) = check.split(op.char)

            conditions.add(Condition(prop[0], op, valueStr.toInt(), dst))
        }
    }

    return Rule(name, conditions, finalDst)
}

private fun parsePart(str: String): Part {
    val props = str.substring(1, str.lastIndex).split(',')
        .map { it.split('=') }
        .associate { Pair(it[0][0], it[1].toInt()) }

    return Part(props['x']!!, props['m']!!, props['a']!!, props['s']!!)
}
