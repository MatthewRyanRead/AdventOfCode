package tech.readonly.aoc2021;

import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Day23Part2 {
    public static void main(final String[] args) throws Exception {
        final AtomicInteger prevId = new AtomicInteger(-1);

        final List<Node> hall =
                IntStream.range(0, 11)
                        .mapToObj(i -> new Node(prevId.incrementAndGet()))
                        .collect(toList());
        for (int i = 2; i < hall.size() - 1; i++) {
            final Node curr = hall.get(i);
            final Node prev = hall.get(i - 1);
            curr.edges.add(Pair.of(prev, 2));
            prev.edges.add(Pair.of(curr, 2));
        }
        hall.get(0).edges.add(Pair.of(hall.get(1), 1));
        hall.get(1).edges.add(Pair.of(hall.get(0), 1));
        hall.get(hall.size() - 2).edges.add(Pair.of(hall.get(hall.size() - 1), 1));
        hall.get(hall.size() - 1).edges.add(Pair.of(hall.get(hall.size() - 2), 1));

        final Set<Node> nodes = new HashSet<>(hall);
        final Node[][] colNodes = new Node[4][4];

        try (final Scanner scanner = new Scanner(new File("inputs/day23.txt"))) {
            scanner.nextLine();
            scanner.nextLine();

            String[] row = scanner.nextLine().trim().replace("#", "").split("");
            for (int i = 0; i < 4; i++) {
                final Node colNode =
                        new Node(
                                prevId.incrementAndGet(),
                                row[i],
                                new String[] {"A", "B", "C", "D"}[i]);
                final Node hallNode = hall.get(2 * (i + 1));
                colNode.edges.add(Pair.of(hallNode, 2));
                hallNode.edges.add(Pair.of(colNode, 2));

                colNodes[0][i] = colNode;
                nodes.add(colNode);
            }

            for (int i = 1; i < 4; i++) {
                row = scanner.nextLine().trim().replace("#", "").split("");

                for (int j = 0; j < 4; j++) {
                    final Node colNode =
                            new Node(
                                    prevId.incrementAndGet(),
                                    row[i],
                                    new String[] {"A", "B", "C", "D"}[i]);
                    final Node prevNode = colNodes[i - 1][j];
                    colNode.edges.add(Pair.of(prevNode, 1));
                    prevNode.edges.add(Pair.of(colNode, 1));
                    colNodes[i][j] = colNode;
                    nodes.add(colNode);
                }
            }
        }

        solve(new Game(nodes, 0));
    }

    private static void solve(final Game input) {
        final PriorityQueue<Game> games =
                new PriorityQueue<>(
                        Comparator.<Game>comparingInt(g -> g.numMoves)
                                .thenComparingInt(g -> g.score));
        games.add(input);
        final Set<Game> gamesSeen = new HashSet<>();
        gamesSeen.add(input);

        while (true) {
            if (games.isEmpty()) {
                throw new IllegalStateException();
            }

            final Game currGame = games.remove();
            if (currGame.isSolved()) {
                System.out.println("Part 2: " + currGame.score);
                return;
            }

            for (final Node node : currGame.nodesById.values()) {
                if (node.value.equals(".")
                        || (node != currGame.lastMoved
                                && currGame.lastMoved != null
                                && (currGame.lastMoved.id == 2
                                        || currGame.lastMoved.id == 4
                                        || currGame.lastMoved.id == 6
                                        || currGame.lastMoved.id == 8))) {
                    continue;
                }

                for (final Pair<Node, Integer> edge : node.edges) {
                    if (!edge.fst.value.equals(".")) {
                        continue;
                    }

                    final Game newGame = currGame.swapNodes(node, edge);
                    if (!gamesSeen.contains(newGame)) {
                        games.add(newGame);
                        gamesSeen.add(newGame);
                    }
                }
            }
        }
    }

    public static class Game {
        private final Map<Integer, Node> nodesById;
        private final int score;

        private int numMoves = 0;
        private Node lastMoved = null;

        private Game(final Set<Node> nodes, final int score) {
            this.nodesById = nodes.stream().collect(toMap(n -> n.id, identity()));
            this.score = score;
        }

        private Game swapNodes(final Node one, final Pair<Node, Integer> twoPair) {
            final Node two = twoPair.fst;
            final Node newOne = new Node(one.id, two.value, one.goal);
            final Node newTwo = new Node(two.id, one.value, two.goal);
            newOne.edges.addAll(one.edges);
            newTwo.edges.addAll(two.edges);

            final Set<Node> newNodes = new HashSet<>(nodesById.values());
            newNodes.remove(one);
            newNodes.remove(two);
            newNodes.add(newOne);
            newNodes.add(newTwo);

            final Game newGame = new Game(newNodes, this.score + getScore(one, twoPair.snd));
            newGame.numMoves = this.numMoves + 1;
            newGame.lastMoved = newTwo;
            return newGame;
        }

        private boolean isSolved() {
            return nodesById.values().stream().allMatch(n -> n.value.equals(n.goal));
        }

        private static int getScore(final Node source, final int distance) {
            switch (source.value) {
                case "A":
                    return distance;
                case "B":
                    return 10 * distance;
                case "C":
                    return 100 * distance;
                case "D":
                    return 1000 * distance;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Game)) return false;

            final Game other = (Game) o;
            for (int i = 0; i < 27; i++) {
                if (!nodesById.get(i).value.equals(other.nodesById.get(i).value)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(
                    nodesById.values().stream().map(n -> n.value).toArray(String[]::new));
        }

        @Override
        public String toString() {
            return "#############\n#"
                    + IntStream.range(0, 11)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining())
                    + "#\n###"
                    + IntStream.range(11, 15)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "###\n  #"
                    + IntStream.range(15, 19)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(19, 23)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(23, 27)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #########";
        }
    }

    private static class Node {
        private final int id;
        private final String value;
        private final String goal;
        private final Set<Pair<Node, Integer>> edges = new HashSet<>();

        private Node(final int id) {
            this.id = id;
            this.value = ".";
            this.goal = ".";
        }

        private Node(final int id, final String value, final String goal) {
            this.id = id;
            this.value = value;
            this.goal = goal;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Node)) return false;

            final Node other = (Node) o;
            return id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
