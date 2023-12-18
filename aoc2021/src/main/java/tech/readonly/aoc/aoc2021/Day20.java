package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Day20 {
    private static boolean defaultValue1;
    private static boolean defaultValue2;
    private static boolean prevDefaultValue;
    private static boolean currDefaultValue;

    public static void main(final String[] args) throws Exception {
        final String[] algo;
        final Map<Pair<Long, Long>, Boolean> pixelByCoordinates = new HashMap<>();
        long maxX, maxY;

        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day20.txt").getFile()))) {
            algo = scanner.nextLine().trim().split("");
            scanner.nextLine();

            long y = 0L;
            long x = 0L;

            while (scanner.hasNextLine()) {
                final String[] pixels = scanner.nextLine().trim().split("");
                x = 0L;

                for (final String pixel : pixels) {
                    pixelByCoordinates.put(Pair.of(x++, y), pixel.equals("#"));
                }

                y++;
            }

            maxX = x;
            maxY = y;
        }

        // 000000000 and 111111111, for exterior infinite pixels
        defaultValue1 = algo[0].equals("#");
        defaultValue2 = defaultValue1 && algo[511].equals("#");
        prevDefaultValue = false;
        currDefaultValue = defaultValue1;

        Image image1 = new Image(new HashMap<>(pixelByCoordinates), 0L, maxX, 0L, maxY);
        image1 = runAlgo(algo, image1);
        image1 = runAlgo(algo, image1);

        System.out.println(
                "Part 1: " + image1.pixelByCoords.values().stream().filter(v -> v).count());

        Image image2 = new Image(new HashMap<>(pixelByCoordinates), 0L, maxX, 0L, maxY);
        for (int i = 0; i < 50; i++) {
            image2 = runAlgo(algo, image2);
        }

        System.out.println(
                "Part 2: " + image2.pixelByCoords.values().stream().filter(v -> v).count());
    }

    private static Image runAlgo(final String[] algo, final Image input) {
        final Image output = new Image(input);

        // sorting is just for ease of debugging
        input.pixelByCoords.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getKey().snd))
                .sorted(Comparator.comparingLong(e -> e.getKey().fst))
                .forEach(entry -> calcAndSetPixel(algo, entry.getKey(), input, output));
        for (long x = output.minX; x < output.maxX; x++) {
            calcAndSetPixel(algo, Pair.of(x, output.minY), input, output);
            calcAndSetPixel(algo, Pair.of(x, output.maxY - 1), input, output);
        }
        for (long y = output.minY + 1; y < output.maxY; y++) {
            calcAndSetPixel(algo, Pair.of(output.minX, y), input, output);
            calcAndSetPixel(algo, Pair.of(output.maxX - 1, y), input, output);
        }

        prevDefaultValue = currDefaultValue;
        if (currDefaultValue == defaultValue1) {
            currDefaultValue = defaultValue2;
        } else {
            currDefaultValue = defaultValue1;
        }

        return output;
    }

    private static void calcAndSetPixel(
            final String[] algo,
            final Pair<Long, Long> coords,
            final Image input,
            final Image output) {
        final int indexCode = getIndexCode(coords, input);
        final String outputPixel = algo[indexCode];
        output.pixelByCoords.put(coords, outputPixel.equals("#"));
    }

    private static int getIndexCode(final Pair<Long, Long> coords, final Image image) {
        final StringBuilder sb = new StringBuilder();

        for (long y = coords.snd - 1L; y <= coords.snd + 1L; y++) {
            for (long x = coords.fst - 1L; x <= coords.fst + 1L; x++) {
                sb.append(
                        image.pixelByCoords.getOrDefault(Pair.of(x, y), prevDefaultValue)
                                ? "1"
                                : "0");
            }
        }

        return Integer.parseInt(sb.toString(), 2);
    }

    private static class Image {
        private final Map<Pair<Long, Long>, Boolean> pixelByCoords;
        // maximums are exclusive
        private final long minX, maxX, minY, maxY;

        private Image(
                final Map<Pair<Long, Long>, Boolean> pixelByCoords,
                final long minX,
                final long maxX,
                final long minY,
                final long maxY) {
            this.pixelByCoords = new HashMap<>(pixelByCoords);
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        private Image(final Image initial) {
            this.pixelByCoords = new HashMap<>();
            this.minX = initial.minX - 1;
            this.maxX = initial.maxX + 1;
            this.minY = initial.minY - 1;
            this.maxY = initial.maxY + 1;
        }

        @Override
        public String toString() {
            return LongStream.range(minY, maxY)
                    .boxed()
                    .map(
                            y ->
                                    LongStream.range(minX, maxX)
                                            .boxed()
                                            .map(x -> pixelByCoords.get(Pair.of(x, y)) ? "#" : ".")
                                            .collect(Collectors.joining()))
                    .collect(Collectors.joining("\n"));
        }
    }
}
