package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day24 {
    public static void main(final String[] args) throws Exception {
        final List<Statement<?>> statements = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day24.txt"))) {
            while (scanner.hasNextLine()) {
                final String[] parts = scanner.nextLine().trim().split(" ");

                final Instruction inst = Instruction.valueOf(parts[0]);
                final Register reg = Register.valueOf(parts[1]);

                if (parts.length < 3) {
                    statements.add(new Statement<>(inst, reg, null));
                } else if (parts[2].matches("[wxyz]")) {
                    statements.add(new Statement<>(inst, reg, Register.valueOf(parts[2])));
                } else {
                    statements.add(new Statement<>(inst, reg, Integer.parseInt(parts[2])));
                }
            }
        }

        System.out.println(
                statements.stream().map(Statement::toString).collect(Collectors.joining("\n")));
    }

    public enum Instruction {
        inp,
        add,
        mul,
        div,
        mod,
        eql;
    }

    public enum Register {
        w,
        x,
        y,
        z;
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
