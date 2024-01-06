lines = split(read("../resources/inputs/Day17.txt", String), '\n')

active = Set{Vector{Int}}()
for (x, line) in pairs(lines)
    for (y, char) in pairs(line)
        if char == '#'
            push!(active, [x, y, 0])
        end
    end
end

function neighbours3D(coords::Vector{Int})::Vector{Vector{Int}}
    n = Vector{Vector{Int}}()
    for x in -1:1
        for y in -1:1
            for z in -1:1
                if x == 0 && y == 0 && z == 0
                    continue
                end

                push!(n, [coords[1] + x, coords[2] + y, coords[3] + z])
            end
        end
    end

    return n
end

function solve(neighbour_fn::Function)
    for _ in 1:6
        inactive_neighbours = Set{Vector{Int}}()
        to_activate = Set{Vector{Int}}()
        to_deactivate = Set{Vector{Int}}()

        for coords in active
            n = neighbour_fn(coords)
            for neighbour in filter(a -> !(a in active), n)
                push!(inactive_neighbours, neighbour)
            end

            num_active = length(filter(neighbour -> neighbour in active, n))
            if num_active < 2 || num_active > 3
                push!(to_deactivate, coords)
            end
        end

        for coords in inactive_neighbours
            n = neighbour_fn(coords)
            num_active = length(filter(neighbour -> neighbour in active, n))
            if num_active == 3
                push!(to_activate, coords)
            end
        end

        for coords in to_activate
            push!(active, coords)
        end
        for coords in to_deactivate
            delete!(active, coords)
        end
    end
end

solve(neighbours3D)
println("Part 1: ", length(active))

empty!(active)
for (x, line) in pairs(lines)
    for (y, char) in pairs(line)
        if char == '#'
            push!(active, [x, y, 0, 0])
        end
    end
end

function neighbours4D(coords::Vector{Int})::Vector{Vector{Int}}
    n = Vector{Vector{Int}}()
    wless_neighbours = neighbours3D(coords)
    push!(wless_neighbours, [coords[1], coords[2], coords[3]])

    for w in -1:1
        for n3D in wless_neighbours
            if n3D[1] == coords[1] && n3D[2] == coords[2] && n3D[3] == coords[3] && w == 0
                continue
            end

            push!(n, [n3D[1], n3D[2], n3D[3], coords[4] + w])
        end
    end

    return n
end

solve(neighbours4D)
println("Part 2: ", length(active))
