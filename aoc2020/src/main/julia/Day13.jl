using GeneralizedCRT

lines = split(read("../resources/inputs/Day13.txt", String), '\n')

start_time = parse(Int, lines[1])
busses = map(b -> if b != "x" parse(Int, b) else nothing end, split(lines[2], ','))

arrivals = map(b -> if b === nothing b else b - (start_time % b) end, busses)
first_arrival = findmin(filter(p -> p.second !== nothing, pairs(arrivals)))
first_bus = busses[first_arrival[2]]

println("Part 1: ", first_bus * first_arrival[1])

n = filter(b -> b !== nothing, busses)
a = map(p -> mod(-p.first + 1, p.second), sort(collect(filter(p -> p.second !== nothing, pairs(busses)))))

println("Part 2: ", crt(a, n)[1])
