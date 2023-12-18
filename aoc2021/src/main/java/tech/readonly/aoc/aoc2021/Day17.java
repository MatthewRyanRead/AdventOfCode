package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Day17 {
    public static void main(final String[] args) throws Exception {
        final String input;
        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day17.txt").getFile()))) {
            input = scanner.nextLine().trim();
        }

        final String[] xy = input.split(",");
        final String[] xStr = xy[0].split("=")[1].split("\\.\\.");
        final String[] yStr = xy[1].split("=")[1].split("\\.\\.");
        final long[] x = new long[] {Long.parseLong(xStr[0]), Long.parseLong(xStr[1])};
        final long[] y = new long[] {Long.parseLong(yStr[0]), Long.parseLong(yStr[1])};
        final long minX = Math.min(x[0], x[1]);
        final long maxX = Math.max(x[0], x[1]);
        final long minY = Math.min(y[0], y[1]);
        final long maxY = Math.max(y[0], y[1]);

        final long maxYVel = Math.abs(minY > 0 ? maxY : Math.max(Math.abs(minY), maxY));

        System.out.println("Part 1: " + maxYVel * (maxYVel - 1) / 2);

        final long minXVel;
        if (minX > 0) minXVel = (long) Math.sqrt((1 + minX) / 2.0 + 1) - 1;
        else minXVel = minX;

        final long maxXVel;
        if (maxX < 0) maxXVel = (long) -Math.sqrt((1 + -maxX) / 2.0 + 1) + 1;
        else maxXVel = maxX;

        final long minYVel;
        if (minY > 0) minYVel = (long) Math.sqrt((1 + minY) / 2.0 + 1) - 1;
        else minYVel = minY;

        final Set<Pair<Long, Long>> values = new HashSet<>();

        for (long currYVel = minYVel; currYVel <= maxYVel; currYVel++) {
            for (long currXVel = minXVel; currXVel <= maxXVel; currXVel++) {
                if (onTarget(currXVel, currYVel, minX, maxX, minY, maxY)) {
                    values.add(Pair.of(currXVel, currYVel));
                }
            }
        }

        final List<Pair> pairs =
                values.stream()
                        .sorted(
                                Comparator.comparingLong(p -> ((Pair<Long, Long>) p).fst)
                                        .thenComparingLong(p -> ((Pair<Long, Long>) p).snd))
                        .collect(Collectors.toList());
        System.out.println("Part 2: " + values.size());
    }

    private static boolean onTarget(
            long xVel,
            long yVel,
            final long minX,
            final long maxX,
            final long minY,
            final long maxY) {
        final int xIncrement = (int) -Math.signum(xVel);

        long xPos = 0;
        long yPos = 0;

        for (long i = xVel, j = yVel; yPos > minY || j > 0; j--) {
            xPos += i;
            yPos += j;

            if (xPos >= minX && xPos <= maxX && yPos >= minY && yPos <= maxY) {
                return true;
            }

            if (i != 0) i += xIncrement;
        }

        return false;
    }
}
