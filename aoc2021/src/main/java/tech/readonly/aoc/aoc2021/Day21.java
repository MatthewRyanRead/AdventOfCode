package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Day21 {
    public static void main(final String[] args) throws Exception {
        final int[] startPos = new int[2];

        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day21.txt").getFile()))) {
            startPos[0] = Integer.parseInt(scanner.nextLine().split(": ")[1].trim());
            startPos[1] = Integer.parseInt(scanner.nextLine().split(": ")[1].trim());
        }

        part1(startPos);
        part2(startPos);
    }

    private static void part1(final int[] startPos) {
        final int[] positions = new int[] {startPos[0], startPos[1]};
        final int[] points = new int[2];

        long iter = 1L;
        for (; ; iter++) {
            final int move = (int) (((3 * iter) - 1) * 3) % 10;
            final int index = (int) (iter - 1) % 2;

            positions[index] = ((positions[index] + move - 1) % 10) + 1;
            points[index] += positions[index];

            if (points[index] >= 1000) {
                break;
            }
        }

        System.out.println("Part 1: " + 3 * iter * Math.min(points[0], points[1]));
    }

    private static void part2(final int[] startPos) {
        // would be more performant with a min queue to always take the least-advanced game first,
        // or a LRU cache, but this is still ridiculously fast since the win condition is just 21+
        final Map<GameState, Long> countByState = new HashMap<>();
        countByState.put(new GameState(startPos), 1L);

        // 27 permutations, but only 7 unique totals
        final List<Pair<Integer, Integer>> moveCountPairs = new ArrayList<>();
        moveCountPairs.add(Pair.of(3, 1)); // 1,1,1
        moveCountPairs.add(Pair.of(4, 3)); // 1,1,2; 1,2,1; 2,1,1
        moveCountPairs.add(Pair.of(5, 6)); // 1,1,3; 1,3,1; 3,1,1; 1,2,2; 2,1,2; 2,2,1
        moveCountPairs.add(Pair.of(6, 7)); // 1,2,3; 1,3,2; 2,1,3; 2,3,1; 3,1,2; 3,2,1; 2,2,2
        moveCountPairs.add(Pair.of(7, 6)); // 1,3,3; 3,1,3; 3,3,1; 2,2,3; 2,3,2; 3,2,2
        moveCountPairs.add(Pair.of(8, 3)); // 2,3,3; 3,2,3; 3,3,2
        moveCountPairs.add(Pair.of(9, 1)); // 3,3,3

        final long[] wins = new long[2];
        final AtomicBoolean isPlayerOnesTurn = new AtomicBoolean(false);

        while (!countByState.isEmpty()) {
            isPlayerOnesTurn.set(!isPlayerOnesTurn.get());
            final Map<GameState, Long> currCountByState = new HashMap<>(countByState);
            countByState.clear();

            final int index = isPlayerOnesTurn.get() ? 0 : 1;

            currCountByState.forEach(
                    (gameState, count) ->
                            moveCountPairs.forEach(
                                    moveCountPair -> {
                                        final int[] newPos =
                                                new int[] {
                                                    gameState.positions[0], gameState.positions[1]
                                                };
                                        final int[] newPts =
                                                new int[] {
                                                    gameState.points[0], gameState.points[1]
                                                };

                                        newPos[index] =
                                                ((newPos[index] + moveCountPair.fst - 1) % 10) + 1;
                                        newPts[index] += newPos[index];

                                        final GameState newGameState =
                                                new GameState(newPos, newPts);

                                        final long newCount =
                                                (count * moveCountPair.snd)
                                                        + countByState.getOrDefault(
                                                                newGameState, 0L);

                                        if (newPts[index] >= 21) {
                                            wins[index] += newCount;
                                        } else {
                                            countByState.put(newGameState, newCount);
                                        }
                                    }));
        }

        System.out.println("Part 2: " + Math.max(wins[0], wins[1]));
    }

    public static class GameState {
        private final int[] positions = new int[2];
        private final int[] points = new int[2];

        private GameState(final int[] positions) {
            this.positions[0] = positions[0];
            this.positions[1] = positions[1];
        }

        private GameState(final int[] positions, final int[] points) {
            this.positions[0] = positions[0];
            this.positions[1] = positions[1];
            this.points[0] = points[0];
            this.points[1] = points[1];
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final GameState gameState = (GameState) o;
            return Arrays.equals(positions, gameState.positions)
                    && Arrays.equals(points, gameState.points);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(positions);
            result = 31 * result + Arrays.hashCode(points);
            return result;
        }
    }
}
