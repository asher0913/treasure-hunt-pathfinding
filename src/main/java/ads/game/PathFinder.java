package ads.game;

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
    private class Node implements Comparable<Node> {
        Position pos;    // current grid position
        Node parent;     // previous node in the path
        int g;           // cost from start to this node
        int h;           // estimated cost from this node to nearest treasure
        int f;           // total estimated cost (g + h)

        Node(Position pos, Node parent, int g, int h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        /**
         * Compare nodes by their total cost f = g + h.
         * Lower f has higher priority.
         */
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
    }

    /**
     * Compute the Manhattan distance from the given position to the nearest hidden treasure.
     * This serves as the heuristic function for A*.
     *
     * @param pos the position from which to estimate
     * @return the smallest |dx|+|dy| distance to any 'T' cell, or 0 if none found
     */
    private int heuristic(Position pos) {
        int minDistance = Integer.MAX_VALUE;
        // scan the entire map for hidden treasures ('T')
        for (int i = 0; i < GameMap.SIZE; i++) {
            for (int j = 0; j < GameMap.SIZE; j++) {
                if (gameMap.getCell(i, j) == 'T') {
                    int dx = Math.abs(pos.x - i);
                    int dy = Math.abs(pos.y - j);
                    minDistance = Math.min(minDistance, dx + dy);
                }
            }
        }
        // if no treasure is found, return 0 so h does not bias the search
        return (minDistance == Integer.MAX_VALUE) ? 0 : minDistance;
    }

    /**
     * Find a path from the start position to the nearest treasure using A* search.
     *
     * @param start the starting Position
     * @return a List of Positions representing the path, including start and treasure; empty if none
     */
    public List<Position> findPathAStar(Position start) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(); // nodes to be evaluated
        Set<Position> closedSet = new HashSet<>();           // nodes already evaluated

        // initialize with the starting node
        openSet.add(new Node(start, null, 0, heuristic(start)));
        Node targetNode = null;

        // possible moves: up, down, left, right
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // if we've reached a treasure, stop
            if (gameMap.getCell(current.pos.x, current.pos.y) == 'T') {
                targetNode = current;
                break;
            }

            closedSet.add(current.pos);

            // examine neighbors
            for (int i = 0; i < 4; i++) {
                int nx = current.pos.x + dx[i];
                int ny = current.pos.y + dy[i];

                // skip out‑of‑bounds
                if (nx < 0 || nx >= GameMap.SIZE || ny < 0 || ny >= GameMap.SIZE) {
                    continue;
                }
                // skip obstacles
                if (gameMap.getCell(nx, ny) == 'X') {
                    continue;
                }

                Position neighborPos = new Position(nx, ny);
                if (closedSet.contains(neighborPos)) {
                    continue; // already evaluated
                }

                int tentativeG = current.g + 1;          // cost to move one step
                int h = heuristic(neighborPos);          // estimated cost to goal
                Node neighborNode = new Node(neighborPos, current, tentativeG, h);
                openSet.add(neighborNode);
            }
        }

        // reconstruct path if we found a treasure
        List<Position> path = new ArrayList<>();
        if (targetNode == null) {
            return path; // no path found
        }
        for (Node node = targetNode; node != null; node = node.parent) {
            path.add(node.pos);
        }
        Collections.reverse(path);
        return path;
    }

    // --- BFS algorithm implementation ---

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

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Position current = queue.poll();

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
