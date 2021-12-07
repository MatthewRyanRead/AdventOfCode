package tech.readonly.aoc2021;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day7 {
    public static void main(final String[] args) throws Exception {
        final List<Integer> input;
        try (final Scanner scanner = new Scanner(new File("inputs/samples/day7.txt"))) {
            input = Arrays.stream(scanner.nextLine().split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
    }
}
