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
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static tech.readonly.aoc2021.Day23Part2.Edge.Direction.DOWN;
import static tech.readonly.aoc2021.Day23Part2.Edge.Direction.LEFT;
import static tech.readonly.aoc2021.Day23Part2.Edge.Direction.RIGHT;
import static tech.readonly.aoc2021.Day23Part2.Edge.Direction.UP;
import static tech.readonly.aoc2021.Day23Part2.Edge.Direction.VALID_CONTINUATIONS;

public class Day23Part2 {
    public static void main(final String[] args) throws Exception {
        final AtomicInteger prevId = new AtomicInteger(-1);

        final List<Node> hall =
                IntStream.range(0, 7)
                        .mapToObj(i -> new Node(prevId.incrementAndGet()))
                        .collect(toList());
        for (int i = 2; i < hall.size() - 1; i++) {
            final Node curr = hall.get(i);
            final Node prev = hall.get(i - 1);
            curr.edges.add(new Edge(curr, prev, 2, RIGHT));
            prev.edges.add(new Edge(prev, curr, 2, LEFT));
        }

        Node outer = hall.get(0);
        Node inner = hall.get(1);
        outer.edges.add(new Edge(outer, inner, 1, RIGHT));
        inner.edges.add(new Edge(inner, outer, 1, LEFT));

        outer = hall.get(hall.size() - 1);
        inner = hall.get(hall.size() - 2);
        outer.edges.add(new Edge(outer, inner, 1, LEFT));
        inner.edges.add(new Edge(inner, outer, 1, RIGHT));

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
                final Node hallNodeLeft = hall.get(i + 1);
                final Node hallNodeRight = hall.get(i + 2);
                colNode.edges.add(new Edge(colNode, hallNodeLeft, 2, UP));
                colNode.edges.add(new Edge(colNode, hallNodeRight, 2, UP));
                hallNodeLeft.edges.add(new Edge(hallNodeLeft, colNode, 2, DOWN));
                hallNodeRight.edges.add(new Edge(hallNodeRight, colNode, 2, DOWN));

                colNodes[0][i] = colNode;
                nodes.add(colNode);
            }

