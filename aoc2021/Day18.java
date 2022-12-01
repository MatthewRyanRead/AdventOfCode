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

        part1(input);
        part2(input);
    }

    private static void part1(final List<String> input) {
        final LinkedList<SnailNumber> snailNumbers = parseSnailNumbers(input);
        final LinkedList<LinkedList<SnailNumber>> atomsLToR1 =
                snailNumbers.stream()
                        .map(sn -> sn.dfs(new LinkedList<>()))
                        .collect(Collectors.toCollection(LinkedList::new));

        final SnailNumber result = doSnailAddition(snailNumbers, atomsLToR1);
        System.out.println("Part 1: " + result.getMagnitude());
    }

    private static void part2(final List<String> input) {
        final LinkedList<SnailNumber> snailNumbers = parseSnailNumbers(input);

        SnailNumber maxNum = new SnailNumber(-1, 0);
        for (int i = 0; i < snailNumbers.size(); i++) {
            for (int j = i + 1; j < snailNumbers.size(); j++) {
                final SnailNumber numI = snailNumbers.get(i);
                final SnailNumber numJ = snailNumbers.get(j);

                // i then j
                SnailNumber result = doSnailAddition(numI.clone(), numJ.clone());
                if (result.getMagnitude() > maxNum.getMagnitude()) {
                    maxNum = result;
                }

                // j then i
                result = doSnailAddition(numJ.clone(), numI.clone());
                if (result.getMagnitude() > maxNum.getMagnitude()) {
                    maxNum = result;
                }
            }
        }

        System.out.println("Part 2: " + maxNum.getMagnitude());
    }

    private static LinkedList<SnailNumber> parseSnailNumbers(final List<String> input) {
        return input.stream()
                .map(i -> parseSnailNumber(i, 0).result)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static ParseKey parseSnailNumber(String input, final int depth) {
        final String nextChar = input.substring(0, 1);
        input = input.substring(1);

        final SnailNumber number;

        if (nextChar.equals("[")) {
            ParseKey nextResult = parseSnailNumber(input, depth + 1);
            final SnailNumber left = nextResult.result;
            input = nextResult.remainder;

            if (input.isEmpty()) {
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

    private static SnailNumber doSnailAddition(
            final SnailNumber firstNum, final SnailNumber secondNum) {
        final LinkedList<SnailNumber> kyloRenEtc = firstNum.dfs(new LinkedList<>());
        final LinkedList<SnailNumber> secondOrder = secondNum.dfs(new LinkedList<>());

        final LinkedList<SnailNumber> toAdd = new LinkedList<>();
        toAdd.add(firstNum);
        toAdd.add(secondNum);
        final LinkedList<LinkedList<SnailNumber>> orders = new LinkedList<>();
        orders.add(kyloRenEtc);
        orders.add(secondOrder);

        return doSnailAddition(toAdd, orders);
    }

    private static SnailNumber doSnailAddition(
            final LinkedList<SnailNumber> snailNumbersToAdd,
            final LinkedList<LinkedList<SnailNumber>> atomsLToR) {
        while (snailNumbersToAdd.size() > 1) {
            final SnailNumber toAddLeft = snailNumbersToAdd.remove(0);
            toAddLeft.incrementDepth();
            final SnailNumber toAddRight = snailNumbersToAdd.remove(0);
            toAddRight.incrementDepth();

            final SnailNumber added = new SnailNumber(toAddLeft, toAddRight, 0);
            toAddLeft.parent = added;
            toAddRight.parent = added;
            snailNumbersToAdd.add(0, added);

            LinkedList<SnailNumber> order = atomsLToR.get(0);
            final LinkedList<SnailNumber> newRightHalf = atomsLToR.remove(1);
            order.addAll(newRightHalf);

            atomsLToR.set(0, reduceSnailNumber(order));
        }

        return snailNumbersToAdd.get(0);
    }

    private static LinkedList<SnailNumber> reduceSnailNumber(final LinkedList<SnailNumber> order) {
        boolean changed;
        do {
            LinkedList<SnailNumber> intermediateOrder = new LinkedList<>();
            changed = explode(order, intermediateOrder);

            order.clear();

            changed |= split(intermediateOrder, order);
        } while (changed);

        return order;
    }

    private static boolean explode(
            final LinkedList<SnailNumber> originalOrder, final LinkedList<SnailNumber> newOrder) {
        boolean changed = false;
        // explode loop
        for (int i = 0; i < originalOrder.size(); i++) {
            final SnailNumber num = originalOrder.get(i);

            if (num.depth < 5) {
                newOrder.add(num);
                continue;
            }

            changed = true;
            newOrder.add(explode(num.parent, originalOrder, newOrder, i++));
        }

        return changed;
    }

    private static SnailNumber explode(
            final SnailNumber toExplode,
            final LinkedList<SnailNumber> remainingOrder,
            final LinkedList<SnailNumber> orderSoFar,
            final int index) {
        if (index > 0 && !orderSoFar.isEmpty()) {
            final SnailNumber prev = orderSoFar.get(orderSoFar.size() - 1);
            prev.value += toExplode.left.value;
        }
        if (index < remainingOrder.size() - 2) {
            // we ran into the left atom first; skip the right one
            final SnailNumber next = remainingOrder.get(index + 2);
            next.value += toExplode.right.value;
        }

        final SnailNumber replacementZero = new SnailNumber(0, toExplode.depth);
        replacementZero.parent = toExplode.parent;
        if (replacementZero.parent.left == toExplode) {
            replacementZero.parent.left = replacementZero;
        } else {
            replacementZero.parent.right = replacementZero;
        }

        return replacementZero;
    }

    private static boolean split(
            final LinkedList<SnailNumber> order, final LinkedList<SnailNumber> finalOrder) {
        for (int i = 0; i < order.size(); i++) {
            final SnailNumber num = order.get(i);
            if (num.splitMe()) {
                finalOrder.add(num.left);
                finalOrder.add(num.right);

                finalOrder.addAll(order.subList(i + 1, order.size()));
                return true;
            }

            finalOrder.add(num);
        }

        return false;
    }

    static class SnailNumber implements Cloneable {
        private Type type;
        private long value;
        private SnailNumber left;
        private SnailNumber right;
        private int depth;
        private SnailNumber parent;

        enum Type {
            ATOM,
            PAIR
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

            return this.right.dfs(this.left.dfs(lToR));
        }

        private long getMagnitude() {
            if (this.type == ATOM) {
                return this.value;
            }
            return 3 * this.left.getMagnitude() + 2 * this.right.getMagnitude();
        }

        private boolean splitMe() {
            if (this.value < 10) {
                return false;
            }

            this.left = new SnailNumber(this.value / 2, this.depth + 1);
            this.left.parent = this;
            this.right = new SnailNumber(this.value / 2 + (this.value % 2), this.depth + 1);
            this.right.parent = this;

            this.type = PAIR;
            this.value = 0;

            return true;
        }

        private void incrementDepth() {
            this.depth++;

            if (this.type != ATOM) {
                this.left.incrementDepth();
                this.right.incrementDepth();
            }
        }

        @Override
        public String toString() {

            if (this.type == ATOM) {
                return Long.toString(this.value);
            }
            return "[" + this.left + "," + this.right + "]";
        }

        @SuppressWarnings({
            "CloneDoesntDeclareCloneNotSupportedException",
            "MethodDoesntCallSuperMethod"
        })
        @Override
        protected SnailNumber clone() {
            if (this.type == ATOM) {
                return new SnailNumber(this.value, this.depth);
            }

            final SnailNumber newLeft = this.left.clone();
            final SnailNumber newRight = this.right.clone();
            final SnailNumber newMe = new SnailNumber(newLeft, newRight, this.depth);
            newLeft.parent = newMe;
            newRight.parent = newMe;

            return newMe;
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
