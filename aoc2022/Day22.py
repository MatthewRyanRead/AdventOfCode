import math
import re
import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day22.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

r, d, l, u = 0, 1, 2, 3
rc, dc, lc, uc = '>', 'v', '<', '^'
c_by_dir = { dir:c for dir, c in zip((r, d, l, u), (rc, dc, lc, uc)) }
translations_by_dir = { r:(1, 0), d:(0, 1), l:(-1, 0), u:(0, -1) }
wc, oc, bc = '#', '.', ' '

instructions = input[-1]
input = input[:-2]
height = len(input)
width = max(len(line) for line in input)
grid = [[bc for i in range(width + 2)] for i in range(height + 2)]

for y, line in enumerate(input):
    for x, c in enumerate(line):
        if c == bc:
            continue
        grid[y+1][x+1] = c

def print_grid():
    for row in grid:
        print(''.join(row))

pattern = re.compile(r'^(\d+[LR]?)')
curr_pos = (min([i for i, c in enumerate(grid[1]) if c != bc]), 1, r)

instructions2 = instructions[:]

while instructions:
    instr = pattern.match(instructions)[1]
    instructions = instructions[len(instr):]

    turn = None
    if instr[-1] in ['L', 'R']:
        turn = instr[-1]
        move = int(instr[:-1])
    else:
        move = int(instr)

    trans = translations_by_dir[curr_pos[2]]
    for i in range(move):
        match grid[curr_pos[1] + trans[1]][curr_pos[0] + trans[0]]:
            case '#':
                break
            case ' ':
                opposite_trans = (-trans[0], -trans[1])
                while grid[curr_pos[1] + opposite_trans[1]][curr_pos[0] + opposite_trans[0]] != bc:
                    opposite_trans = (opposite_trans[0] - trans[0], opposite_trans[1] - trans[1])

                if grid[curr_pos[1] + opposite_trans[1] + trans[1]][curr_pos[0] + opposite_trans[0] + trans[0]] == wc:
                    break
                else:
                    curr_pos = (curr_pos[0] + opposite_trans[0], curr_pos[1] + opposite_trans[1], curr_pos[2])

        curr_pos = (curr_pos[0] + trans[0], curr_pos[1] + trans[1], curr_pos[2])
        grid[curr_pos[1]][curr_pos[0]] = c_by_dir.get(curr_pos[2])

    if turn == 'L':
        curr_pos = (curr_pos[0], curr_pos[1], (curr_pos[2] - 1) % 4)
    elif turn == 'R':
        curr_pos = (curr_pos[0], curr_pos[1], (curr_pos[2] + 1) % 4)
    grid[curr_pos[1]][curr_pos[0]] = c_by_dir.get(curr_pos[2])

print('Part 1:', (curr_pos[0]) * 4 + (curr_pos[1]) * 1000 + curr_pos[2])

for y, row in enumerate(grid):
    for x, val in enumerate(row):
        if val in c_by_dir.values():
            grid[y][x] = oc

class Node:
    def __init__(self, x, y, val, neighbours = None):
        self.x = x
        self.y = y
        self.val = val
        self.neighbours = neighbours or {}
        self.refacings = {}

    def __str__(self):
        return '(' + str(self.x) + ', ' + str(self.y) + ')'

    def __repr__(self):
        return str(self)

    def set_refacing(self, facing, refacing):
        self.refacings[facing] = refacing

nodes_by_coords = {}
for y, row in enumerate(grid):
    for x, val in enumerate(row):
        if val in c_by_dir.values():
            grid[y][x] = oc
        if val != bc:
            nodes_by_coords[(x, y)] = Node(x, y, grid[y][x])

def print_nodes():
    for y in range(0, height + 2):
        for x in range(0, width + 2):
            if (x, y) in nodes_by_coords:
                print(nodes_by_coords[(x, y)].val, end='')
            else:
                print(bc, end='')
        print()

face_size = min(width, height) // 3
# for the sample input -- not going to bother adding detection code to support all input orientations
#face_num_by_scale_coords = { (3, 1): 1, (1, 2): 2, (2, 2): 3, (3, 2): 4, (3, 3): 5, (4, 3): 6 }
face_num_by_scale_coords = { (2, 1): 1, (1, 3): 2, (1, 4): 3, (3, 1): 4, (2, 3): 5, (2, 2): 6 }

