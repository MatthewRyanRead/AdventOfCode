import SpringRow.Companion.DFS_GREEDY
import SpringRow.Companion.DFS_SKIP
import java.io.File
import java.util.Scanner

data class SpringRow(val str: String, val groups: List<Int>) {
    companion object {
        val DFS_GREEDY = mutableMapOf<SpringRow, Long>()
        val DFS_SKIP = mutableMapOf<SpringRow, Long>()
    }
}

fun main() {
    val part1Rows = mutableListOf<SpringRow>()
    val part2Rows = mutableListOf<SpringRow>()
    Scanner(File("inputs/Day12.txt")).use { scanner ->
        while (scanner.hasNextLine()) {
            var (row, groupsStr) = scanner.nextLine().trim().split(' ')
            val groups = groupsStr.split(',').map { it.toInt() }

            // reduce gaps to 1 spot
            var row2 = row.replace("..", ".")
            while (row != row2) {
                row = row2
                row2 = row.replace("..", ".")
            }

            part1Rows.add(SpringRow(row.trim('.'), groups))

            val extendedRow = (1..5).map { row }.joinToString(separator = "?")
            val extendedGroups = (1..5).flatMap { groups }

            part2Rows.add(SpringRow(extendedRow.trim('.'), extendedGroups))
        }
    }

    println("Part 1: ${solve(part1Rows)}")
    println("Part 2: ${solve(part2Rows)}")
}

private fun solve(rows: List<SpringRow>): Long {
    return rows.sumOf { dfs(it) }
}

private fun dfs(row: SpringRow): Long {
    var score = DFS_GREEDY.getOrPut(row) { greedy(row) }
    // can only skip if we are not definitely in a group
    if (row.str[0] != '#') {
        score += DFS_SKIP.getOrPut(row) { skip(row) }
    }

    return score
}

private fun greedy(row: SpringRow): Long {
    if (row.str.length < totalNeeded(row.groups)) {
        // not enough chars remain to match the groups
        return 0L
    }

    for (i in 0..<row.groups[0]) {
        // the group must consist of '#' or '?'
        if (row.str[i] == '.') {
            return 0L
        }
    }

    if (row.str.length == row.groups[0]) {
        // the final group matched with no chars remaining
        return 1L
    }

    val newGroups = row.groups.subList(1, row.groups.size)
    val newStr = row.str.substring(row.groups[0] + 1)
    if (newGroups.isEmpty()) {
        if (newStr.contains('#') || row.str[row.groups[0]] == '#') {
            // undesired group remains
            return 0L
        }

        // all groups matched and no "definite" groups remain
        return 1L
    }

    if (newStr.isEmpty() || row.str[row.groups[0]] == '#') {
        // nothing remains for needed groups, or the current group was actually bigger than desired
        return 0L
    }

    return dfs(SpringRow(newStr, newGroups))
}

private fun skip(row: SpringRow): Long {
    // need enough for the groups + at least one to skip
    if (row.str.length <= totalNeeded(row.groups)) {
        return 0L
    }

    var newIndex = 1
    if (row.str[newIndex] == '.') {
        // also skip the space if we landed on it
        newIndex++
    }

    return dfs(SpringRow(row.str.substring(newIndex), row.groups))
}

fun totalNeeded(groups: List<Int>): Int {
    return groups.sum() + groups.size - 1
}
