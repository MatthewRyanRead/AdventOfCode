using Multibreak
using PyCall

lines = split(read("../resources/inputs/Day19.txt", String), '\n')

inputs = filter(line -> !isempty(line) && !contains(line, ':'), lines)
base_rules = filter(line -> contains(line, '"'), lines)
other_rules = filter(line -> contains(line, ':') && !contains(line, '"'), lines)

function init_vals()
    for rule in base_rules
        parts = split(rule, ": ")
        global val_by_id[parse(Int, parts[1])] = "$(parts[2][2])"
    end
end

val_by_id = Dict{Int, String}()
init_vals()

rule_by_id = Dict{Int, Vector{Vector{Int}}}()
for rule in other_rules
    parts = split(rule, ": ")
    or_strs = split(parts[2], " | ")
    ors = Vector{Vector{Int}}()
    for or_str in or_strs
        or = Vector{Int}()
        for component in split(or_str, ' ')
            push!(or, parse(Int, component))
        end

        push!(ors, or)
    end

    global rule_by_id[parse(Int, parts[1])] = ors
end

function get_regex(id::Int, depth::Int = 0)::String
    if depth > 20
        return ""
    end
    if id in keys(val_by_id)
        return val_by_id[id]
    end

    ors = rule_by_id[id]
    strs = Vector{String}()
    @multibreak begin
        for or in ors
            str = ""
            for component_id in or
                partial = get_regex(component_id, depth+1)
                if isempty(partial)
                    break; continue
                end

                str *= partial
            end

            push!(strs, str)
        end
    end

    regex_str = "($(join(filter(str -> !isempty(str), strs), '|')))"
    val_by_id[id] = regex_str

    return regex_str
end

function solve()::Vector{String}
    # P2 results in "regular expression is too large" with Julia's Regex class,
    # even after I condensed it a bunch. So, let's just use Python :P
    re = pyimport("re")
    matcher = re.compile("^$(get_regex(0))\$")

    return filter(input -> matcher.match(input) !== nothing, inputs)
end

println("Part 1: ", length(solve()))

val_by_id = Dict{Int, String}()
init_vals()
rule_by_id[8] = [[42], [42, 8]]
rule_by_id[11] = [[42, 31], [42, 11, 31]]

println("Part 2: ", length(solve()))