for coords, node in nodes_by_coords.items():
    x = coords[0]
    y = coords[1]

    for dir, trans in translations_by_dir.items():
        pos = (x + trans[0], y + trans[1])
        if pos in nodes_by_coords:
            node.neighbours[dir] = nodes_by_coords[pos]
        else:
            x_scale = math.ceil(x / face_size)
            y_scale = math.ceil(y / face_size)
            face_num = face_num_by_scale_coords[(x_scale, y_scale)]

            match (face_num, dir):
                # for the sample input -- again, not going to bother generalizing
                #case (1, 0): # 1r -> 6r
                #    n_x = x + face_size
                #    n_y = 3*face_size - y + 1
                #    outgoing = (dir, r)
                #    incoming = (l, l)
                #case (1, 2): # 1l -> 3u
                #    n_x = y + face_size
                #    n_y = face_size + 1
                #    outgoing = (dir, u)
                #    incoming = (r, d)
                #case (1, 3): # 1u -> 2u
                #    n_x = 3*face_size - x + 1
                #    n_y = y + face_size
                #    outgoing = (dir, u)
                #    incoming = (d, d)
                #case (2, 1): #  2d -> 5d
                #    n_x = 3*face_size - x + 1
                #    n_y = y + face_size
                #    outgoing = (dir, d)
                #    incoming = (u, u)
                #case (2, 2): # 2l -> 6d
                #    n_x = 5*face_size - y + 1
                #    n_y = 3*face_size
                #    outgoing = (dir, d)
                #    incoming = (r, u)
                #case (3, 1): # 3d -> 5l
                #    n_x = y + 1
                #    n_y = 4*face_size - x + 1
                #    outgoing = (dir, l)
                #    incoming = (u, r)
                #case (4, 0): # 4r -> 6u
                #    n_x = 5*face_size - y + 1
                #    n_y = 2*face_size + 1
                #    outgoing = (dir, u)
                #    incoming = (l, d)
                case (1, 2): # 1l -> 2l
                    n_x = 1
                    n_y = 151 - y
                    outgoing = (dir, l)
                    incoming = (r, r)
                case (1, 3): # 1u -> 3l
                    n_x = 1
                    n_y = x + 100
                    outgoing = (dir, l)
                    incoming = (d, r)
                case (2, 3): # 2u -> 6l
                    n_x = 51
                    n_y = x + 50
                    outgoing = (dir, l)
                    incoming = (d, r)
                case (3, 0): # 3r -> 5d
                    n_x = y - 100
                    n_y = 150
                    outgoing = (dir, d)
                    incoming = (l, u)
                case (3, 1): # 3d -> 4u
                    n_x = x + 100
                    n_y = 1
                    outgoing = (dir, u)
                    incoming = (u, d)
                case (4, 0): # 4r -> 5r
                    n_x = 100
                    n_y = 151 - y
                    outgoing = (dir, r)
                    incoming = (l, l)
                case (4, 1): # 4d -> 6r
                    n_x = 100
                    n_y = x - 50
                    outgoing = (dir, r)
                    incoming = (u, l)
                case _:
                    continue

            neighbour = nodes_by_coords[(n_x, n_y)]

            if outgoing:
                node.set_refacing(outgoing[1], incoming[0])
                neighbour.set_refacing(outgoing[0], incoming[1])

            node.neighbours[outgoing[0]] = neighbour
            neighbour.neighbours[outgoing[1]] = node

curr_node = nodes_by_coords[(face_size + 1, 1)]
curr_facing = r
curr_node.val = c_by_dir.get(curr_facing)

while instructions2:
    instr = pattern.match(instructions2)[1]
    instructions2 = instructions2[len(instr):]

    turn = None
    if instr[-1] in ['L', 'R']:
        turn = instr[-1]
        move = int(instr[:-1])
    else:
        move = int(instr)

    for i in range(move):
        neighbour = curr_node.neighbours[curr_facing]

        match neighbour.val:
            case '#':
                break

        trans = translations_by_dir[curr_facing]
        if grid[curr_node.y + trans[1]][curr_node.x + trans[0]] == bc:
            curr_facing = neighbour.refacings[curr_facing]
        curr_node = neighbour
        curr_node.val = c_by_dir[curr_facing]

    if turn == 'L':
        curr_facing = (curr_facing - 1) % 4
    elif turn == 'R':
        curr_facing = (curr_facing + 1) % 4
    curr_node.val = c_by_dir.get(curr_facing)

print('Part 2:', curr_node.x * 4 + curr_node.y * 1000 + curr_facing)
