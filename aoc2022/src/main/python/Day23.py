import sys

import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day23.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

elf_coords = set()
for y, line in enumerate(input):
    for x, c in enumerate(line):
        if c == '#':
            elf_coords.add((x, y))

bounds = []
def calc_bounds():
    global bounds

    bounds = [sys.maxsize, -(sys.maxsize - 1), sys.maxsize, -(sys.maxsize - 1)]
    for elf_coord in elf_coords:
        bounds[0] = min(bounds[0], elf_coord[0])
        bounds[1] = max(bounds[1], elf_coord[0])
        bounds[2] = min(bounds[2], elf_coord[1])
        bounds[3] = max(bounds[3], elf_coord[1])

def print_coords():
    calc_bounds()

    for y in range(bounds[2], bounds[3] + 1):
        for x in range(bounds[0], bounds[1] + 1):
            if (x, y) in elf_coords:
                print('#', end='')
            else:
                print('.', end='')
        print()
    print()

translations = [((1, -1), (1, 0), (1, 1)), ((-1, -1), (0, -1), (1, -1)), ((-1, 1), (0, 1), (1, 1)), ((-1, -1), (-1, 0), (-1, 1))]
dir_by_trans = { t:dir for dir, t in zip(('E', 'N', 'S', 'W'), tuple(translations)) }

for i in range(sys.maxsize):
    translations = translations[1:] + translations[:1]
    orig_by_proposed = {}
    canceled = set()

    for coords in elf_coords:
        needs_move = False
        for y in [-1, 0, 1]:
            for x in [-1, 0, 1]:
                if x == y == 0:
                    continue
                if (coords[0] + x, coords[1] + y) in elf_coords:
                    needs_move = True
                    break

        if not needs_move:
            continue

        for options in translations:
            proposed_move = (coords[0] + options[1][0], coords[1] + options[1][1])
            for option in options:
                if (coords[0] + option[0], coords[1] + option[1]) in elf_coords:
                    proposed_move = None
                    break

            if not proposed_move:
                continue
            
            if proposed_move in orig_by_proposed:
                canceled.add(proposed_move)
            else:
                orig_by_proposed[proposed_move] = coords
            break

    if orig_by_proposed == {}:
        print('Part 2:', i + 1)
        break

    for proposed in canceled:
        del orig_by_proposed[proposed]
    for proposed, orig in orig_by_proposed.items():
        elf_coords.remove(orig)
        elf_coords.add(proposed)

    if i == 9:
        calc_bounds()
        width = bounds[1] - bounds[0] + 1
        height = bounds[3] - bounds[2] + 1
        num_blanks = width*height - len(elf_coords)
        print('Part 1:', num_blanks)
