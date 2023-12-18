package tech.readonly.aoc.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * This solution is the hottest of garbage 😂
 */
public class Day3 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day3.txt").getFile()))) {
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
        }

        final int[] counts = new int[input.get(0).length()];

        for (int i = 0; i < input.get(0).length(); i++) {
            counts[i] = getCount(input, i);
        }

        int gamma = 0;
        int epsilon = 0;

        for (int i = input.get(0).length() - 1; i >= 0; i--) {
            gamma = gamma << 1;
            epsilon = epsilon << 1;

            if (counts[i] == 0) {
                throw new IllegalStateException();
            }

            if (counts[i] > 0) {
                gamma += 1;
            } else {
                epsilon += 1;
            }
        }

        System.out.printf("Part 1: %d%n", epsilon * gamma);

        List<String> oxyGenRatings = input;
        List<String> co2ScrubRatings = input;

        String finalOGR = "";
        String finalCSR = "";

        for (int i = 0; i < input.get(0).length(); i++) {
            if (oxyGenRatings.size() == 1) {
                finalOGR = oxyGenRatings.get(0);
                oxyGenRatings.clear();
            }
            if (co2ScrubRatings.size() == 1) {
                finalCSR = co2ScrubRatings.get(0);
                co2ScrubRatings.clear();
            }

            final int index = i;

            if (getCount(oxyGenRatings, index) >= 0) {
                oxyGenRatings =
                        oxyGenRatings.stream()
                                .filter(s -> s.getBytes()[index] == '1')
                                .collect(Collectors.toList());
            } else {
                oxyGenRatings =
                        oxyGenRatings.stream()
                                .filter(s -> s.getBytes()[index] == '0')
                                .collect(Collectors.toList());
            }

            if (getCount(co2ScrubRatings, index) < 0) {
                co2ScrubRatings =
                        co2ScrubRatings.stream()
                                .filter(s -> s.getBytes()[index] == '1')
                                .collect(Collectors.toList());
            } else {
                co2ScrubRatings =
                        co2ScrubRatings.stream()
                                .filter(s -> s.getBytes()[index] == '0')
                                .collect(Collectors.toList());
            }
        }

        if (oxyGenRatings.size() > 1 || co2ScrubRatings.size() > 1) {
            throw new IllegalStateException();
        }

        if (oxyGenRatings.size() == 1) {
            finalOGR = oxyGenRatings.get(0);
        }
        if (co2ScrubRatings.size() == 1) {
            finalCSR = co2ScrubRatings.get(0);
        }

        int ogr = Integer.parseInt(finalOGR, 2);
        int csr = Integer.parseInt(finalCSR, 2);

        System.out.printf("Part 2: %d%n", ogr * csr);
    }

    private static int getCount(final List<String> input, final int index) {
        int count = 0;

        for (final String line : input) {
            if (line.getBytes()[index] == '1') {
                count++;
            } else {
                count--;
            }
        }

        return count;
    }
}
