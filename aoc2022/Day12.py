import heapq
import requests
import sys

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day12.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

height = len(input)
width = len(input[0])
distances = [[sys.maxsize] * width for y in range(height)]

class Node:
    def __init__(self, x, y, val):
        self.x = x
        self.y = y
        self.val = val
        self.neighbours = None

    def __lt__(self, other):
        global distances
        return distances[self.y][self.x] < distances[other.y][other.x]
    
    def __repr__(self):
        return '(' + str(self.x) + ', ' + str(self.y) + ') => ' + str(self.val)

nodes = [[None] * width for y in range(height)]
start = None
end = None

for y, line in enumerate(input):
    for x, char in enumerate(line):
        if char == 'S':
            start = (x, y)
            nodes[y][x] = Node(x, y, 0)
        elif char == 'E':
            end = (x, y)
            distances[y][x] = 0
            nodes[y][x] = Node(x, y, 25)
        else:
            nodes[y][x] = Node(x, y, ord(char) - 97)

for y, row in enumerate(nodes):
    for x, node in enumerate(row):
        curr_node = nodes[y][x]
        curr_node.neighbours = set()
        cutoff = curr_node.val - 1

        if x < width - 1 and nodes[y][x + 1].val >= cutoff:
            curr_node.neighbours.add(nodes[y][x + 1])
        if x > 0 and nodes[y][x - 1].val >= cutoff:
            curr_node.neighbours.add(nodes[y][x - 1])
        if y < height - 1 and nodes[y + 1][x].val >= cutoff:
            curr_node.neighbours.add(nodes[y + 1][x])
        if y > 0 and nodes[y - 1][x].val >= cutoff:
            curr_node.neighbours.add(nodes[y - 1][x])

def elfsger_djikstra(from_node, all_nodes):
    global distances

    priority_queue = sum(all_nodes, [])

    while len(priority_queue) != 0:
        heapq.heapify(priority_queue)
        min_node = priority_queue[0]
        priority_queue = priority_queue[1:]

        for neighbour in min_node.neighbours:
            curr_distance = distances[neighbour.y][neighbour.x]
            new_distance = distances[min_node.y][min_node.x] + 1

            if (new_distance < curr_distance):
                distances[neighbour.y][neighbour.x] = new_distance

elfsger_djikstra(end, nodes)
distance_from_start = distances[start[1]][start[0]]

print('Part 1:', distance_from_start)

min_distance = distance_from_start
for y, row in enumerate(nodes):
    for x, node in enumerate(row):
        if node.val != 0:
            continue

        min_distance = min(min_distance, distances[node.y][node.x])

print('Part 2:', min_distance)
