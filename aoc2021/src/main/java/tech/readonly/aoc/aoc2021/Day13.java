package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class Day13 {
    public static void main(final String[] args) throws Exception {
        final List<Pair<Integer, Integer>> dots = new ArrayList<>();
        int maxX = 0;
        int maxY = 0;
        final List<Pair<String, Integer>> folds = new ArrayList<>();

        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day13.txt").getFile()))) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                if (line.startsWith("fold")) {
                    final String[] fold = line.split(" ")[2].split("=");
                    folds.add(Pair.of(fold[0], parseInt(fold[1])));
                } else {
                    final String[] coords = line.split(",");
                    final int x = parseInt(coords[0]);
                    final int y = parseInt(coords[1]);

                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;

                    dots.add(Pair.of(x, y));
                }
            }
        }

        final int[][] paper = new int[maxY + 1][maxX + 1];
        for (final Pair<Integer, Integer> dot: dots) {
            paper[dot.snd][dot.fst]++;
        }

        for (int i = 0; i < folds.size(); i++) {
            final boolean horizontal = folds.get(i).fst.equals("y");
            final int axis = folds.get(i).snd;

            if (horizontal) {
                int num = maxY - axis;

                for (int yOffset = 1; yOffset <= num; yOffset++) {
                    for (int x = 0; x <= maxX; x++) {
                        paper[axis - yOffset][x] += paper[axis + yOffset][x];
                    }
                }

                maxY = axis - 1;
            }
            else {
                int num = maxX - axis;

                for (int xOffset = 1; xOffset <= num; xOffset++) {
                    for (int y = 0; y <= maxY; y++) {
                        paper[y][axis - xOffset] += paper[y][axis + xOffset];
                    }
                }

                maxX = axis - 1;
            }

            if (i == 0) {
                final int fold1MaxX = maxX;
                System.out.println("Part 1: " + Arrays.stream(paper)
                        .limit(maxY + 1)
                        .flatMapToInt(row -> Arrays.stream(row).limit(fold1MaxX + 1))
                        .map(val -> val == 0 ? 0 : 1)
                        .sum());
            }
        }

        final int finalMaxX = maxX;
        System.out.println("Part 2:\n" + Arrays.stream(paper)
                .limit(maxY + 1)
                .map(row -> Arrays.stream(row)
                        .limit(finalMaxX + 1)
                        .mapToObj(val -> val == 0 ? " " : "#")
                        .collect(Collectors.joining()))
                .collect(Collectors.joining("\n")));
    }
}
