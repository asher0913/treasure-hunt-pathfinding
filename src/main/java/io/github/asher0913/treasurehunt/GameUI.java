package io.github.asher0913.treasurehunt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main window for the Treasure Hunt game.
 * <p>
 * This class sets up the Swing UI, including the game board display,
 * control buttons (hints, note, restart, pause), status labels (score, timer),
 * key bindings for movement, and a countdown timer that penalizes the player
 * if they take more than 10 seconds between actions.
 */
public class GameUI extends JFrame {
    // Core game logic object
    private TreasureHuntGame game;

    // Panel that draws the grid, player, obstacles, and found treasures
    private BoardPanel boardPanel;

    // Label showing current score and number of treasures found
    private JLabel statusLabel;

    // Label showing remaining seconds on the 10-second countdown
    private JLabel timerLabel;

    // Buttons for requesting hints and controlling game flow
    private JButton hintAStarButton;
    private JButton hintBFSButton;
    private JButton noteButton;
    private JButton restartButton;
    private JButton pauseButton;

    // Swing timer that fires every 1 second to decrement the countdown
    private Timer countdownTimer;

    // How many seconds remain before automatic penalty
    private int remainingSeconds = 10;

    // Whether the countdown is currently paused
    private boolean paused = false;

    /**
     * Constructor: initializes game logic, UI components, key bindings, and timers.
     */
    public GameUI() {
        //Initialize game state
        initGame();

        //Basic window setup
        setTitle("Treasure Hunt Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 980);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //Create and add the board drawing panel in the center
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        //Build control panel with buttons and labels at the bottom
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(230, 230, 250));

        // Initialize control components
        hintAStarButton = new JButton("Hint (A*)");
        hintBFSButton   = new JButton("Hint (BFS)");
        noteButton      = new JButton("Note");
        restartButton   = new JButton("Restart");
        pauseButton     = new JButton("Pause");
        statusLabel     = new JLabel(game.getGameStatus()); // e.g. "Score: 95 | Treasures: 1/3"
        timerLabel      = new JLabel("Time: 10s");

        // Apply consistent font styling
        Font font = new Font("SansSerif", Font.BOLD, 14);
        for (JComponent comp : new JComponent[]{
                hintAStarButton, hintBFSButton, noteButton,
                restartButton, pauseButton, statusLabel, timerLabel
        }) {
            comp.setFont(font);
        }

        // Add components to control panel
        controlPanel.add(hintAStarButton);
        controlPanel.add(hintBFSButton);
        controlPanel.add(noteButton);
        controlPanel.add(restartButton);
        controlPanel.add(pauseButton);
        controlPanel.add(statusLabel);
        controlPanel.add(timerLabel);
        add(controlPanel, BorderLayout.SOUTH);

        // Set up key bindings for WSAD movement
        InputMap im = boardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = boardPanel.getActionMap();
        im.put(KeyStroke.getKeyStroke("W"), "moveUp");
        im.put(KeyStroke.getKeyStroke("S"), "moveDown");
        im.put(KeyStroke.getKeyStroke("A"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("D"), "moveRight");
        am.put("moveUp",    new MoveAction("W"));
        am.put("moveDown",  new MoveAction("S"));
        am.put("moveLeft",  new MoveAction("A"));
        am.put("moveRight", new MoveAction("D"));

        // Hint (A*) button: pause countdown, show hint, then resume
        hintAStarButton.addActionListener(e -> {
            if (game.isGameOver()) return; // no hints after game over
            pauseTimer();
            String hint = game.getHintAStar();
            JOptionPane.showMessageDialog(
                    this,
                    hint,
                    "Hint (A*)",
                    JOptionPane.INFORMATION_MESSAGE
            );
            statusLabel.setText(game.getGameStatus());
            boardPanel.repaint();
            resumeTimer();
            checkGameOver();
        });

        // Hint (BFS) button: same behavior as A*
        hintBFSButton.addActionListener(e -> {
            if (game.isGameOver()) return;
            pauseTimer();
            String hint = game.getHintBFS();
            JOptionPane.showMessageDialog(
                    this,
                    hint,
                    "Hint (BFS)",
                    JOptionPane.INFORMATION_MESSAGE
            );
            statusLabel.setText(game.getGameStatus());
            boardPanel.repaint();
            resumeTimer();
            checkGameOver();
        });

        // Note button: display instructions including timer rules
        noteButton.addActionListener(e -> {
            pauseTimer();
            String note =
                    "Game Instructions:\n" +
                            "1. Use W/A/S/D to move one cell per press.\n" +
                            "2. Each move costs 1 point; hitting an obstacle costs an additional 10 points.\n" +
                            "3. Treasures remain hidden until found; found treasures stay marked.\n" +
                            "4. You have a 10-second timer for each move or hint.\n" +
                            "   • If 10 seconds elapse, you lose 5 points automatically.\n" +
                            "   • Timer resets after every move or hint.\n" +
                            "5. Each hint costs 2 points and shows only the next step.\n" +
                            "6. Hitting the boundary shows a warning (no score deducted).\n" +
                            "7. Goal: find all three treasures before your score drops to zero.";
            JOptionPane.showMessageDialog(
                    this,
                    note,
                    "Game Note",
                    JOptionPane.INFORMATION_MESSAGE
            );
            resumeTimer();
        });

        // Restart button: confirm, reinitialize game, reset timer
        restartButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to restart?",
                    "Restart Game",
                    JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                initGame();
                statusLabel.setText(game.getGameStatus());
                boardPanel.repaint();
                boardPanel.requestFocusInWindow();
                resumeTimer();
            }
        });

        // Pause/Resume button for countdown timer
        pauseButton.addActionListener(e -> {
            if (paused) {
                resumeTimer();
                pauseButton.setText("Pause");
            } else {
                pauseTimer();
                pauseButton.setText("Resume");
            }
        });

        // Initialize countdown timer
        countdownTimer = new Timer(1000, e -> {
            if (!paused) {
                remainingSeconds--;
                if (remainingSeconds <= 0) {
                    // Auto-penalty when timer reaches zero
                    game.getPlayer().adjustScore(-5);
                    statusLabel.setText(game.getGameStatus());
                    JOptionPane.showMessageDialog(
                            this,
                            "Time's up! You lost 5 points.",
                            "Timeout",
                            JOptionPane.WARNING_MESSAGE
                    );
                    remainingSeconds = 10; // reset countdown
                }
                timerLabel.setText("Time: " + remainingSeconds + "s");
            }
        });
        countdownTimer.setInitialDelay(0); // start immediately
        countdownTimer.start();
    }

    /**
     * Pause the countdown timer (e.g., before showing a dialog).
     */
    private void pauseTimer() {
        paused = true;
        countdownTimer.stop();
    }

    /**
     * Resume the countdown timer and reset to full 10 seconds.
     */
    private void resumeTimer() {
        paused = false;
        remainingSeconds = 10;
        timerLabel.setText("Time: 10s");
        countdownTimer.start();
    }

    /**
     * Initialize or restart the core game logic.
     */
    private void initGame() {
        game = new TreasureHuntGame();
    }

    /**
     * If game is over (all treasures found or score ≤ 0), show game-over dialog.
     */
    private void checkGameOver() {
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Game Over!\n" + game.getGameStatus(),
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Inner class to handle WSAD key presses for player movement.
     * After moving, it resets the countdown timer and checks game status.
     */
    private class MoveAction extends AbstractAction {
        private final String direction;

        MoveAction(String direction) {
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (game.isGameOver()) return;

            // Perform move of one cell in specified direction
            String msg = game.movePlayer(direction, 1);
            statusLabel.setText(game.getGameStatus());
            boardPanel.repaint();
            resumeTimer(); // reset and restart timer after move

            // Show contextual feedback dialogs
            if (msg.contains("boundary")) {
                JOptionPane.showMessageDialog(
                        GameUI.this,
                        "You hit the boundary!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            } else if (msg.contains("obstacle")) {
                JOptionPane.showMessageDialog(
                        GameUI.this,
                        msg,
                        "Collision",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (msg.contains("Treasure found")) {
                JOptionPane.showMessageDialog(
                        GameUI.this,
                        msg,
                        "Treasure",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            checkGameOver();
        }
    }

    /**
     * Custom JPanel that draws the game board, coordinate labels on all four sides,
     * and the player, obstacles, and found treasures.
     */
    private class BoardPanel extends JPanel {
        private final int cellSize = 35;  // pixel size of each grid cell
        private final int offsetX  = 40;  // left margin for row labels
        private final int offsetY  = 40;  // top margin for column labels

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size       = GameMap.SIZE;
            int boardW     = size * cellSize;
            int boardH     = size * cellSize;

            // Fill background with a light color
            g.setColor(new Color(245, 245, 245));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw coordinate labels
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(Color.BLACK);

            // Top: columns a–t
            for (int col = 0; col < size; col++) {
                char label = (char)('a' + col);
                int x = offsetX + col*cellSize + cellSize/2 - 4;
                int y = offsetY - 10;
                g.drawString(String.valueOf(label), x, y);
            }
            // Bottom: columns a–t
            for (int col = 0; col < size; col++) {
                char label = (char)('a' + col);
                int x = offsetX + col*cellSize + cellSize/2 - 4;
                int y = offsetY + boardH + 20;
                g.drawString(String.valueOf(label), x, y);
            }
            // Left: rows 20–1
            for (int row = 0; row < size; row++) {
                String label = String.valueOf(size - row);
                int x = offsetX - 30;
                int y = offsetY + row*cellSize + cellSize/2 + 5;
                g.drawString(label, x, y);
            }
            // Right: rows 20–1
            for (int row = 0; row < size; row++) {
                String label = String.valueOf(size - row);
                int x = offsetX + boardW + 10;
                int y = offsetY + row*cellSize + cellSize/2 + 5;
                g.drawString(label, x, y);
            }

            // Draw each cell and its contents
            GameMap mapData = game.getMap();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int x = offsetX + j*cellSize;
                    int y = offsetY + i*cellSize;

                    // Draw cell background
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, cellSize, cellSize);

                    // Draw cell border
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, cellSize, cellSize);

                    // Determine what to draw in this cell
                    char cell = mapData.getDisplayCell(i, j);
                    if (cell == 'P') {
                        // Player: blue circle
                        g.setColor(Color.BLUE);
                        g.fillOval(x+5, y+5, cellSize-10, cellSize-10);
                    } else if (cell == 'X') {
                        // Revealed obstacle: red square
                        g.setColor(Color.RED);
                        g.fillRect(x+5, y+5, cellSize-10, cellSize-10);
                    } else if (cell == 'F') {
                        // Found treasure: green circle
                        g.setColor(new Color(0,150,0));
                        g.fillOval(x+5, y+5, cellSize-10, cellSize-10);
                    }
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int size  = GameMap.SIZE;
            int width = size*cellSize + offsetX*2 + 40;
            int height= size*cellSize + offsetY*2 + 40;
            return new Dimension(width, height);
        }
    }
}