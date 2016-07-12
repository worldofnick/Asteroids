package assign10;

import javax.swing.*;

import java.awt.*;

import static assign10.Constants.*;

/**
 * Implements an asteroid game.
 * 
 * @author Joe Zachary
 *
 */
public class Game extends JFrame
{
    // Label to display the current score
    private JLabel scoreLabel;
    // Label to display the current number of lives left
    private JLabel livesLabel;
    private JButton teleportButton;

    /**
     * Launches the game
     */
    public static void main (String[] args)
    {
        Game a = new Game();
        a.setVisible(true);
    }

    /**
     * Lays out the game and creates the controller
     */
    public Game ()
    {
        // Title at the top
        setTitle(TITLE);

        // Default behavior on closing
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The main playing area and the controller
        Screen screen = new Screen();
        Controller controller = new Controller(this, screen);

        // This panel contains the screen to prevent the screen from being
        // resized
        JPanel screenPanel = new JPanel();
        screenPanel.setLayout(new GridBagLayout());
        screenPanel.add(screen);

        // This panel contains buttons and labels
        JPanel controls = new JPanel();

        // The button that starts the game
        JButton startGame = new JButton(START_LABEL);
        livesLabel = new JLabel();
        scoreLabel = new JLabel();
        teleportButton = new JButton("Teleport");
        teleportButton.setVisible(false);
        
        controls.add(startGame);
        controls.add(livesLabel);
        controls.add(scoreLabel);
        //controls.add(teleportButton);
        
        // Organize everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(screenPanel, "Center");
        mainPanel.add(controls, "North");
        setContentPane(mainPanel);
        pack();

        // Connect the controller to the start button
        startGame.addActionListener(controller);
        teleportButton.addActionListener(controller);
    }
    
    /**
     * Sets the text for the lives label
     */
    public void setLives(int lives) {
        livesLabel.setText("Lives: " + lives);
    }
    
    /**
     * Sets the text for the score label
     */
    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
    
    /**
     * Shows the teleport button
     */
    public void unhideTeleportButton() {
        teleportButton.setVisible(true);
    }
                

}
