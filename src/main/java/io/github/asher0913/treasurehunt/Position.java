package io.github.asher0913.treasurehunt;

/**
 * Represents a coordinate on the game board.
 * <p>
 * The board is a two-dimensional grid of size SIZE×SIZE, where (0,0) refers
 * to the top‑left corner. This class provides utility methods for comparing
 * positions and converting to chess‑style notation.
 */
public class Position {
    /**
     * Row index on the board.
     * Value 0 corresponds to the topmost row, increasing downward.
     */
    public int x;

    /**
     * Column index on the board.
     * Value 0 corresponds to the leftmost column, increasing to the right.
     */
    public int y;

    /**
     * Create a new Position with the given row and column.
     *
     * @param x the row index (0 = top)
     * @param y the column index (0 = left)
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Two Position objects are equal if they refer to the same grid cell.
     *
     * @param o another object
     * @return true if o is a Position with the same x and y values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;            // same reference
        }
        if (!(o instanceof Position)) {
            return false;           // not even a Position
        }
        Position pos = (Position) o;
        return this.x == pos.x && this.y == pos.y;
    }

    /**
     * Compute a hash code consistent with equals().
     * Uses a simple formula to combine row and column.
     *
     * @return hash code for this Position
     */
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    /**
     * Return a string useful for debugging, showing numeric coordinates.
     *
     * @return "(x, y)" representation
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Convert this Position into chess‑style notation:
     * <ul>
     *   <li>Columns labeled 'a' through 't' for 20 columns</li>
     *   <li>Rows labeled 20 down to 1 (so row index 0 becomes '20')</li>
     * </ul>
     *
     * @return a string like "a1", "b4", etc.
     */
    public String toNotation() {
        // column letter: 'a' + y
        char col = (char) ('a' + y);
        // row number: invert x so that x=0 → 20, x=SIZE-1 → 1
        int row = GameMap.SIZE - x;
        return "" + col + row;
    }
}