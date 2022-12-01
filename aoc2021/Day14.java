package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day14 {
    public static void main(final String[] args) throws Exception {
        String initialPolymer = "";
        final Map<String, String> insertionMappings = new HashMap<>();

        try (final Scanner scanner = new Scanner(new File("inputs/day14.txt"))) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                final String[] parts = line.split(" -> ");

                if (parts.length == 1) {
                    initialPolymer = line;
                } else {
                    insertionMappings.put(parts[0], parts[1]);
                }
            }
        }

        String polymer1 = initialPolymer;

        // naive approach

        for (int i = 0; i < 10; i++) {
            final String[] elements = polymer1.split("");
            final StringBuilder nextPoly = new StringBuilder();

            for (int j = 0; j < elements.length - 1; j++) {
                final String elementPair = elements[j] + elements[j + 1];
                nextPoly.append(elements[j])
                        .append(insertionMappings.getOrDefault(elementPair, ""));
            }

            nextPoly.append(elements[elements.length - 1]);
            polymer1 = nextPoly.toString();
        }

        final Map<String, Long> countsByElement1 = new HashMap<>();
        Arrays.stream(polymer1.split(""))
                .forEach(e -> countsByElement1.put(e, countsByElement1.getOrDefault(e, 0L) + 1L));
        final List<Pair<String, Long>> incidenceOrderedElements1 =
                countsByElement1.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                        .sorted(Comparator.comparing(p -> p.snd))
                        .collect(Collectors.toList());

        final Pair<String, Long> leastElement1 = incidenceOrderedElements1.get(0);
        final Pair<String, Long> mostElement1 =
                incidenceOrderedElements1.get(incidenceOrderedElements1.size() - 1);

        System.out.println("Part 1: " + (mostElement1.snd - leastElement1.snd));

        // idea: just track counts of pairs; each pair deterministically results in itself or one
        // other -- and the order of pair processing within each step does not matter.

        final Map<Bond, Long> countsByElementPair2 = new HashMap<>();
        final Map<String, Long> countsByElement2 = new HashMap<>();
        final String[] elements = initialPolymer.split("");

        for (int j = 0; j < elements.length - 1; j++) {
            final Bond bond = new Bond(elements[j], elements[j + 1]);

            countsByElementPair2.put(bond, countsByElementPair2.getOrDefault(bond, 0L) + 1L);
            countsByElement2.put(
                    elements[j + 1], countsByElement2.getOrDefault(elements[j + 1], 0L) + 1L);
        }
        countsByElement2.put(elements[0], countsByElement2.getOrDefault(elements[0], 0L) + 1L);

        for (int i = 0; i < 40; i++) {
            final Map<Bond, Long> countsToAdd = new HashMap<>();
            final Map<Bond, Long> countsToSubtract = new HashMap<>();

            for (final Entry<Bond, Long> entry : countsByElementPair2.entrySet()) {
                final Bond elementPair = entry.getKey();
                final String elementToInsert = insertionMappings.get(elementPair.string);

                if (elementToInsert != null) {
                    final Long bondCount = entry.getValue();

                    final Long elemCount = countsByElement2.getOrDefault(elementToInsert, 0L);
                    countsByElement2.put(elementToInsert, elemCount + bondCount);

                    final Bond newBond1 = new Bond(elementPair.leftElement, elementToInsert);
                    final Bond newBond2 = new Bond(elementToInsert, elementPair.rightElement);
                    countsToAdd.put(newBond1, countsToAdd.getOrDefault(newBond1, 0L) + bondCount);
                    countsToAdd.put(newBond2, countsToAdd.getOrDefault(newBond2, 0L) + bondCount);

                    countsToSubtract.put(
                            elementPair,
                            countsToSubtract.getOrDefault(elementPair, 0L) + bondCount);
                }
            }

            countsToAdd.forEach(
                    (bond, count) ->
                            countsByElementPair2.put(
                                    bond, countsByElementPair2.getOrDefault(bond, 0L) + count));
            countsToSubtract.forEach(
                    (bond, count) ->
                            countsByElementPair2.put(
                                    bond, countsByElementPair2.getOrDefault(bond, 0L) - count));
            countsToAdd.clear();
            countsToSubtract.clear();
        }

        final List<Pair<String, Long>> incidenceOrderedElements2 =
                countsByElement2.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue()))
                        .sorted(Comparator.comparing(p -> p.snd))
                        .collect(Collectors.toList());
        final Pair<String, Long> leastElement2 = incidenceOrderedElements2.get(0);
        final Pair<String, Long> mostElement2 =
                incidenceOrderedElements2.get(incidenceOrderedElements2.size() - 1);

        System.out.println("Part 2: " + (mostElement2.snd - leastElement2.snd));
    }

    private static final class Bond {
        private final String leftElement;
        private final String rightElement;
        private final String string;

        public Bond(final String leftElement, final String rightElement) {
            this.leftElement = leftElement;
            this.rightElement = rightElement;
            this.string = leftElement + rightElement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftElement, rightElement);
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Bond)) return false;

            final Bond other = (Bond) obj;

            return leftElement.equals(other.leftElement) && rightElement.equals(other.rightElement);
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
