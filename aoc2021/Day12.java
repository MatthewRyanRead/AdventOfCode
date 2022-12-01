package tech.readonly.aoc2021;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Day12 {
    public static void main(final String[] args) throws Exception {
        final List<String> input = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new File("inputs/day12.txt"))) {
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine().trim());
            }
        }

        final Map<String, Set<String>> edges = new HashMap<>();
        final List<Path> startPaths = new ArrayList<>();

        for (final String edge : input) {
            final String[] vertices = edge.split("-");
            final String startNode = vertices[0];
            final String endNode = vertices[1];

            if (startNode.equals("start")) {
                startPaths.add(new Path(endNode));
            } else if (endNode.equals("start")) {
                startPaths.add(new Path(startNode));
            } else {
                addEdge(edges, startNode, endNode);
            }
        }

        final List<Path> currPaths1 = new ArrayList<>(startPaths);
        final List<Path> completedPaths1 = new ArrayList<>();
        compute(currPaths1, edges, completedPaths1, 1);

        System.out.println("Part 1: " + completedPaths1.size());

        final List<Path> currPaths2 = new ArrayList<>(startPaths);
        final List<Path> completedPaths2 = new ArrayList<>();
        compute(currPaths2, edges, completedPaths2, 2);

        System.out.println("Part 2: " + completedPaths2.size());
    }

    private static void compute(
            final List<Path> currPaths,
            final Map<String, Set<String>> edges,
            final List<Path> completedPaths,
            final int smallCaveLimit) {
        final List<Path> newPaths = new ArrayList<>();

        do {
            newPaths.clear();

            for (final Path path : currPaths) {
                final String lastNode = path.getLastNode();
                final Set<String> options = edges.get(lastNode);

                for (final String option : options) {
                    if (smallCaveLimit <= path.visitCountByNode.getOrDefault(option, 0)
                            && option.toLowerCase().equals(option)) continue;

                    final Path newPath = path.clone();
                    if (!newPath.addNode(option, smallCaveLimit == 2)) continue;

                    if (option.equals("end")) {
                        completedPaths.add(newPath);
                    } else {
                        newPaths.add(newPath);
                    }
                }
            }

            currPaths.clear();
            currPaths.addAll(newPaths);
        } while (!newPaths.isEmpty());

        completedPaths.removeIf(p -> !p.getLastNode().equals("end"));
    }

    private static void addEdge(
            final Map<String, Set<String>> edges, final String startNode, final String endNode) {
        edges.computeIfAbsent(startNode, x -> new HashSet<>());
        edges.computeIfAbsent(endNode, x -> new HashSet<>());

        edges.get(startNode).add(endNode);
        edges.get(endNode).add(startNode);
    }

    private static class Path {
        private final List<String> orderedNodes = new ArrayList<>();
        private final Map<String, Integer> visitCountByNode = new HashMap<>();
        private boolean hasOneSmallDuplicate = false;

        public Path(final String initialNode) {
            this.addNode(initialNode, false);
        }

        public boolean addNode(final String node, boolean allowOneSmallDuplicate) {
            visitCountByNode.putIfAbsent(node, 0);
            final int visitCount = visitCountByNode.get(node);

            if (visitCount == 1 && node.toLowerCase().equals(node)) {
                if (hasOneSmallDuplicate || !allowOneSmallDuplicate) {
                    return false;
                } else {
                    hasOneSmallDuplicate = true;
                }
            }

            orderedNodes.add(node);
            visitCountByNode.put(node, visitCount + 1);
            return true;
        }

        public String getLastNode() {
            return orderedNodes.get(orderedNodes.size() - 1);
        }

        @Override
        protected Path clone() {
            final Path clone = new Path(orderedNodes.get(0));
            for (int i = 1; i < orderedNodes.size(); i++) {
                clone.addNode(orderedNodes.get(i), hasOneSmallDuplicate);
            }

            return clone;
        }
    }
}
