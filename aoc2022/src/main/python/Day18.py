import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day18.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

cubes = set()
for line in input:
    cubes.add(tuple([int(coord) for coord in line.split(',')]))
neighbour_translations = [(-1, 0, 0), (0, -1, 0), (0, 0, -1), (1, 0, 0), (0, 1, 0), (0, 0, 1)]

surface_area = 6 * len(cubes)
for cube in cubes:
    for translation in neighbour_translations:
        new_coords = (cube[0] + translation[0], cube[1] + translation[1], cube[2] + translation[2])
        if new_coords in cubes:
            surface_area -= 1

print('Part 1:', surface_area)

class Cube:
    def __init__(self, coords, filled):
        self.coords = coords
        self.filled = filled

limits = [min([x for x, y, z in cubes]) - 1, max([x for x, y, z in cubes]) + 2,
          min([y for x, y, z in cubes]) - 1, max([y for x, y, z in cubes]) + 2,
          min([z for x, y, z in cubes]) - 1, max([z for x, y, z in cubes]) + 2]
big_cube = [[[None for x in range(limits[0], limits[1])] for y in range(limits[2], limits[3])] for z in range(limits[4], limits[5])]

def coords_to_indices(coords):
    return tuple([c - limits[i*2] for i, c in enumerate(coords)])    

def indices_to_coords(indices):
    return tuple([c + limits[i*2] for i, c in enumerate(indices)])

for z in range(limits[4], limits[5]):
    big_cube += []

    for y in range(limits[2], limits[3]):
        big_cube[coords_to_indices((0, 0, z))[2]] += []

        for x in range(limits[0], limits[1]):
            indices = coords_to_indices((x, y, z))
            big_cube[indices[2]][indices[1]][indices[0]] = Cube((x, y, z), (x, y, z) in cubes)

surface_area = 0
to_visit = set([big_cube[0][0][0]])
seen = set()
while len(to_visit) > 0:
    curr_cube = to_visit.pop()
    seen.add(curr_cube)

    for transition in neighbour_translations:
        indices = coords_to_indices(curr_cube.coords)
        x = indices[0] + transition[0]
        y = indices[1] + transition[1]
        z = indices[2] + transition[2]
        try: # lazy bounds handling
            neighbour = big_cube[z][y][x]
            if neighbour.filled:
                surface_area += 1
            elif not neighbour in seen:
                to_visit.add(neighbour)
        except Exception:
            pass

print('Part 2:', surface_area)
