import requests

input_uri = "https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day3.txt"
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

def get_priority(char):
    if ord(char) < 91:
        return ord(char) - 38
    return ord(char) - 96

score_part1 = 0
for line in input:
    half_len = len(line) // 2
    first = set(line[:half_len])
    second = set(line[half_len:])
    common_chars = first.intersection(second)
    score_part1 += get_priority(list(common_chars)[0])

score_part2 = 0
for i in range(0, len(input), 3):
    lines = input[i:i+3]
    common_chars = set(lines[0]).intersection(lines[1]).intersection(lines[2])
    score_part2 += get_priority(list(common_chars)[0])

print("Part 1:", score_part1)
print("Part 2:", score_part2)
