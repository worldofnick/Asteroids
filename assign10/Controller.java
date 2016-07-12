package assign10;

import static assign10.Constants.EDGE_OFFSET;
import static assign10.Constants.END_DELAY;
import static assign10.Constants.FRAME_INTERVAL;
import static assign10.Constants.GAME_OVER;
import static assign10.Constants.SIZE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * Controls a game of asteroids
 * 
 * @author Joe Zachary
 */
public class Controller implements CollisionListener, ActionListener,
        KeyListener, CountdownTimerListener
{
    // Shared random number generator
    private Random random;

    // The ship (if one is active) or null (otherwise)
    private Ship ship;

    // When this timer goes off, it is time to refresh the animation
    private Timer refreshTimer;

    // Count of how many transitions have been made. This is used to keep two
    // conflicting transitions from being made at almost the same time.
    private int transitionCount;

    // Number of lives left
    private int lives;

    // The Game and Screen objects being controlled
    private Game game;
    private Screen screen;

    // Indicates which keys are down
    private boolean leftDown;
    private boolean rightDown;
    private boolean upDown;
    private boolean downDown;
    
    // How many levels have been passed to speed up the asteroids
    private int level;
    
    // The current score for the game
    private int score;
    
    // Keeps track of the amount of bullets on screen
    private int bulletCount;
    
    // Keeps track of how many asteroids have been destroyed, when 28 have been destroyed more asteroids are placed and the level is increased
    private int destroyedAsteroidCount;

    
    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller (Game game, Screen screen)
    {
        // Record the game and screen objects
        this.game = game;
        this.screen = screen;

        // Initialize the random number generator
        random = new Random();

        // Set up the refresh timer.
        refreshTimer = new Timer(FRAME_INTERVAL, this);
        transitionCount = 0;

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        refreshTimer.start();
    }

    /**
     * Configures the game screen to display the splash screen
     */
    private void splashScreen ()
    {
        // Clear the screen and display the legend
        screen.clear();
        screen.setLegend("Asteroids");

        // Place four asteroids near the corners of the screen.
        placeAsteroids();

        // Make sure there's no ship
        ship = null;

    }

    /**
     * Get the number of transitions that have occurred.
     */
    public int getTransitionCount ()
    {
        return transitionCount;
    }

    /**
     * The game is over. Displays a message to that effect and enables the start
     * button to permit playing another game.
     */
    private void finalScreen ()
    {
        screen.setLegend(GAME_OVER);
        screen.removeCollisionListener(this);
        screen.removeKeyListener(this);
    }

    /**
     * Places four asteroids near the corners of the screen. Gives them random
     * velocities and rotations.
     */
    private void placeAsteroids ()
    {
        // Screen cleared increase asteroids speed
        level++;
        
        Participant a = new Asteroid(0, 2, EDGE_OFFSET, EDGE_OFFSET);
        a.setVelocity(3 + level, random.nextDouble() * 2 * Math.PI);
        a.setRotation(2 * Math.PI * random.nextDouble());
        screen.addParticipant(a);

        a = new Asteroid(1, 2, SIZE - EDGE_OFFSET, EDGE_OFFSET);
        a.setVelocity(3 + level, random.nextDouble() * 2 * Math.PI);
        a.setRotation(2 * Math.PI * random.nextDouble());
        screen.addParticipant(a);

        a = new Asteroid(2, 2, EDGE_OFFSET, SIZE - EDGE_OFFSET);
        a.setVelocity(3 + level, random.nextDouble() * 2 * Math.PI);
        a.setRotation(2 * Math.PI * random.nextDouble());
        screen.addParticipant(a);

        a = new Asteroid(3, 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET);
        a.setVelocity(3 + level, random.nextDouble() * 2 * Math.PI);
        a.setRotation(2 * Math.PI * random.nextDouble());
        screen.addParticipant(a);
    }

    /**
     * Set things up and begin a new game.
     */
    private void initialScreen ()
    {
        // Clear the screen
        screen.clear();

        // Place four asteroids
        placeAsteroids();

        // Place the ship
        placeShip();

        // Reset statistics
        lives = 3;
        score = 0;
        level = -1;
        destroyedAsteroidCount = 0;
        
        // Update labels after resetting statistics
        game.setLives(lives);
        game.setScore(score);
        game.unhideTeleportButton();

        // Start listening to events. In case we're already listening, take
        // care to avoid listening twice.
        screen.removeCollisionListener(this);
        screen.removeKeyListener(this);
        screen.addCollisionListener(this);
        screen.addKeyListener(this);

        // Give focus to the game screen
        screen.requestFocusInWindow();
        
    }

    /**
     * Place a ship in the center of the screen.
     */
    private void placeShip ()
    {
        if (ship == null)
        {
            ship = new Ship();
        }
        ship.setPosition(SIZE / 2, SIZE / 2);
        ship.setRotation(-Math.PI / 2);
        screen.addParticipant(ship);
        reset();
    }

    /**
     * Called when a new ship is placed to reset it to a non-moving state
     */
    private void reset ()
    {
        leftDown = false;
        rightDown = false;
        upDown = false;
        downDown = false;
        bulletCount = 0;
    }

    private void fireBullet ()
    {
        
        if (8 > bulletCount)
        {
            bulletCount++;
            Bullet bullet = new Bullet(ship.getXNose(), ship.getYNose(),ship.getRotation());
            CountdownTimer timer = new CountdownTimer(this, bullet, Constants.BULLET_DURATION);
            screen.addParticipant(bullet);
            playSoundAtPath("src/asteroids/asteroids_shoot.wav");
        }
    }

    /**
     * Deal with collisions between participants.
     */
    @Override
    public void collidedWith (Participant p1, Participant p2)
    {
        if (p1 instanceof Asteroid && p2 instanceof Ship)
        {
            asteroidCollision((Asteroid) p1);
            shipCollision((Ship) p2);
        }
        else if (p1 instanceof Ship && p2 instanceof Asteroid)
        {
            asteroidCollision((Asteroid) p2);
            shipCollision((Ship) p1);
        }
        else if (p1 instanceof Asteroid && p2 instanceof Bullet)
        {
            asteroidCollision((Asteroid) p1);
            screen.removeParticipant(p2);
            bulletCount--;
            Asteroid a = (Asteroid) p1;
            score += Constants.SCORE_MODIFIER * (a.getSize() + 1);
            if (didEarnLife())
            {
                lives++;
            }
        }
        else if (p1 instanceof Bullet && p2 instanceof Asteroid)
        {
            asteroidCollision((Asteroid) p2);
            screen.removeParticipant(p1);
            bulletCount--;
            Asteroid a = (Asteroid) p2;
            score += Constants.SCORE_MODIFIER * (a.getSize() + 1); 
            if (didEarnLife())
            {
                lives++;
            }
        }
        
        game.setLives(lives);
        game.setScore(score);
        
    }

    /**
     * The ship has collided with something
     */
    private void shipCollision (Ship s)
    {
        // Remove the ship from the screen and null it out
        screen.removeParticipant(s);
        ship = null;

        // Display a legend and make it disappear in one second
        screen.setLegend("Ouch!");
        new CountdownTimer(this, null, 1000);

        // Decrement lives
        lives--;

        // Start the timer that will cause the next round to begin.
        new TransitionTimer(END_DELAY, transitionCount, this);
        
        // Play explosion sound
        playSoundAtPath("src/asteroids/asteroids_explosion.wav");
    }

    /**
     * Something has hit an asteroid
     */
    private void asteroidCollision (Asteroid a)
    {
        // The asteroid disappears
        screen.removeParticipant(a);
        
        destroyedAsteroidCount++;
        
        // Create two smaller asteroids. Put them at the same position
        // as the one that was just destroyed and give them a random
        // direction.
        int size = a.getSize();
        size = size - 1;
        if (size >= 0)
        {
            int speed = 3;
            Asteroid a1 = new Asteroid(random.nextInt(4), size, a.getX(),
                    a.getY());
            Asteroid a2 = new Asteroid(random.nextInt(4), size, a.getX(),
                    a.getY());
            a1.setVelocity(speed + 2.5, random.nextDouble() * 2 * Math.PI);
            a2.setVelocity(speed + 2.5, random.nextDouble() * 2 * Math.PI);
            a1.setRotation(2 * Math.PI * random.nextDouble());
            a2.setRotation(2 * Math.PI * random.nextDouble());
            screen.addParticipant(a1);
            screen.addParticipant(a2);
        }
        
        // Checking if all asteroids of the scene have been cleared
        // If cleared reset count and place more asteroids
        if (destroyedAsteroidCount >= 28)
        {
            placeAsteroids();
            destroyedAsteroidCount = 0;
        }
        
        // Place debris
        for(int i = 0; i < 8; i++) {
            placeDebris(a.getX(), a.getY());
        }
        
        // Play explosion sound
        playSoundAtPath("src/asteroids/asteroids_explosion.wav");
        
    }

    /**
     * Places debris where an asteroid was destroyed
     */
    private void placeDebris (double x, double y)
    {
        Debris d = new Debris(x, y);
        d.setVelocity(1, random.nextDouble() * 2 * Math.PI);
        CountdownTimer timer = new CountdownTimer(this, d,
                1500 + (random.nextInt(500)));
        screen.addParticipant(d);
    }
    
    /**
     * Returns true if the player has earned a new life false otherwise
     */
    private boolean didEarnLife() {
        
        if (score % Constants.NEW_LIFE_THRESHOLD == 0)
        {
            return true;
        }
        return false;
    }


    /**
     * This method will be invoked because of button presses and timer events.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        // The start button has been pressed. Stop whatever we're doing
        // and bring up the initial screen
        if (e.getSource() instanceof JButton)
        {
            JButton button = (JButton) e.getSource();
            if (button.getText().equals(Constants.START_LABEL))
            {
                transitionCount++;
                initialScreen();
            } else {
                // Teleport button
                teleportShip();
                
            }
        }

        // Time to refresh the screen
        else if (e.getSource() == refreshTimer)
        {

            // Check which keys are down and change the ships location based on them
            if (ship != null)
            {

                if (leftDown)
                {
                    ship.rotate(-Math.PI / 16);
                }
                if (rightDown)
                {
                    ship.rotate(Math.PI / 16);
                }
                if (upDown)
                {
                    ship.accelerate(.7);
                    playSoundAtPath("src/asteroids/asteroids_thrust.wav");
                }
                if (downDown)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        ship.friction();
                    }
                }


            }
            // Refresh screen
            screen.refresh();
        }
    }

    /**
     * Teleports the ship to a random location on screen
     */
    private void teleportShip ()
    {
      if (ship != null)
    {
        int randomX = random.nextInt(SIZE);
        int randomY = random.nextInt(SIZE);
        screen.removeParticipant(ship);
        ship = new Ship();
        ship.setPosition(randomX, randomY);
        screen.addParticipant(ship);
    }
      
    }
      /**
       * Play audio clip at path
       */
      private void playSoundAtPath(String path) 
      {
          try
          {
             Clip clip = AudioSystem.getClip();
             clip.open(AudioSystem.getAudioInputStream(new File(path)));
             clip.start();
              
          }
          catch (Exception exc)
          {
              exc.printStackTrace(System.out);
          }
      }

    /**
     * Based on the state of the controller, transition to the next state.
     */
    public void performTransition ()
    {
        // Record that a transition was made. That way, any other pending
        // transitions will be ignored.
        transitionCount++;

        // If there are no lives left, the game is over. Show
        // the final screen.
        if (lives == 0)
        {
            finalScreen();
        }

        // The ship must have been destroyed. Place a new one and
        // continue on the current level
        else
        {
            placeShip();
        }
    }

    /**
     * Deals with certain key presses
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        if (ship != null)
        {
            if (e.getKeyCode() == KeyEvent.VK_LEFT)
            {
                leftDown = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            {
                rightDown = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_UP)
            {
                upDown = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN)
            {
                downDown = true;
            }
            else if(e.getKeyCode() == KeyEvent.VK_SPACE) 
            {
               fireBullet();
            }

        }
    }

    @Override
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
        {
            if (ship != null)
                leftDown = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            if (ship != null)
                rightDown = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP)
        {
            upDown = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
        {
            downDown = false;
        }

    }

    @Override
    public void keyTyped (KeyEvent e)
    {
    }

    /**
     * Callback for countdown timer. Used to create transient effects.
     */
    @Override
    public void timeExpired (Participant p)
    {
        if (p instanceof Bullet)
        {
            screen.removeParticipant(p);
            bulletCount--;
        } 
        else if (p instanceof Debris)
        {
            screen.removeParticipant(p);
        }
        screen.setLegend("");

    }
}
