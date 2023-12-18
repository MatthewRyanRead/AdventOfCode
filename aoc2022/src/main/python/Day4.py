import requests

input_uri = "https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day4.txt"
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

score_part1 = 0
score_part2 = 0
for line in input:
    assignments = line.strip().split(',')
    ass1 = [int(str) for str in assignments[0].split('-')]
    ass2 = [int(str) for str in assignments[1].split('-')]
    if (ass1[0] <= ass2[0] and ass1[1] >= ass2[1]) or (ass2[0] <= ass1[0] and ass2[1] >= ass1[1]):
        score_part1 += 1
    if (ass1[0] <= ass2[0] and ass1[1] >= ass2[0]) or (ass1[0] <= ass2[1] and ass1[1] >= ass2[1]) or \
        (ass2[0] <= ass1[0] and ass2[1] >= ass1[0]) or (ass2[0] <= ass1[1] and ass2[1] >= ass1[1]):
        score_part2 += 1

print("Part 1:", score_part1)
print("Part 2:", score_part2)
