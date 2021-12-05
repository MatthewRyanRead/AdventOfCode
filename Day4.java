package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day4 {
    public static class Board {
        private final Integer[][] squares = new Integer[5][5];
        private final Map<Integer, Pair<Integer, Integer>> coordsByVal = new HashMap<>();
        private final int[] scores = new int[10];
        private boolean done = false;

        public Board(final String[] lines) {
            for (int i = 0; i < 5; i++) {
                final Integer[] vals =
                        Arrays.stream(lines[i].split(" +"))
                                .map(Integer::parseInt)
                                .toArray(Integer[]::new);
                squares[i] = vals;
                for (int j = 0; j < 5; j++) {
                    coordsByVal.put(vals[j], Pair.of(i, j));
                }
            }
        }

        public boolean valCalled(final int val) {
            final Pair<Integer, Integer> coords = coordsByVal.get(val);
            if (coords == null || done) {
                return false;
            }

            squares[coords.fst][coords.snd] = -1;
            scores[coords.fst] = scores[coords.fst] + 1;
            scores[coords.snd + 5] = scores[coords.snd + 5] + 1;

            return scores[coords.fst] == 5 || scores[coords.snd + 5] == 5;
        }

        public int calcFinalScore(final int multiplier) {
            done = true;
            return multiplier
                    * Arrays.stream(squares)
                            .flatMap(Arrays::stream)
                            .filter(val -> val != -1)
                            .reduce(0, Integer::sum);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Board.class.getSimpleName() + "[", "]")
                    .add("squares=" + Arrays.deepToString(squares))
                    .add("coordsByVal=" + coordsByVal)
                    .toString();
        }
    }

    private static final Pattern BOARD_LINE_PATTERN = Pattern.compile("^([0-9]+ +){4}[0-9]+$");

    public static void main(final String[] args) throws Exception {
        final Scanner scanner = new Scanner(new FileInputStream("inputs/day4.txt"));
        final String numbersStr = scanner.nextLine();
        final List<Integer> numbers =
                Arrays.stream(numbersStr.split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

        final List<Board> boards = new ArrayList<>();
        final String[] currBoard = new String[5];
        int lineNum = 0;

        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine().trim();
            if (BOARD_LINE_PATTERN.matcher(line).matches()) {
                currBoard[lineNum++] = line;
            }

            if (lineNum == 5) {
                boards.add(new Board(currBoard));
                lineNum = 0;
            }
        }

        System.out.println("First value printed is Part 1's answer; last value is Part 2's");
        for (final Integer numberCalled : numbers) {
            for (final Board board : boards) {
                if (board.valCalled(numberCalled)) {
                    System.out.println(board.calcFinalScore(numberCalled));
                }
            }
        }
    }
}
