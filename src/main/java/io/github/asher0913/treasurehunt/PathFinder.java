package io.github.asher0913.treasurehunt;

import java.util.*;

/**
 * Provides pathfinding functionality on the game map.
 * <p>
 * Supports two algorithms:
 * <ul>
 *   <li>A* (findPathAStar): uses a heuristic (Manhattan distance) to guide search.</li>
 *   <li>BFS (findPathBFS): a simple breadth‑first search to find the shortest path.</li>
 * </ul>
 * Both methods return a list of Positions from the start to the first treasure found.
 */
public class PathFinder {
    private GameMap gameMap;

    /**
     * Create a PathFinder for the given map.
     *
     * @param map the GameMap on which to perform searches
     */
    public PathFinder(GameMap map) {
        this.gameMap = map;
    }

    // --- A* algorithm implementation ---

    /**
     * Internal node class for A* search.
     * Holds the position, cost from start (g), heuristic cost (h), and total cost (f).
     */
    /** Nodes taken off the frontier and expanded by the most recent search. */
    private int lastExpansions;

    public int getLastExpansions() {
        return lastExpansions;
    }

    private record Node(Position pos, Node parent, int g, int f) {}

    /** Every treasure on the board, so the heuristic does not rescan the grid for each node. */
    private List<Position> treasures() {
        List<Position> found = new ArrayList<>();
        for (int i = 0; i < GameMap.SIZE; i++) {
            for (int j = 0; j < GameMap.SIZE; j++) {
                if (gameMap.getCell(i, j) == 'T') {
                    found.add(new Position(i, j));
                }
            }
        }
        return found;
    }

    /** Manhattan distance to the nearest treasure: admissible and consistent on a 4-connected grid. */
    private static int heuristic(Position pos, List<Position> treasures) {
        int best = Integer.MAX_VALUE;
        for (Position t : treasures) {
            best = Math.min(best, Math.abs(pos.x - t.x) + Math.abs(pos.y - t.y));
        }
        return treasures.isEmpty() ? 0 : best;
    }

    /**
     * A* to the nearest treasure. Ties on f are broken towards the larger g (deeper nodes), which on
     * open ground walks straight at the goal instead of widening a diamond of equal-f nodes. Because
     * the heuristic is consistent, a position is final the first time it is expanded, so later
     * copies of it on the frontier are skipped.
     */
    public List<Position> findPathAStar(Position start) {
        List<Position> goals = treasures();
        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(Node::f).thenComparing(Comparator.comparingInt(Node::g).reversed()));
        Map<Position, Integer> bestG = new HashMap<>();
        Set<Position> closed = new HashSet<>();
        open.add(new Node(start, null, 0, heuristic(start, goals)));
        bestG.put(start, 0);
        lastExpansions = 0;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (!closed.add(current.pos())) {
                continue; // a stale, longer copy of a position already expanded
            }
            lastExpansions++;
            if (gameMap.getCell(current.pos().x, current.pos().y) == 'T') {
                List<Position> path = new ArrayList<>();
                for (Node node = current; node != null; node = node.parent()) {
                    path.add(node.pos());
                }
                Collections.reverse(path);
                return path;
            }
            for (int i = 0; i < 4; i++) {
                int nx = current.pos().x + dx[i];
                int ny = current.pos().y + dy[i];
                if (nx < 0 || nx >= GameMap.SIZE || ny < 0 || ny >= GameMap.SIZE || gameMap.getCell(nx, ny) == 'X') {
                    continue;
                }
                Position next = new Position(nx, ny);
                int g = current.g() + 1;
                if (closed.contains(next) || g >= bestG.getOrDefault(next, Integer.MAX_VALUE)) {
                    continue;
                }
                bestG.put(next, g);
                open.add(new Node(next, current, g, g + heuristic(next, goals)));
            }
        }
        return new ArrayList<>(); // no treasure reachable
    }

    /**
     * Find a path from the start position to the nearest treasure using breadth‑first search.
     *
     * @param start the starting Position
     * @return a List of Positions representing the path, including start and treasure; empty if none
     */
    public List<Position> findPathBFS(Position start) {
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> parentMap = new HashMap<>();
        boolean[][] visited = new boolean[GameMap.SIZE][GameMap.SIZE];

        queue.add(start);
        visited[start.x][start.y] = true;
        Position target = null;
        lastExpansions = 0;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            lastExpansions++;

            // check if we've reached a treasure
            if (gameMap.getCell(current.x, current.y) == 'T') {
                target = current;
                break;
            }

            // enqueue valid neighbors
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (nx < 0 || nx >= GameMap.SIZE || ny < 0 || ny >= GameMap.SIZE) {
                    continue; // skip out‑of‑bounds
                }
                if (visited[nx][ny] || gameMap.getCell(nx, ny) == 'X') {
                    continue; // skip visited or obstacle
                }

                Position neighbor = new Position(nx, ny);
                queue.add(neighbor);
                visited[nx][ny] = true;
                parentMap.put(neighbor, current);
            }
        }

        // reconstruct path if found
        List<Position> path = new ArrayList<>();
        if (target == null) {
            return path; // no treasure reachable
        }
        // backtrack from target to start
        for (Position cur = target; !cur.equals(start); cur = parentMap.get(cur)) {
            path.add(cur);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }
}
