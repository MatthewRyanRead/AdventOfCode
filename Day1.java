package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Day1 {
    public static void main(final String[] args) throws Exception {
        final List<Integer> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day1.txt"))) {
            while (scanner.hasNext()) {
                input.add(Integer.parseInt(scanner.nextLine()));
            }
        }

        int count1 = 0;
        int count2 = 0;

        for (int i = 1; i < input.size(); i++) {
            count1 += input.get(i) > input.get(i - 1) ? 1 : 0;
            if (i >= 3) {
                count2 += input.get(i ) > input.get(i - 3) ? 1 : 0;
            }
        }

        System.out.println("Part 1:" + count1);
        System.out.println("Part 1:" + count2);
    }
}
