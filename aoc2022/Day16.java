package tech.readonly.aoc2022;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;

public class Day16 {
    // this enum ended up being overkill (an object wouldn't push us OOM),
    // but leaving it in rather than doing a rewrite
    private enum Node {
        AA,
        AW,
        AY,
        BE,
        BI,
        BL,
        BV,
        CC,
        CD,
        CR,
        DV,
        DY,
        EC,
        ED,
        FT,
        FY,
        GC,
        GF,
        GG,
        GJ,
        HH,
        HX,
        IN,
        IT,
        IX,
        JC,
        JN,
        KJ,
        KR,
        LX,
        MF,
        MK,
        MM,
        MZ,
        NL,
        OW,
        PB,
        PH,
        PN,
        PR,
        PS,
        QR,
        RI,
        RL,
        RM,
        RQ,
        SD,
        SI,
        SV,
        TR,
        VE,
        XG,
        YG,
        YH,
        YY,
        ZG,
        ZQ;
    }

    private static class CacheKey {
        private final int timeLeft;
        private final Node currNode;
        private final Set<Node> nodesLeftToVisit;
        private final boolean haveElephant;

        public CacheKey(final int timeLeft,
                        final Node currNode,
                        final Set<Node> nodesLeftToVisit,
                        final boolean haveElephant) {
            this.timeLeft = timeLeft;
            this.currNode = currNode;
            this.nodesLeftToVisit = nodesLeftToVisit;
            this.haveElephant = haveElephant;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CacheKey cacheKey = (CacheKey) o;
            return timeLeft == cacheKey.timeLeft &&
                    haveElephant == cacheKey.haveElephant &&
                    currNode == cacheKey.currNode &&
                    Objects.equals(nodesLeftToVisit, cacheKey.nodesLeftToVisit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeLeft, currNode, nodesLeftToVisit, haveElephant);
        }
    }

    private static class DistanceMap {
        private static class Key {
            private final Node key1;
            private final Node key2;

            public Key(Node key1, Node key2) {
                this.key1 = key1;
                this.key2 = key2;
            }

            @Override
            public boolean equals(final Object o) {
                final Key other = (Key) o;
                return (key1 == other.key1 && key2 == other.key2) ||
                        (key1 == other.key2 && key2 == other.key1);
            }

            @Override
            public int hashCode() {
                return key1.ordinal() < key2.ordinal()
                        ? Objects.hash(key1, key2)
                        : Objects.hash(key2, key1);
            }
        }

        private final Map<Key, Integer> distanceByNodes = new HashMap<>();

        private Integer get(final Node node1, final Node node2) {
            if (node1 == node2) {
                return 0;
            }

            return distanceByNodes.getOrDefault(new Key(node1, node2), Integer.MAX_VALUE / 2);
        }

        private void put(final Node node1, final Node node2, final int distance) {
            distanceByNodes.put(new Key(node1, node2), distance);
        }
    }

    private static final Map<Node, Integer> FLOW_RATE_BY_NODE = new EnumMap<>(Node.class);
    private static final Map<CacheKey, Integer> BEST_SCORE_BY_KEY = new HashMap<>();
    private static final DistanceMap DISTANCES = new DistanceMap();

    public static void main(final String[] args) throws Exception {
        final Pattern pattern = Pattern.compile(
                "Valve (..) has flow rate=(.+); tunnels? leads? to valves? (.+)");

        try (final Scanner scanner = new Scanner(new File("inputs/Day16.txt"))) {
            while (scanner.hasNextLine()) {
                final Matcher matcher = pattern.matcher(scanner.nextLine().trim());
                //noinspection ResultOfMethodCallIgnored
                matcher.matches();

                final Node node = Node.valueOf(matcher.group(1));
                FLOW_RATE_BY_NODE.put(node, parseInt(matcher.group(2)));

                for (final String connectedName : matcher.group(3).split(", ")) {
                    DISTANCES.put(node, Node.valueOf(connectedName), 1);
                }
            }
        }

        // http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm
        for (final Node i : Node.values()) {
            for (final Node j : Node.values()) {
                for (final Node k : Node.values()) {
                    final int ijDistance = DISTANCES.get(i, j);
                    final int jkDistance = DISTANCES.get(j, k);
                    final int kiDistance = DISTANCES.get(k, i);

                    if (ijDistance > jkDistance + kiDistance) {
                        DISTANCES.put(i, j, jkDistance + kiDistance);
                    }
                }
            }
        }

        final Set<Node> openableNodes = EnumSet.allOf(Node.class);
        openableNodes.removeIf(n -> FLOW_RATE_BY_NODE.get(n) == 0);

        final int scorePart1 = walk(30, Node.AA, openableNodes, false);
        System.out.println("Part 1: " + scorePart1);

        final int scorePart2 = walk(26, Node.AA, openableNodes, true);
        System.out.println("Part 2: " + scorePart2);
    }

    private static int walk(final int timeLeft,
                            final Node lastNode,
                            final Set<Node> openableNodes,
                            final boolean haveElephant) {
        final CacheKey cacheKey = new CacheKey(timeLeft, lastNode, openableNodes, haveElephant);
        if (BEST_SCORE_BY_KEY.containsKey(cacheKey)) {
            return BEST_SCORE_BY_KEY.get(cacheKey);
        }

        int maxScore = 0;

        for (final Node nextNode : openableNodes) {
            final int newTimeLeft = timeLeft - DISTANCES.get(lastNode, nextNode) - 1;
            if (newTimeLeft <= 0) {
                continue;
            }

            final Set<Node> remainingOpenableNodes = EnumSet.copyOf(openableNodes);
            remainingOpenableNodes.remove(nextNode);

            final int score = FLOW_RATE_BY_NODE.get(nextNode) * newTimeLeft +
                    walk(newTimeLeft, nextNode, remainingOpenableNodes, haveElephant);

            maxScore = max(maxScore, score);
        }

        if (haveElephant) {
            maxScore = max(maxScore, walk(26, Node.AA, openableNodes, false));
        }

        BEST_SCORE_BY_KEY.put(cacheKey, maxScore);
        return maxScore;
    }
}
