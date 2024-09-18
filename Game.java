package main;


import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JFrame implements ActionListener {

    JButton b1 = new JButton("Play");
    JPanel p = new JPanel(new FlowLayout());
    private int score; // Track the current score
    private HighScoreTracker highScoreTracker; // Instance of the HighScoreTracker class

    public Game() {
        setSize(1080, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        b1.addActionListener(this);
        p.add(b1);
        getContentPane().add(p);
        setVisible(true);

        // Initialize the high score tracker
        highScoreTracker = new HighScoreTracker();
        score = 0; // Initialize score to 0 when the game starts
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new Thread(new GamePanel()).start();
    }

    // Example method to simulate updating the score (you should adjust this in your actual game logic)
    public void updateScore(int points) {
        score += points; // Add points to the current score
    }

    // This method should be called when the game ends
    public void endGame() {
        highScoreTracker.checkHighScore(score); // Check if the current score is a new high score
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // Display the current score and high score
        g.drawString("Score: " + score, 10, 10);
        g.drawString("High Score: " + highScoreTracker.getHighScore(), 10, 30);
    }

    // public static void main(String[] args) {
    //    new Game();
    // }
}