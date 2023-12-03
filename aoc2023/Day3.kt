import java.io.File
import java.util.Scanner
import java.util.stream.IntStream

data class Coord(val row: Int, val col: Int)
data class CoordLine(val row: Int, val startCol: Int, val endCol: Int)

fun main() {
    val board = mutableListOf<String>()
    Scanner(File("inputs/Day3.txt")).use {
        while (it.hasNextLine()) {
            board.add(it.nextLine().trim())
        }
    }

    val (numCoords, symbolCoords) = getNumCoords(board)

    part1(board, numCoords, symbolCoords)
    part2(board, numCoords, symbolCoords)
}

fun getNumCoords(board: List<String>): Pair<List<CoordLine>, Set<Coord>> {
    val numCoords = mutableListOf<CoordLine>()
    val symbolCoords = mutableSetOf<Coord>()

    board.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            if (row[x].isDigit()) {
                val startCol = x
                var endCol = x
                while (x < row.length && row[x].isDigit()) {
                    endCol = x
                    x++
                }

                numCoords.add(CoordLine(y, startCol, endCol))
            } else if (row[x] != '.') {
                symbolCoords.add(Coord(y, x))
                x++
            } else {
                x++
            }
        }
    }

    return Pair(numCoords, symbolCoords)
}

fun part1(board: List<String>, numCoords: List<CoordLine>, symbolCoords: Set<Coord>) {
    val sum = numCoords.filter {
        for (y in (it.row - 1)..(it.row + 1)) {
            for (x in (it.startCol - 1)..(it.endCol + 1)) {
                if (Coord(y, x) in symbolCoords) {
                    return@filter true
                }
            }
        }
        return@filter false
    }.sumOf {
        board[it.row].substring(it.startCol, it.endCol + 1).toLong()
    }

    println("Part 1: $sum")
}

fun part2(board: List<String>, numCoords: List<CoordLine>, symbolCoords: Set<Coord>) {
    val numById = numCoords.withIndex().associateBy(
            { it.index },
            { board[it.value.row].substring(it.value.startCol, it.value.endCol + 1).toLong() }
    )
    val numIdByCoord = mutableMapOf<Coord, Int>()
    numCoords.forEachIndexed { id, coord ->
        IntStream.rangeClosed(coord.startCol, coord.endCol)
                .boxed()
                .map { x -> Coord(coord.row, x) }
                .forEach { numIdByCoord[it] = id }
    }

    var ratioSum = 0L
    for (symbolCoord in symbolCoords) {
        if (board[symbolCoord.row][symbolCoord.col] != '*') {
            continue
        }

        val nearbyNumIds = mutableSetOf<Int>()
        for (y in (symbolCoord.row - 1)..(symbolCoord.row + 1)) {
            for (x in (symbolCoord.col - 1)..(symbolCoord.col + 1)) {
                val nearbyCoord = Coord(y, x)
                if (nearbyCoord in numIdByCoord.keys) {
                    nearbyNumIds.add(numIdByCoord[nearbyCoord]!!)
                }
            }
        }

        if (nearbyNumIds.size == 2) {
            val numIds = nearbyNumIds.toList()
            ratioSum += numById[numIds[0]]!! * numById[numIds[1]]!!
        }
    }

    println("Part 2: $ratioSum")
}
