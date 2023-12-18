import z3

def coalesce_value(reg_or_val):
    match reg_or_val:
        case 'w':
            return registers[reg_or_val]
        case 'x':
            return registers[reg_or_val]
        case 'y':
            return registers[reg_or_val]
        case 'z':
            return registers[reg_or_val]

    return int(reg_or_val)

registers = { 'w': z3.BitVecVal(0, 64), 'x': z3.BitVecVal(0, 64), 'y': z3.BitVecVal(0, 64), 'z': z3.BitVecVal(0, 64)}
monad = []
optimizer = z3.Optimize()

file = open('../resources/inputs/day24.txt')
for line in file:
    monad.append(line.split())
file.close()

number = []
for i in range(14):
    number.append(z3.BitVec('digit_' + str(i), 64))
    optimizer.add(number[-1] >= 1)
    optimizer.add(number[-1] <= 9)

digit_idx = 0

for i in range(len(monad)):
    instruction, first_register, *second_reg_or_val = monad[i]
    first_value = registers[first_register]
    val2 = coalesce_value(second_reg_or_val[0]) if len(second_reg_or_val) > 0 else 0
    constrained_result = z3.BitVec('result_' + str(i), 64)

    match instruction:
        case 'inp':
            registers[first_register] = number[digit_idx]
            digit_idx += 1
            continue
        case 'add':
            optimizer.add(constrained_result == first_value + val2)
        case 'mul':
            optimizer.add(constrained_result == first_value * val2)
        case 'mod':
            optimizer.add(first_value >= 0)
            optimizer.add(val2 > 0)
            optimizer.add(constrained_result == first_value % val2)
        case 'div':
            optimizer.add(val2 != 0)
            optimizer.add(constrained_result == first_value / val2)
        case 'eql':
            optimizer.add(constrained_result == z3.If(first_value == val2, z3.BitVecVal(1, 64), z3.BitVecVal(0, 64)))

    registers[first_register] = constrained_result

optimizer.add(registers['z'] == 0)

final_num = number[0]
for i in range(1, 14):
    final_num = final_num * 10 + number[i]

optimizer.push()
optimizer.maximize(final_num)
optimizer.check()
result = optimizer.model()

max_accepted_num = 0
for i in range(14):
    max_accepted_num = 10 * max_accepted_num + int(str(result[number[i]]))

print('Part 1: ' + str(max_accepted_num))

optimizer.pop()
optimizer.minimize(final_num)
optimizer.check()
result = optimizer.model()

min_accepted_num = 0
for i in range(14):
    min_accepted_num = 10 * min_accepted_num + int(str(result[number[i]]))

print('Part 2: ' + str(min_accepted_num))