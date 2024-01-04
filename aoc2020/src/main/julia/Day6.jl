lines = split(read("../resources/inputs/Day6.txt", String), '\n')

group_responses = Vector{Vector{String}}()
curr_group = Vector{String}()

for line in lines
    if isempty(line)
        push!(group_responses, curr_group)
        global curr_group = Vector{String}()
    else
        push!(curr_group, line)
    end
end
push!(group_responses, curr_group)

any_total = 0
all_total = 0
for group_response in group_responses
    any_yes = Set{Char}()
    all_yes = nothing

    for response in group_response
        chars = Set{Char}()
        for char in response
            push!(chars, char)
        end
        any_yes = union(any_yes, chars)

        if all_yes === nothing
            all_yes = chars
        else
            all_yes = intersect(all_yes, chars)
        end
    end

    global any_total += length(any_yes)
    global all_total += length(all_yes)
end

println("Part 1: ", any_total)
println("Part 2: ", all_total)
