import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.*;

public class ColorMatchGame extends JFrame {
    // --- Constants ---
    private static final String[] COLOR_NAMES = {"Red", "Yellow", "Blue", "Green", "Orange", "Magenta"};
    private static final Color[] INK_COLORS = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
    private static final int GAME_DURATION_SECONDS = 60;
    private enum Difficulty { EASY, MEDIUM, HARD }
    // --- UI Components ---
    private final JLabel promptLabel = new JLabel("Press Restart to Begin", SwingConstants.CENTER);
    private final JButton redBtn = new JButton("Red");
    private final JButton yellowBtn = new JButton("Yellow");
    private final JButton blueBtn = new JButton("Blue");
    private final JButton greenBtn = new JButton("Green");
    private final JButton orangeBtn = new JButton("Orange");
    private final JButton magentaBtn = new JButton("Magenta");
    private final JButton restartBtn = new JButton("Restart Game");
    private final JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
    private final JLabel timeLabel = new JLabel("Time: 60");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel correctLabel = new JLabel("Correct: 0");
    private final JLabel wrongLabel = new JLabel("Wrong: 0");
    private final JLabel totalLabel = new JLabel("Total: 0");
    // --- Game State ---
    private final Random rng = new Random();
    private int remainingSeconds = GAME_DURATION_SECONDS;
    private int score = 0, correct = 0, wrong = 0, total = 0;
    private int currentInkIndex = -1; // index into COLOR_NAMES/INK_COLORS for current ink color
    private boolean running = false;  // whether the game is active
    private Difficulty difficulty = Difficulty.MEDIUM;
    private JPanel colorGrid;
    private JButton[] allColorButtons;
    // Timer ticks each second to decrement remaining time
    private final Timer timer = new Timer(1000, (ActionEvent e) -> {
        if (!running) return;
        remainingSeconds--;
        if (remainingSeconds <= 0) {
            remainingSeconds = 0;
            setRunning(false); // stops the game and disables buttons
        }
        updateStatus();
    });

