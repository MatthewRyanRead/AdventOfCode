import functools
import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day13.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
# simplify parsing by making all atoms single-digit and contiguous chars
# the values are 0 through 10, and ':' comes after '9' in the charset
input = response.raw.read().decode('utf-8').replace('10', ':').splitlines()

# technically this is not a snail number (see https://adventofcode.com/2021/day/18),
# since it supports arbitrary children, but whatever.
class SnailNumber:
    def __init__(self, value=None, parent=None):
        self.value = value
        self.parent = parent
        self.children = []
        if parent:
            parent.children.append(self)

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        if self.value != None:
            return str(self.value)

        string = ''
        for child in self.children:
            string += str(child) + ','
        return '[' + (len(string) > 0 and string[:-1] or '') + ']'

def parse_snail_number(number_str):
    number = None

    for char in number_str:
        if char == '[':
            new_number = SnailNumber(parent=number)
            number = new_number
        elif char == ']':
            number = number.parent or number
        elif char != ',':
            child = SnailNumber(value=(ord(char) - ord('0')), parent=number)

    if number == None:
        raise Exception
    return number

num_pairs = (len(input) // 3) + 1
pairs = []

for i in range(num_pairs):
    index = i*3
    pair_one_str = input[index]
    pair_two_str = input[index + 1]
    pairs.append([parse_snail_number(pair_one_str), parse_snail_number(pair_two_str)])

def compare_snail_numbers(one, two):
    if one.value != None and two.value != None:
        # negative for less, zero for equal, positive for greater
        return one.value - two.value

    if one.value != None and two.children == []:
        # two ends first, so one is bigger
        return 1

    if one.value != None:
        # upcast one
        one_wrapper = SnailNumber()
        one_wrapper.children.append(one)
        return compare_snail_numbers(one_wrapper, two)

    # one's value is None

    if two.value != None:
        two_wrapper = SnailNumber()
        two_wrapper.children.append(two)
        return compare_snail_numbers(one, two_wrapper)

    # both values are None

    one_size = len(one.children)
    two_size = len(two.children)
    for i in range(max(one_size, two_size)):
        if i >= one_size:
            return -1
        if i >= two_size:
            return 1

        child_result = compare_snail_numbers(one.children[i], two.children[i])
        if child_result != 0:
            return child_result

    return 0

index_sum = 0
for i, pair in enumerate(pairs):
    if compare_snail_numbers(pair[0], pair[1]) <= 0:
        index_sum += i + 1

print('Part 1:', index_sum)

all_numbers = [pair[0] for pair in pairs]
all_numbers += [pair[1] for pair in pairs]

wrapper_for_two = SnailNumber()
inner_wrapper_for_two = SnailNumber(parent=wrapper_for_two)
two = SnailNumber(value=2, parent=inner_wrapper_for_two)
all_numbers.append(wrapper_for_two)
wrapper_for_six = SnailNumber()
inner_wrapper_for_six = SnailNumber(parent=wrapper_for_six)
six = SnailNumber(value=6, parent=inner_wrapper_for_six)
all_numbers.append(wrapper_for_six)

all_numbers.sort(key=functools.cmp_to_key(compare_snail_numbers))

index_product = 1
for i, number in enumerate(all_numbers):
    if number == wrapper_for_two or number == wrapper_for_six:
        index_product *= i + 1

print('Part 2:', index_product)
