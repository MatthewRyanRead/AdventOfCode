package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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

        snailNumbers.forEach(System.out::println);
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

        SnailNumber split(final int depth);

        SnailNumber explode(final int depth);
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
        public SnailNumber explode(final int depth) {
            return this;
        }

        @Override
        public SnailNumber split(final int depth) {
            if (value <= 9) return this;

            SnailNumber nextMe = new Pair(new Atom(value / 2), new Atom(value / 2 + (value % 2)));
            SnailNumber newMe;
            do {
                do {
                    newMe = nextMe;
                    nextMe = newMe.explode(depth);
                } while (nextMe != newMe);

                newMe = nextMe;
                nextMe = newMe.split(depth);
            } while (nextMe != newMe);

            return newMe;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }
    }

    // TODO: merge classes, pass in a "call stack", propagate an explode up the stack, set this.type
    // TODO: to PRUNE_ME, abort?
    // TODO: alternatively, maintain L -> R linkedlist

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
        public SnailNumber split(final int depth) {
            this.left = left.split(depth + 1);
            this.right = right.split(depth + 1);

            return this;
        }

        @Override
        public SnailNumber explode(final int depth) {
            if (depth > 4) throw new IllegalStateException();

            if (depth < 4) {
                this.left = left.explode(depth + 1);
            }
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
