package ads.game;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Treasure Hunt application.
 * <p>
 * Initializes the Swing UI on the Event Dispatch Thread to ensure
 * all GUI operations are performed in a thread‑safe manner.
 */
public class Main {
    /**
     * Main method launches the game window.
     * <p>
     * Uses SwingUtilities.invokeLater to schedule the creation and
     * display of the GameUI on the Swing event thread.
     *
     * @param args command‑line arguments
     */
    public static void main(String[] args) {
        // Schedule a task for the Event Dispatch Thread:
        // creating and showing the GameUI.
        SwingUtilities.invokeLater(() -> {
            // Instantiate the main game window
            GameUI ui = new GameUI();
            // Make the window visible
            ui.setVisible(true);
            // Request focus so that key bindings (WSAD controls) will work immediately
            ui.requestFocusInWindow();
        });
    }
}
