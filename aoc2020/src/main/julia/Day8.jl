using Multibreak

lines = split(read("../resources/inputs/Day8.txt", String), '\n')

commands = Vector{Pair{String, Int}}()
for line in lines
    parts = split(line, " ")
    cmd = parts[1]
    val = parse(Int, parts[2])

    push!(commands, Pair(cmd, val))
end

function exec(cmds::Vector{Pair{String, Int}}, swap_idx::Int)::Pair{Bool, Int}
    visited = Set{Int}()
    acc = 0
    curr_idx = 1
    curr_op = 1

    while curr_idx ∉ visited
        if curr_idx > length(cmds)
            return Pair(true, acc)
        end

        push!(visited, curr_idx)
        cmd = cmds[curr_idx]
        op = cmd.first
        val = cmd.second

        if curr_op == swap_idx
            if op == "jmp"
                op = "nop"
            elseif op == "nop"
                op = "jmp"
            end
        end

        if op == "acc"
            acc += val
            curr_idx += 1
        elseif op == "jmp"
            curr_op += 1
            curr_idx += val
        else
            curr_op += 1
            curr_idx += 1
        end
    end

    return Pair(false, acc)
end

println("Part 1: ", exec(commands, 0).second)

for i in 1:count(o -> o.first == "nop" || o.first == "jmp", commands)
    result = exec(commands, i)
    if result.first
        println("Part 2: ", result.second)
        break
    end
end
