import requests
import sys

class Node:
    def __init__(self, name, parent = None, size = 0):
        self.name = name
        self.parent = parent
        self.size = size
        self.children = {}

        if parent != None:
            parent.children[name] = self

    def to_string(self, indent_level = 0):
        my_str = ('  '*indent_level) + '- ' + self.name
        if self.children == {}:
            my_str += ' (dir'
            if size != 0:
                my_str += ', size=' + str(self.size)
            my_str += ')'
        else:
            my_str += ' (file, size=' + str(self.size) + ')'

        for _, child in self.children.items():
            my_str += '\n' + child.to_string(indent_level + 1)
        return my_str
    
    def __str__(self):
        return self.to_string()

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day7.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

root = None
curr_node = None

for line in input:
    if line[:4] == '$ cd':
        if line[5:] == '..':
            curr_node = curr_node.parent
        elif line[5:] == '/':
            if root == None:
                root = Node('/')
            curr_node = root
        else:
            name = line[5:]
            curr_node = Node(name, curr_node)
    elif line[:3] == 'dir':
        name = line[4:]
        Node(name, curr_node)
    elif line[:4] != '$ ls':
        parts = line.split(' ')
        name = parts[1]
        size = int(parts[0])
        Node(name, curr_node, size)

total_under_100k = 0

def compute_size(node):
    if node.children == {}:
        return node.size

    for _, child in node.children.items():
        node.size += compute_size(child)

    global total_under_100k
    if node.size < 100000:
        total_under_100k += node.size

    return node.size

compute_size(root)
print('Part 1:', total_under_100k)

space_free = 70000000 - root.size
space_needed = 30000000 - space_free

def get_smallest_dir_to_delete(curr_node, selected_node):
    global space_needed

    if curr_node.children == {}:
        return selected_node

    if curr_node.size < selected_node.size and curr_node.size >= space_needed:
        selected_node = curr_node

    for _, child in curr_node.children.items():
        best_child = get_smallest_dir_to_delete(child, selected_node)
        if best_child.size < selected_node.size:
            selected_node = best_child
    
    return selected_node

print('Part 2:', get_smallest_dir_to_delete(root, root).size)
