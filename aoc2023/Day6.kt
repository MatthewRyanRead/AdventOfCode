import java.io.File
import java.util.Scanner
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class Race(val id : Int, val maxTime : Long, val dist : Long)

fun main() {
    val races = mutableListOf<Race>()
    Scanner(File("inputs/Day6.txt")).use {
        val times = it.nextLine().trim().split(" ").map { s -> s.toLong() }
        val distances = it.nextLine().trim().split(" ").map { s -> s.toLong() }

        times.indices.forEach { i ->
                races.add(Race(id = i + 1, maxTime = times[i], dist = distances[i]))
        }
    }

    println("Part 1: ${solve(races)}")

    val part2Race = Race(
            id = 0,
            maxTime = races.joinToString(separator = "") { r -> r.maxTime.toString() }.toLong(),
            dist = races.joinToString(separator = "") { r -> r.dist.toString() }.toLong(),
    )

    println("Part 2: ${solve(listOf(part2Race))}")
}

fun solve(races : List<Race>) : Long {
    var score = 1L

    for (race in races) {
        // quadratic formula where a = 1
        val maxTimeSquared : Long = race.maxTime * race.maxTime
        val discriminantRoot : Double = sqrt(maxTimeSquared - 4.0 * race.dist)
        val realChargeTimeLow : Double = (race.maxTime - discriminantRoot) / 2.0
        val realChargeTimeHigh : Double = (race.maxTime + discriminantRoot) / 2.0

        // 10.0 -> 10 -> 11, 10.1 -> 10 -> 11, 9.9 -> 9 -> 10, etc
        val winningChargeTimeLow = realChargeTimeLow.toLong() + 1L
        // 10.0 -> 10.0 -> 10 -> 9, 10.1 -> 11.0 -> 11 -> 10, 9.9 -> 10.0 -> 10 -> 9, etc
        val winningChargeTimeHigh = ceil(realChargeTimeHigh).roundToLong() - 1L

        score *= (winningChargeTimeHigh - winningChargeTimeLow) + 1L
    }

    return score
}
