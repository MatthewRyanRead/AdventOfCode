struct Entry
    min::Int
    max::Int
    ch::Char
    pass::AbstractString

    function Entry(m1::Int, m2::Int, c::Char, p::AbstractString)
        new(m1, m2, c, p)
    end
end

entries = Vector{Entry}()

for str in split(read("../resources/inputs/Day2.txt", String), '\n')
    parts = split(str, ' ')
    nums = split(parts[1], '-')
    min = parse(Int, nums[1])
    max = parse(Int, nums[2])
    ch = parts[2][1]
    push!(entries, Entry(min, max, ch, parts[3]))
end

num_valid = 0
for entry in entries
    c = count(ch -> ch == entry.ch, entry.pass)
    if c >= entry.min && c <= entry.max
        global num_valid += 1
    end
end

println("Part 1: ", num_valid)

num_valid = 0
for entry in entries
    if (entry.pass[entry.min] == entry.ch) ⊻ (entry.pass[entry.max] == entry.ch)
        global num_valid += 1
    end
end

println("Part 2: ", num_valid)
