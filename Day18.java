package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class Day18 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day18.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        final List<SnailNumber> snailNumbers =
                input.stream().map(i -> parseSnailNumber(i).result).collect(Collectors.toList());

        snailNumbers.forEach(sn -> System.out.println(sn));
    }

    private static ParseKey parseSnailNumber(String input) {
        String nextChar = input.substring(0, 1);
        input = input.substring(1);

        final SnailNumber number;

        if (nextChar.equals("[")) {
            ParseKey nextResult = parseSnailNumber(input);
            final SnailNumber left = nextResult.result;
            input = nextResult.remainder;

            if (input.isEmpty()) {
                if (!(left instanceof Atom)) {
                    throw new IllegalStateException();
                }

                return new ParseKey(left, input);
            }

            nextResult = parseSnailNumber(input);
            final SnailNumber right = nextResult.result;
            input = nextResult.remainder;

            number = new Pair(left, right);
        } else {
            final long value = Long.parseLong(nextChar);
            number = new Atom(value);
        }

        input = consumeTrailingCharacters(input);
        return new ParseKey(number, input);
    }

    private static String consumeTrailingCharacters(String input) {
        String nextChar;

        while (!input.isEmpty()) {
            nextChar = input.substring(0, 1);

            if (!nextChar.equals("]") && !nextChar.equals(",")) {
                break;
            }

            input = input.substring(1);
        }

        return input;
    }

    private interface SnailNumber {
        long getMagnitude();
    }

    private static class Atom implements SnailNumber {
        long value;

        public Atom(final long value) {
            this.value = value;
        }

        @Override
        public long getMagnitude() {
            return value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }
    }

    private static class Pair implements SnailNumber {
        private SnailNumber left;
        private SnailNumber right;

        private Pair(final SnailNumber left, final SnailNumber right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public long getMagnitude() {
            return 3 * left.getMagnitude() + 2 * right.getMagnitude();
        }

        @Override
        public String toString() {
            return "[" + left + "," + right + "]";
        }
    }

    private static class ParseKey {
        private final SnailNumber result;
        private final String remainder;

        private ParseKey(final SnailNumber result, final String remainder) {
            this.result = result;
            this.remainder = remainder;
        }
    }
}
