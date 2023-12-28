using Multibreak

nums = Set{Int}()
for str in split(read("../resources/inputs/Day1.txt", String), '\n')
    push!(nums, parse(Int, str))
end

for num in nums
    complement = 2020 - num
    if complement in nums
        println("Part 1: ", num * complement)
        break
    end
end

@multibreak begin
    for num1 in nums
        for num2 in nums
            complement = 2020 - num1 - num2
            if complement in nums
                println("Part 2: ", num1 * num2 * complement)
                break; break
            end
        end
    end
end
