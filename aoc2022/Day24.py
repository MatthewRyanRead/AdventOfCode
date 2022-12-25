import collections
import enum
import math
import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day24.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

class Thing(enum.IntEnum):
    EMPTY = 0
    WALL = 1
    BLIZZ_LEFT = 2
    BLIZZ_RIGHT = 4
    BLIZZ_UP = 8
    BLIZZ_DOWN = 16
    ELVES = 32
    GOAL = 64

c_by_v = { v:c for c, v in zip(('.', '#', '<', '>', '^', 'v', 'E', 'G'), [e.value for e in Thing]) }
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_UP.value | Thing.BLIZZ_DOWN.value] = 4
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_UP.value]   = 3
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_DOWN.value] = 3
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_UP.value    | Thing.BLIZZ_DOWN.value] = 4
c_by_v[Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_UP.value    | Thing.BLIZZ_DOWN.value] = 3
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_RIGHT.value] = 2
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_UP.value]    = 2
c_by_v[Thing.BLIZZ_LEFT.value  | Thing.BLIZZ_DOWN.value]  = 2
c_by_v[Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_UP.value]    = 2
c_by_v[Thing.BLIZZ_RIGHT.value | Thing.BLIZZ_DOWN.value]  = 2
c_by_v[Thing.BLIZZ_UP.value    | Thing.BLIZZ_DOWN.value]  = 2
v_by_c = { c:v for v, c in c_by_v.items() }

# left, right, up, down, none
TRANSFORMS = ((-1, 0), (1, 0), (0, -1), (0, 1), (0, 0))

b_by_t = { t:b for t, b in zip(TRANSFORMS[:-1], (Thing.BLIZZ_LEFT.value, Thing.BLIZZ_RIGHT.value, Thing.BLIZZ_UP.value, Thing.BLIZZ_DOWN.value)) }

class Node:
    def __init__(self, x, y, val):
        self.x = x
        self.y = y
        self.val = val

    def __str__(self):
        return c_by_v[self.val]

    def __repr__(self):
        return str(self)

input = input[1:-1]
grid = [[] for _ in input]
for y, line in enumerate(input):
    for x, c in enumerate(line[1:-1]):
        grid[y] += [Node(x, y, v_by_c[c])]

height = len(grid)
width = len(grid[0])
last_y_idx = height - 1
last_x_idx = width - 1
lcm = math.lcm(width, height)

def value_at_time(x, y, curr_grid_num):
    val = 0
    for transform in TRANSFORMS[:-1]:
        n_x = (x + curr_grid_num * transform[0]) % width
        n_y = (y + curr_grid_num * transform[1]) % height

        blizz = b_by_t[(-transform[0], -transform[1])]
        val |= blizz & grid[n_y][n_x].val

    return val

def print_grid(curr_pos, time = 0):
    print('#G' + '#' * width)

    for y, row in enumerate(grid):
        print('#', end='')

        for x, val in enumerate(row):
            val = value_at_time(x, y, time)
            if val == 0 and (x, y) == curr_pos:
                print('E', end='')
            else:
                print(c_by_v[val], end='')

        print('#')
    
    print('#' * width + 'G#' + '\n')

states = collections.deque()
start_pos = (0, -1)
end_pos = (last_x_idx, last_y_idx + 1)
states.append((start_pos, 1))
states_seen_set = set()
states_seen_set.add((start_pos, 1))

trip = 1
while len(states) > 0:
    curr_pos, num_turns = states.popleft()
    grid_num = num_turns % lcm

    for transform in TRANSFORMS:
        n_x, n_y = curr_pos[0] + transform[0], curr_pos[1] + transform[1]
        new_pos = (n_x, n_y)

        if new_pos != start_pos and new_pos != end_pos and (n_x not in range(width) or n_y not in range(height)):
            continue

        state_key = (new_pos, num_turns + 1)

        if new_pos == start_pos or value_at_time(n_x, n_y, grid_num) == Thing.EMPTY.value:
            if not state_key in states_seen_set:
                states.append(state_key)
                states_seen_set.add(state_key)
        elif new_pos == end_pos:
            print('After trip', str(trip) + ':', num_turns)
            if trip == 3:
                exit()

            trip += 1
            temp = start_pos
            start_pos = end_pos
            end_pos = temp

            states.clear()
            states_seen_set.clear()
            states.append(state_key)
            states_seen_set.add(state_key)
