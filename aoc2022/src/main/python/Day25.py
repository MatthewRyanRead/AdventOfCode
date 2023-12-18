import math

import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day25.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

total = 0
for line in input:
    add_str = ''.join(c != '-' and c != '=' and c or '0' for c in line)
    sub_str = ''.join(c == '-' and '1' or c == '=' and '2' or '0' for c in line)
    total += int(add_str, base=5) - int(sub_str, base=5)

result = ''
for pow in range(math.ceil(math.log(total, 5)), -1, -1):
    factor, best_without = 5**pow, sum(2*(5**p) for p in range(pow))

    if total > best_without + factor:
        total -= 2*factor
        result += '2'
    elif total > best_without:
        total -= factor
        result += '1'
    elif -total > best_without + factor:
        total += 2*factor
        result += '='
    elif -total > best_without:
        total += factor
        result += '-'
    else:
        result += '0'

print(result.lstrip('0'))
