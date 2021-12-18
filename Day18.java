package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static tech.readonly.aoc2021.Day18.SnailNumber.Type.ATOM;
import static tech.readonly.aoc2021.Day18.SnailNumber.Type.PAIR;

public class Day18 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day18.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        final LinkedList<SnailNumber> snailNumbers =
                input.stream()
                        .map(i -> parseSnailNumber(i, 0).result)
                        .collect(Collectors.toCollection(LinkedList::new));
        final LinkedList<LinkedList<SnailNumber>> atomsLToR =
                snailNumbers.stream()
                        .map(sn -> sn.dfs(new LinkedList<>()))
                        .collect(Collectors.toCollection(LinkedList::new));

        while (snailNumbers.size() > 1) {
            final SnailNumber toAddLeft = snailNumbers.remove(0);
            toAddLeft.incrementDepth();
            final SnailNumber toAddRight = snailNumbers.remove(0);
            toAddRight.incrementDepth();

            final SnailNumber added = new SnailNumber(toAddLeft, toAddRight, 0);
            toAddLeft.parent = added;
            toAddRight.parent = added;
            snailNumbers.add(0, added);

            LinkedList<SnailNumber> lToR = atomsLToR.get(0);
            final LinkedList<SnailNumber> newRightHalf = atomsLToR.remove(1);
            lToR.addAll(newRightHalf);

            boolean changed;
            do {
                changed = false;
                LinkedList<SnailNumber> newOrder = new LinkedList<>();

                for (int i = 0; i < lToR.size(); i++) {
                    SnailNumber num = lToR.get(i);

                    if (num.depth >= 6) {
                        throw new IllegalStateException();
                    }
                    if (num.depth < 5) {
                        newOrder.add(num);
                        continue;
                    }

                    changed = true;

                    final SnailNumber toExplode = num.parent;
                    if (toExplode == null || toExplode.parent == null) {
                        throw new IllegalStateException();
                    }

                    final SnailNumber toAdd = new SnailNumber(0, toExplode.depth);
                    toAdd.parent = toExplode.parent;
                    if (toAdd.parent.left == toExplode) {
                        toAdd.parent.left = toAdd;
                    } else {
                        toAdd.parent.right = toAdd;
                    }

                    if (i > 0 && !newOrder.isEmpty()) {
                        SnailNumber prev = newOrder.get(newOrder.size() - 1);
                        if (prev == toExplode.left) {
                            newOrder.pop();
                            if (!newOrder.isEmpty()) {
                                prev = newOrder.get(newOrder.size() - 1);
                            } else {
                                prev = null;
                            }
                        }

                        if (prev != null) {
                            prev.value += toExplode.left.value;
                        }
                    }
                    if (i < lToR.size() - 1) {
                        int offset = 1;
                        SnailNumber next = lToR.get(i + offset);
                        if (next == toExplode.right) {
                            if (i < lToR.size() - 2) {
                                next = lToR.get(i + ++offset);
                            } else {
                                next = null;
                            }
                        }

                        if (next != null) {
                            next.value += toExplode.right.value;
                        }
                    }

                    newOrder.add(toAdd);
                    // we ran into the left atom first; skip the right one
                    i++;
                }

                lToR = newOrder;
                newOrder = new LinkedList<>();

                for (int i = 0; i < lToR.size(); i++) {
                    final SnailNumber num = lToR.get(i);
                    if (num.splitMe()) {
                        changed = true;
                        newOrder.add(num.left);
                        newOrder.add(num.right);

                        newOrder.addAll(lToR.subList(i + 1, lToR.size()));

                        break;
                    }

                    newOrder.add(num);
                }

                lToR = newOrder;
            } while (changed);

            atomsLToR.set(0, lToR);
        }

        System.out.println("Part 1: " + snailNumbers.get(0).getMagnitude());
    }

    private static ParseKey parseSnailNumber(String input, final int depth) {
        String nextChar = input.substring(0, 1);
        input = input.substring(1);

        final SnailNumber number;

        if (nextChar.equals("[")) {
            ParseKey nextResult = parseSnailNumber(input, depth + 1);
            final SnailNumber left = nextResult.result;
            input = nextResult.remainder;

            if (input.isEmpty()) {
                if (left.type != ATOM) {
                    throw new IllegalStateException();
                }

                return new ParseKey(left, input);
            }

            nextResult = parseSnailNumber(input, depth + 1);
            final SnailNumber right = nextResult.result;
            input = nextResult.remainder;

            number = new SnailNumber(left, right, depth);
            left.parent = number;
            right.parent = number;
        } else {
            final long value = Long.parseLong(nextChar);
            number = new SnailNumber(value, depth);
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

    static class SnailNumber {
        private Type type;
        private long value;
        private SnailNumber left;
        private SnailNumber right;
        private int depth;
        private SnailNumber parent;

        enum Type {
            ATOM,
            PAIR,
            PRUNE_ME,
            ;
        }

        private SnailNumber(final long value, final int depth) {
            this.value = value;
            this.depth = depth;
            this.type = ATOM;
        }

        private SnailNumber(final SnailNumber left, final SnailNumber right, final int depth) {
            this.left = left;
            this.right = right;
            this.depth = depth;
            this.type = PAIR;
        }

        private LinkedList<SnailNumber> dfs(final LinkedList<SnailNumber> lToR) {
            if (this.type == ATOM) {
                lToR.add(this);
                return lToR;
            }

            return right.dfs(left.dfs(lToR));
        }

        private long getMagnitude() {
            if (this.type == ATOM) {
                return value;
            }
            return 3 * left.getMagnitude() + 2 * right.getMagnitude();
        }

        private boolean splitMe() {
            if (this.type != ATOM || this.parent == null) {
                throw new IllegalStateException();
            }
            if (this.value < 10) {
                return false;
            }

            this.left = new SnailNumber(this.value / 2, depth + 1);
            this.left.parent = this;
            this.right = new SnailNumber(this.value / 2 + (this.value % 2), depth + 1);
            this.right.parent = this;

            this.type = PAIR;
            this.value = 0;

            return true;
        }

        private void incrementDepth() {
            depth++;

            if (this.type != ATOM) {
                this.left.incrementDepth();
                this.right.incrementDepth();
            }
        }

        @Override
        public String toString() {

            if (type == ATOM) {
                return Long.toString(value);
            }
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
