package io.github.asher0913.treasurehunt;

import java.util.*;

/**
 * Manages the core game logic for Treasure Hunt.
 * <p>
 * The player moves around a grid, uncovers obstacles, and searches for hidden treasures.
 * Supports two hint modes (A* and BFS) and keeps track of score and found treasures.
 */
public class TreasureHuntGame {
    private GameMap map;                      // The underlying game board
    private Player player;                    // Tracks player position and score
    private PathFinder pathFinder;            // Provides A* and BFS pathfinding
    private int treasuresFound;               // How many treasures have been located
    private final int totalTreasures = 3;     // Total treasures to find
    private Set<Position> foundTreasures;     // Positions of treasures already uncovered

    /**
     * Initialize a new game: create map, place player, set up pathfinder.
     */
    public TreasureHuntGame() {
        map = new GameMap();
        Position start = generatePlayerStart();
        player = new Player(start);
        map.setCell(start.x, start.y, 'P');
        pathFinder = new PathFinder(map);
        treasuresFound = 0;
        foundTreasures = new HashSet<>();
    }

    /**
     * Pick a random empty cell on the map for the player's starting point.
     * Loops until it finds a '.' cell.
     */
    private Position generatePlayerStart() {
        Random rand = new Random();
        while (true) {
            int x = rand.nextInt(GameMap.SIZE);
            int y = rand.nextInt(GameMap.SIZE);
            if (map.getCell(x, y) == '.') {
                return new Position(x, y);
            }
        }
    }

    /**
     * Move the player in one of four directions.
     *
     * @param direction "W", "A", "S", or "D" for up, left, down, right
     * @param steps     how many cells to move (normally 1)
     * @return a message describing the result (boundary hit, obstacle, treasure, or normal move)
     */
    public String movePlayer(String direction, int steps) {
        Position current = player.getPosition();
        Position newPos = new Position(current.x, current.y);

        // determine new coordinates based on input
        switch (direction.toUpperCase()) {
            case "W":
                newPos.x -= steps;
                break;
            case "S":
                newPos.x += steps;
                break;
            case "A":
                newPos.y -= steps;
                break;
            case "D":
                newPos.y += steps;
                break;
            default:
                return "Invalid direction!";
        }

        // check map boundaries
        if (newPos.x < 0 || newPos.x >= GameMap.SIZE
                || newPos.y < 0 || newPos.y >= GameMap.SIZE) {
            return "You hit the boundary!";
        }

        // deduct 1 point for a manual move
        player.adjustScore(-1);

        char cell = map.getCell(newPos.x, newPos.y);

        // obstacle handling
        if (cell == 'X') {
            map.revealCell(newPos.x, newPos.y);   // reveal it on the map
            player.adjustScore(-10);              // penalty for obstacle
            return "You hit an obstacle at "
                    + newPos.toNotation()
                    + "! Score deducted by 10.";
        }

        // clear the old position: keep 'F' if it was a found treasure
        if (foundTreasures.contains(current)) {
            map.setCell(current.x, current.y, 'F');
        } else {
            map.setCell(current.x, current.y, '.');
        }

        // treasure handling
        if (cell == 'T') {
            treasuresFound++;
            foundTreasures.add(new Position(newPos.x, newPos.y));
            player.setPosition(newPos);
            map.setCell(newPos.x, newPos.y, 'F');  // mark as found
            return "Treasure found at "
                    + newPos.toNotation()
                    + "! Total treasures: "
                    + treasuresFound;
        }

        // normal move
        player.setPosition(newPos);
        map.setCell(newPos.x, newPos.y, 'P');
        return "Moved to " + newPos.toNotation() + ".";
    }

    /**
     * Provide a hint using the A* algorithm.
     * Deducts 2 points and returns the next step toward the nearest treasure.
     *
     * @return A* hint message
     */
    public String getHintAStar() {
        player.adjustScore(-2);
        List<Position> path = pathFinder.findPathAStar(player.getPosition());
        if (path.size() < 2) {
            return "A* Hint: No valid next step found!";
        }
        Position next = path.get(1);
        return "A* Hint: Next step " + next.toNotation();
    }

    /**
     * Provide a hint using the BFS algorithm.
     * Deducts 2 points and returns the next step toward the nearest treasure.
     *
     * @return BFS hint message
     */
    public String getHintBFS() {
        player.adjustScore(-2);
        List<Position> path = pathFinder.findPathBFS(player.getPosition());
        if (path.size() < 2) {
            return "BFS Hint: No valid next step found!";
        }
        Position next = path.get(1);
        return "BFS Hint: Next step " + next.toNotation();
    }

    /** @return the game map instance */
    public GameMap getMap() {
        return map;
    }

    /** @return the player instance */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return a summary string of current score and treasure count
     */
    public String getGameStatus() {
        return "Score: "
                + player.getScore()
                + " | Treasures: "
                + treasuresFound
                + "/"
                + totalTreasures;
    }

    /**
     * Determine if the game has ended:
     * either all treasures are found, or the player's score has dropped to zero.
     */
    public boolean isGameOver() {
        return treasuresFound >= totalTreasures
                || player.getScore() <= 0;
    }
}