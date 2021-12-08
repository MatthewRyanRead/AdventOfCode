package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day8 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day8.txt"))) {
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
        }

        // I have a good guess as to what part 2 will be (decipher the entire message), but I'm not putting money on
        // that since this puzzle is easily the worst written so far. Doing part 1 quick and dirty, then we'll see.

        int count1 = input.stream()
                .map(line -> line.split(" \\| ")[1])
                .flatMap(numbers -> Arrays.stream(numbers.split(" ")))
                .map(
                        number -> {
                            switch (number.length()) {
                                case 2:
                                case 3:
                                case 4:
                                case 7:
                                    return 1;
                                default:
                                    return 0;
                            }
                        })
                .reduce(Integer::sum)
                .orElseThrow();

        System.out.println("Part 1: " + count1);

        // ok yep, it's still completely unclear what the "output" values are supposed to be or why
        // the letters are in a different order compared to the digits, but whatever it's solvable

        // this is massively janky, but it works ¯\_(ツ)_/¯

        final Map<String, String> digitByOrderedSegments = new HashMap<>();
        digitByOrderedSegments.put("abcefg", "0");
        digitByOrderedSegments.put("cf", "1");
        digitByOrderedSegments.put("acdeg", "2");
        digitByOrderedSegments.put("acdfg", "3");
        digitByOrderedSegments.put("bcdf", "4");
        digitByOrderedSegments.put("abdfg", "5");
        digitByOrderedSegments.put("abdefg", "6");
        digitByOrderedSegments.put("acf", "7");
        digitByOrderedSegments.put("abcdefg", "8");
        digitByOrderedSegments.put("abcdfg", "9");

        final List<Integer> results = new ArrayList<>();

        for (final String line : input) {
            final String[] parts = line.split(" \\| ");
            final List<String> inputs = splitAndSort(parts[0]);
            final List<String> outputs = splitAndSort(parts[1]);

            final Map<String, String> wireBySegment = new HashMap<>();

            final String digit1 = inputs.stream().filter(digit -> digit.length() == 2).findFirst().orElseThrow();
            final List<String> digit1Chars = Arrays.stream(digit1.split("")).collect(Collectors.toList());
            // 0,6,9 have 6 segments; 0 and 9 have both c and f, 6 has only f
            int count = (int) Stream.of(digit1Chars.get(0))
                    .flatMap(cOrF -> inputs.stream().filter(digit -> digit.length() == 6 && digit.contains(cOrF)))
                    .count();
            wireBySegment.put(digit1Chars.get(count % 2), "c");
            wireBySegment.put(digit1Chars.get((count + 1) % 2), "f");

            final String digit7 = inputs.stream().filter(digit -> digit.length() == 3).findFirst().orElseThrow();
            final List<String> digit7Chars = Arrays.stream(digit7.split("")).collect(Collectors.toList());
            digit7Chars.removeAll(digit1Chars);
            wireBySegment.put(digit7Chars.get(0), "a");

            final String digit4 = inputs.stream().filter(digit -> digit.length() == 4).findFirst().orElseThrow();
            final List<String> digit4Chars = Arrays.stream(digit4.split("")).collect(Collectors.toList());
            digit4Chars.removeAll(digit1Chars);
            // 2,3,5 have 5 segments; only 5 has b
            count = (int) Stream.of(digit4Chars.get(0))
                    .flatMap(bOrD -> inputs.stream().filter(digit -> digit.length() == 5 && digit.contains(bOrD)))
                    .count();
            wireBySegment.put(digit4Chars.get((count - 1) / 2), "b");
            wireBySegment.put(digit4Chars.get(count % 3), "d");

            final String digit8 = inputs.stream().filter(d -> d.length() == 7).findFirst().orElseThrow();
            final List<String> digit8Chars = Arrays.stream(digit8.split("")).collect(Collectors.toList());
            digit8Chars.removeAll(digit1Chars);
            digit8Chars.removeAll(digit7Chars);
            digit8Chars.removeAll(digit4Chars);
            // 2,3,5 have 5 segments; only 2 has e
            count = (int) Stream.of(digit8Chars.get(0))
                    .flatMap(eOrG -> inputs.stream().filter(d -> d.length() == 5 && d.contains(eOrG)))
                    .count();
            wireBySegment.put(digit8Chars.get((count - 1) / 2), "e");
            wireBySegment.put(digit8Chars.get(count % 3), "g");

            results.add(Integer.parseInt(outputs.stream()
                    .map(o -> Pair.of(o, Arrays.stream(o.split(""))
                            .map(wireBySegment::get)
                            .sorted()
                            .collect(Collectors.joining())))
                    .map(p -> digitByOrderedSegments.get(p.snd))
                    .collect(Collectors.joining())));
        }

        System.out.println("Part 2: " + results.stream().reduce(Integer::sum).orElseThrow());
    }

    private static List<String> splitAndSort(final String tokens) {
        return Arrays.stream(tokens.split(" "))
                .map(digit -> {
                    final byte[] bytes = digit.getBytes();
                    Arrays.sort(bytes);
                    return new String(bytes);
                })
                .collect(Collectors.toList());
    }
}
