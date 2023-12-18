import re

import requests
import z3

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day21.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

class Monkey:
    def __init__(self, name, val = None, left = None, right = None, operator = None):
        self.name = name
        self.val = val
        self.left = left
        self.right = right
        self.operator = operator

    def __str__(self):
        return self.name + ': ' + ((self.val is not None and str(self.val)) or self.left.name + ' ' + self.operator + ' ' + self.right.name)

    def __repr__(self):
        return str(self)

    def evaluate(self):
        if self.val != None:
            return self.val

        left = self.left.evaluate()
        right = self.right.evaluate()

        match self.operator:
            case '+':
                return left + right
            case '-':
                return left - right
            case '*':
                return left * right
            case '/':
                return left // right

monkeys_by_name = {}

pattern = re.compile(r'^(.+): (.+) ([*/+-]) (.+)$')
processed_monkeys = True
while processed_monkeys:
    processed_monkeys = False
    for line in input:
        parts = line.split(': ')

        if parts[0] in monkeys_by_name:
            continue

        matches = pattern.match(line)
        if not matches:
            monkeys_by_name[parts[0]] = Monkey(parts[0], val = int(parts[1]))
            processed_monkeys = True
        else:
            left = matches[2]
            right = matches[4]

            if left in monkeys_by_name and right in monkeys_by_name:
                left = monkeys_by_name[left]
                right = monkeys_by_name[right]

                monkeys_by_name[matches[1]] = Monkey(parts[0], left = left, right = right, operator = matches[3])
                processed_monkeys = True

root = monkeys_by_name['root']
humn = monkeys_by_name['humn']

print('Part 1:', root.evaluate())

root.operator = '='
solver = z3.Solver()
z3_ints_by_name = {}

def create_z3_ints(node):
    if node.val != None:
        if node.name in z3_ints_by_name:
            num = z3_ints_by_name[node.name]
        else:
            num = z3.Int(node.name)
            z3_ints_by_name[node.name] = num

        if node != humn:
            solver.add(num == node.val)
    else:
        if node.name in z3_ints_by_name:
            return z3_ints_by_name[node.name]

        num = z3.Int(node.name)
        z3_ints_by_name[node.name] = num

        left = create_z3_ints(node.left)
        right = create_z3_ints(node.right)

        match node.operator:
            case '+':
                solver.add(num == left + right)
            case '-':
                solver.add(num == left - right)
            case '*':
                solver.add(num == left * right)
            case '/':
                solver.add(num == left / right)
            case '=':
                solver.add(left == right)

    return num

create_z3_ints(root)
if solver.check() != z3.sat:
    raise Exception('could not solve')

print('Part 2:', solver.model()[z3_ints_by_name['humn']])
