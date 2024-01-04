using Memoization

lines = split(read("../resources/inputs/Day10.txt", String), '\n')

joltages = parse.(Int, lines)
push!(joltages, 0)
sort!(joltages)
push!(joltages, joltages[end] + 3)
last_idx = lastindex(joltages)

count_by_step = Dict{Int, Int}(1 => 0, 2 => 0, 3 => 0)
for i in 2:last_idx
    step = joltages[i] - joltages[i - 1]
    count_by_step[step] += 1
end

println("Part 1: ", count_by_step[1] * count_by_step[3])

@memoize function ways_reachable(from_idx::Int)::Int
    if from_idx == last_idx
        return 1
    end

    ways = ways_reachable(from_idx + 1)
    joltage = joltages[from_idx]
    if from_idx + 1 < last_idx && joltages[from_idx + 2] <= joltage + 3
        ways += ways_reachable(from_idx + 2)
        if from_idx + 2 < last_idx && joltages[from_idx + 3] <= joltage + 3
            ways += ways_reachable(from_idx + 3)
        end
    end

    return ways
end

println("Part 2: ", ways_reachable(1))
