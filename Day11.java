package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Day11 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day11.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        final Integer[][] energies = new Integer[input.size()][input.get(0).length()];
        for (int y = 0; y < input.size(); y++) {
            final String[] nums = input.get(y).split("");
            for (int x = 0; x < nums.length; x++) {
                energies[y][x] = Integer.parseInt(nums[x]);
            }
        }

        long totalFlashesAfter100 = 0L;
        int firstSyncFlash = -1;

        for (int i = 0; firstSyncFlash == -1; i++) {
            final Set<Pair<Integer, Integer>> flashers = new HashSet<>();

            for (int y = 0; y < energies.length; y++) {
                for (int x = 0; x < energies[y].length; x++) {
                    energies[y][x] += 1;
                    if (energies[y][x] > 9) {
                        flashers.add(Pair.of(x, y));
                    }
                }
            }

            final long newFlashes = handleFlashes(flashers, energies);
            if (i < 100) {
                totalFlashesAfter100 += newFlashes;
            }

            int numReset = 0;
            for (int y = 0; y < energies.length; y++) {
                for (int x = 0; x < energies[y].length; x++) {
                    if (energies[y][x] > 9) {
                        energies[y][x] = 0;
                        numReset++;
                    }
                }
            }

            if (numReset == energies.length * energies[0].length) {
                firstSyncFlash = i + 1;
            }
        }

        System.out.println("Part 1: " + totalFlashesAfter100);
        System.out.println("Part 2: " + firstSyncFlash);
    }

    private static long handleFlashes(
            final Set<Pair<Integer, Integer>> flashers, final Integer[][] energies) {
        if (flashers.isEmpty()) return 0L;

        Set<Pair<Integer, Integer>> currFlashers = new HashSet<>(flashers);

        do {
            final Set<Pair<Integer, Integer>> nextFlashers = new HashSet<>();

            for (final Pair<Integer, Integer> flasher : currFlashers) {
                final Set<Pair<Integer, Integer>> neighbors = getNeighbors(flasher, energies);

                for (final Pair<Integer, Integer> neighbor : neighbors) {
                    if (flashers.contains(neighbor)) continue;

                    energies[neighbor.snd][neighbor.fst] += 1;

                    if (energies[neighbor.snd][neighbor.fst] > 9) {
                        nextFlashers.add(neighbor);
                    }
                }
            }

            flashers.addAll(nextFlashers);
            currFlashers = nextFlashers;
        } while (!currFlashers.isEmpty()
                && currFlashers.size() != energies.length * energies[0].length);

        return flashers.size();
    }

    private static Set<Pair<Integer, Integer>> getNeighbors(
            final Pair<Integer, Integer> octopus, final Integer[][] energies) {
        final int height = energies.length;
        final int width = energies[0].length;
        final int x = octopus.fst;
        final int y = octopus.snd;

        final Set<Pair<Integer, Integer>> neighbors = new HashSet<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                if (x + i < 0) continue;
                if (y + j < 0) continue;
                if (x + i >= width) continue;
                if (y + j >= height) continue;

                neighbors.add(Pair.of(x + i, y + j));
            }
        }

        return neighbors;
    }
}
