package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tech.readonly.aoc2021.Day19.Inversion.DEFAULT;
import static tech.readonly.aoc2021.Day19.Inversion.INVERSIONS;
import static tech.readonly.aoc2021.Day19.Swap.SWAPS;
import static tech.readonly.aoc2021.Day19.Swap.XYZ;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class Day19 {
    public static void main(final String[] args) throws Exception {
        final Set<Scanner> scanners = new HashSet<>();

        try (final java.util.Scanner input = new java.util.Scanner(new File("inputs/day19.txt"))) {
            Scanner currScanner = null;

            while (input.hasNextLine()) {
                final String line = input.nextLine().trim();

                if (line.isEmpty()) continue;

                if (line.startsWith("---")) {
                    currScanner = new Scanner(currScanner == null ? 0 : currScanner.id + 1);
                    scanners.add(currScanner);
                } else {
                    //noinspection ConstantConditions
                    currScanner.beacons.add(new Beacon(line));
                }
            }
        }
        scanners.forEach(Scanner::calcDistances);

        final Scanner firstScanner = scanners.stream().findFirst().get();
        scanners.remove(firstScanner);
        final List<Scanner> transformedScanners = new ArrayList<>();
        transformedScanners.add(firstScanner);
        int referenceScannerIndex = 0;

        do {
            final Scanner referenceScanner = transformedScanners.get(referenceScannerIndex);
            Scanner foundMatch = null;
            final List<Scanner> scannerList = new ArrayList<>(scanners);

            findMatch:
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
                            final BeaconDistance checkDistance =
                                    checkDistances.stream().findFirst().get();
                            final BeaconDistance refDistance =
                                    refDistances.stream()
                                            .filter(
                                                    rd ->
                                                            rd.distSet.containsAll(
                                                                    checkDistance.distSet))
                                            .findFirst()
                                            .get();

                            Inversion inversion = DEFAULT;
                            Swap swap = XYZ;

                            // I don't want to implement a matrix class (or figure out a library
                            // for it), so I'm going to do all 48 permutations -- the 24 mirrored
                            // ones are wasted, but this is still more than fast enough
                            transformFinder:
                            for (int k = 0; k < INVERSIONS.size(); k++) {
                                inversion = INVERSIONS.get(k);

                                for (int m = 0; m < SWAPS.size(); m++) {
                                    swap = SWAPS.get(m);
                                    int[] xyz =
                                            inversion.transform(
                                                    checkDistance.distList.get(0),
                                                    checkDistance.distList.get(1),
                                                    checkDistance.distList.get(2));
                                    xyz = swap.transform(xyz[0], xyz[1], xyz[2]);
                                    final List<Integer> xyzList =
                                            Arrays.stream(xyz).boxed().collect(Collectors.toList());

                                    if (xyzList.equals(refDistance.distList)) {
                                        break transformFinder;
                                    }
                                }
                            }

                            final Beacon unscaledTransform =
                                    new Beacon(
                                            checkDistance.beacon1,
                                            inversion,
                                            swap,
                                            new int[] {0, 0, 0});
                            final int[] offset =
                                    new int[] {
                                        unscaledTransform.x - refDistance.beacon1.x,
                                        unscaledTransform.y - refDistance.beacon1.y,
                                        unscaledTransform.z - refDistance.beacon1.z,
                                    };

                            final List<Beacon> beaconsToTransform =
                                    new ArrayList<>(scannerToCheck.beacons);
                            scannerToCheck.beacons.clear();

                            for (final Beacon beaconToTransform : beaconsToTransform) {
                                scannerToCheck.beacons.add(
                                        new Beacon(beaconToTransform, inversion, swap, offset));
                            }

                            scannerToCheck.distances.clear();
                            scannerToCheck.calcDistances();
                            scannerToCheck.position[0] = -offset[0];
                            scannerToCheck.position[1] = -offset[1];
                            scannerToCheck.position[2] = -offset[2];

                            foundMatch = scannerToCheck;
                            scanners.remove(scannerToCheck);

                            break findMatch;
                        }
                    }
                }
            }

            if (foundMatch != null) {
                transformedScanners.add(foundMatch);
            } else if (++referenceScannerIndex > transformedScanners.size() - 1) {
                referenceScannerIndex = 0;
            }
        } while (!scanners.isEmpty());

        final Set<Beacon> allBeacons =
                transformedScanners.stream()
                        .flatMap(s -> s.beacons.stream())
                        .collect(Collectors.toSet());
        allBeacons.removeIf(
                b ->
                        allBeacons.stream()
                                .anyMatch(
                                        b2 ->
                                                b != b2
                                                        && b.x == b2.x
                                                        && b.y == b2.y
                                                        && b.z == b2.z));

        System.out.println("Part 1: " + allBeacons.size());

        int maxDistance = 0;
        for (int i = 0; i < transformedScanners.size(); i++) {
            final Scanner scanner1 = transformedScanners.get(i);

            for (int j = 0; j < transformedScanners.size(); j++) {
                if (i == j) continue;

                final Scanner scanner2 = transformedScanners.get(j);

                final int distance =
                        Math.abs(scanner1.position[0] - scanner2.position[0])
                                + Math.abs(scanner1.position[1] - scanner2.position[1])
                                + Math.abs(scanner1.position[2] - scanner2.position[2]);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }

        System.out.println("Part 2: " + maxDistance);
    }

    private static class Scanner {
        private final int id;
        private final List<Beacon> beacons = new ArrayList<>();
        private final Map<Beacon, Set<BeaconDistance>> distances = new HashMap<>();

        private final int[] position = {0, 0, 0};

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
                    id,
                    beacons.stream()
                            .sorted(Comparator.comparingInt(b -> b.z))
                            .sorted(Comparator.comparingInt(b -> b.y))
                            .sorted(Comparator.comparingInt(b -> b.x))
                            .map(Beacon::toString)
                            .collect(Collectors.joining("\n")));
        }
    }

    static class Beacon {
        private final int x, y, z;

        private Beacon(final String string) {
            final int[] xyz =
                    Arrays.stream(string.split(",")).mapToInt(Integer::parseInt).toArray();

            x = xyz[0];
            y = xyz[1];
            z = xyz[2];
        }

        private Beacon(
                final Beacon beacon,
                final Inversion inversion,
                final Swap swap,
                final int[] offset) {
            int[] xyz = inversion.transform(beacon.x, beacon.y, beacon.z);
            xyz = swap.transform(xyz[0], xyz[1], xyz[2]);

            this.x = xyz[0] - offset[0];
            this.y = xyz[1] - offset[1];
            this.z = xyz[2] - offset[2];
        }

        @Override
        public String toString() {
            return String.format("%d,%d,%d", x, y, z);
        }
    }

    private static class BeaconDistance {
        private final Beacon beacon1;
        private final List<Integer> distList = new ArrayList<>(3);
        private final Set<Integer> distSet = new HashSet<>(3);

        private BeaconDistance(final Beacon beacon1, final Beacon beacon2) {
            this.beacon1 = beacon1;
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

    enum Inversion {
        DEFAULT(new int[] {1, 1, 1}),
        NEGATE_X(new int[] {-1, 1, 1}),
        NEGATE_Y(new int[] {1, -1, 1}),
        NEGATE_Z(new int[] {1, 1, -1}),
        NEGATE_XY(new int[] {-1, -1, 1}),
        NEGATE_XZ(new int[] {-1, 1, -1}),
        NEGATE_YZ(new int[] {1, -1, -1}),
        NEGATE_XYZ(new int[] {-1, -1, -1});

        static final Map<Integer, Inversion> INVERSIONS =
                Arrays.stream(Inversion.values())
                        .collect(Collectors.toMap(Enum::ordinal, Function.identity()));
        private final int[] matrix;

        Inversion(final int[] matrix) {
            this.matrix = matrix;
        }

        private int[] transform(final int x, final int y, final int z) {
            return new int[] {x * matrix[0], y * matrix[1], z * matrix[2]};
        }
    }

    enum Swap {
        XYZ(new int[] {0, 1, 2}),
        XZY(new int[] {0, 2, 1}),
        YXZ(new int[] {1, 0, 2}),
        YZX(new int[] {2, 0, 1}),
        ZXY(new int[] {1, 2, 0}),
        ZYX(new int[] {2, 1, 0});

        static final Map<Integer, Swap> SWAPS =
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
    }
}
