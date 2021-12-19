package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day19 {
    public static void main(final String[] args) throws Exception {
        final List<Scanner> scanners = new ArrayList<>();

        try (final java.util.Scanner input =
                new java.util.Scanner(new File("inputs/samples/day19.txt"))) {
            Scanner currScanner = null;

            while (input.hasNextLine()) {
                final String line = input.nextLine().trim();

                if (line.isEmpty()) continue;

                if (line.startsWith("---")) {
                    currScanner = new Scanner(currScanner == null ? 0 : currScanner.id + 1);
                    scanners.add(currScanner);
                } else {
                    currScanner.beacons.add(new Beacon(line));
                }
            }
        }

        System.out.println(
                scanners.stream().map(Scanner::toString).collect(Collectors.joining("\n\n")));
    }

    private static class Scanner {
        final int id;
        final List<Beacon> beacons = new ArrayList<>();

        private Scanner(final int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return String.format(
                    "--- scanner %d ---%n%s",
                    id, beacons.stream().map(Beacon::toString).collect(Collectors.joining("\n")));
        }
    }

    private static class Beacon {
        final int x, y, z;
        final Transform transform;
        final Swap swap;

        private Beacon(final String string) {
            final int[] xyz =
                    Arrays.stream(string.split(",")).mapToInt(Integer::parseInt).toArray();

            x = xyz[0];
            y = xyz[1];
            z = xyz[2];
            transform = Transform.DEFAULT;
            swap = Swap.XYZ;
        }

        private Beacon(final Beacon beacon, final Transform transform, final Swap swap) {
            int[] xyz = beacon.swap.reverse(beacon.x, beacon.y, beacon.z);
            xyz = beacon.transform.reverse(xyz[0], xyz[1], xyz[2]);
            xyz = transform.transform(xyz[0], xyz[1], xyz[2]);
            xyz = swap.transform(xyz[0], xyz[1], xyz[2]);

            this.x = xyz[0];
            this.y = xyz[1];
            this.z = xyz[2];
            this.transform = transform;
            this.swap = swap;
        }

        @Override
        public String toString() {
            return String.format("%d,%d,%d", x, y, z);
        }

        private enum Transform {
            DEFAULT(new int[] {1, 1, 1}),
            NEGATE_X(new int[] {-1, 1, 1}),
            NEGATE_Y(new int[] {1, -1, 1}),
            NEGATE_Z(new int[] {1, 1, -1}),
            NEGATE_XY(new int[] {-1, -1, 1}),
            NEGATE_XZ(new int[] {-1, 1, -1}),
            NEGATE_YZ(new int[] {1, -1, -1}),
            NEGATE_XYZ(new int[] {-1, -1, -1});

            private static final Map<Integer, Transform> TRANSFORMS =
                    Arrays.stream(Transform.values())
                            .collect(Collectors.toMap(Enum::ordinal, Function.identity()));
            private final int[] matrix;

            Transform(final int[] matrix) {
                this.matrix = matrix;
            }

            private int[] transform(final int x, final int y, final int z) {
                return new int[] {x * matrix[0], y * matrix[1], z * matrix[2]};
            }

            private int[] reverse(final int x, final int y, final int z) {
                return transform(x, y, z);
            }
        }

        private enum Swap {
            XYZ(new int[] {0, 1, 2}),
            XZY(new int[] {0, 2, 1}),
            YXZ(new int[] {1, 0, 2}),
            YZX(new int[] {2, 0, 1}),
            ZXY(new int[] {1, 2, 0}),
            ZYX(new int[] {2, 1, 0});

            private static final Map<Integer, Swap> SWAPS =
                    Arrays.stream(Swap.values())
                            .collect(Collectors.toMap(Enum::ordinal, Function.identity()));
            private final int[] positions;

            Swap(final int[] positions) {
                this.positions = positions;
            }

            private int[] transform(final int x, final int y, final int z) {
                final int[] result = new int[3];
                result[positions[0]] = x;
                result[positions[1]] = y;
                result[positions[2]] = z;
                return result;
            }

            private int[] reverse(final int x, final int y, final int z) {
                int[] input = new int[] {x, y, z};
                int[] result = new int[3];

                for (int i = 0; i < 3; i++) {
                    result[i] = input[positions[i]];
                }

                return result;
            }
        }
    }
}
