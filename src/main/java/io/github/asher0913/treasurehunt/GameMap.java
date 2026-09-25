package io.github.asher0913.treasurehunt;

import java.util.Random;

/**
 * Represents the 20×20 game board for Treasure Hunt.
 * <p>
 * Internally stores:
 * <ul>
 *   <li>a grid of characters: '.' empty, 'X' obstacle, 'T' hidden treasure, 'F' found treasure, 'P' player</li>
 *   <li>a parallel boolean array to track which obstacles have been revealed</li>
 * </ul>
 */
public class GameMap {
    /** Board dimension (number of rows and columns). */
    public static final int SIZE = 20;

    /** Character grid holding the map contents. */
    private char[][] grid;

    /**
     * Flags for whether an obstacle at [i][j] has been revealed.
     * If false and grid[i][j]=='X', we still render it as '.'.
     */
    private boolean[][] revealed;

    /**
     * Initialize the map: fill with empty cells, then place obstacles and treasures.
     */
    private final Random rand;

    public GameMap() {
        this(new Random(), 15, 3);
    }

    /**
     * A reproducible board: the same seed, obstacle count and treasure count always give the same map.
     */
    public GameMap(long seed, int obstacles, int treasures) {
        this(new Random(seed), obstacles, treasures);
    }

    private GameMap(Random rand, int obstacles, int treasures) {
        this.rand = rand;
        grid     = new char[SIZE][SIZE];
        revealed = new boolean[SIZE][SIZE];
        initMap();                     // set all cells to empty
        generateObstacles(obstacles);  // place random obstacles
        generateTreasures(treasures);  // place hidden treasures
    }

    /**
     * Fill the entire grid with '.' and mark all revealed flags false.
     */
    private void initMap() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j]     = '.';
                revealed[i][j] = false;
            }
        }
    }

    /**
     * Randomly place the specified number of obstacles ('X') on empty cells.
     * Ensures no two obstacles share the same position.
     *
     * @param count how many obstacles to add
     */
    private void generateObstacles(int count) {
        int placed = 0;
        while (placed < count) {
            int x = rand.nextInt(SIZE);
            int y = rand.nextInt(SIZE);
            if (grid[x][y] == '.') {
                grid[x][y]     = 'X';
                revealed[x][y] = false; // obstacle starts hidden
                placed++;
            }
        }
    }

    /**
     * Randomly place the specified number of treasures ('T') on empty cells.
     * Treasures remain hidden until the player finds them.
     *
     * @param count how many treasures to add
     */
    private void generateTreasures(int count) {
        int placed = 0;
        while (placed < count) {
            int x = rand.nextInt(SIZE);
            int y = rand.nextInt(SIZE);
            if (grid[x][y] == '.') {
                grid[x][y] = 'T'; // hidden treasure
                placed++;
            }
        }
    }

    /**
     * Get the raw character stored at the given coordinates.
     *
     * @param x row index (0 = top)
     * @param y column index (0 = left)
     * @return the character in grid[x][y]
     */
    public char getCell(int x, int y) {
        return grid[x][y];
    }

    /**
     * Overwrite the character at the specified position.
     * Used for placing or removing player, found treasure, etc.
     *
     * @param x     row index
     * @param y     column index
     * @param value new character to store
     */
    public void setCell(int x, int y, char value) {
        grid[x][y] = value;
    }

    /**
     * Mark an obstacle as revealed so that getDisplayCell will show 'X' instead of '.'.
     *
     * @param x row index of the obstacle
     * @param y column index of the obstacle
     */
    public void revealCell(int x, int y) {
        revealed[x][y] = true;
    }


    /**
     * Determine what character should be shown on screen at (x,y).
     * <ul>
     *   <li>If it's an unrevealed obstacle ('X' & not revealed), show '.'</li>
     *   <li>If it's a hidden treasure ('T'), show '.'</li>
     *   <li>Otherwise show the actual stored character ('X', 'F', 'P', or '.')</li>
     * </ul>
     *
     * @param x row index
     * @param y column index
     * @return character to render on the UI
     */
    public char getDisplayCell(int x, int y) {
        char cell = grid[x][y];
        if (cell == 'X' && !revealed[x][y]) {
            return '.'; // keep obstacle hidden
        } else if (cell == 'T') {
            return '.'; // keep treasure hidden until found
        } else {
            return cell; // show player, revealed obstacle, or found treasure
        }
    }
}