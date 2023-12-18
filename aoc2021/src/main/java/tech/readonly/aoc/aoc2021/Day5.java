package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Day5 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day5.txt").getFile()))) {
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
        }

        final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> lines = new ArrayList<>();
        int xSize = 0;
        int ySize = 0;

        for (final String line : input) {
            final String[] points = line.trim().split(" -> ");
            if (points.length != 2) throw new IllegalStateException();

            String[] coords = points[0].split(",");
            if (coords.length != 2) throw new IllegalStateException();
            final Pair<Integer, Integer> first =
                    Pair.of(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            if (xSize < first.fst) {
                xSize = first.fst;
            }
            if (ySize < first.snd) {
                ySize = first.snd;
            }

            coords = points[1].split(",");
            if (coords.length != 2) throw new IllegalStateException();
            final Pair<Integer, Integer> second =
                    Pair.of(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
            if (xSize <= first.fst) {
                xSize = first.fst + 1;
            }
            if (ySize <= first.snd) {
                ySize = first.snd + 1;
            }

            lines.add(Pair.of(first, second));
        }

        final int[][] map1 = new int[ySize][xSize];
        final int[][] map2 = new int[ySize][xSize];

        for (final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> line : lines) {
            final int minX = Math.min(line.fst.fst, line.snd.fst);
            final int maxX = Math.max(line.fst.fst, line.snd.fst);
            final int minY = Math.min(line.fst.snd, line.snd.snd);
            final int maxY = Math.max(line.fst.snd, line.snd.snd);

            if (minX == maxX) {
                for (int i = minY; i <= maxY; i++) {
                    map1[i][minX] = map1[i][minX] + 1;
                    map2[i][minX] = map2[i][minX] + 1;
                }
            } else if (minY == maxY) {
                for (int i = minX; i <= maxX; i++) {
                    map1[minY][i] = map1[minY][i] + 1;
                    map2[minY][i] = map2[minY][i] + 1;
                }
            } else {
                if (maxX - minX != maxY - minY) {
                    throw new IllegalStateException();
                }

                final boolean reverseX = line.fst.snd > line.snd.snd ^ line.fst.fst > line.snd.fst;
                for (int i = 0; i <= maxY - minY; i++) {
                    final int x = reverseX ? maxX - i : minX + i;
                    map2[minY + i][x] = map2[minY + i][x] + 1;
                }
            }
        }

        int numOverlaps1 = 0;
        int numOverlaps2 = 0;
        for (int i = 0; i < map1.length; i++) {
            for (int j = 0; j < map1[i].length; j++) {
                if (map1[i][j] > 1) {
                    numOverlaps1++;
                }
                if (map2[i][j] > 1) {
                    numOverlaps2++;
                }
            }
        }

        System.out.println("Part 1: " + numOverlaps1);
        System.out.println("Part 2: " + numOverlaps2);

        //System.out.printf(Arrays.stream(map1).map(Arrays::toString).collect(Collectors.joining("%n")) + "%n%n");
        //System.out.printf(Arrays.stream(map2).map(Arrays::toString).collect(Collectors.joining("%n")) + "%n");
    }
}