    public ColorMatchGame() {
        super("Color Match Game");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        // Central big label for the colored word
        promptLabel.setFont(promptLabel.getFont().deriveFont(Font.BOLD, 48f));
        promptLabel.setOpaque(true);
        promptLabel.setBackground(Color.WHITE);
        add(promptLabel, BorderLayout.CENTER);
        // Top status bar with metrics
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        statusPanel.setBackground(Color.WHITE);
        for (JLabel lbl : new JLabel[]{timeLabel, scoreLabel, correctLabel, wrongLabel, totalLabel}) {
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 16f));
            statusPanel.add(lbl); }
        // Difficulty selector
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.PLAIN, 16f));
        modeCombo.setFocusable(false);
        modeCombo.setSelectedItem("Medium");
        modeCombo.addActionListener(e -> {
            String sel = (String) modeCombo.getSelectedItem();
            if ("Easy".equals(sel)) difficulty = Difficulty.EASY;
            else if ("Hard".equals(sel)) difficulty = Difficulty.HARD;
            else difficulty = Difficulty.MEDIUM;
            refreshButtonsForDifficulty();
            if (running) nextRound();
        });
        statusPanel.add(modeLabel);
        statusPanel.add(modeCombo);

        add(statusPanel, BorderLayout.NORTH);
        // Bottom control area: 2x2 color grid (center) and separate restart (right)
        JPanel controls = new JPanel(new BorderLayout(10, 10));
        controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controls.setBackground(Color.WHITE);
        // Color selection buttons grid (dynamic layout by difficulty)
        colorGrid = new JPanel();
        colorGrid.setBackground(Color.WHITE);
        allColorButtons = new JButton[]{redBtn, yellowBtn, blueBtn, greenBtn, orangeBtn, magentaBtn};
        for (int i = 0; i < allColorButtons.length; i++) {
            final int idx = i;
            JButton b = allColorButtons[i];
            b.setFocusable(false);
            b.setFont(b.getFont().deriveFont(Font.BOLD, 18f));
            b.addActionListener(evt -> handleSelection(idx));
        }
        refreshButtonsForDifficulty();

        controls.add(colorGrid, BorderLayout.CENTER);
        // Restart button placed separately to the right
        JPanel restartPanel = new JPanel(new GridBagLayout()); // center the button nicely
        restartPanel.setBackground(Color.WHITE);
        restartBtn.setFont(restartBtn.getFont().deriveFont(Font.BOLD, 16f));
        restartBtn.setFocusable(false);
        restartBtn.addActionListener(evt -> restartGame());
        restartPanel.add(restartBtn);
        controls.add(restartPanel, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);
        // Initial non-running state
        setRunning(false);
        timer.setRepeats(true);
        pack();
        setSize(820, 360);
        setLocationRelativeTo(null); // center the window
        setVisible(true); }

    /** Starts a new round by choosing a random WORD and a random INK color. */
    private void nextRound() {
        // Random word text and ink color index are chosen within active options
        int active = getActiveOptionCount();
        String wordText = COLOR_NAMES[rng.nextInt(active)];
        currentInkIndex = rng.nextInt(active);
        promptLabel.setText(wordText);
        promptLabel.setForeground(INK_COLORS[currentInkIndex]); }

    /** Handles a player's selection: idx is the selected color index aligned with COLOR_NAMES. */
    private void handleSelection(int idx) {
        if (!running) return; // ignore input when game not running
        boolean isCorrect = (idx == currentInkIndex);
        if (isCorrect) { score++; correct++; } else { score--; wrong++; }
        total++;
        updateStatus();
        nextRound(); }

    /** Enables/disables gameplay and color buttons, and starts/stops the timer as needed. */
    private void setRunning(boolean shouldRun) {
        running = shouldRun;
        int active = getActiveOptionCount();
        for (int i = 0; i < allColorButtons.length; i++) {
            allColorButtons[i].setEnabled(shouldRun && i < active);
        }
        if (shouldRun) { if (!timer.isRunning()) timer.start(); }
        else { if (timer.isRunning()) timer.stop(); } }

    /** Resets timer and all metrics, enables input, and immediately starts a new prompt. */
    private void restartGame() {
        remainingSeconds = GAME_DURATION_SECONDS;
        score = correct = wrong = total = 0;
        updateStatus();
        setRunning(true);
        nextRound(); }

    /** Updates the top status labels to reflect current game state. */
    private void updateStatus() {
        timeLabel.setText("Time: " + remainingSeconds);
        scoreLabel.setText("Score: " + score);
        correctLabel.setText("Correct: " + correct);
        wrongLabel.setText("Wrong: " + wrong);
        totalLabel.setText("Total: " + total);
        if (!running && remainingSeconds == 0) {
            promptLabel.setText("Time Up!"); } }

    private int getActiveOptionCount() {
        switch (difficulty) {
            case EASY: return 3;
            case HARD: return 6;
            default: return 4;
        }
    }

    private void refreshButtonsForDifficulty() {
        colorGrid.removeAll();
        int active = getActiveOptionCount();
        // Choose grid layout based on active count
        if (active == 3) {
            colorGrid.setLayout(new GridLayout(1, 3, 10, 10));
        } else if (active == 4) {
            colorGrid.setLayout(new GridLayout(2, 2, 10, 10));
        } else { // 6
            colorGrid.setLayout(new GridLayout(2, 3, 10, 10));
        }
        for (int i = 0; i < active; i++) {
            colorGrid.add(allColorButtons[i]);
        }
        // Ensure enable state matches running state
        setRunning(running);
        colorGrid.revalidate();
        colorGrid.repaint();
    }

    public static void main(String[] args) {
        // Ensure UI is created on the Event Dispatch Thread for Swing safety
        SwingUtilities.invokeLater(ColorMatchGame::new); }}