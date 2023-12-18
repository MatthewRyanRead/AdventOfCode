package tech.readonly.aoc.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day10 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day10.txt").getFile()))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        long score1 = 0L;
        final List<List<Byte>> incomplete2 = new ArrayList<>();

        for (final String line : input) {
            final List<Byte> stack = new ArrayList<>();

            for (final byte symbol : line.getBytes()) {
                final int lastIndex = stack.size() - 1;

                switch (symbol) {
                    case '(':
                    case '[':
                    case '{':
                    case '<':
                        stack.add(symbol);
                        break;
                    case ')':
                        if (stack.get(lastIndex) != '(') {
                            score1 += 3L;
                            break;
                        }
                        stack.remove(lastIndex);
                        break;
                    case ']':
                        if (stack.get(lastIndex) != '[') {
                            score1 += 57L;
                            break;
                        }
                        stack.remove(lastIndex);
                        break;
                    case '}':
                        if (stack.get(lastIndex) != '{') {
                            score1 += 1197L;
                            break;
                        }
                        stack.remove(lastIndex);
                        break;
                    case '>':
                        if (stack.get(lastIndex) != '<') {
                            score1 += 25137L;
                            break;
                        }
                        stack.remove(lastIndex);
                        break;
                    default:
                        throw new IllegalStateException();
                }

                // no change -- nothing added or removed due to error
                if (stack.size() == lastIndex + 1) {
                    stack.clear();
                    break;
                }
            }

            if (!stack.isEmpty()) {
                incomplete2.add(stack);
            }
        }

        System.out.println("Part 1: " + score1);

        List<Long> scores2 = new ArrayList<>();

        for (final List<Byte> line : incomplete2) {
            long score = 0L;

            for (int i = line.size() - 1; i >= 0; i--) {
                score *= 5;
                switch (line.get(i)) {
                    case '(':
                        score += 1L;
                        break;
                    case '[':
                        score += 2L;
                        break;
                    case '{':
                        score += 3L;
                        break;
                    case '<':
                        score += 4L;
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            scores2.add(score);
        }

        scores2 = scores2.stream().sorted().collect(Collectors.toList());
        if (scores2.size() % 2 == 0) {
            throw new IllegalStateException();
        }

        System.out.println("Part 2: " + scores2.get(scores2.size() / 2));
    }
}
