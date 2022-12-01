package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day6 {
    public static void main(final String[] args) throws Exception {
        final List<Fish> inputFish;
        try (final Scanner scanner = new Scanner(new File("inputs/day6.txt"))) {
            final String input = scanner.nextLine();
            inputFish =
                    Arrays.stream(input.trim().split(","))
                            .map(Integer::parseInt)
                            .map(Fish::new)
                            .collect(Collectors.toList());
        }

        final List<Fish> part1Fish = inputFish.stream().map(Fish::new).collect(Collectors.toList());

        for (int i = 0; i < 80; i++) {
            final List<Fish> newFish = new ArrayList<>();

            for (final Fish currFish : part1Fish) {
                if (currFish.daysLeftTillSpawn == 0) {
                    currFish.daysLeftTillSpawn = 6;
                    newFish.add(new Fish());
                } else {
                    currFish.daysLeftTillSpawn--;
                }
            }

            part1Fish.addAll(newFish);
        }

        System.out.println("Part 1: " + part1Fish.size());

        // lmao, I KNEW that wasn't going to cut it for part 2
        // use total counts instead of iteration over individual fish -- no need to resort to pure math just yet

        final Map<Integer, Long> fishCountByDaysRemaining = inputFish.stream()
                .collect(Collectors.toMap(f -> f.daysLeftTillSpawn, f -> 1L, Long::sum));

        for (int i = 0; i < 256; i++) {
            long numToSpawn = 0L;

            for (int j = 0; j <= 8; j++) {
                if (j == 0) {
                    numToSpawn = fishCountByDaysRemaining.getOrDefault(0, 0L);
                } else {
                    fishCountByDaysRemaining.put(
                            j - 1, fishCountByDaysRemaining.getOrDefault(j, 0L));
                }

                fishCountByDaysRemaining.put(j, 0L);
            }

            fishCountByDaysRemaining.put(8, numToSpawn);
            fishCountByDaysRemaining.put(
                    6, numToSpawn + fishCountByDaysRemaining.getOrDefault(6, 0L));
        }

        System.out.println(
                "Part 2: " + fishCountByDaysRemaining.values().stream().reduce(Long::sum).get());
    }

    private static class Fish {
        private int daysLeftTillSpawn = 8;

        public Fish() {}

        public Fish(final int daysLeftTillSpawn) {
            this.daysLeftTillSpawn = daysLeftTillSpawn;
        }

        public Fish(final Fish fish) {
            this.daysLeftTillSpawn = fish.daysLeftTillSpawn;
        }
    }
}
