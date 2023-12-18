package tech.readonly.aoc.aoc2021;

import tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
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
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.DOWN;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.DOWN_AND_LEFT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.DOWN_AND_RIGHT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.LEFT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.RIGHT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.UP;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.UP_AND_LEFT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.UP_AND_RIGHT;
import static tech.readonly.aoc.aoc2021.Day23Part2.Edge.Direction.VALID_CONTINUATIONS;

public class Day23Part2 {
    public static final int HALL_LENGTH = 7;
    public static final int NUM_COLS = 4;
    public static final int COL_DEPTH = 4;
    public static final int SCORE_CUTOFF = 45376; // got this manually
    public static final List<String> COLUMN_GOALS = List.of("A", "B", "C", "D");

    public static void main(final String[] args) throws Exception {
        final AtomicInteger prevId = new AtomicInteger(-1);

        final List<Node> hall =
                IntStream.range(0, HALL_LENGTH)
                        .mapToObj(i -> new Node(prevId.incrementAndGet()))
                        .collect(toList());
        for (int i = 2; i < hall.size() - 1; i++) {
            final Node curr = hall.get(i);
            final Node prev = hall.get(i - 1);
            curr.edges.add(new Edge(curr, prev, 2, LEFT));
            prev.edges.add(new Edge(prev, curr, 2, RIGHT));
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
        final Node[][] colNodes = new Node[NUM_COLS][COL_DEPTH];

        try (final Scanner scanner = new Scanner(
                new File(ClassLoader.getSystemResource("inputs/day23.txt").getFile()))) {
            scanner.nextLine();
            scanner.nextLine();

            String[] row = scanner.nextLine().trim().replace("#", "").split("");
            for (int i = 0; i < NUM_COLS; i++) {
                final Node colNode =
                        new Node(prevId.incrementAndGet(), row[i], COLUMN_GOALS.get(i));
                final Node hallNodeLeft = hall.get(i + 1);
                final Node hallNodeRight = hall.get(i + 2);
                colNode.edges.add(new Edge(colNode, hallNodeLeft, 2, UP_AND_LEFT));
                colNode.edges.add(new Edge(colNode, hallNodeRight, 2, UP_AND_RIGHT));
                hallNodeLeft.edges.add(new Edge(hallNodeLeft, colNode, 2, DOWN_AND_RIGHT));
                hallNodeRight.edges.add(new Edge(hallNodeRight, colNode, 2, DOWN_AND_LEFT));

                colNodes[0][i] = colNode;
                nodes.add(colNode);
            }

            for (int i = 1; i < COL_DEPTH; i++) {
                row = scanner.nextLine().trim().replace("#", "").split("");

                for (int j = 0; j < NUM_COLS; j++) {
                    final Node colNode =
                            new Node(prevId.incrementAndGet(), row[j], COLUMN_GOALS.get(j));
                    final Node prevNode = colNodes[i - 1][j];
                    colNode.edges.add(new Edge(colNode, prevNode, 1, UP));
                    prevNode.edges.add(new Edge(prevNode, colNode, 1, DOWN));
                    colNodes[i][j] = colNode;
                    nodes.add(colNode);
                }
            }
        }

        solve(new Game(nodes));
    }

    public static void solve(final Game input) {
        final PriorityQueue<Game> games =
                new PriorityQueue<>(
                        Comparator.<Game>comparingInt(g -> g.numMoves)
                                .thenComparingInt(g -> g.score));
        games.add(input);
        final Map<String, Integer> gamesSeen = new HashMap<>();
        gamesSeen.put(input.compactString(), Integer.MAX_VALUE);

        int minAnswer = Integer.MAX_VALUE;

        do {
            final Game currGame = games.remove();
            if (currGame.isSolved()) {
                minAnswer = Math.min(minAnswer, currGame.score);
                continue;
            }

            final List<Edge> moves = getAllPossibleMoves(currGame);
            for (final Edge move : moves) {
                final Game newGame = currGame.makeMove(move);
                if (newGame.score > SCORE_CUTOFF) {
                    continue;
                }

                final String newGameStr = newGame.compactString();
                final int prevScore = gamesSeen.getOrDefault(newGameStr, Integer.MAX_VALUE);

                if (newGame.score < prevScore) {
                    gamesSeen.put(newGameStr, newGame.score);
                    games.add(newGame);
                }
            }
        } while (!games.isEmpty());

        System.out.println("Part 2: " + minAnswer);
    }

    public static List<Edge> getAllPossibleMoves(final Game game) {
        final List<Edge> moves = new ArrayList<>();
        final Node lastNode =
                game.lastMove == null ? null : game.nodesById.get(game.lastMove.destId);

        if (lastNode != null && !isNodeFinishedMoving(lastNode, game)) {
            if (game.lastMove.direction == UP
                    || (lastNode.numCompletedMoves == 1
                            && (game.lastMove.direction == LEFT
                                    || game.lastMove.direction == RIGHT))) {
                return getMovesForNode(game, lastNode);
            }
            if (game.lastMove.direction.name().startsWith("DOWN")
                    && game.nodesById.containsKey(lastNode.id + NUM_COLS)
                    && game.nodesById.get(lastNode.id + NUM_COLS).isEmpty()) {
                return getMovesForNode(game, lastNode);
            }
        }

        for (final Node node : game.nodesById.values()) {
            if (node.isEmpty() || isNodeFinishedMoving(node, game)) continue;

            moves.addAll(getMovesForNode(game, node));
        }

        // always prioritize moving nodes into place
        if (moves.stream().anyMatch(e -> e.direction.name().startsWith("DOWN"))) {
            moves.removeIf(e -> !e.direction.name().startsWith("DOWN"));
        }

        return moves;
    }

    public static List<Edge> getMovesForNode(final Game game, final Node startNode) {
        if (startNode.numCompletedMoves == 1
                && !game.hasColumnEmptied(COLUMN_GOALS.indexOf(startNode.value) + HALL_LENGTH)) {
            return List.of();
        }

        final List<Edge> moves =
                getValidMovesForNode(
                        game,
                        startNode,
                        game.lastMove != null && startNode.id == game.lastMove.destId);

        return moves;
    }

    public static List<Edge> getValidMovesForNode(
            final Game game, final Node start, final boolean continuing) {
        final List<Edge> moves = new ArrayList<>();
        final Set<Direction> validDirections =
                continuing
                        ? VALID_CONTINUATIONS.get(game.lastMove.direction)
                        : EnumSet.allOf(Direction.class);

        for (final Edge edge : start.edges) {
            final Node dest = game.nodesById.get(edge.destId);
            if (!dest.isEmpty() || !validDirections.contains(edge.direction)) continue;

            if (dest.shouldBeEmpty()
                    || (continuing && game.lastMove.direction == UP)
                    || (!continuing && edge.direction == UP)
                    || (dest.goal.equals(start.value) && game.hasColumnEmptied(dest.id))) {
                moves.add(edge);
            }
        }

        return moves;
    }

    public static boolean isNodeFinishedMoving(final Node node, final Game game) {
        if (!node.value.equals(node.goal)) {
            return false;
        }

        if (node.id >= game.nodesById.size() - NUM_COLS) {
            return true;
        }

        final Node nodeBelow = game.nodesById.get(node.id + NUM_COLS);
        return nodeBelow.value.equals(nodeBelow.goal);
    }

    public static class Game {
        public final Map<Integer, Node> nodesById;
        public final int score;
        public final int numMoves;
        public final Edge lastMove;
        public final List<Game> ancestors = new ArrayList<>();

        public Game(final Set<Node> nodes) {
            this(nodes, 0, 0, null);
        }

        private Game(
                final Set<Node> nodes, final int score, final int numMoves, final Edge lastMove) {
            this.nodesById = nodes.stream().collect(toMap(n -> n.id, identity()));
            this.score = score;
            this.numMoves = numMoves;
            this.lastMove = lastMove;
        }

        public Game makeMove(final Edge move) {
            final Node one = this.nodesById.get(move.startId);
            final Node two = this.nodesById.get(move.destId);

            final Set<Node> newNodes = new HashSet<>(this.nodesById.values());
            newNodes.remove(one);
            newNodes.remove(two);

            final Node newOne = new Node(one, two.value, 0);
            final Node newTwo = new Node(two, one.value, one.numCompletedMoves);
            newNodes.add(newOne);
            newNodes.add(newTwo);

            if (this.lastMove != null && move.startId != this.lastMove.destId) {
                final Node lastNode = this.nodesById.get(this.lastMove.destId);
                newNodes.remove(lastNode);

                final Node newLastNode = lastNode.makeMoveCompleted();
                newNodes.add(newLastNode);
            }

            final Game newGame =
                    new Game(
                            newNodes,
                            this.score + getScore(newTwo.value, move.cost),
                            this.numMoves + 1,
                            move);
            newGame.ancestors.addAll(this.ancestors);
            newGame.ancestors.add(this);

            return newGame;
        }

        public boolean isSolved() {
            return this.nodesById.values().stream().allMatch(n -> n.value.equals(n.goal));
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

        public boolean hasColumnEmptied(final int nodeId) {
            final int columnId = (nodeId - HALL_LENGTH) % NUM_COLS;
            for (int i = 0; i < COL_DEPTH; i++) {
                final Node colNode = this.nodesById.get((i * NUM_COLS) + HALL_LENGTH + columnId);
                if (!colNode.isEmpty() && !colNode.value.equals(colNode.goal)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Game)) return false;

            final Game other = (Game) o;
            for (int i = 0; i < 23; i++) {
                if (!this.nodesById.get(i).value.equals(other.nodesById.get(i).value)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(
                    this.nodesById.values().stream().map(n -> n.value).toArray(String[]::new));
        }

        @Override
        public String toString() {
            return "#############\n#"
                    + IntStream.range(0, 2)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining())
                    + "."
                    + IntStream.range(2, 3)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining())
                    + "."
                    + IntStream.range(3, 4)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining())
                    + "."
                    + IntStream.range(4, 5)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining())
                    + "."
                    + IntStream.range(5, 7)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining())
                    + "#\n###"
                    + IntStream.range(7, 11)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining("#"))
                    + "###\n  #"
                    + IntStream.range(11, 15)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(15, 19)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #"
                    + IntStream.range(19, 23)
                            .mapToObj(i -> this.nodesById.get(i).value)
                            .collect(joining("#"))
                    + "#\n  #########";
        }

        public String compactString() {
            return IntStream.range(0, 23)
                    .mapToObj(i -> this.nodesById.get(i).value)
                    .collect(joining());
        }
    }

    public static class Node {
        public final int id;
        public final String value;
        public final String goal;
        public final Set<Edge> edges = new HashSet<>();
        public final int numCompletedMoves;

        public Node(final int id) {
            this.id = id;
            this.value = ".";
            this.goal = ".";
            this.numCompletedMoves = 0;
        }

        public Node(final int id, final String value, final String goal) {
            this.id = id;
            this.value = value;
            this.goal = goal;
            this.numCompletedMoves = 0;
        }

        public Node(final Node base, final String value, final int numCompletedMoves) {
            this.id = base.id;
            this.value = value;
            this.goal = base.goal;
            this.edges.addAll(base.edges);
            this.numCompletedMoves = numCompletedMoves;
        }

        public Node makeMoveCompleted() {
            int increment = 1;
            if (this.numCompletedMoves == 0 && !this.shouldBeEmpty()) {
                // moved out, through the hall, and into goal in 1 move
                // count it as 1 move into the hall and 1 out, to make other logic simpler
                increment = 2;
            }

            return new Node(this, this.value, this.numCompletedMoves + increment);
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
            return this.id == ((Node) o).id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id);
        }
    }

    public static class Edge {
        public enum Direction {
            LEFT,
            RIGHT,
            UP,
            DOWN,
            UP_AND_LEFT,
            UP_AND_RIGHT,
            DOWN_AND_LEFT,
            DOWN_AND_RIGHT;

            public static final Map<Direction, Set<Direction>> VALID_CONTINUATIONS =
                    Map.of(
                            LEFT, Set.of(LEFT, DOWN_AND_LEFT),
                            RIGHT, Set.of(RIGHT, DOWN_AND_RIGHT),
                            UP, Set.of(UP, UP_AND_LEFT, UP_AND_RIGHT),
                            DOWN, Set.of(DOWN),
                            UP_AND_LEFT, Set.of(LEFT, DOWN_AND_LEFT),
                            UP_AND_RIGHT, Set.of(RIGHT, DOWN_AND_RIGHT),
                            DOWN_AND_LEFT, Set.of(DOWN),
                            DOWN_AND_RIGHT, Set.of(DOWN));
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
            return this.startId + "->" + this.destId;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Edge)) return false;
            return this.toString().equals(o.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.toString());
        }
    }
}
