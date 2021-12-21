package tech.readonly.aoc2021;

import java.io.File;
import java.util.Scanner;

public class Day21 {
    public static void main(final String[] args) throws Exception {
        final int[] points = new int[2];
        final int[] positions = new int[2];

        try (final Scanner scanner = new Scanner(new File("inputs/day21.txt"))) {
            positions[0] = Integer.parseInt(scanner.nextLine().split(": ")[1].trim());
            positions[1] = Integer.parseInt(scanner.nextLine().split(": ")[1].trim());
        }

        long iter = 1L;
        for (; ; iter++) {
            final int move = (int) (((3 * iter) - 1) * 3) % 10;
            final int index = (int) (iter - 1) % 2;

            positions[index] = (((positions[index] - 1) + move) % 10) + 1;
            points[index] += positions[index];

            if (points[index] >= 1000) {
                break;
            }
        }

        System.out.println("Part 1: " + 3 * iter * Math.min(points[0], points[1]));
    }
}
