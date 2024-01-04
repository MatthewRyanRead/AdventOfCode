lines = split(read("../resources/inputs/Day7.txt", String), '\n')

nodes = Set{String}()
edges = Set{Pair{String, Pair{String, Int}}}()

for line in lines
    parts = split(first(line, length(line) - 1), " contain ")
    parent_name = split(parts[1], " bags")[1]
    children = split(parts[2], ", ")

    push!(nodes, parent_name)

    if children[1] == "no other bags"
        continue
    end

    for child in children
        child_parts = split(child, " ", limit=2)
        count = parse(Int, child_parts[1])
        child_name = split(child_parts[2], r" bags?")[1]

        push!(nodes, child_name)
        push!(edges, Pair(parent_name, Pair(child_name, count)))
    end
end

visited = Set{String}()
to_visit = Set{String}(["shiny gold"])

while !isempty(to_visit)
    node = pop!(to_visit)
    push!(visited, node)

    for edge in edges
        if edge.second.first == node && edge.first ∉ visited
            push!(to_visit, edge.first)
        end
    end
end

println("Part 1: ", length(visited) - 1)

function count_bags(node::String)::Int
    count = 0
    for edge in edges
        if edge.first == node
            count += edge.second.second * (count_bags(edge.second.first) + 1)
        end
    end

    return count
end

println("Part 2: ", count_bags("shiny gold"))
