import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day10.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

addends_by_step = {}
x = 1
score = 0
message = ''
idx = 0
step = 0
delay = False

while idx < len(input):
    step += 1
    x += addends_by_step.pop(step, 0)

    if (step-20)%40 == 0:
        score += x * step
    
    screen_pos = (step - 1) % 40
    if screen_pos == 0:
        message += '\n'
    if screen_pos in [x - 1, x, x + 1]:
        message += '#'
    else:
        message += '.'

    if delay:
        delay = False
        continue

    line = input[idx]
    idx += 1
    instruction = line[:4]

    if instruction == 'addx':
        delay = True
        addends_by_step[step + 2] = int(line[5:])

print('Part 1:', score)
print('Part 2:', message)
