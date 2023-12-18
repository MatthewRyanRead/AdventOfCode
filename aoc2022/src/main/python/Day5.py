import requests

input_uri = "https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day5.txt"
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

stack_lines = []
moves = []
for i in range(len(input)):
    if input[i] == '':
        stack_lines = input[i-2::-1]
        moves = input[i+1:]
        break

num_stacks = len(stack_lines[0])//4 + 1
stacks_part1 = [[] for i in range(num_stacks)]
stacks_part2 = [[] for i in range(num_stacks)]

for line in stack_lines:
    for i in range(num_stacks):
        box = line[i*4:i*4+4].strip()
        if box == '':
            continue

        stacks_part1[i].append(box[1:-1])
        stacks_part2[i].append(box[1:-1])

for move in moves:
    count = int(move.split('move ')[1].split(' ')[0])
    col_from = int(move.split(' from ')[1].split(' ')[0]) - 1
    col_to = int(move.split(' to ')[1]) - 1

    for i in range(count):
        stacks_part1[col_to].append(stacks_part1[col_from].pop())

    stacks_part2[col_to].extend(stacks_part2[col_from][-count:])
    stacks_part2[col_from] = stacks_part2[col_from][:-count]

print("Part 1:", ''.join(stacks_part1[i][-1] for i in range(num_stacks)))
print("Part 2:", ''.join(stacks_part2[i][-1] for i in range(num_stacks)))
