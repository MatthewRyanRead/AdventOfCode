import math
import sys

import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day9.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

up = 0
right = 1
down = 2
left = 3
translations = [[0, -1], [1, 0], [0, 1], [-1, 0]]
command_map = { 'U': up, 'R': right, 'D': down, 'L': left }

def translate(old_pos, translation):
    return [old_pos[0] + translation[0], old_pos[1] + translation[1]]
def adjacent(pos1, pos2):
    return (abs(pos1[0] - pos2[0]) < 2) and (abs(pos1[1] - pos2[1]) < 2)

commands = [[] for line in input]
maximums = [0, 0, 0, 0]

for i in range(len(input)):
    line = input[i]
    direction = command_map[line[0]]
    distance = int(line[2:])
    commands[i].append(direction)
    commands[i].append(distance)
    maximums[direction] += distance

# make the visual grid the largest we could possibly need, we'll trim it later
grid = [['.' for i in range(maximums[left] + maximums[right] + 1)] for i in range (maximums[up] + maximums[down] + 1)]

start_pos = [maximums[left], maximums[up]]
grid[start_pos[1]][start_pos[0]] = 's'
snake = [start_pos.copy() for i in range(10)]

tail_visited = set()
tail_visited.add(tuple(start_pos))
second_visited = set()
second_visited.add(tuple(start_pos))

directions = ['up', 'right', 'down', 'left']

def snake_it_up(snake, leader_index):
    follower_index = leader_index + 1
    leader_pos = snake[leader_index]
    follower_pos = snake[follower_index]

    # leader was covering follower or has moved to a diagonal
    if adjacent(leader_pos, follower_pos):
        return False

    # apply the 1-2 translations that move the follower closer to the leader
    for i in range(len(translations)):
        translation = translations[i]
        potential_new_pos = translate(follower_pos, translation)

        if math.dist(leader_pos, potential_new_pos) < math.dist(leader_pos, follower_pos):
            follower_pos = potential_new_pos

    snake[follower_index] = follower_pos

    return True

for command in commands:
    for i in range(command[1]):
        direction = command[0]

        snake[0] = translate(snake[0], translations[direction])
        head_val = grid[snake[0][1]][snake[0][0]]

        for i in range(len(snake) - 1):
            moved = snake_it_up(snake, i)
            if not moved:
                break

        second_visited.add(tuple(snake[1]))
        tail_pos = snake[len(snake) - 1]
        tail_visited.add(tuple(tail_pos))

        if grid[tail_pos[1]][tail_pos[0]] != 's':
            grid[tail_pos[1]][tail_pos[0]] = '#'

min_x = sys.maxsize
min_y = sys.maxsize
max_x = 0
max_y = 0
for pos in tail_visited:
    min_x = min(min_x, pos[0])
    min_y = min(min_y, pos[1])
    max_x = max(max_x, pos[0] + 1)
    max_y = max(max_y, pos[1] + 1)

# trim the grid
grid = [row[min_x : max_x] for row in grid[min_y : max_y]]

#for row in grid:
#    for value in row:
#        print(value, end='')
#    print()

print('\nPart 1:', len(second_visited))
print('Part 2:', len(tail_visited))
