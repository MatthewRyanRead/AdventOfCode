package tech.readonly.aoc.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Day2 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day2.txt").getFile()))) {
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
        }

        int aim = 0;
        int depth1 = 0;
        int depth2 = 0;
        int forward = 0;

        for (final String line : input) {
            final String[] command = line.split(" ");
            if (command.length != 2) {
                throw new IllegalStateException();
            }
            final int val = Integer.parseInt(command[1]);

            switch (command[0]) {
                case "forward":
                    forward += val;
                    depth2 += aim * val;
                    break;
                case "up":
                    depth1 -= val;
                    aim -= val;
                    break;
                case "down":
                    depth1 += val;
                    aim += val;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        System.out.println("Part 1: " + depth1 * forward);
        System.out.println("Part 2: " + depth2 * forward);
    }
}
