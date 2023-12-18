import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/src/main/resources/inputs/Day15.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

p1_row = 2_000_000
dimension_limit = 4_000_000
covered_points_per_row = {}
covered_points_for_p1 = { p1_row: [] }

for line in input:
    coords = line.split('Sensor at ')[1].split(': closest beacon is at ')
    sensor_coords = coords[0].split(', ')
    sensor_coords = (int(sensor_coords[0][2:]), int(sensor_coords[1][2:]))
    beacon_coords = coords[1].split(', ')
    beacon_coords = (int(beacon_coords[0][2:]), int(beacon_coords[1][2:]))
    dist = abs(sensor_coords[0] - beacon_coords[0]) + abs(sensor_coords[1] - beacon_coords[1])

    # for every 'sensed' row, save the interval that can be seen
    for y in range(max(0, sensor_coords[1] - dist), min(dimension_limit, sensor_coords[1] + dist + 1)):
        y_dist = abs(y - sensor_coords[1])

        clamped_interval = (max(0, sensor_coords[0] - dist + y_dist), min(dimension_limit, sensor_coords[0] + dist - y_dist))
        if not y in covered_points_per_row:
            covered_points_per_row[y] = [clamped_interval]
        else:
            covered_points_per_row[y].append(clamped_interval)

        if y == p1_row:
            covered_points_for_p1[y].append((sensor_coords[0] - dist + y_dist, sensor_coords[0] + dist - y_dist))

# [(1, 4), (2, 3)] -> [(1, 4)]
# [(1, 3), (2, 5)] -> [(1, 5)]
# [(1, 4), (6, 7)] -> [(1, 4), (6, 7)]
# etc.
def condense_all_tuples(lists_of_tuples):
    condensed_lists = {}

    for y, list_of_tuples in lists_of_tuples.items():
        sorted_list = sorted(list_of_tuples)
        new_list = sorted_list[:1]

        for i in range(len(sorted_list) - 1):
            coords_one = new_list[-1]
            coords_two = sorted_list[i + 1]

            if coords_one[1] >= coords_two[0] - 1:
                new_list[-1] = ((coords_one[0], max(coords_one[1], coords_two[1])))
            else:
                new_list.append(coords_two)

        condensed_lists[y] = new_list

    return condensed_lists

covered_points_for_p1 = condense_all_tuples(covered_points_for_p1)
part_1_count = sum(coords[1] - coords[0] for coords in covered_points_for_p1[p1_row])
print('Part 1:', part_1_count)

# any row with disjoint points holds a solution between said points
# in this case, it's just one row of two points -- with only a gap of 1 between them!
covered_points_per_row = condense_all_tuples(covered_points_per_row)
solution = [(y, covered_points[0][1] + 1) for y, covered_points in covered_points_per_row.items() if len(covered_points) > 1][0]
print('Part 2:', solution[0] + solution[1] * 4_000_000)
