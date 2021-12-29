package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Day25 {
    public static final int EMPTY = 0;
    public static final int CHEVRON = 1;
    public static final int VEE = 2;

    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day25.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        final String[][] strMap =
                input.stream().map(line -> line.split("")).toArray(String[][]::new);
        final int numRows = strMap.length;
        final int numCols = strMap[0].length;

        final int[][] intMap = new int[numRows][numCols];
        for (int y = 0; y < numRows; y++) {
            for (int x = 0; x < numCols; x++) {
                final String val = strMap[y][x];
                intMap[y][x] = val.equals(".") ? EMPTY : (val.equals(">") ? CHEVRON : VEE);
            }
        }

        final AtomicInteger numMoved = new AtomicInteger();
        int numLoops = 0;
        do {
            numMoved.set(0);
            final List<Pair<Integer, Integer>> coordsToMove = new ArrayList<>();

            for (int y = 0; y < numRows; y++) {
                for (int x = 0; x < numCols; x++) {
                    if (intMap[y][x] != CHEVRON) {
                        continue;
                    }

                    final int nextX = (x + 1) % numCols;
                    if (intMap[y][nextX] == EMPTY) {
                        coordsToMove.add(Pair.of(x, y));
                    }
                }
            }

            coordsToMove.forEach(
                    p -> {
                        final int nextX = (p.fst + 1) % numCols;
                        intMap[p.snd][p.fst] = EMPTY;
                        intMap[p.snd][nextX] = CHEVRON;
                        numMoved.incrementAndGet();
                    });
            coordsToMove.clear();

            for (int x = 0; x < numCols; x++) {
                for (int y = 0; y < numRows; y++) {
                    if (intMap[y][x] != VEE) {
                        continue;
                    }

                    final int nextY = (y + 1) % numRows;
                    if (intMap[nextY][x] == EMPTY) {
                        coordsToMove.add(Pair.of(x, y));
                    }
                }
            }

            coordsToMove.forEach(
                    p -> {
                        final int nextY = (p.snd + 1) % numRows;
                        intMap[p.snd][p.fst] = EMPTY;
                        intMap[nextY][p.fst] = VEE;
                        numMoved.incrementAndGet();
                    });
            coordsToMove.clear();

            numLoops++;
        } while (numMoved.get() > 0);

        System.out.println("Part 1: " + numLoops);
    }
}
