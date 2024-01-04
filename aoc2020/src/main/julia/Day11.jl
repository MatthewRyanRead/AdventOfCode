lines = split(read("../resources/inputs/Day11.txt", String), '\n')

function make_grid()::Vector{Vector{Char}}
    grid = Vector{Vector{Char}}()
    for line in lines
        row = Vector{Char}()
        for char in line
            if char == '.'
                push!(row, char)
            else
                push!(row, '#')
            end
        end
        push!(grid, row)
    end

    return grid
end

function occupied_neighbours(grid::Vector{Vector{Char}}, row::Int, col::Int)::Int
    n = 0
    max_col = lastindex(grid[1])

    for r in max(1, row - 1):min(row + 1, lastindex(grid))
        for c in max(1, col - 1):min(col + 1, max_col)
            if r == row && c == col
                continue
            end

            char = grid[r][c]
            if (char == '#')
                n += 1
            end
        end
    end

    return n
end

# this is slow, but input is small enough that I'm not going to add caching for the nearest-chairs-in-sight
function occupied_in_sight(grid::Vector{Vector{Char}}, row::Int, col::Int)::Int
    n = 0
    max_row = lastindex(grid)
    max_col = lastindex(grid[1])

    found = Dict{Pair{Int, Int}, Bool}(
        Pair(-1, -1) => false,
        Pair(-1, 0) => false,
        Pair(-1, 1) => false,
        Pair(0, -1) => false,
        Pair(0, 1) => false,
        Pair(1, -1) => false,
        Pair(1, 0) => false,
        Pair(1, 1) => false
    )

    for i in 1:max(max_row - row, row - 1, max_col - col, col - 1)
        prev_row = row - i
        next_row = row + i
        prev_col = col - i
        next_col = col + i

        for r in Set{Int}([prev_row, row, next_row])
            if r <= 0 || r > max_row
                continue
            end

            for c in Set{Int}([prev_col, col, next_col])
                if c <= 0 || c > max_col || (r == row && c == col)
                    continue
                end

                p = Pair(sign(row - r), sign(col - c))
                if grid[r][c] != '.' && !found[p]
                    found[p] = true
                    if grid[r][c] == '#'
                        n += 1
                    end
                end
            end
        end
    end

    return n
end

function print_grid(grid::Vector{Vector{Char}})
    for row in grid
        for c in row
            print(c)
        end
        println()
    end
    println()
end

function solve(visibly_occupied::Function, too_many_occupying::Int)::Int
    grid = make_grid()

    changed = true
    while (changed)
        changed = false
        indices_to_flip = Set{Pair{Int, Int}}()

        for (r, row) in pairs(grid)
            for (c, char) in pairs(row)
                if char == '.'
                    continue
                end

                n = visibly_occupied(grid, r, c)

                if n == 0 && char == 'L'
                    changed = true
                    push!(indices_to_flip, Pair(r, c))
                elseif n >= too_many_occupying && char == '#'
                    changed = true
                    push!(indices_to_flip, Pair(r, c))
                end
            end
        end

        for p in indices_to_flip
            if grid[p.first][p.second] == '#'
                grid[p.first][p.second] = 'L'
            else
                grid[p.first][p.second] = '#'
            end
        end

        #print_grid(grid)
    end

    return sum(row -> count(c -> c == '#', row), grid)
end

println("Part 1: ", solve(occupied_neighbours, 4))
println("Part 2: ", solve(occupied_in_sight, 5))
