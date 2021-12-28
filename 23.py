import collections
import itertools
import math
import re

ROOMS = {
    'A': 2,
    'B': 4,
    'C': 6,
    'D': 8,
}
ROOM_POSITIONS = set(ROOMS.values())
MOVE_COSTS = {
    'A': 1,
    'B': 10,
    'C': 100,
    'D': 1000,
}

def canReach(board, start_pos, end_pos):
    '''Checks for any piece blocking the path from
    start_pos to end_pos.'''
    a = min(start_pos, end_pos)
    b = max(start_pos, end_pos)
    for pos in range(a, b+1):
        if pos == start_pos:
            continue
        if pos in ROOM_POSITIONS:
            continue
        if board[pos] != '.':
            return False
    return True


def roomOnlyContainsGoal(board, piece, dest_pos):
    assert dest_pos in ROOM_POSITIONS
    inRoom = board[dest_pos]
    return len(inRoom) == inRoom.count('.') + inRoom.count(piece) 


def getPieceFromRoom(room):
    for c in room:
        if c != '.':
            return c


def possibleMoves(board, pos):
    piece = board[pos]
    # print(board, pos, piece)
    if pos not in ROOM_POSITIONS:
        if canReach(board, pos, ROOMS[piece]) and roomOnlyContainsGoal(board, piece, ROOMS[piece]):
            return [ROOMS[piece]]
        return []

    movingLetter = getPieceFromRoom(piece)
    if pos == ROOMS[movingLetter] and roomOnlyContainsGoal(board, movingLetter, pos):
        return []

    possible = []
    for dest in range(len(board)):
        if dest == pos:
            continue
        if dest in ROOM_POSITIONS and ROOMS[movingLetter] != dest:
            continue
        if ROOMS[movingLetter] == dest:
            if not roomOnlyContainsGoal(board, movingLetter, dest):
                continue
        if canReach(board, pos, dest):
            possible.append(dest)
    return possible


def addToRoom(letter, room):
    room = list(room)
    dist = room.count('.')
    assert dist != 0
    room[dist-1] = letter
    return ''.join(room), dist


def move(board, pos, dest):
    new_board = board[:]
    dist = 0
    movingLetter = getPieceFromRoom(board[pos])
    if len(board[pos]) == 1:
        new_board[pos] = '.'
    else:
        new_room = ''
        found = False
        for c in board[pos]:
            if c == '.':
                dist += 1
                new_room += c
            elif not found:
                new_room += '.'
                dist += 1
                found = True
            else:
                new_room += c
        new_board[pos] = new_room
    
    dist += abs(pos - dest)
    addl_dist = 0

    if len(board[dest]) == 1:
        new_board[dest] = movingLetter
    else:
        new_board[dest], addl_dist = addToRoom(movingLetter, board[dest])
        dist += addl_dist

    return new_board, dist * MOVE_COSTS[movingLetter]


def solve(board):
    states = {tuple(board): 0}
    board_by_board = {tuple(board): []}
    queue = [board]
    while queue:
        # print(len(queue))
        board = queue.pop()
        board_tuple = tuple(board)

        for pos, piece in enumerate(board):
            if getPieceFromRoom(piece) is None:
                continue
            dests = possibleMoves(board, pos)
            # print('{} ({}) can move to {}'.format(piece, pos, dests))
            for dest in dests:
                new_board, addl_cost = move(board, pos, dest)
                new_cost = states[board_tuple] + addl_cost
                new_board_tuple = tuple(new_board)
                cost = states.get(new_board_tuple, 9999999)
                if new_cost < cost:
                    #print(board, '->', new_board, ':', new_cost)
                    states[new_board_tuple] = new_cost
                    queue.append(new_board)
                    board_by_board[new_board_tuple] = list(board_by_board.get(board_tuple, []))
                    board_by_board[new_board_tuple].append(board)

    completed_board = ('.', '.', 'AAAA', '.', 'BBBB', '.', 'CCCC', '.', 'DDDD', '.', '.')
    count = states[completed_board]
    return count, board_by_board[completed_board]

board = ['.', '.', 'DDDB', '.', 'ACBA', '.', 'BBAD', '.', 'CACC', '.', '.']
count, prev_boards = solve(board)

prev_board_set = set()

for prev_board in prev_boards:
    prev_board_tuple = tuple(prev_board)
    if prev_board_tuple not in prev_board_set:
        print('#############')
        print('#' + prev_board[0] + prev_board[1] + '.' + prev_board[3] + '.' + prev_board[5] + '.' + prev_board[7] + '.' + prev_board[9] + prev_board[10] + '#')
        print('###' + prev_board[2][0] + '#' + prev_board[4][0] + '#' + prev_board[6][0] + '#' + prev_board[8][0] + '###')
        print('  #' + prev_board[2][1] + '#' + prev_board[4][1] + '#' + prev_board[6][1] + '#' + prev_board[8][1] + '#')
        print('  #' + prev_board[2][2] + '#' + prev_board[4][2] + '#' + prev_board[6][2] + '#' + prev_board[8][2] + '#')
        print('  #' + prev_board[2][3] + '#' + prev_board[4][3] + '#' + prev_board[6][3] + '#' + prev_board[8][3] + '#')
        print('  #########')
        print('')
        prev_board_set.add(prev_board_tuple)
print(count)