import requests
import sys

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day14.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

if len(sys.argv) == 1:
    raise Exception('version argument required -- `python3 Day14.py 1` or `python3 Day14.py 2`; ' + \
            'optionally provide `-P` before the version to print the grid')
version = int(sys.argv[len(sys.argv) - 1])

# [-x, x, -y, y]
grid_range = [sys.maxsize, -(sys.maxsize - 1), sys.maxsize, -(sys.maxsize - 1)]
lines = []
for line in input:
    points = line.split(' -> ')
    points = [point.split(',') for point in points]
    points = [(int(point[0]), int(point[1])) for point in points]
    lines.append(points)

    for point in points:
        grid_range[0] = min(grid_range[0], point[0], 500)
        grid_range[1] = max(grid_range[1], point[0], 500)
        grid_range[2] = min(grid_range[2], point[1], 0)
        grid_range[3] = max(grid_range[3], point[1], 3)

x_offset = -grid_range[0]
y_offset = -grid_range[2]

width = x_offset + grid_range[1] + 1
height = y_offset + grid_range[3] + (version == 2 and 3 or 1)

if version == 2:
    width *= 5
    # recenter after widening
    x_offset += int((x_offset + grid_range[1] + 1) * 1.5)

grid = [['.' for x in range(width)] for y in range(height - 1)]
grid.append([(version == 2 and '#' or '.') for x in range(width)])
grid[y_offset + 0][x_offset + 500] = '+'

for points in lines:
    for i in range(len(points) - 1):
        point_one = min(points[i], points[i + 1])
        point_two = max(points[i], points[i + 1])

        for x in range(point_one[0], point_two[0] + 1):
            for y in range(point_one[1], point_two[1] + 1):
                grid[y + y_offset][x + x_offset] = '#'

num_grains = 0

while True:
    curr_pos = (x_offset + 500, y_offset)
    if grid[curr_pos[1]][curr_pos[0]] == 'o':
        break

    grid[curr_pos[1]][curr_pos[0]] = 'o'
    fell_off = False

    while True:
        if curr_pos[1] == height - 1:
            # bottom reached -- only in v1
            fell_off = True
            break
        elif grid[curr_pos[1] + 1][curr_pos[0]] == '.':
            # fall straight down
            grid[curr_pos[1]][curr_pos[0]] = '.'
            grid[curr_pos[1] + 1][curr_pos[0]] = 'o'
            curr_pos = (curr_pos[0], curr_pos[1] + 1)
        elif curr_pos[0] == 0:
            # left reached -- only in v1
            fell_off = True
            break
        elif grid[curr_pos[1] + 1][curr_pos[0] - 1] == '.':
            # fall down and left
            grid[curr_pos[1]][curr_pos[0]] = '.'
            grid[curr_pos[1] + 1][curr_pos[0] - 1] = 'o'
            curr_pos = (curr_pos[0] - 1, curr_pos[1] + 1)
        elif curr_pos[0] == width - 1:
            # right reached -- only in v1
            fell_off = True
            break
        elif grid[curr_pos[1] + 1][curr_pos[0] + 1] == '.':
            # fall down and right
            grid[curr_pos[1]][curr_pos[0]] = '.'
            grid[curr_pos[1] + 1][curr_pos[0] + 1] = 'o'
            curr_pos = (curr_pos[0] + 1, curr_pos[1] + 1)
        else:
            # stop
            break
    
    if fell_off:
        grid[curr_pos[1]][curr_pos[0]] = 'x'
        break

    num_grains += 1

if sys.argv[1] == '--print' or sys.argv[1] == '-P':
    for row in grid:
        print(''.join(row))
    print()

print('Part ' + str(version) + ':', num_grains)
