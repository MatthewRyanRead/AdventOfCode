package tech.readonly.aoc.aoc2023

import java.io.File
import java.util.ArrayDeque
import java.util.Scanner

private abstract class Module(
    val name: String,
    val incoming: MutableList<String>,
    val outgoing: List<String>,
) {
    open fun addIncoming(src: String) {
        incoming.add(src)
    }

    abstract fun pulse(pulse: Pulse): List<Pulse>
}

private class Broadcaster(name: String, incoming: MutableList<String>, outgoing: List<String>) :
    Module(name, incoming, outgoing) {
    override fun pulse(pulse: Pulse): List<Pulse> {
        return outgoing.map { Pulse(name, it, pulse.low) }
    }
}

private class FlipFlop(name: String, incoming: MutableList<String>, outgoing: List<String>) :
    Module(name, incoming, outgoing) {
    var on = false

    override fun pulse(pulse: Pulse): List<Pulse> {
        if (!pulse.low) {
            return emptyList()
        }

        return if (on) {
            on = false
            outgoing.map { Pulse(name, it, true) }
        } else {
            on = true
            outgoing.map { Pulse(name, it, false) }
        }
    }
}

private class Conjunction(name: String, incoming: MutableList<String>, outgoing: List<String>) :
    Module(name, incoming, outgoing) {
    val lastLowBySrc = mutableMapOf<String, Boolean>()

    override fun pulse(pulse: Pulse): List<Pulse> {
        lastLowBySrc[pulse.src] = pulse.low

        if (lastLowBySrc.values.all { !it }) {
            return outgoing.map { Pulse(name, it, true) }
        }

        return outgoing.map { Pulse(name, it, false) }
    }

    override fun addIncoming(src: String) {
        super.addIncoming(src)
        lastLowBySrc[src] = true
    }
}

private data class Pulse(val src: String, val dst: String, val low: Boolean)

fun main() {
    println("Part 1: ${part1(getModules())}")
    println("Part 2: ${part2(getModules())}")
}

private fun getModules(): Map<String, Module> {
    val moduleByName = mutableMapOf<String, Module>()
    Scanner(File(ClassLoader.getSystemResource("inputs/Day20.txt").file)).use { scanner ->
        sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.forEach {
            var (name, outgoingStr) = it.split(" -> ")
            val outgoing = outgoingStr.split(", ")

            val module = when (name[0]) {
                '%' -> {
                    name = name.substring(1)
                    FlipFlop(name, mutableListOf(), outgoing)
                }

                '&' -> {
                    name = name.substring(1)
                    Conjunction(name, mutableListOf(), outgoing)
                }

                else -> Broadcaster(name, mutableListOf(), outgoing)
            }

            moduleByName[name] = module
        }
    }

    moduleByName.values.forEach { src ->
        src.outgoing.forEach { dst ->
            moduleByName[dst]?.addIncoming(src.name)
        }
    }

    return moduleByName
}

private fun part1(moduleByName: Map<String, Module>): Long {
    var lowPulses = 0L
    var highPulses = 0L
    val pendingPulses = ArrayDeque<Pulse>()

    for (i in 1..1000) {
        pendingPulses.add(Pulse("", "broadcaster", true))
        while (pendingPulses.isNotEmpty()) {
            val pulse = pendingPulses.poll()
            if (pulse.low) lowPulses++ else highPulses++
            pendingPulses.addAll(moduleByName[pulse.dst]?.pulse(pulse) ?: emptyList())
        }
    }

    return lowPulses * highPulses
}

private fun part2(moduleByName: Map<String, Module>): Long {
    val modulesToWatch = moduleByName["qt"]!!.incoming.toSet()
    val cycleLenByModule = mutableMapOf<String, Long>()
    val pendingPulses = ArrayDeque<Pulse>()

    for (i in 1L..Long.MAX_VALUE) {
        pendingPulses.add(Pulse("", "broadcaster", true))
        while (pendingPulses.isNotEmpty()) {
            val pulse = pendingPulses.poll()
            if (modulesToWatch.contains(pulse.src) && !pulse.low
                && !cycleLenByModule.containsKey(pulse.src)
            ) {
                cycleLenByModule[pulse.src] = i
            }
            pendingPulses.addAll(moduleByName[pulse.dst]?.pulse(pulse) ?: emptyList())
        }

        if (cycleLenByModule.size == modulesToWatch.size) {
            return cycleLenByModule.values.reduce { acc, len -> acc * len }
        }
    }

    return -1L
}
