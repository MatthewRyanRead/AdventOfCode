import enum
import math
import re
import requests

input_uri = 'https://raw.githubusercontent.com/MatthewRyanRead/AdventOfCode/main/aoc2022/inputs/Day19.txt'
response = requests.get(input_uri, stream=True)
response.raw.decode_content = True
input = response.raw.read().decode('utf-8').splitlines()

Resource = enum.Enum('Resource', ['ORE', 'CLAY', 'OBSIDIAN', 'GEODE'])

class Bot:
    def __init__(self, gets, costs):
        self.gets = gets
        self.costs = costs

    def __str__(self):
        return str(self.gets) + ': ' + str(self.costs)

    def __repr__(self):
        return str(self)

class Blueprint:
    def __init__(self, id, bots):
        self.id = id
        self.bots = bots
        self.max_costs = { r:0 for r in Resource }
        for bot in bots.values():
            for resource, amount in bot.costs.items():
                self.max_costs[resource] = max(self.max_costs[resource], amount)

    def __str__(self):
        return 'id: ' + str(self.id) + ', bots: ' + str(self.bots)

    def __repr__(self):
        return str(self)

pattern = 'Blueprint (.+): Each ore robot costs (.+) ore. Each clay robot costs (.+) ore. ' + \
        'Each obsidian robot costs (.+) ore and (.+) clay. Each geode robot costs (.+) ore and (.+) obsidian.'

blueprints = []
for line in input:
    matches = re.findall(pattern, line)[0]
    id = int(matches[0])
    ore_ore_cost  = int(matches[1])
    clay_ore_cost = int(matches[2])
    obs_ore_cost  = int(matches[3])
    obs_clay_cost = int(matches[4])
    geo_ore_cost  = int(matches[5])
    geo_obs_cost  = int(matches[6])

    orebot  = Bot(Resource.ORE,      { Resource.ORE : ore_ore_cost,  Resource.CLAY : 0,             Resource.OBSIDIAN : 0,            Resource.GEODE : 0 })
    claybot = Bot(Resource.CLAY,     { Resource.ORE : clay_ore_cost, Resource.CLAY : 0,             Resource.OBSIDIAN : 0,            Resource.GEODE : 0 })
    obsbot  = Bot(Resource.OBSIDIAN, { Resource.ORE : obs_ore_cost,  Resource.CLAY : obs_clay_cost, Resource.OBSIDIAN : 0,            Resource.GEODE : 0 })
    geobot  = Bot(Resource.GEODE,    { Resource.ORE : geo_ore_cost,  Resource.CLAY : 0,             Resource.OBSIDIAN : geo_obs_cost, Resource.GEODE : 0 })

    blueprints.append(Blueprint(id, { Resource.ORE:orebot, Resource.CLAY:claybot, Resource.OBSIDIAN:obsbot, Resource.GEODE:geobot }))

class State:
    def __init__(self, time, resource_counts, bot_counts):
        self.time = time
        self.resource_counts = resource_counts
        self.bot_counts = bot_counts
    
    def __hash__(self):
        return hash(tuple([self.time, frozenset(self.bot_counts.items()), frozenset(self.resource_counts.items())]))

def gather(state, time = 1):
    new_resource_counts = {}
    for resource in Resource:
        new_resource_counts[resource] = state.resource_counts[resource] + time * state.bot_counts[resource]

    return State(state.time - time, new_resource_counts, state.bot_counts)

def build(blueprint, state, bot_resource):
    new_bot_counts = state.bot_counts.copy()
    new_bot_counts[bot_resource] += 1

    new_resource_counts = state.resource_counts.copy()
    for resource, cost in blueprint.bots[bot_resource].costs.items():
        new_resource_counts[resource] = state.resource_counts[resource] - cost

    return State(state.time, new_resource_counts, new_bot_counts)

def time_to_gather_resources_for_bot(blueprint, state, bot_resource):
    time_needed = 0
    for resource, cost in blueprint.bots[bot_resource].costs.items():
        resource_count = state.resource_counts[resource]
        if resource_count >= cost:
            continue

        gathering_bots = state.bot_counts[resource]
        if gathering_bots == 0:
            return None

        amount_needed = (cost - resource_count)
        time_needed = max(time_needed, math.ceil(amount_needed / gathering_bots))

    if time_needed + 1 < state.time:
        return time_needed

    return None

def upper_bound(blueprint, state):
    max_ore = state.resource_counts[Resource.ORE] + state.bot_counts[Resource.ORE] * state.time \
            + (state.time * (state.time - 1)) // 2
    max_obsidian = state.resource_counts[Resource.OBSIDIAN] + state.bot_counts[Resource.OBSIDIAN] * state.time \
            + (state.time * (state.time - 1)) // 2
    max_new_geobots = max(max_ore      // blueprint.bots[Resource.GEODE].costs[Resource.ORE], \
                          max_obsidian // blueprint.bots[Resource.GEODE].costs[Resource.OBSIDIAN])
    will_gather = state.resource_counts[Resource.GEODE] + state.bot_counts[Resource.GEODE] * state.time

    if max_new_geobots >= state.time:
        return will_gather + (state.time * (state.time - 1)) // 2
    return will_gather + max_new_geobots * (max_new_geobots - 1) // 2 + (state.time - max_new_geobots) * max_new_geobots

def solve(blueprint, state, bot_resource_to_build = None, cache = None, best_score_so_far = 0):
    if bot_resource_to_build is not None:
        state = gather(state)
        state = build(blueprint, state, bot_resource_to_build)

    cache = cache or set()
    if state in cache or upper_bound(blueprint, state) <= best_score_so_far:
        return 0
    cache.add(state)

    for bot_resource in Resource:
        if bot_resource != Resource.GEODE and \
                state.bot_counts[bot_resource] * state.time + state.resource_counts[bot_resource] >= state.time * blueprint.max_costs[bot_resource]:
            continue

        time_needed = time_to_gather_resources_for_bot(blueprint, state, bot_resource)
        if time_needed == None:
            continue

        new_state = gather(state, time_needed)
        score = solve(blueprint, State(new_state.time, new_state.resource_counts, new_state.bot_counts), bot_resource, cache, best_score_so_far)
        best_score_so_far = max(score, best_score_so_far)

    score = state.resource_counts[Resource.GEODE] + state.bot_counts[Resource.GEODE] * state.time
    best_score_so_far = max(score, best_score_so_far)
    return best_score_so_far

initial_bot_counts = { r : r == Resource.ORE and 1 or 0 for r in Resource }
initial_resource_counts = { r : 0 for r in Resource }

initial_state = State(24, initial_resource_counts, initial_bot_counts)
score = 0
for blueprint in blueprints:
    score += solve(blueprint, initial_state) * blueprint.id
print('Part 1:', score)

initial_state = State(32, initial_resource_counts, initial_bot_counts)
score = 1
for blueprint in blueprints[:3]:
    score *= solve(blueprint, initial_state)
print('Part 2:', score)
