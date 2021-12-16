package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Day16 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();

        try (final Scanner scanner = new Scanner(new File("inputs/day16.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(
                        Arrays.stream(scanner.nextLine().trim().split(""))
                                .map(hexChar -> Integer.parseInt(hexChar, 16))
                                .map(Integer::toBinaryString)
                                .map(
                                        s -> {
                                            while (s.length() < 4) {
                                                //noinspection StringConcatenationInLoop
                                                s = "0" + s;
                                            }
                                            return s;
                                        })
                                .collect(Collectors.joining()));
            }
        }

        final List<Packet> packets =
                input.stream()
                        .map(Day16::parsePackets)
                        .map(
                                packetList -> {
                                    if (packetList.size() != 1) {
                                        throw new IllegalStateException();
                                    }
                                    return packetList.get(0);
                                })
                        .collect(Collectors.toList());

        final List<Packet> packetsToProcess = new ArrayList<>();
        packetsToProcess.add(packets.get(0));
        long versionSum = 0L;

        while (!packetsToProcess.isEmpty()) {
            final Packet currPacket = packetsToProcess.remove(packetsToProcess.size() - 1);
            packetsToProcess.addAll(currPacket.subPackets);
            versionSum += Integer.parseInt(currPacket.version, 2);
        }

        System.out.println("Part 1: " + versionSum);

        System.out.println(
                "Part 2: "
                        + packets.stream()
                                .map(Packet::value)
                                .map(String::valueOf)
                                .collect(Collectors.joining(" ")));
    }

    private static List<Packet> parsePackets(String binaryPacket) {
        // version + type + shortest literal = 11
        if (binaryPacket.length() < 11) return new ArrayList<>();

        final String version = binaryPacket.substring(0, 3);
        binaryPacket = binaryPacket.substring(3);

        final String type = binaryPacket.substring(0, 3);
        binaryPacket = binaryPacket.substring(3);

        // type 4 is a literal
        if (type.equals("100")) {
            return parseLiteralAndSiblings(version, type, binaryPacket);
        }

        return parseOperatorAndSiblings(version, type, binaryPacket);
    }

    private static List<Packet> parseLiteralAndSiblings(
            final String version, final String type, String remainderOfPacket) {
        final StringBuilder sb = new StringBuilder();

        while (true) {
            final String group = remainderOfPacket.substring(0, 5);
            remainderOfPacket = remainderOfPacket.substring(5);

            // first bit is only a marker
            sb.append(group.substring(1));

            // marker bit of 0 denotes the last group
            if (group.startsWith("0")) {
                break;
            }
        }

        final String literal = sb.toString();

        final Packet packet = new Packet(version, type, literal);
        final List<Packet> allPackets = new ArrayList<>();
        allPackets.add(packet);

        // any further packets are siblings
        allPackets.addAll(parsePackets(remainderOfPacket));

        return allPackets;
    }

    private static List<Packet> parseOperatorAndSiblings(
            final String version, final String type, String remainderOfPacket) {
        // all other types are operators
        final String lengthType = remainderOfPacket.substring(0, 1);
        remainderOfPacket = remainderOfPacket.substring(1);

        final List<Packet> siblings = new ArrayList<>();
        final List<Packet> children = new ArrayList<>();

        if (lengthType.equals("1")) {
            final int numChildren = Integer.parseInt(remainderOfPacket.substring(0, 11), 2);
            remainderOfPacket = remainderOfPacket.substring(11);

            final List<Packet> allRemainingPackets = parsePackets(remainderOfPacket);
            allRemainingPackets.stream().limit(numChildren).forEach(children::add);
            allRemainingPackets.stream().skip(numChildren).forEach(siblings::add);
        } else {
            final int lengthOfChildren = Integer.parseInt(remainderOfPacket.substring(0, 15), 2);
            remainderOfPacket = remainderOfPacket.substring(15);

            children.addAll(parsePackets(remainderOfPacket.substring(0, lengthOfChildren)));
            siblings.addAll(parsePackets(remainderOfPacket.substring(lengthOfChildren)));
        }

        final Packet packet = new Packet(version, type, children);
        final List<Packet> allPackets = new ArrayList<>();
        allPackets.add(packet);
        allPackets.addAll(siblings);

        return allPackets;
    }

    public static class Packet {
        private final List<Packet> subPackets = new ArrayList<>();
        private final String version;
        private final String type;

        private Long value = null;

        public Packet(final String version, final String type, final String literal) {
            this.version = version;
            this.type = type;
            this.value = Long.parseLong(literal, 2);
        }

        public Packet(final String version, final String type, final List<Packet> subPackets) {
            this.version = version;
            this.type = type;
            this.subPackets.addAll(subPackets);
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent") // invalid, given the size checks
        public long value() {
            if (value != null) {
                return value;
            }

            switch (type) {
                case "000":
                    if (subPackets.size() < 1) {
                        throw new IllegalStateException();
                    }
                    value = subPackets.stream().map(Packet::value).reduce(0L, Long::sum);
                    break;
                case "001":
                    if (subPackets.size() < 1) {
                        throw new IllegalStateException();
                    }
                    value = subPackets.stream().map(Packet::value).reduce(1L, (a, b) -> a * b);
                    break;
                case "010":
                    if (subPackets.size() < 1) {
                        throw new IllegalStateException();
                    }
                    value =
                            subPackets.stream()
                                    .map(Packet::value)
                                    .sorted()
                                    .limit(1)
                                    .findFirst()
                                    .get();
                    break;
                case "011":
                    if (subPackets.size() < 1) {
                        throw new IllegalStateException();
                    }
                    value =
                            subPackets.stream()
                                    .sorted(Comparator.comparingLong(Packet::value).reversed())
                                    .limit(1)
                                    .findFirst()
                                    .get()
                                    .value();
                    break;
                case "101":
                    if (subPackets.size() != 2) {
                        throw new IllegalStateException();
                    }
                    value = subPackets.get(0).value() > subPackets.get(1).value() ? 1L : 0L;
                    break;
                case "110":
                    if (subPackets.size() != 2) {
                        throw new IllegalStateException();
                    }
                    value = subPackets.get(0).value() < subPackets.get(1).value() ? 1L : 0L;
                    break;
                case "111":
                    if (subPackets.size() != 2) {
                        throw new IllegalStateException();
                    }
                    value = subPackets.get(0).value() == subPackets.get(1).value() ? 1L : 0L;
                    break;
                default:
                    throw new IllegalStateException();
            }

            return value;
        }
    }
}
