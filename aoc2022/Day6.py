import requests

input_uri = "https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day6.txt"
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()[0]

found_part1 = False

for i in range(len(input) - 14):
    if not found_part1 and len(set(input[i:i+4])) == 4:
        found_part1 = True
        print("Part 1:", i+4)
    if len(set(input[i:i+14])) == 14:
        print("Part 2:", i+14)
        break
