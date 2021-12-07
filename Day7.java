package tech.readonly.aoc2021;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day7 {
    public static void main(final String[] args) throws Exception {
        final List<Integer> input;
        try (final Scanner scanner = new Scanner(new File("inputs/day7.txt"))) {
            input =
                    Arrays.stream(scanner.nextLine().split(","))
                            .map(Integer::parseInt)
                            .sorted()
                            .collect(Collectors.toList());
        }

        int result1 = calc1(input, input.get(input.size() / 2));
        if (input.size() % 2 == 0) {
            result1 = Math.min(result1, calc1(input, input.size() / 2 - 1));
        }

        System.out.println("Part 1: " + result1);

        final int averageFloor = input.stream().reduce(Integer::sum).orElseThrow() / input.size();
        int result2 = Math.min(calc2(input, averageFloor), calc2(input, averageFloor + 1));

        System.out.println("Part 2: " + result2);
    }

    public static int calc1(final List<Integer> input, final int destinationVal) {
        return input.stream()
                .map(i -> Math.abs(i - destinationVal))
                .reduce(Integer::sum)
                .orElseThrow();
    }

    public static int calc2(final List<Integer> input, final int destinationVal) {
        return input.stream()
                .map(
                        i -> {
                            final int difference = Math.abs(i - destinationVal);
                            return difference * (difference + 1) / 2;
                        })
                .reduce(Integer::sum)
                .orElseThrow();
    }
}
