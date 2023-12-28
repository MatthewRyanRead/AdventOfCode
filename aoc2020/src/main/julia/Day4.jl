lines = split(read("../resources/inputs/Day4.txt", String), '\n')

passports = Vector{Dict{String, String}}()
curr_dict = Dict{String, String}()
for line in lines
    if line == ""
        push!(passports, curr_dict)
        global curr_dict = Dict{String, String}()
    else
        parts = split(line, ' ')
        for part in parts
            key, value = split(part, ':')
            curr_dict[key] = value
        end
    end
end
push!(passports, curr_dict)

needed_keys = Set{String}(["byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid"])
valid_eye_colours = Set{String}(["amb", "blu", "brn", "gry", "grn", "hzl", "oth"])

function p2_checks(passport::Dict{String, String})
    byr = parse(Int, passport["byr"])
    if byr < 1920 || byr > 2002
        return 0
    end

    iyr = parse(Int, passport["iyr"])
    if iyr < 2010 || iyr > 2020
        return 0
    end

    eyr = parse(Int, passport["eyr"])
    if eyr < 2020 || eyr > 2030
        return 0
    end

    hgt = passport["hgt"]
    if length(hgt) < 4
        return 0
    end

    hgt = (parse(Int, first(hgt, length(hgt)-2)), last(hgt, 2))
    if hgt[2] == "cm"
        if hgt[1] < 150 || hgt[1] > 193
            return 0
        end
    elseif hgt[2] == "in"
        if hgt[1] < 59 || hgt[1] > 76
            return 0
        end
    else
        return 0
    end

    hcl = passport["hcl"]
    if !occursin(r"^#[0-9a-f]{6}$", hcl)
        return 0
    end

    ecl = passport["ecl"]
    if ecl ∉ valid_eye_colours
        return 0
    end

    pid = passport["pid"]
    if !occursin(r"^[0-9]{9}$", pid)
        return 0
    end

    return 1
end

num_valid_p1 = 0
num_valid_p2 = 0
for passport in passports
    if length(intersect(needed_keys, keys(passport))) == length(needed_keys)
        global num_valid_p1 += 1
        global num_valid_p2 += p2_checks(passport)
    end
end

println("Part 1: ", num_valid_p1)
println("Part 2: ", num_valid_p2)
