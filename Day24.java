package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tech.readonly.aoc2021.Day24.Register.clearRegisters;
import static tech.readonly.aoc2021.Day24.Register.w;
import static tech.readonly.aoc2021.Day24.Register.x;
import static tech.readonly.aoc2021.Day24.Register.y;
import static tech.readonly.aoc2021.Day24.Register.z;

@SuppressWarnings("unused")
public class Day24 {
    public static final long MAX_GUESS = 99999999999999L;
    public static final long MIN_GUESS = 11111111111111L;

    public static void main(final String[] args) throws Exception {
        List<Statement<?>> test = readInput("inputs/samples/day24.txt");
        System.out.println("2 * -1 == " + runProgram(test, new int[] {2}, x));
        clearRegisters();
        System.out.println("-2 * -1 == " + runProgram(test, new int[] {-2}, x));
        clearRegisters();

        test = readInput("inputs/samples/day24_2.txt");
        System.out.println("(3 * 3 == 9) == " + (runProgram(test, new int[] {3, 9}, z) == 1));
        clearRegisters();
        System.out.println("(3 * -3 == 9) == " + (runProgram(test, new int[] {-3, 9}, z) == 1));
        clearRegisters();

        test = readInput("inputs/samples/day24_3.txt");
        runProgram(test, new int[] {15});
        System.out.println("15 == 0b" + w.get() + x.get() + y.get() + z.get());
        clearRegisters();
        runProgram(test, new int[] {10});
        System.out.println("10 == 0b" + w.get() + x.get() + y.get() + z.get());
        clearRegisters();

        final List<Statement<?>> monad = readInput("inputs/day24.txt");
        System.out.println("Part 1: " + part1(monad));
    }

    public static List<Statement<?>> readInput(final String fileName) throws Exception {
        try (final Scanner scanner = new Scanner(new File(fileName))) {
            final List<Statement<?>> program = new ArrayList<>();

            while (scanner.hasNextLine()) {
                final String[] parts = scanner.nextLine().trim().split(" ");

                final Instruction inst = Instruction.valueOf(parts[0]);
                final Register reg = Register.valueOf(parts[1]);

                if (parts.length < 3) {
                    program.add(new Statement<>(inst, reg, null));
                } else if (parts[2].matches("[wxyz]")) {
                    program.add(new Statement<>(inst, reg, Register.valueOf(parts[2])));
                } else {
                    program.add(new Statement<>(inst, reg, Integer.parseInt(parts[2])));
                }
            }

            return List.copyOf(program);
        }
    }

    public static long part1(final List<Statement<?>> monad) {
        final Deque<Pair<Long, Long>> guesses = new ArrayDeque<>();
        guesses.add(
                Pair.of(MIN_GUESS + (MAX_GUESS - MIN_GUESS) / 2L, (MAX_GUESS - MIN_GUESS) / 2L));
        long maxValidGuess = MIN_GUESS;

        while (!guesses.isEmpty()) {
            final Pair<Long, Long> guessPair = guesses.removeLast();
            if (guessPair.snd == 0L) {
                continue;
            }

            final long guess = clampGuess(guessPair.fst);
            final long nextOffset = (guessPair.snd + ((guess - guessPair.fst) / 2L)) / 2L;

            if (guess < maxValidGuess) {
                guesses.addFirst(Pair.of(guess + nextOffset, nextOffset));
                continue;
            }

            final boolean validGuess = runProgram(monad, Long.toString(guess), z) == 0;
            guesses.addFirst(Pair.of(guess + nextOffset, nextOffset));
            if (validGuess) {
                maxValidGuess = guess;
            } else {
                guesses.addFirst(Pair.of(guess - nextOffset, nextOffset));
            }
        }

        return maxValidGuess;
    }

