lines = split(read("../resources/inputs/Day12.txt", String), '\n')

cmds = Vector{Pair{Char, Int}}()
for line in lines
    cmd = line[1]
    val = parse(Int, line[2:end])
    push!(cmds, Pair(cmd, val))
end

function solve(p1::Bool)
    pos_dir_waypt = [[0, 0], [1, 0], [10, 1]]
    fwd_idx = if p1 2 else 3 end
    dir_idx = if p1 1 else 3 end

    for cmd in cmds
        if cmd.first == 'F'
            pos_dir_waypt[1] += [cmd.second] .* pos_dir_waypt[fwd_idx]
        elseif cmd.first == 'N'
            pos_dir_waypt[dir_idx] += [cmd.second] .* [0, 1]
        elseif cmd.first == 'E'
            pos_dir_waypt[dir_idx] += [cmd.second] .* [1, 0]
        elseif cmd.first == 'S'
            pos_dir_waypt[dir_idx] += [cmd.second] .* [0, -1]
        elseif cmd.first == 'W'
            pos_dir_waypt[dir_idx] += [cmd.second] .* [-1, 0]
        elseif cmd.first == 'L'
            for _ in 1:(cmd.second / 90)
                pos_dir_waypt[fwd_idx] = [-pos_dir_waypt[fwd_idx][2], pos_dir_waypt[fwd_idx][1]]
            end
        elseif cmd.first == 'R'
            for _ in 1:(cmd.second / 90)
                pos_dir_waypt[fwd_idx] = [pos_dir_waypt[fwd_idx][2], -pos_dir_waypt[fwd_idx][1]]
            end
        end
    end

    return pos_dir_waypt[1]
end

p1 = solve(true)
println("Part 1: ", abs(p1[1]) + abs(p1[2]))

p2 = solve(false)
println("Part 2: ", abs(p2[1]) + abs(p2[2]))
