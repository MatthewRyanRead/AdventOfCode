using Multibreak

lines = split(read("../resources/inputs/Day9.txt", String), '\n')

nums = parse.(Int, lines)

function part1(lookback::Int)::Int
    @multibreak begin
        for i in (lookback + 1):length(nums)
            num = nums[i]
            for j in (i - lookback):(i - 2)
                for k in (j + 1):(i - 1)
                    if j == k
                        continue
                    end

                    if nums[j] + nums[k] == num
                        break; break; continue
                    end
                end
            end

            return i
        end
    end

    error("No answer found")
end

invalid_num_idx = part1(25)
invalid_num = nums[invalid_num_idx]
println("Part 1: ", invalid_num)

function part2()::Int
    sum = 0
    for i in (invalid_num_idx - 1):-1:1
        sum = 0
        for j in i:-1:1
            sum += nums[j]
            if sum == invalid_num
                return minimum(nums[j:i]) + maximum(nums[j:i])
            elseif sum > invalid_num
                break
            end
        end
    end

    error("No answer found")
end

println("Part 2: ", part2())
