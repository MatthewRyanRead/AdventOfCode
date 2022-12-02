import requests

input_uri = "https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day2.txt"
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8')

moveset = ['R', 'P', 'S']
win_map = { 'R':'S', 'P':'R', 'S':'P' }
lose_map = { v:k for k, v in win_map.items() }
score_map = { 'R': 1, 'P': 2, 'S': 3 }
opponent_map = { 'A': 'R', 'B': 'P', 'C': 'S' }
player_map = { 'X': 'R', 'Y': 'P', 'Z': 'S' } # for p1

def compute_score_part1():
    score = 0
    for line in input.splitlines():
        opponent_move = opponent_map[line[0]]
        player_move = player_map[line[2]]

        score += score_map[player_move]

        if win_map[player_move] == opponent_move:
            score += 6
        elif player_move == opponent_move:
            score += 3

    return score

def compute_score_part2():
    score = 0
    for line in input.splitlines():
        opponent_move = opponent_map[line[0]]
        player_instruction = line[2]

        if player_instruction == 'X':
            player_move = win_map[opponent_move]
        elif player_instruction == 'Y':
            player_move = opponent_move
            score += 3
        else:
            player_move = lose_map[opponent_move]
            score += 6

        score += score_map[player_move]

    return score

print('Part 1:', compute_score_part1())
print('Part 2:', compute_score_part2())
