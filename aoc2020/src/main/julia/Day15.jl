line = read("../resources/inputs/Day15.txt", String)

nums = parse.(Int, split(line, ','))
prev = Dict{Int, Int}()
last = Dict{Int, Int}()

last_num = -1
for (i, num) in pairs(nums)
    last[num] = i
    global last_num = num
end

# I expected to need to find a cycle, but the number of iterations here is actually pretty small
for i in (lastindex(nums) + 1):30000000
    curr_num = 0
    if last_num in keys(prev)
        curr_num = last[last_num] - prev[last_num]
    end

    if curr_num in keys(last)
        prev[curr_num] = last[curr_num]
    end

    last[curr_num] = i
    global last_num = curr_num

    if i == 2020
        println("Part 1: ", last_num)
    end
end

println("Part 2: ", last_num)
