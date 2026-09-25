package io.github.asher0913.treasurehunt;

import java.util.List;

/**
 * A* against breadth-first search on seeded random boards: both must find a shortest path to the
 * nearest treasure, and the interesting number is how many cells each expands to do it.
 *
 * <pre>mvn -q compile exec:java -Dexec.mainClass=io.github.asher0913.treasurehunt.PathfindingBenchmark</pre>
 */
public final class PathfindingBenchmark {
    private PathfindingBenchmark() {}

    public record Result(int boards, int reachable, int sameLength, double bfsExpansions, double aStarExpansions) {}

    public static Result run(int boards, int obstacles, int treasures) {
        int reachable = 0;
        int sameLength = 0;
        long bfs = 0;
        long aStar = 0;
        for (int seed = 0; seed < boards; seed++) {
            GameMap map = new GameMap(seed, obstacles, treasures);
            Position start = new Position(0, 0);
            if (map.getCell(0, 0) != '.') {
                map.setCell(0, 0, '.');
            }
            PathFinder finder = new PathFinder(map);
            List<Position> b = finder.findPathBFS(start);
            int bfsCount = finder.getLastExpansions();
            List<Position> a = finder.findPathAStar(start);
            int aStarCount = finder.getLastExpansions();
            if (b.isEmpty()) {
                continue;
            }
            reachable++;
            sameLength += a.size() == b.size() ? 1 : 0;
            bfs += bfsCount;
            aStar += aStarCount;
        }
        return new Result(boards, reachable, sameLength, (double) bfs / reachable, (double) aStar / reachable);
    }

    public static void main(String[] args) {
        System.out.println("| Obstacles | Treasures | Boards with a reachable treasure | A* path = BFS path length | BFS expansions | A* expansions |");
        System.out.println("|---:|---:|---:|---:|---:|---:|");
        for (int[] setting : new int[][] {{15, 3}, {15, 1}, {80, 3}, {80, 1}, {140, 1}}) {
            Result r = run(1000, setting[0], setting[1]);
            System.out.printf("| %d | %d | %d | %d | %.1f | %.1f |%n",
                    setting[0], setting[1], r.reachable(), r.sameLength(), r.bfsExpansions(), r.aStarExpansions());
        }
    }
}
