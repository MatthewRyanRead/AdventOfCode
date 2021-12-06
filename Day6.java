package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Day6 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/samples/day6.txt"))) {
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
        }


    }
}
