lines = split(read("../resources/inputs/Day5.txt", String), '\n')

ids = Vector{Int}()

for line in lines
    minRow = 0
    maxRow = 127
    minCol = 0
    maxCol = 7
    #println(minRow, ' ', maxRow, ' ', minCol, ' ', maxCol)

    for char in line
        if char == 'F'
            maxRow = maxRow - ((maxRow - minRow + 1) ÷ 2)
        elseif char == 'B'
            minRow = minRow + ((maxRow - minRow + 1) ÷ 2)
        elseif char == 'L'
            maxCol = maxCol - ((maxCol - minCol + 1) ÷ 2)
        elseif char == 'R'
            minCol = minCol + ((maxCol - minCol + 1) ÷ 2)
        end

        #println(minRow, ' ', maxRow, ' ', minCol, ' ', maxCol)
    end

    #println(minRow == maxRow, ' ', minCol == maxCol, ' ', minRow, ' ', minCol, ' ', maxRow * 8 + maxCol)
    push!(ids, maxRow * 8 + maxCol)
end

ids = sort(ids)

println("Part 1: ", ids[end])

for (i, id) in pairs(ids)
    if i == length(ids)
        error("Seat not found")
    end

    if id + 2 == ids[i + 1]
        println("Part 2: ", id + 1)
        break
    end
end
