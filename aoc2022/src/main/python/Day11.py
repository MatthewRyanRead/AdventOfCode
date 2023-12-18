import copy

import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day11.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

class Addition:
    @staticmethod
    def operate(value, addend):
        return value + addend

class Multiplication:
    @staticmethod
    def operate(value, multiplier):
        return value * multiplier

class Monkey:
    def __init__(self, id, worry_operation, worry_operand, test_divisor, test_pass_monkey_id, test_fail_monkey_id, items):
        self.id = id
        self.worry_operation = worry_operation
        self.worry_operand = worry_operand
        self.test_divisor = test_divisor
        self.test_pass_monkey_id = test_pass_monkey_id
        self.test_fail_monkey_id = test_fail_monkey_id
        self.items = items
        self.inspect_count = 0

    def inspect(self):
        self.items = [self.worry_operation.operate(item, self.worry_operand or item) for item in self.items]
        self.inspect_count += len(self.items)

    def get_bored(self):
        for i, item in enumerate(self.items):
            self.items[i] = item // 3

    def test_and_send(self, monkeys, common_multiple):
        for item in self.items:
            if item > common_multiple:
                item %= common_multiple

            monkey_idx = self.test_fail_monkey_id
            if item % self.test_divisor == 0:
                monkey_idx = self.test_pass_monkey_id
            monkeys[monkey_idx].items.append(item)

        self.items = []

monkeys_part1 = []
num_monkeys = (len(input) + 1) // 7
for i in range(num_monkeys):
    monkey_idx = i * 7
    items = [int(val) for val in input[monkey_idx + 1].split(': ')[1].split(', ')]
    worry_operation, worry_operand = [(i == 0 and (v == '*' and Multiplication or Addition)) or (v != 'old' and int(v) or None) \
        for i, v in enumerate(input[monkey_idx + 2].split('old ')[1].split(' '))]
    test_divisor = int(input[monkey_idx + 3].split('by ')[1])
    test_pass_monkey_id = int(input[monkey_idx + 4].split('monkey ')[1])
    test_fail_monkey_id = int(input[monkey_idx + 5].split('monkey ')[1])

    monkeys_part1.append(Monkey(i, worry_operation, worry_operand, test_divisor, test_pass_monkey_id, test_fail_monkey_id, items))

common_multiple = 1
for monkey in monkeys_part1:
    common_multiple *= monkey.test_divisor
monkeys_part2 = copy.deepcopy(monkeys_part1)

for round in range(20):
    for monkey in monkeys_part1:
        monkey.inspect()
        monkey.get_bored()
        monkey.test_and_send(monkeys_part1, common_multiple)

scores = sorted([monkey.inspect_count for monkey in monkeys_part1])[-2:]
print('Part 1:', scores[0] * scores[1])

for round in range(10000):
    for monkey in monkeys_part2:
        monkey.inspect()
        # our increased worry pleases the monkeys; they no longer get bored
        monkey.test_and_send(monkeys_part2, common_multiple)

scores = sorted([monkey.inspect_count for monkey in monkeys_part2])[-2:]
print('Part 2:', scores[0] * scores[1])