            for (int i = 1; i < 4; i++) {
                row = scanner.nextLine().trim().replace("#", "").split("");

                for (int j = 0; j < 4; j++) {
                    final Node colNode =
                            new Node(
                                    prevId.incrementAndGet(),
                                    row[j],
                                    new String[] {"A", "B", "C", "D"}[i]);
                    final Node prevNode = colNodes[i - 1][j];
                    colNode.edges.add(new Edge(colNode, prevNode, 1, UP));
                    prevNode.edges.add(new Edge(prevNode, colNode, 1, DOWN));
                    colNodes[i][j] = colNode;
                    nodes.add(colNode);
                }
            }
        }

        solve(new Game(nodes, 0));
    }

    public static void solve(final Game input) {
        final PriorityQueue<Game> games =
                new PriorityQueue<>(
                        Comparator.<Game>comparingInt(g -> g.numMoves)
                                .thenComparingInt(g -> g.score));
        games.add(input);
        final Map<String, Integer> gamesSeen = new HashMap<>();
        gamesSeen.put(input.compactString(), Integer.MAX_VALUE);

        while (true) {
            if (games.isEmpty()) {
                throw new IllegalStateException();
            }

            final Game currGame = games.remove();
            if (currGame.isSolved()) {
                System.out.println("Part 2: " + currGame.score);
                continue;
            }
            if (gamesSeen.size() % 10000 == 0) {
                System.out.println(
                        "\nCurrent game score: "
                                + currGame.score
                                + "\nGames left to assess: "
                                + games.size());
            }

            if (games.isEmpty() && gamesSeen.size() > 1) {
                System.out.println("Last game assessed:\n" + currGame);
            }

            generateMoves(currGame)
                    .forEach(
                            edge -> {
                                final Game newGame = currGame.makeMove(edge);
                                final String newGameStr = currGame.compactString();
                                final int prevScore =
                                        gamesSeen.getOrDefault(newGameStr, Integer.MAX_VALUE);
                                if (newGame.score < prevScore) {
                                    gamesSeen.put(newGameStr, newGame.score);
                                    games.add(newGame);
                                }
                            });
        }
    }

    public static List<Edge> generateMoves(final Game game) {
        final List<Edge> moves = new ArrayList<>();
        for (final Node node : game.nodesById.values()) {
            if (node.isEmpty()) continue;

            if (game.lastMove != null && game.lastMove.destId != node.id) {
                final Node lastNode = game.nodesById.get(game.lastMove.destId);
                // must keep moving lastNode
                if (!lastNode.shouldBeEmpty() && !lastNode.value.equals(lastNode.goal)) {
                    continue;
                }
            }

            moves.addAll(generateMoves(game, node));
        }

        return moves;
    }

    public static List<Edge> generateMoves(final Game game, final Node startNode) {
        if (game.lastMove != null && startNode.id == game.lastMove.destId) {
            return generateFurtherMoves(game, startNode);
        }

        return generateNewMoves(game, startNode);
    }

    public static List<Edge> generateFurtherMoves(final Game game, final Node lastMoved) {
        final List<Edge> moves = new ArrayList<>();

        for (final Edge edge : lastMoved.edges) {
            final Node dest = game.nodesById.get(edge.destId);

            if (VALID_CONTINUATIONS.get(game.lastMove.direction).contains(edge.direction)
                    && dest.isEmpty()
                    && (dest.shouldBeEmpty()
                            || dest.goal.equals(lastMoved.value)
                            || game.lastMove.direction == UP)) {
                boolean shouldAdd = true;

                if (dest.goal.equals(lastMoved.value)) {
                    for (final Edge edge2 : dest.edges) {
                        final Node attached = game.nodesById.get(edge2.destId);
                        if (!attached.shouldBeEmpty()
                                && !attached.isEmpty()
                                && !attached.goal.equals(attached.value)) {
                            shouldAdd = false;
                            break;
                        }
                    }
                }

                if (shouldAdd) {
                    moves.add(edge);
                }
            }
        }

        return moves;
    }

    public static List<Edge> generateNewMoves(final Game game, final Node startNode) {
        final List<Edge> moves = new ArrayList<>();

        for (final Edge edge : startNode.edges) {
            final Node dest = game.nodesById.get(edge.destId);
            if (dest.isEmpty()
                    && (dest.shouldBeEmpty()
                            || dest.goal.equals(startNode.value)
                            || edge.direction == UP)) {
                boolean shouldAdd = true;

                if (dest.goal.equals(startNode.value)) {
                    for (final Edge edge2 : dest.edges) {
                        final Node attached = game.nodesById.get(edge2.destId);
                        if (!attached.shouldBeEmpty()
                                && !attached.isEmpty()
                                && !attached.goal.equals(attached.value)) {
                            shouldAdd = false;
                            break;
                        }
                    }
                }

                if (shouldAdd) {
                    moves.add(edge);
                }
            }
        }

        return moves;
    }

    public static class Game {
        public final Map<Integer, Node> nodesById;
        public final int score;

        public int numMoves = 0;
        public Edge lastMove;

        public Game(final Set<Node> nodes, final int score) {
            this.nodesById = nodes.stream().collect(toMap(n -> n.id, identity()));
            this.score = score;
        }

        public Game makeMove(final Edge edge) {
            final Node one = nodesById.get(edge.startId);
            final Node two = nodesById.get(edge.destId);
            final Node newOne = new Node(one.id, two.value, one.goal);
            final Node newTwo = new Node(two.id, one.value, two.goal);
            newOne.edges.addAll(one.edges);
            newTwo.edges.addAll(two.edges);

            final Set<Node> newNodes = new HashSet<>(nodesById.values());
            newNodes.remove(one);
            newNodes.remove(two);
            newNodes.add(newOne);
            newNodes.add(newTwo);

            final Game newGame = new Game(newNodes, this.score + getScore(newTwo.value, edge.cost));
            newGame.numMoves = this.numMoves + 1;
            newGame.lastMove = edge;

            return newGame;
        }

        public boolean isSolved() {
            return nodesById.values().stream().allMatch(n -> n.value.equals(n.goal));
        }

        public static int getScore(final String value, final int distance) {
            switch (value) {
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
                    + IntStream.range(0, 2).mapToObj(i -> nodesById.get(i).value).collect(joining())
                    + "."
                    + IntStream.range(2, 3).mapToObj(i -> nodesById.get(i).value).collect(joining())
                    + "."
                    + IntStream.range(3, 4).mapToObj(i -> nodesById.get(i).value).collect(joining())
                    + "."
                    + IntStream.range(4, 5).mapToObj(i -> nodesById.get(i).value).collect(joining())
                    + "."
                    + IntStream.range(5, 7).mapToObj(i -> nodesById.get(i).value).collect(joining())
                    + "#\n###"
                    + IntStream.range(7, 11)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "###\n  #"
                    + IntStream.range(11, 15)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(15, 19)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(19, 23)
                            .mapToObj(i -> nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #########";
        }

        public String compactString() {
            return IntStream.range(0, 23).mapToObj(i -> nodesById.get(i).value).collect(joining());
        }
    }

    public static class Node {
        public final int id;
        public final String value;
        public final String goal;
        public final Set<Edge> edges = new HashSet<>();

        public Node(final int id) {
            this.id = id;
            this.value = ".";
            this.goal = ".";
        }

        public Node(final int id, final String value, final String goal) {
            this.id = id;
            this.value = value;
            this.goal = goal;
        }

        public boolean isEmpty() {
            return this.value.equals(".");
        }

        public boolean shouldBeEmpty() {
            return this.goal.equals(".");
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Node)) return false;
            return id == ((Node) o).id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class Edge {
        public enum Direction {
            LEFT,
            RIGHT,
            UP,
            DOWN;

            public static final Map<Direction, Set<Direction>> VALID_CONTINUATIONS =
                    Map.of(
                            LEFT, Set.of(LEFT, DOWN),
                            RIGHT, Set.of(RIGHT, DOWN),
                            UP, Set.of(UP, LEFT, RIGHT),
                            DOWN, Set.of(DOWN, LEFT, RIGHT));
        }

        public final int startId;
        public final int destId;
        public final int cost;
        public final Direction direction;

        public Edge(final Node start, final Node dest, final int cost, final Direction direction) {
            this.startId = start.id;
            this.destId = dest.id;
            this.cost = cost;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return startId + "->" + destId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge)) return false;
            return this.toString().equals(o.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.toString());
        }
    }
}
