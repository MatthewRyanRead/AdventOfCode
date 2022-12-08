import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day8.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

west_side  = 0
east_side  = 1
north_side = 2
south_side = 3

class Tree:
    def __init__(self, x, y, h):
        self.x = x
        self.y = y
        self.height = h
        self.visible = set()
        self.neighbours = {}
        self.max_blocking_heights = { west_side: -1, east_side: -1, north_side: -1, south_side: -1 }

    def __repr__(self):
        return str(self.height)

    def __hash__(self):
        return hash(self.y) * 31 + hash(self.x)

    def __eq__(self, other):
        return self.y == other.y and self.x == other.x

height = len(input)
width = len(input[0])
trees = []

for y in range(height):
    trees.append([])

    for x in range(width):
        tree = Tree(x, y, int(input[y][x]))
        trees[y].append(tree)

        if y == 0:
            tree.visible.add(north_side)
        else:
            tree.neighbours[north_side] = trees[y-1][x]
            trees[y-1][x].neighbours[south_side] = tree

        if y == height - 1:
            tree.visible.add(south_side)

        if x == 0:
            tree.visible.add(west_side)
        else:
            tree.neighbours[west_side] = trees[y][x-1]
            trees[y][x-1].neighbours[east_side] = tree

        if x == width - 1:
            tree.visible.add(east_side)

def make_visible(curr_tree, outward_side, inward_side):
    neighbour = curr_tree.neighbours[inward_side]

    block_height = max(curr_tree.height, curr_tree.max_blocking_heights[outward_side])
    neighbour.max_blocking_heights[outward_side] = block_height

    if neighbour.height > block_height:
        neighbour.visible.add(outward_side)

for y in range(0, height):
    for x in range(0, width - 1):        
        make_visible(trees[y][x], west_side, east_side)
    for x in range(width - 1, 0, -1):        
        make_visible(trees[y][x], east_side, west_side)
for x in range(0, width):
    for y in range(0, height - 1):        
        make_visible(trees[y][x], north_side, south_side)
    for y in range(height - 1, 0, -1):        
        make_visible(trees[y][x], south_side, north_side)

score_part1 = 0
for y in range(height):
    for x in range(width):
        tree = trees[y][x]
        if len(tree.visible) > 0:
            print(str(tree.height), end='')
            score_part1 += 1
        else:
            print('.', end='')
    print()

print('\nPart 1:', score_part1)

winning_score = 0

# There's a smarter way to do this, but I refuse at this point. Lol.
# O(n^3) should be perfectly doable with n=99 and low fixed costs, so let's just brute force it.
for y in range(height):
    for x in range(width):
        south_score = 0
        north_score = 0
        east_score = 0
        west_score = 0

        tree = trees[y][x]

        for y2 in range(y + 1, height):
            south_score += 1
            if trees[y2][x].height >= tree.height:
                break

        for y2 in range(y - 1, -1, -1):
            north_score += 1
            if trees[y2][x].height >= tree.height:
                break

        for x2 in range(x + 1, width):
            east_score += 1
            if trees[y][x2].height >= tree.height:
                break

        for x2 in range(x - 1, -1, -1):
            west_score += 1
            if trees[y][x2].height >= tree.height:
                break
        
        total_score = south_score * north_score * east_score * west_score
        if total_score > winning_score:
            winning_score = total_score

print('Part 2:', winning_score)
