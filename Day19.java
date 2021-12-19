package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day19 {
    public static void main(final String[] args) throws Exception {
        final Set<Scanner> scanners = new HashSet<>();

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

        scanners.forEach(Scanner::calcDistances);

        final AtomicBoolean flag = new AtomicBoolean();
        final AtomicReference<Scanner> firstScanner = new AtomicReference<>();
        scanners.removeIf(
                s -> {
                    if (!flag.getAndSet(true)) {
                        firstScanner.set(s);
                        return true;
                    }
                    return false;
                });

        final List<Scanner> transformedScanners = new ArrayList<>();
        transformedScanners.add(firstScanner.get());
        int referenceScannerIndex = 0;

        do {
            final Scanner referenceScanner = transformedScanners.get(referenceScannerIndex);
            final List<Scanner> matchesFound = new ArrayList<>();
            final List<Scanner> scannerList = new ArrayList<>(scanners);
            boolean breakout = false;

            for (final Scanner scannerToCheck : scannerList) {
                for (int i = 0; i < referenceScanner.beacons.size(); i++) {
                    final Set<BeaconDistance> refDistances =
                            referenceScanner.distances.get(referenceScanner.beacons.get(i));

                    for (int j = 0; j < scannerToCheck.beacons.size(); j++) {
                        Beacon beaconToCheck = scannerToCheck.beacons.get(j);
                        final Set<BeaconDistance> checkDistances =
                                new HashSet<>(scannerToCheck.distances.get(beaconToCheck));
                        checkDistances.retainAll(refDistances);

                        if (checkDistances.size() >= 11) {
                            // TODO: need to re-orient
                            matchesFound.add(scannerToCheck);
                            scanners.remove(scannerToCheck);
                            breakout = true;
                            break;
                        }
                    }

                    if (breakout) break;
                }

                if (breakout) break;
            }

            if (matchesFound.isEmpty()
                    && ++referenceScannerIndex > transformedScanners.size() - 1) {
                referenceScannerIndex = 0;
            }

            transformedScanners.addAll(matchesFound);
        } while (!scanners.isEmpty());

        System.out.println(
                scanners.stream().map(Scanner::toString).collect(Collectors.joining("\n\n")));
    }

    private static class Scanner {
        private final int id;
        private final List<Beacon> beacons = new ArrayList<>();
        private final Map<Beacon, Set<BeaconDistance>> distances = new HashMap<>();

        private Scanner(final int id) {
            this.id = id;
        }

        private void calcDistances() {
            for (int i = 0; i < beacons.size(); i++) {
                final Beacon beacon1 = beacons.get(i);
                final Set<BeaconDistance> beaconDistances = new HashSet<>();

                for (int j = 0; j < beacons.size(); j++) {
                    if (i == j) continue;

                    beaconDistances.add(new BeaconDistance(beacon1, beacons.get(j)));
                }

                distances.put(beacon1, beaconDistances);
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "--- scanner %d ---%n%s",
                    id, beacons.stream().map(Beacon::toString).collect(Collectors.joining("\n")));
        }
    }

    private static class Beacon {
        private final int x, y, z;
        private final Transform transform;
        private final Swap swap;

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

    private static class BeaconDistance {
        private final Beacon beacon1, beacon2;
        private final List<Integer> distList = new ArrayList<>(3);
        private final Set<Integer> distSet = new HashSet<>(3);

        private BeaconDistance(final Beacon beacon1, final Beacon beacon2) {
            this.beacon1 = beacon1;
            this.beacon2 = beacon2;
            distList.add(beacon1.x - beacon2.x);
            distList.add(beacon1.y - beacon2.y);
            distList.add(beacon1.z - beacon2.z);
            distList.forEach(val -> distSet.add(Math.abs(val)));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BeaconDistance)) return false;

            final BeaconDistance other = (BeaconDistance) obj;

            return Objects.equals(distSet, other.distSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(distSet);
        }
    }
}
