import java.io.File
import java.util.Scanner

data class LensCommand(val str: String) {
    companion object Cache {
        val HASH_CACHE = mutableMapOf<String, Int>()
    }

    val id = str.toCharArray().takeWhile { it != '-' && it != '=' }.toCharArray()
        .joinToString(separator = "")
    val operator = str[id.length]
    val focalLength = operator.takeIf { it == '=' }?.let { str.substring(id.length + 1).toInt() }

    fun idHash() = hash(id)
    fun strHash() = hash(str)

    private fun hash(hashStr: String): Int {
        return HASH_CACHE.getOrPut(hashStr) {
            hashStr.filterIndexed { i, _ -> i != 0 }.map { it.code }
                .fold((hashStr[0].code * 17) % 256) { acc, i -> ((acc + i) * 17) % 256 }
        }
    }
}

data class Box(val id: Int) {
    val list = mutableListOf<LensCommand>()
    private val indexById = mutableMapOf<String, Int>()

    fun upsert(command: LensCommand) {
        val prevIndex = indexById[command.id]
        if (prevIndex != null) {
            list[prevIndex] = command
        } else {
            list.addLast(command)
            indexById[command.id] = list.lastIndex
        }
    }

    fun delete(command: LensCommand) {
        val index = indexById[command.id]
        if (index != null) {
            list.removeAt(index)
            indexById.remove(command.id)

            // not the most efficient, but today we don't have to worry about it
            indexById.filter { it.value > index }.forEach { indexById[it.key] = it.value - 1 }
        }
    }

    override fun toString(): String {
        return "Box $id: ${list.joinToString(separator = " ") { "[${it.id} ${it.focalLength}]" }}"
    }
}

fun main() {
    val commands = Scanner(File("inputs/Day15.txt")).use { scanner ->
        scanner.nextLine().trim()
    }.split(',').map { LensCommand(it) }

    println("Part 1: ${part1(commands)}")
    println("Part 2: ${part2(commands)}")
}

private fun part1(commands: List<LensCommand>): Int {
    return commands.sumOf { it.strHash() }
}

private fun part2(commands: List<LensCommand>): Int {
    val boxesById = mutableMapOf<Int, Box>()
    commands.forEach { command ->
        val box = boxesById.computeIfAbsent(command.idHash()) { Box(it) }

        if (command.operator == '-') {
            box.delete(command)
        } else {
            box.upsert(command)
        }
    }

    return boxesById.entries.sumOf {
        (it.key + 1) * it.value.list.mapIndexed { i, c -> (i + 1) * c.focalLength!! }.sum()
    }
}
