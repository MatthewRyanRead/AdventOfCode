import requests

DECRYPTION_KEY = 811_589_153

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day20.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

class NumberNode:
    def __init__(self, val, prev=None, next=None):
        self.val = val
        self.prev = prev
        self.next = next
        if prev:
            prev.next = self
        if next:
            next.prev = self

    def __str__(self):
        return str(self.val)

    def __repr__(self):
        return str(self)

    def extricate(self):
        self.prev.next = self.next
        self.next.prev = self.prev

    def reassign_prev(self, prev):
        self.prev = prev
        prev.next = self

    def reassign_next(self, next):
        self.next = next
        next.prev = self

    def reassign_neighbours(self, prev, next):
        self.reassign_prev(prev)
        self.reassign_next(next)

num_numbers = len(input)

numbers_p1 = [NumberNode(int(input[0]))]
numbers_p2 = [NumberNode(int(input[0]) * DECRYPTION_KEY)]
for i in range(1, num_numbers):
    numbers_p1.append(NumberNode(int(input[i]), numbers_p1[-1]))
    numbers_p2.append(NumberNode(int(input[i]) * DECRYPTION_KEY, numbers_p2[-1]))
numbers_p1[0].reassign_prev(numbers_p1[-1])
numbers_p2[0].reassign_prev(numbers_p2[-1])

def mix_numbers(number_list):
    for i, num in enumerate(number_list):
        if num.val == 0:
            zero_idx = i
            continue

        num.extricate()
        to_move = num.val % (num_numbers - 1)

        curr_next = num.next
        for i in range(to_move):
            curr_next = curr_next.next

        num.reassign_neighbours(curr_next.prev, curr_next)

    return zero_idx

def solve(number_list, zero_idx):
    zero = number_list[zero_idx]

    curr_node = zero
    for i in range(1000):
        curr_node = curr_node.next
    x = curr_node
    for i in range(1000):
        curr_node = curr_node.next
    y = curr_node
    for i in range(1000):
        curr_node = curr_node.next
    z = curr_node

    return x.val + y.val + z.val

zero_idx = mix_numbers(numbers_p1)
print('Part 1:', solve(numbers_p1, zero_idx))

for i in range(10):
    mix_numbers(numbers_p2)
print('Part 2:', solve(numbers_p2, zero_idx))
