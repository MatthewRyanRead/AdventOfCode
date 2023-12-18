import math

import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day17.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8')

LEFT, RIGHT = -1, 1
directions = [RIGHT if char == '>' else LEFT for char in input]
num_dirs = len(directions)

# flipped vertically because I am building the grid down
rocks = [
    ['####'],
    ['.#.',
     '###',
     '.#.'],
    ['###',
     '..#',
     '..#'],
    ['#',
     '#',
     '#',
     '#'],
    ['##',
     '##']
]
num_rocks = len(rocks)

left_of_rock = [[(0, 0)],
                [(1, 0), (0, 1), (1, 2)],
                [(2, 2), (2, 1), (0, 0)],
                [(0, 0), (0, 1), (0, 2), (0, 3)],
                [(0, 0), (0, 1)]]
right_of_rock = [[(3, 0)],
                 [(1, 0), (2, 1), (1, 2)],
                 [(2, 2), (2, 1), (2, 0)],
                 [(0, 0), (0, 1), (0, 2), (0, 3)],
                 [(1, 0), (1, 1)]]
bottom_of_rock = [[(0, 0), (1, 0), (2, 0), (3, 0)],
                  [(0, 1), (1, 2), (2, 1)],
                  [(0, 2), (1, 2), (2, 2)],
                  [(0, 3)],
                  [(0, 1), (1, 1)]]

grid = [['#', '#', '#', '#', '#', '#', '#']]

def print_grid():
    global grid

    for row in reversed(grid):
        print(''.join(row))
    print()

def move(rock, rockIdx, dir, curr_pos):
    global grid, left_of_rock, right_of_rock, LEFT, RIGHT

    if dir == LEFT:
        coords = left_of_rock
        if curr_pos[0] == 0:
            return False
    else:
        coords = right_of_rock
        if curr_pos[0] == 7 - len(rock[0]):
            return False

    for coord in coords[rockIdx]:
        if grid[curr_pos[1] + coord[1]][curr_pos[0] + coord[0] + dir] == '#':
            return False

    for rowIdx, row in enumerate(rock):
        for colIdx, val in enumerate(row):
            if val == '#':
                grid[curr_pos[1] + rowIdx][curr_pos[0] + colIdx] = '.'

    for colIdx in range(len(rock[0])):
        for rowIdx, row in enumerate(rock):
            existing = grid[curr_pos[1] + rowIdx][curr_pos[0] + colIdx + dir]
            grid[curr_pos[1] + rowIdx][curr_pos[0] + colIdx + dir] = row[colIdx] == '#' and '#' or existing

    return True

# this could be replaced with a further-genericized move(), but not worth it at this point
def drop(rock, rockIdx, curr_pos):
    global grid, bottom_of_rock

    for coord in bottom_of_rock[rockIdx]:
        if grid[curr_pos[1] - 2 + len(rock) - coord[1]][curr_pos[0] + coord[0]] == '#':
            return False

    for rowIdx, row in enumerate(rock):
        for colIdx, val in enumerate(row):
            if val == '#':
                grid[curr_pos[1] + rowIdx][curr_pos[0] + colIdx] = '.'

    for rowIdx, row in enumerate(rock):
        for colIdx, val in enumerate(row):
            if val == '#':
                grid[curr_pos[1] + rowIdx - 1][curr_pos[0] + colIdx] = val

    return True

def trim_grid():
    global grid

    while grid[len(grid) - 1] == ['.', '.', '.', '.', '.', '.', '.']:
        grid = grid[:-1]

dirIdx = 0
height_addend = 0
max_rocks = 1_000_000_000_000
cache = {}
# actual number of rows before repeat is much less, but this is the theoretical max
cache_row_count = math.lcm(num_rocks, num_dirs)

i = -1
while i+1 < max_rocks:
    i += 1
    rockIdx = i % num_rocks
    rock = rocks[rockIdx]

    trim_grid()
    if i == 2022:
        print('Part 1:', len(grid) - 1)

    if len(grid) > cache_row_count and height_addend == 0:
        cache_key = (rockIdx, dirIdx % num_dirs, tuple([tuple(row) for row in grid[-cache_row_count:]]))

        if cache_key in cache:
            result = cache[cache_key]
            num_repeat_moves = i - result[0]
            num_repeat_lines = len(grid) - result[1]
            num_skips = ((max_rocks - i) // num_repeat_moves)

            height_addend = num_skips * num_repeat_lines
            i += num_skips * num_repeat_moves
        else:
            cache[cache_key] = (i, len(grid))

    # add space for rock and drop it in
    grid += [['.', '.', '.', '.', '.', '.', '.'] for j in range(3 + len(rock))]
    for rowIdx, cols in enumerate(rock):
        for colIdx, val in enumerate(cols):
            grid[len(grid) - len(rock) + rowIdx][2 + colIdx] = val

    curr_pos = (2, len(grid) - len(rock))
    while True:
        dir = directions[dirIdx % num_dirs]
        curr_pos = (curr_pos[0] + (dir if move(rock, rockIdx, dir, curr_pos) else 0), curr_pos[1])
        dirIdx += 1

        if not drop(rock, rockIdx, curr_pos):
            break
        curr_pos = (curr_pos[0], curr_pos[1] - 1)

trim_grid()
print('Part 2:', len(grid) - 1 + height_addend)
