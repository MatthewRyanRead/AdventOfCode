grid = split(read("../resources/inputs/Day3.txt", String), '\n')

num_rows = length(grid)
num_cols = length(grid[1])

function solve(step::Tuple{Int, Int})::Int
    num_trees = 0
    curr_pos = (1, 1)
    for _ in (1 + step[1]):step[1]:num_rows
        next_row = curr_pos[1] + step[1]
        next_col = curr_pos[2] + step[2]
        if next_col > num_cols
            next_col = (next_col - num_cols)
        end
        curr_pos = (next_row, next_col)

        if grid[curr_pos[1]][curr_pos[2]] == '#'
            num_trees += 1
        end
    end

    return num_trees
end

part_1_result = solve((1, 3))
println("Part 1: ", part_1_result)

other_results = (solve((1, 1)), solve((1, 5)), solve((1, 7)), solve((2, 1)))
println("Part 2: ", part_1_result * reduce(*, other_results))
