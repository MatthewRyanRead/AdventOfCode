package tech.readonly.aoc.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Day15 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();

        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day15.txt").getFile()))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        int height = input.size();
        int width = input.get(0).length();

        final Map<Pair<Integer, Integer>, Node> nodesByCoords1 = new HashMap<>();
        for (int i = 0; i < height; i++) {
            final String[] weights = input.get(i).split("");

            for (int j = 0; j < width; j++) {
                final long weight = Long.parseLong(weights[j]);
                final Node node = new Node(j, i, weight);
                nodesByCoords1.put(Pair.of(j, i), node);
            }
        }

        createEdges(height, width, nodesByCoords1);

        final long[][] distances1 =
                openTheDoorGetOnTheFloorEverybodyWalkTheDjikstrasaur(
                        height, width, nodesByCoords1.values());

        System.out.println("Part 1: " + distances1[height - 1][width - 1]);

        final Map<Pair<Integer, Integer>, Node> nodesByCoords2 = new HashMap<>();

        for (final Entry<Pair<Integer, Integer>, Node> entry : nodesByCoords1.entrySet()) {
            final Node original = entry.getValue();
            nodesByCoords2.put(entry.getKey(), original);

            for (int i = 1; i < 5; i++) {
                long newWeight = original.weight + i;
                while (newWeight > 9) newWeight -= 9;

                final Node cascadedNode = new Node(original.x, original.y + height * i, newWeight);
                nodesByCoords2.put(Pair.of(cascadedNode.x, cascadedNode.y), cascadedNode);
            }

            for (int i = 0; i < 5; i++) {
                final Node referenceNode =
                        nodesByCoords2.get(Pair.of(original.x, original.y + height * i));

                for (int j = 1; j < 5; j++) {
                    long newWeight = referenceNode.weight + j;
                    while (newWeight > 9) newWeight -= 9;

                    final Node cascadedNode =
                            new Node(referenceNode.x + width * j, referenceNode.y, newWeight);
                    nodesByCoords2.put(Pair.of(cascadedNode.x, cascadedNode.y), cascadedNode);
                }
            }
        }

        height *= 5;
        width *= 5;
        createEdges(height, width, nodesByCoords2);
        final long[][] distances2 =
                openTheDoorGetOnTheFloorEverybodyWalkTheDjikstrasaur(
                        height, width, nodesByCoords2.values());

        System.out.println("Part 2: " + distances2[height - 1][width - 1]);
    }

    private static class Node {
        private final int x;
        private final int y;
        private final long weight;

        private final List<Node> connectedNodes = new ArrayList<>();

        private Node(final int x, final int y, final long weight) {
            this.x = x;
            this.y = y;
            this.weight = weight;
        }

        private void addConnection(final Node node) {
            connectedNodes.add(node);
        }
    }

    private static void createEdges(
            final int height,
            final int width,
            final Map<Pair<Integer, Integer>, Node> nodesByCoords) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final Node node = nodesByCoords.get(Pair.of(j, i));

                if (i < width - 1) {
                    node.addConnection(nodesByCoords.get(Pair.of(j, i + 1)));
                }
                if (j < height - 1) {
                    node.addConnection(nodesByCoords.get(Pair.of(j + 1, i)));
                }
                if (i > 0) {
                    node.addConnection(nodesByCoords.get(Pair.of(j, i - 1)));
                }
                if (j > 0) {
                    node.addConnection(nodesByCoords.get(Pair.of(j - 1, i)));
                }
            }
        }
    }

    private static long[][] openTheDoorGetOnTheFloorEverybodyWalkTheDjikstrasaur(
            final int height, final int width, final Collection<Node> nodes) {
        final long[][] distances = new long[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                distances[i][j] = Integer.MAX_VALUE;
            }
        }

        distances[0][0] = 0;
        final PriorityQueue<Node> unprocessed =
                new PriorityQueue<>(Comparator.comparing(node -> distances[node.y][node.x]));
        unprocessed.addAll(nodes);

        while (!unprocessed.isEmpty()) {
            final Node minNode = unprocessed.remove();

            for (final Node connectedNode : minNode.connectedNodes) {
                final long existingDistance = distances[connectedNode.y][connectedNode.x];
                final long currDistance = distances[minNode.y][minNode.x] + connectedNode.weight;

                if (currDistance < existingDistance) {
                    distances[connectedNode.y][connectedNode.x] = currDistance;
                    unprocessed.remove(connectedNode);
                    unprocessed.add(connectedNode);
                }
            }
        }

        return distances;
    }
}
