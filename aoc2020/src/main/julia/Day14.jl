using Combinatorics

lines = split(read("../resources/inputs/Day14.txt", String), '\n')

or_mask = 0
and_mask = 0
memory = Dict{String, Int}()
for line in lines
    parts = split(line, " = ")
    if parts[1] == "mask"
        global or_mask = parse(Int, replace(parts[2], 'X' => '0'), base = 2)
        global and_mask = parse(Int, replace(parts[2], 'X' => '1'), base = 2)
    else
        addr = parts[1][5:end-1]
        val = parse(Int, parts[2])
        memory[addr] = (val | or_mask) & and_mask
    end
end

println("Part 1: ", sum(values(memory)))

function get_addrs(start_addr::Int)::Set{Int}
    addrs = Set{Int}()
    for ones in one_options
        addr = start_addr
        for i in x_indices
            bit = 1 << (i - 1)
            if i in ones
                addr = addr | bit
            else
                addr = addr & ~bit
            end
        end

        push!(addrs, addr)
    end

    return addrs
end

memory = Dict{Int, Int}()
for line in lines
    parts = split(line, " = ")
    if parts[1] == "mask"
        global or_mask = parse(Int, replace(parts[2], 'X' => '0'), base = 2)
        char_by_idx = pairs(collect(reverse(parts[2])))
        global x_indices = map(p -> p.first, collect(filter(p -> p.second == 'X', char_by_idx)))
        # ironically, powerset() can't accept a Set as input
        global one_options = powerset(x_indices)
        global x_indices = Set(x_indices)
    else
        addr = parse(Int, parts[1][5:end-1]) | or_mask
        addrs = get_addrs(addr)
        val = parse(Int, parts[2])

        for a in addrs
            memory[a] = val
        end
    end
end

println("Part 2: ", sum(values(memory)))
