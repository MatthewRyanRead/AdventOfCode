package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day9 {
    public static void main(final String[] args) throws Exception {
        final CaveFloor caveFloor = new CaveFloor(new ArrayList<>());
        try (final Scanner scanner = new Scanner(new FileInputStream("inputs/day9.txt"))) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                caveFloor.map.add(
                        Arrays.stream(line.trim().split(""))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()));
            }
        }

        final int height = caveFloor.map.size();
        final int width = caveFloor.map.get(0).size();

        final Map<Pair<Integer, Integer>, Integer> lowPoints = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = caveFloor.get(x, y);

                if (x > 0 && val >= caveFloor.get(x - 1, y)) continue;
                if (y > 0 && val >= caveFloor.get(x, y - 1)) continue;

                if (x < width  - 1 && val >= caveFloor.get(x + 1, y)) continue;
                if (y < height - 1 && val >= caveFloor.get(x, y + 1)) continue;

                lowPoints.put(new Pair<>(x, y), val);
            }
        }

        System.out.println("Part 1: " + lowPoints.values()
                .stream()
                .map(lp -> lp + 1)
                .reduce(Integer::sum)
                .orElseThrow());

        final Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> basins =
                lowPoints.keySet()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), x -> new HashSet<>()));
        final Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> basinCandidatesByBasinLowPoint =
                lowPoints.keySet()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), lp -> {
                            final Set<Pair<Integer, Integer>> points = new HashSet<>();
                            points.add(lp);
                            return points;
                        }));

        while (true) {
            boolean candidatesConsidered = false;
            for (final Pair<Integer, Integer> lowPoint: basinCandidatesByBasinLowPoint.keySet()) {
                final Set<Pair<Integer, Integer>> candidates = basinCandidatesByBasinLowPoint.get(lowPoint);
                candidatesConsidered |= !candidates.isEmpty();

                List<Pair<Integer, Integer>> newCandidates = new ArrayList<>();
                for (final Pair<Integer, Integer> candidate: candidates) {
                    if (caveFloor.get(candidate.fst, candidate.snd) < 9) {
                        basins.get(lowPoint).add(candidate);
                        if (candidate.fst > 0) newCandidates.add(Pair.of(candidate.fst - 1, candidate.snd));
                        if (candidate.snd > 0) newCandidates.add(Pair.of(candidate.fst, candidate.snd - 1));
                        if (candidate.fst < width  - 1) newCandidates.add(Pair.of(candidate.fst + 1, candidate.snd));
                        if (candidate.snd < height - 1) newCandidates.add(Pair.of(candidate.fst, candidate.snd + 1));
                    }
                }

                newCandidates = newCandidates.stream()
                        .filter(nc -> !basins.get(lowPoint).contains(nc))
                        .collect(Collectors.toList());
                basinCandidatesByBasinLowPoint.put(lowPoint, new HashSet<>(newCandidates));
            }

            if (!candidatesConsidered) break;
        }

        final Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> biggestBasins = basins.entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> ((Map.Entry<Pair, Set>) e).getValue().size()).reversed())
                .limit(3)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        System.out.println("Part 2: " + biggestBasins.values()
                .stream()
                .map(Set::size)
                .reduce(1, (a, b) -> a * b));
    }

    public static class CaveFloor {
        private final List<List<Integer>> map;

        public CaveFloor(final List<List<Integer>> map) {
            this.map = map;
        }

        public int get(int x, int y) {
            if (y >= map.size() || x >= map.get(y).size()) {
                throw new IllegalArgumentException();
            }

            return map.get(y).get(x);
        }
    }
}
