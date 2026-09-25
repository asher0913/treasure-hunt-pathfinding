package io.github.asher0913.treasurehunt;

/**
 * Represents the player in the Treasure Hunt game.
 * <p>
 * This class keeps track of the player's current location on the map
 * and their remaining score. The player starts with a fixed score
 * and loses points for moves, hints, and collisions.
 */
public class Player {
    /**
     * The player's current position on the game grid.
     * (0,0) corresponds to the top‑left corner.
     */
    private Position pos;

    /**
     * The player's score. Starts at 100 and is adjusted
     * downward as the player moves or uses hints.
     */
    private int score;

    /**
     * Construct a new Player at the specified starting position.
     * The score is initialized to 100.
     *
     * @param start the initial grid position of the player
     */
    public Player(Position start) {
        this.pos = start;
        this.score = 100;
    }

    /**
     * Retrieve the player's current position.
     *
     * @return the Position object representing where the player is
     */
    public Position getPosition() {
        return pos;
    }

    /**
     * Update the player's position.
     * This method should be called whenever the player moves
     * to a new cell on the grid.
     *
     * @param pos the new Position to set for the player
     */
    public void setPosition(Position pos) {
        this.pos = pos;
    }

    /**
     * Get the player's current score.
     *
     * @return the integer score remaining for the player
     */
    public int getScore() {
        return score;
    }

    /**
     * Adjust the player's score by the given amount.
     * This is used for move penalties, hint costs, and obstacle collisions.
     *
     * @param delta the amount to add (or subtract, if negative) from the score
     */
    public void adjustScore(int delta) {
        if (score == 0 && delta < 0) {
            return;
        }
        score = Math.max(0, score + delta);
    }
}