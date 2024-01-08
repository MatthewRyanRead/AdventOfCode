lines = split(read("../resources/inputs/Day18.txt", String), '\n')

function do_math!(stack::Vector{Union{Char, Int}})::Int
    operator::Union{Nothing, Char} = nothing
    operand::Union{Nothing, Int} = nothing
    while !isempty(stack)
        val = pop!(stack)
        if val in ['*', '+']
            operator = val
        elseif operand === nothing
            operand = val
        elseif operator == '*'
            operand = operand * val
        else
            operand = operand + val
        end
    end

    return operand
end

function do_addition_first!(stack::Vector{Union{Char, Int}})::Int
    addition = false
    remaining = Vector{Union{Char, Int}}()
    while !isempty(stack)
        val = pop!(stack)
        if val == '+'
            addition = true
        elseif val != '*' && addition
            push!(remaining, val + pop!(remaining))
            addition = false
        else
            push!(remaining, val)
        end
    end

    return do_math!(reverse!(remaining))
end

function parenthetical!(stack::Vector{Union{Char, Int}}, do_math_fn!::Function)::Int
    tmp = Vector{Union{Char, Int}}()
    while true
        char = pop!(stack)
        if char == '('
            break
        end
        push!(tmp, char)
    end

    return do_math_fn!(tmp)
end

total_p1 = 0
total_p2 = 0
for line in lines
    stack_p1 = Vector{Union{Char, Int}}()
    stack_p2 = Vector{Union{Char, Int}}()
    for char in line
        if char == ' '
            continue
        end

        if char == ')'
            push!(stack_p1, parenthetical!(stack_p1, do_math!))
            push!(stack_p2, parenthetical!(stack_p2, do_addition_first!))
        elseif char in ['(', '*', '+']
            push!(stack_p1, char)
            push!(stack_p2, char)
        else
            push!(stack_p1, parse(Int, char))
            push!(stack_p2, parse(Int, char))
        end
    end

    global total_p1 += do_math!(reverse!(stack_p1))
    global total_p2 += do_addition_first!(reverse!(stack_p2))
end

println("Part 1: ", total_p1)
println("Part 2: ", total_p2)
