using Multibreak

lines = split(read("../resources/inputs/Day16.txt", String), '\n')

ranges_by_name = Dict{String, Set{UnitRange{Int}}}()
my_ticket = Vector{Int}()
other_tickets = Set{Vector{Int}}()

section = 1
for line in lines
    if isempty(line)
        global section = -section - 1
        continue
    end
    # this is a silly way to skip a line without an extra flag, but hey it works
    if section < 0
        global section = -section
        continue
    end

    if section == 1
        parts = split(line, ": ")
        range_strs = split(parts[2], " or ")
        ranges = Set{UnitRange{Int}}()
        for range in range_strs
            nums = parse.(Int, split(range, '-'))
            push!(ranges, nums[1]:nums[2])
        end
        ranges_by_name[parts[1]] = ranges
    elseif section == 2
        append!(my_ticket, parse.(Int, split(line, ',')))
    else
        push!(other_tickets, parse.(Int, split(line, ',')))
    end
end

bad_tickets = Set{Vector{Int}}()
error_rate = 0
for ticket in other_tickets
    @multibreak begin
        for num in ticket
            for ranges in values(ranges_by_name)
                for range in ranges
                    if num >= range[begin] && num <= range[end]
                        break; break; continue
                    end
                end
            end

            global error_rate += num
            push!(bad_tickets, ticket)
        end
    end
end

println("Part 1: ", error_rate)

valid_tickets = setdiff(other_tickets, bad_tickets)
possible_idxs_by_name = Dict{String, Set{Int}}(
        name => Set{Int}(eachindex(my_ticket)) for name in keys(ranges_by_name))
for (name, ranges) in ranges_by_name
    for ticket in valid_tickets
        @multibreak begin
            for (i, num) in pairs(ticket)
                for range in ranges
                    if num >= range[begin] && num <= range[end]
                        break; continue
                    end
                end

                delete!(possible_idxs_by_name[name], i)
            end
        end
    end
end

function find_compatible_indices(pi_by_b::Dict{String, Set{Int}})::Union{Nothing, Dict{String, Int}}
    if all(p -> length(p.second) == 1, collect(pi_by_b))
        return Dict{String, Int}([Pair(n, first(s)) for (n, s) in pi_by_b])
    end
    if any(p -> length(p.second) == 0, collect(pi_by_b))
        return nothing
    end

    # should be able to take any remaining pair here, but some orderings don't work :/
    # not going to spend time figuring out why, though
    (name, options) = reduce((a, b) -> if length(a.second) <= length(b.second) a else b end,
            collect(pi_by_b))
    delete!(pi_by_b, name)

    for option in options
        sub_dict = Dict{String, Set{Int}}([Pair(n, copy(s)) for (n, s) in pi_by_b])
        for idxs in values(sub_dict)
            delete!(idxs, option)
        end

        result = find_compatible_indices(sub_dict)
        if result !== nothing
            result[name] = option
            return result
        end
    end

    return nothing
end

product = 1
for (name, idx) in find_compatible_indices(possible_idxs_by_name)
    if !startswith(name, "departure")
        continue
    end

    global product *= my_ticket[idx]
end

println("Part 2: ", product)
