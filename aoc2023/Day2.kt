import java.io.File
import java.util.Scanner

data class Game(val id : Long, val red : List<Long>, val green : List<Long>, val blue : List<Long>)

fun main() {
    val games = mutableListOf<Game>()

    Scanner(File("inputs/Day2.txt")).use {
        while (it.hasNextLine()) {
            val (gameStr, pullStr) = it.nextLine().trim().split(":")
            val id = gameStr.trim().split(" ")[1].toLong()
            val pulls = pullStr.trim().split(";")

            val red = mutableListOf<Long>()
            val green = mutableListOf<Long>()
            val blue = mutableListOf<Long>()
            for (pull in pulls) {
                val components = pull.trim().split(",")
                var (r, g, b) = arrayOf(0L, 0L, 0L)

                for (component in components) {
                    val (countStr, colorStr) = component.trim().split(" ")
                    val count = countStr.toLong()
                    when (colorStr) {
                        "red" -> r = count
                        "green" -> g = count
                        "blue" -> b = count
                        else -> {
                            error("unknown color: $colorStr")
                        }
                    }
                }

                red.add(r)
                green.add(g)
                blue.add(b)
            }

            games.add(Game(id = id, red = red, green = green, blue = blue))
        }
    }

    part1(games)
    part2(games)
}

fun part1(games : List<Game>) {
    val (maxRed, maxGreen, maxBlue) = arrayOf(12L, 13L, 14L)
    var idSum = 0L

    for (game in games) {
        if (game.red.all { r -> r <= maxRed }
                && game.green.all { g -> g <= maxGreen }
                && game.blue.all { b -> b <= maxBlue }) {
            idSum += game.id
        }
    }

    println("Part 1: $idSum")
}

fun part2(games: List<Game>) {
    var powerSum = 0L

    for (game in games) {
        powerSum += game.red.max() * game.green.max() * game.blue.max()
    }

    println("Part 2: $powerSum")
}
