package tech.readonly.aoc2021;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class Day22 {
    public static void main(final String[] args) throws Exception {
        final List<Region> inputRegions = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        try (final Scanner scanner = new Scanner(new File("inputs/day22.txt"))) {
            while (scanner.hasNextLine()) {
                final String[] stateAndCoords = scanner.nextLine().trim().split(" ");
                final boolean turnOn = stateAndCoords[0].equals("on");

                final String[][] coords =
                        Arrays.stream(stateAndCoords[1].split(","))
                                .map(s -> s.split("=")[1].split("\\.\\."))
                                .toArray(String[][]::new);

                final Region region =
                        new Region(
                                turnOn,
                                parseInt(coords[0][0]),
                                parseInt(coords[0][1]),
                                parseInt(coords[1][0]),
                                parseInt(coords[1][1]),
                                parseInt(coords[2][0]),
                                parseInt(coords[2][1]));
                inputRegions.add(region);

                minX = Math.min(minX, region.minX);
                maxX = Math.max(maxX, region.maxX);
                minY = Math.min(minY, region.minY);
                maxY = Math.max(maxY, region.maxY);
                minZ = Math.min(minZ, region.minZ);
                maxZ = Math.max(maxZ, region.maxZ);
            }
        }

        final List<Region> clampedRegions = clampToBounds(inputRegions);

        System.out.println("Part 1: " + computeNumOn(clampedRegions));
        System.out.println("Part 2: " + computeNumOn(inputRegions));
    }

    private static long computeNumOn(final List<Region> regions) {
        Set<Region> onRegions = new HashSet<>();

        for (final Region currRegion : regions) {
            if (onRegions.isEmpty()) {
                if (currRegion.turnOn) {
                    onRegions.add(currRegion);
                }
                continue;
            }

            if (currRegion.turnOn) {
                onRegions.addAll(findDisjointRegions(currRegion, onRegions));
            } else {
                final Set<Region> regionToSubtract = Collections.singleton(currRegion);
                onRegions =
                        onRegions.stream()
                                .flatMap(r -> findDisjointRegions(r, regionToSubtract).stream())
                                .collect(Collectors.toSet());
            }
        }

        return onRegions.stream().map(Region::size).reduce(Long::sum).orElseThrow();
    }

    private static List<Region> clampToBounds(final List<Region> input) {
        return input.stream()
                .filter(rs -> rs.maxX >= -50 && rs.minX <= 50)
                .filter(rs -> rs.maxY >= -50 && rs.minY <= 50)
                .filter(rs -> rs.maxZ >= -50 && rs.minZ <= 50)
                .map(
                        rs ->
                                new Region(
                                        rs.turnOn,
                                        Math.max(rs.minX, -50),
                                        Math.min(rs.maxX, 50),
                                        Math.max(rs.minY, -50),
                                        Math.min(rs.maxY, 50),
                                        Math.max(rs.minZ, -50),
                                        Math.min(rs.maxZ, 50)))
                .collect(Collectors.toList());
    }

    private static List<Region> findDisjointRegions(
            final Region newRegion, final Set<Region> existingRegions) {
        final Set<Region> remainingRegions = new HashSet<>(existingRegions);

        for (final Region existingRegion : existingRegions) {
            remainingRegions.remove(existingRegion);

            final Region overlap = getOverlap(existingRegion, newRegion);
            if (overlap == null) continue;

            final List<Region> newRegions = subtractRegion(newRegion, overlap);
            if (newRegions.stream().map(Region::size).reduce(Long::sum).orElse(0L) +
                    overlap.size() != newRegion.size()) {
                throw new IllegalStateException();
            }
            return newRegions.stream()
                    .flatMap(nr -> findDisjointRegions(nr, remainingRegions).stream())
                    .collect(Collectors.toList());
        }

        return Collections.singletonList(newRegion);
    }

    private static Region getOverlap(final Region first, final Region second) {
        final int minX = Math.max(first.minX, second.minX);
        final int maxX = Math.min(first.maxX, second.maxX);
        final int minY = Math.max(first.minY, second.minY);
        final int maxY = Math.min(first.maxY, second.maxY);
        final int minZ = Math.max(first.minZ, second.minZ);
        final int maxZ = Math.min(first.maxZ, second.maxZ);

        final Region overlap = new Region(second.turnOn, minX, maxX, minY, maxY, minZ, maxZ);
        if (overlap.makesSense()) {
            return overlap;
        }

        return null;
    }

    private static List<Region> subtractRegion(final Region region, final Region toSubtract) {
        final Region above =
                new Region(
                        region.turnOn,
                        region.minX,
                        region.maxX,
                        toSubtract.maxY + 1,
                        region.maxY,
                        region.minZ,
                        region.maxZ);
        final Region below =
                new Region(
                        region.turnOn,
                        region.minX,
                        region.maxX,
                        region.minY,
                        toSubtract.minY - 1,
                        region.minZ,
                        region.maxZ);
        final Region right =
                new Region(
                        region.turnOn,
                        toSubtract.maxX + 1,
                        region.maxX,
                        toSubtract.minY,
                        toSubtract.maxY,
                        region.minZ,
                        region.maxZ);
        final Region left =
                new Region(
                        region.turnOn,
                        region.minX,
                        toSubtract.minX - 1,
                        toSubtract.minY,
                        toSubtract.maxY,
                        region.minZ,
                        region.maxZ);
        final Region front =
                new Region(
                        region.turnOn,
                        toSubtract.minX,
                        toSubtract.maxX,
                        toSubtract.minY,
                        toSubtract.maxY,
                        toSubtract.maxZ + 1,
                        region.maxZ);
        final Region back =
                new Region(
                        region.turnOn,
                        toSubtract.minX,
                        toSubtract.maxX,
                        toSubtract.minY,
                        toSubtract.maxY,
                        region.minZ,
                        toSubtract.minZ - 1);

        final List<Region> newRegions = new ArrayList<>();
        if (above.makesSense()) newRegions.add(above);
        if (below.makesSense()) newRegions.add(below);
        if (right.makesSense()) newRegions.add(right);
        if (left.makesSense()) newRegions.add(left);
        if (back.makesSense()) newRegions.add(back);
        if (front.makesSense()) newRegions.add(front);

        return newRegions;
    }

    private static class Region {
        private final boolean turnOn;
        private final int minX, maxX, minY, maxY, minZ, maxZ;

        private Region(
                final boolean turnOn,
                final int minX,
                final int maxX,
                final int minY,
                final int maxY,
                final int minZ,
                final int maxZ) {
            this.turnOn = turnOn;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        private long size() {
            return Math.abs((maxX - minX + 1L) * (maxY - minY + 1L) * (maxZ - minZ + 1L));
        }

        private boolean makesSense() {
            return this.minX <= this.maxX && this.minY <= this.maxY && this.minZ <= this.maxZ;
        }

        public boolean equivalentTo(@Nonnull final Object o) {
            if (this == o) return true;
            if (getClass() != o.getClass()) return false;

            final Region region = (Region) o;
            return minX == region.minX
                    && maxX == region.maxX
                    && minY == region.minY
                    && maxY == region.maxY
                    && minZ == region.minZ
                    && maxZ == region.maxZ;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(@Nullable final Object o) {
            if (o == null || !equivalentTo(o)) return false;

            final Region region = (Region) o;
            return turnOn == region.turnOn;
        }

        @Override
        public int hashCode() {
            return Objects.hash(turnOn, minX, maxX, minY, maxY, minZ, maxZ);
        }
    }
}