    public static long clampGuess(final long guess) {
        String guessStr = Long.toString(guess);
        int i;
        if ((i = guessStr.indexOf("0")) > -1) {
            guessStr =
                    guessStr.substring(0, i)
                            + "1"
                            + IntStream.range(i + 1, guessStr.length())
                                    .mapToObj(j -> "0")
                                    .collect(Collectors.joining());
            return Long.parseLong(guessStr);
        }

        return guess;
    }

    public static int runProgram(
            final List<Statement<?>> statements, final int[] input, final Register outputRegister) {
        //noinspection ConstantConditions
        return runProgramImpl(statements, input, outputRegister);
    }

    public static int runProgram(
            final List<Statement<?>> statements,
            final String input,
            final Register outputRegister) {
        final int[] inputNums = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            inputNums[i] = Integer.parseInt(input.substring(i, i + 1));
        }

        //noinspection ConstantConditions
        return runProgramImpl(statements, inputNums, outputRegister);
    }

    public static void runProgram(final List<Statement<?>> statements, final int[] input) {
        runProgramImpl(statements, input, null);
    }

    @Nullable
    private static Integer runProgramImpl(
            final List<Statement<?>> statements,
            final int[] input,
            @Nullable final Register outputRegister) {
        int inputIndex = 0;

        for (final Statement<?> statement : statements) {
            final Instruction inst = statement.inst;
            final Object val;

            if (inst == Instruction.inp) {
                val = input[inputIndex++];
                inst.apply(statement.reg, (int) val);
            } else {
                val = statement.val;
                if (val instanceof Register) {
                    inst.apply(statement.reg, (Register) val);
                } else {
                    inst.apply(statement.reg, (int) val);
                }
            }
        }

        return outputRegister == null ? null : outputRegister.get();
    }

    public enum Instruction {
        inp {
            @Override
            public void apply(final Register register, final int val) {
                register.set(val);
            }
        },
        add {
            @Override
            public void apply(final Register register, final int val) {
                register.set(register.get() + val);
            }

            @Override
            public void apply(final Register one, final Register two) {
                this.apply(one, two.get());
            }
        },
        mul {
            @Override
            public void apply(final Register register, final int val) {
                register.set(register.get() * val);
            }

            @Override
            public void apply(final Register one, final Register two) {
                this.apply(one, two.get());
            }
        },
        div {
            @Override
            public void apply(final Register register, final int val) {
                register.set(register.get() / val);
            }

            @Override
            public void apply(final Register one, final Register two) {
                this.apply(one, two.get());
            }
        },
        mod {
            @Override
            public void apply(final Register register, final int val) {
                register.set(register.get() % val);
            }

            @Override
            public void apply(final Register one, final Register two) {
                this.apply(one, two.get());
            }
        },
        eql {
            @Override
            public void apply(final Register register, final int val) {
                register.set(register.get() == val ? 1 : 0);
            }

            @Override
            public void apply(final Register one, final Register two) {
                this.apply(one, two.get());
            }
        };

        public void apply(final Register register, final int val) {
            throw new UnsupportedOperationException();
        }

        public void apply(final Register one, final Register two) {
            throw new UnsupportedOperationException();
        }
    }

    public enum Register {
        w,
        x,
        y,
        z;

        private static final int[] REGISTERS = new int[4];

        public static void clearRegisters() {
            Arrays.stream(values()).forEach(r -> r.set(0));
        }

        public int get() {
            return REGISTERS[this.ordinal()];
        }

        public void set(final int val) {
            REGISTERS[this.ordinal()] = val;
        }
    }

    public static class Statement<T> {
        public final Instruction inst;
        public final Register reg;
        public final T val;

        public Statement(final Instruction inst, final Register reg, final T val) {
            this.inst = inst;
            this.reg = reg;
            this.val = val;
        }

        @Override
        public String toString() {
            if (val == null) {
                return inst.name() + " " + reg.name();
            }

            return inst.name()
                    + " "
                    + reg.name()
                    + " "
                    + (val instanceof Register ? ((Register) val).name() : val);
        }
    }
}
