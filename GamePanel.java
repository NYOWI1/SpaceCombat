package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.sound.sampled.*;
import java.io.*;
import java.lang.*;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;

public class GamePanel extends JPanel implements Runnable {

	Font font = new Font(Font.MONOSPACED, Font.BOLD, 32);
	KeyDetector keys;
	MouseDetector mouse;
	GameWindowHandler window;

	JFrame frame = new JFrame("Space Combat");
	volatile boolean play;
	boolean inited = false;
	boolean up = false, down = false, right = false, left = false;
	boolean gameOver;
	boolean mainPanel;
	boolean resultPanel;

	BufferedImage backGround;
	BufferedImage opacity;
	MenuPanel menuPanel;
	ResultPanel result;

	private long diff, start = System.currentTimeMillis();
	double theta;
	Thread thread;
	int turbo = 4;
	int numberOfEnemies = 12;
	int speedOfEnemies = 3;
	int speedOfBullets = 20;
	int numberOfHearts = 6;
	int remainingHearts = numberOfHearts;
	int numberOfStationaryEnemies = 3;
	int numberOfEnemyBullets = 15;
	int numberOfBullets = 25;
	int powerup_count = 1;
	int shield_count = 1;
	int countEnemy = numberOfEnemies + numberOfStationaryEnemies;
	double timeForBullet = 0.005;
	int seconds = 90;
    private int highScore;  // To store the high score
    private int currentScore = 0;  // To track current score
    private int remainingHearts1 = 3;
	
	
	Player player;
	HealthBar[] hearts;
	Enemy[] enemy;
	PowerUp powerup;
    Shield shield;
	Bullet[] bullets;
	Bullet[] enemyBullets;
	Enemy[] stationaryEnemy;
	LocalStorage localStorage = new LocalStorage("Resources/data.properties");

	public GamePanel() {
		setSize(1080, 660);
		frame.setSize(1080, 680);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);
		setFocusable(true);
		frame.setLocationRelativeTo(null);
		setBackground(Color.BLACK);
		frame.add(this);
		this.highScore = loadHighScore();
    powerup = new PowerUp();  // Initialize power-up
    shield = new Shield();  // Initialize shield here
		File background = new File("Resources/background2.jpg");
		try {
			backGround = (ImageIO.read(background))
					.getSubimage(0, 0, 1080, 660);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		File opacityF = new File("Resources/opacity.png");
		try {
			opacity = (ImageIO.read(opacityF)).getSubimage(0, 0, 1080, 660);
		} catch (IOException e) {
			e.printStackTrace();
		}
		frame.setVisible(true);
	}

	  private int loadHighScore() {
	        int score = 0;
	        try {
	            File file = new File("Resources/highscore.txt");
	            if (file.exists()) {
	                BufferedReader reader = new BufferedReader(new FileReader(file));
	                score = Integer.parseInt(reader.readLine());
	                reader.close();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return score;
	    }

	    // Method to save high score to file
	    private void saveHighScore(int newHighScore) {
	        try {
	            BufferedWriter writer = new BufferedWriter(new FileWriter("Resources/highscore.txt"));
	            writer.write(String.valueOf(newHighScore));
	            writer.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    // Method to update the player's score and check if they surpassed the high score
	    public void updateScore(int score) {
	        currentScore = score;  // Update the current score
	        if (currentScore > highScore) {
	            System.out.println("New High Score! Extra Life Gained.");
	            highScore = currentScore;
	            saveHighScore(highScore);  // Save the new high score
	            gainExtraLife();  // Grant extra life
	        }
	    }

	    // Method to grant an extra life
	    private void gainExtraLife() {
	        remainingHearts++;
	        System.out.println("Extra life gained! Total Lives: " + remainingHearts);
	    }

	    // Call this method whenever the player gains points
	    public void incrementScore() {
	        currentScore += 10;  // Example increment (change based on your scoring system)
	        updateScore(currentScore);  // Check and update the score
	    }

	    
	
	public void init() {
		keys = new KeyDetector(this);
		mouse = new MouseDetector(this);
		window = new GameWindowHandler(this);

		gameOver = true;
		play = false;
		resultPanel = false;
		menuPanel = new MenuPanel(this);

		mainPanel = true;
		result = new ResultPanel(this);

		player = new Player();

		remainingHearts = numberOfHearts;
		hearts = new HealthBar[numberOfHearts];
		for (int i = 0; i < numberOfHearts; i++) {
			hearts[i] = new HealthBar();
			hearts[i].x = 40 * i;
		}
		
		powerup = new PowerUp();

		enemy = new Enemy[numberOfEnemies];
		for (int i = 0; i < numberOfEnemies; i++) {
			enemy[i] = new Enemy();
		}
		stationaryEnemy = new Enemy[numberOfStationaryEnemies];
		for (int i = 0; i < numberOfStationaryEnemies; i++) {
			stationaryEnemy[i] = new Enemy(2);
		}

		thread = new Thread(this);

		bullets = new Bullet[numberOfBullets];
		for (int i = 0; i < numberOfBullets; i++) {
			bullets[i] = new Bullet(210, 200, 0);
		}

		enemyBullets = new Bullet[numberOfEnemyBullets];
		for (int i = 0; i < numberOfEnemyBullets; i++) {
			enemyBullets[i] = new Bullet(5);
		}

		inited = true;
	}

	public void run() {
		init();
		while (true) {

			main();

			while (!gameOver) {
				play();
			}

			result();
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (inited) {
			if (mainPanel) {
				menuPanel.draw(g2d);
			} else if (!gameOver) {
				g2d.drawImage(backGround, 0, 0, null); // BackGround
				
				if (powerup_count == 1) {
					g2d.drawImage(powerup.getImage(),powerup.x, powerup.y , null);
					
				}
				if (shield_count == 1) {
					g2d.drawImage(shield.getImage(),shield.x, shield.y , null);
					
				}
				
				for (int i = 0; i < numberOfHearts; i++)
					g2d.drawImage(hearts[i].getImage(), hearts[i].x, 620, null);

				// Calculate The angle
				theta = Math.atan2(mouse.getY() - (player.y + 40), mouse.getX()
						- (player.x + 50))
						+ Math.PI / 2;

				for (int i = 0; i < numberOfBullets; i++) {
					if (bullets[i].fired) {
						g2d.drawImage(bullets[i].getImage(), bullets[i].x,
								bullets[i].y, null);
					}
				}
				for (int i = 0; i < numberOfEnemyBullets; i++) {
					if (enemyBullets[i].fired) {
						g2d.drawImage(enemyBullets[i].getImage(), enemyBullets[i].x,
								enemyBullets[i].y, null);
					}
				}
				// g2d.rotate(theta, player.x+50, player.y+40);
				// g2d.drawImage(player.image, player.x+50 , player.y+40 ,
				// null);
				// g2d.rotate(-theta, player.x+50, player.y+40);

				player.draw(g2d, theta);

				for (int v = 0; v < numberOfEnemies; v++) {
					if (enemy[v].isAlive) {
						g2d.drawImage(enemy[v].getImage(), enemy[v].x,
								enemy[v].y, null);

						// enemy[v].draw(g2d);
					}

				}

				for (int v = 0; v < numberOfStationaryEnemies; v++) {
					if (stationaryEnemy[v].isAlive) {
						g2d.drawImage(stationaryEnemy[v].getImage(),
								stationaryEnemy[v].x, stationaryEnemy[v].y,
								null);
					}

				}

			} else if (resultPanel) {
				result.draw(g2d,currentScore);
			}
		}

	}

	public static void main(String[] args) {
		new Thread(new GamePanel()).start();
		backgroundSound();
                 
	}

	public void sleep(int fps) {
		if (fps > 0) {
			diff = System.currentTimeMillis() - start;
			long targetDelay = 1000 / fps;
			if (diff < targetDelay) {
				try {
					Thread.sleep(targetDelay - diff);
				} catch (InterruptedException e) {
				}
			}
			start = System.currentTimeMillis();
		}
	}

	public void addBullet(int x) {
	    // Normal firing pattern
	    if (x == 1) {
	        for (int i = 0; i < numberOfBullets; i++) {
	            if (!bullets[i].fired) {
	                if (i % 2 == 0)
	                    bullets[i].x = player.x + 70;
	                else
	                    bullets[i].x = player.x + 30;
	                bullets[i].y = player.y + 40;
	                bullets[i].theta = theta;
	                bullets[i].sin = Math.sin(bullets[i].theta - Math.PI / 2);
	                bullets[i].cos = Math.cos(bullets[i].theta - Math.PI / 2);
	                bullets[i].fired = true;
	                break;
	            }
	        }
	    }

	    // Power-up firing pattern (random direction)
	    if (x == 2) {
	        for (int i = 0; i < numberOfBullets; i++) {
	            if (!bullets[i].fired) {
	                bullets[i].fired = true;
	                bullets[i].x = player.x + 40;
	                bullets[i].y = player.y + 40;
	                bullets[i].theta = Math.random() * Math.PI * 2;
	                bullets[i].sin = Math.sin(bullets[i].theta - Math.PI / 2);
	                bullets[i].cos = Math.cos(bullets[i].theta - Math.PI / 2);
	                continue;
	            }
	        }
	    }

	    // New firing pattern (Burst/Fan-shape firing)
	    if (x == 3) {
	        int bulletsInBurst = 5;  // Fire 5 bullets in a spread
	        double angleSpread = Math.PI / 6; // Spread bullets over 30 degrees (PI/6 radians)
	        double centerAngle = theta; // Center of the burst is the direction player is facing
	        
	        for (int i = 0; i < bulletsInBurst; i++) {
	            for (int j = 0; j < numberOfBullets; j++) {
	                if (!bullets[j].fired) {
	                    bullets[j].fired = true;
	                    bullets[j].x = player.x + 40;
	                    bullets[j].y = player.y + 40;
	                    
	                    // Calculate angle for each bullet in the burst
	                    double angleOffset = angleSpread * (i - (bulletsInBurst - 1) / 2.0); 
	                    bullets[j].theta = centerAngle + angleOffset;
	                    bullets[j].sin = Math.sin(bullets[j].theta - Math.PI / 2);
	                    bullets[j].cos = Math.cos(bullets[j].theta - Math.PI / 2);
	                    break; // Move to next bullet after firing one
	                }
	            }
	        }
	    }
	}
	
	
	
	public void addEnemyBullet(Enemy sEnemy) {
		for (int i = 0; i < numberOfEnemyBullets; i++) {
			if (!bullets[i].fired) {
				enemyBullets[i].x = sEnemy.x + 25;
				enemyBullets[i].y = sEnemy.y + 25;
				enemyBullets[i].theta = Math.atan2((player.y +40) - (sEnemy.y), (player.x+50) - (sEnemy.x));
				enemyBullets[i].sin = Math.sin(enemyBullets[i].theta) ; //- Math.PI/ 2)
				enemyBullets[i].cos = Math.cos(enemyBullets[i].theta );
				enemyBullets[i].fired = true;
				break;
			}
		}
	}

	public void play() {

		while (play) {
			if (player.isAlive) {
				if (up && player.y > 0)
					player.setY(player.y - turbo);
				if (down && player.y < 580)
					player.setY(player.y + turbo);
				if (right && player.x < 980)
					player.setX(player.x + turbo);
				if (left && player.x > 0)
					player.setX(player.x - turbo);

				for (int i = 0; i < numberOfEnemies; i++) {
					enemy[i].theta = Math.atan2(enemy[i].y - (player.y + 40),
							enemy[i].x - (player.x + 50));

					enemy[i].y = enemy[i].y
							- (int) (speedOfEnemies * Math.sin(enemy[i].theta));

					enemy[i].x = enemy[i].x
							- (int) (speedOfEnemies * Math.cos(enemy[i].theta));

					if (enemy[i].isAlive
							&& remainingHearts != 0
							&& enemy[i].getBounds().intersects(
									player.getBounds())) {
						enemy[i].isAlive = false;
						countEnemy--;
						hearts[--remainingHearts].setImage();
					}
				}

				for (int i = 0; i < numberOfStationaryEnemies; i++) {

					if (stationaryEnemy[i].isAlive
							&& remainingHearts != 0
							&& stationaryEnemy[i].getBounds().intersects(
									player.getBounds())) {
						stationaryEnemy[i].isAlive = false;
						countEnemy--;
						hearts[--remainingHearts].setImage();
					}
				}
				
				
				if (powerup.isAlive
						&& remainingHearts != 0
						&& powerup.getBounds().intersects(
								player.getBounds())) {
					powerup.isAlive = false;
					if (powerup_count > 0) {
						powerup_count -= 1;
					}
					addBullet(2);
				}
				if (shield.isAlive
						&& remainingHearts != 0
						&& shield.getBounds().intersects(
								player.getBounds())) {
					shield.isAlive = false;
					if (shield_count > 0) {
						shield_count -= 1;
					}
					addBullet(3);
				}
				
				for (int i = 0; i < numberOfBullets; i++) {
					if (bullets[i].fired) {
						for (int v = 0; v < numberOfEnemies; v++) {
							if (enemy[v].isAlive) {
								if (enemy[v].getBounds().intersects(
										bullets[i].getBounds())) {
									enemy[v].isAlive = false;
									countEnemy--;
									bullets[i].fired = false;
									incrementScore();
								}
							}
						}
						for (int v = 0; v < numberOfStationaryEnemies; v++) {
							if (stationaryEnemy[v].isAlive) {
								if (stationaryEnemy[v].getBounds().intersects(
										bullets[i].getBounds())) {
									stationaryEnemy[v].isAlive = false;
									countEnemy--;
									bullets[i].fired = false;
									incrementScore();
								}
							}
						}

						if (bullets[i].x < -100 || bullets[i].x > 1180
								|| bullets[i].y < -100 || bullets[i].y > 760)
							bullets[i].fired = false;
						else {
							bullets[i].y = bullets[i].y
									+ (int) (speedOfBullets * bullets[i].sin);

							bullets[i].x = bullets[i].x
									+ (int) (speedOfBullets * bullets[i].cos);
						}
					}
				}

				for (int i = 0; i < numberOfEnemyBullets; i++) {
					if (enemyBullets[i].fired) {
						if (player.getBounds().intersects(
								enemyBullets[i].getBounds())) {
							hearts[--remainingHearts].setImage();
							enemyBullets[i].fired = false;
						}

						if (enemyBullets[i].x < -200 || enemyBullets[i].x > 1300
								|| enemyBullets[i].y < -200 || enemyBullets[i].y > 1000)
							enemyBullets[i].fired = false;
						else {
							enemyBullets[i].y = enemyBullets[i].y
									+ (int) (speedOfBullets * enemyBullets[i].sin);

							enemyBullets[i].x = enemyBullets[i].x
									+ (int) (speedOfBullets * enemyBullets[i].cos);
						}
					}
				}
				if(timeForBullet % seconds == 0){
					int rand = (int)(Math.random()*numberOfStationaryEnemies);
					if(stationaryEnemy[rand].isAlive) {
						addEnemyBullet(stationaryEnemy[rand]);
					}
				}
				int previousScore = localStorage.loadData("Score");

				if (remainingHearts == 0) {
					result.won = false;
					play = false;
					gameOver = true;
					resultPanel = true;
				
		            
		            if (currentScore > previousScore) {
		                localStorage.saveData("Score", currentScore);
		                System.out.println("New high score saved: " + currentScore);
		            } else {
		                System.out.println("No new high score.");
		            }
				}
				if (countEnemy == 0) {
				
		            
		            if (currentScore > previousScore) {
		                localStorage.saveData("Score", currentScore);
		                System.out.println("New high score saved: " + currentScore);
		            } else {
		                System.out.println("No new high score.");
		            }
					result.won = true;
					play = false;
					gameOver = true;
					resultPanel = true;
				}

				repaint();
				sleep(60);
				timeForBullet++;
			}
		}
	}

	public void main() {

		while (mainPanel) {

			for (int i = 0; i < 7; i++) {
				if (menuPanel.rectangle[i].contains(mouse.pMoved))
					menuPanel.activated[i] = true;
				else
					menuPanel.activated[i] = false;
			}

			if (menuPanel.rectangle[0].contains(mouse.pClicked))
				menuPanel.clicked = true;
			else {
				if (!(menuPanel.rectangle[4].contains(mouse.pClicked)
						|| menuPanel.rectangle[5].contains(mouse.pClicked) || menuPanel.rectangle[6]
							.contains(mouse.pClicked))) {
					menuPanel.clicked = false;
				}

			}

			if (menuPanel.rectangle[3].contains(mouse.pClicked))
				System.exit(0);
			if (menuPanel.rectangle[2].contains(mouse.pClicked)) {
				mouse.pClicked.x = 0;
				mouse.pClicked.y = 0;
				new AboutWindow();
			}
			if (menuPanel.rectangle[1].contains(mouse.pClicked)) {
				mouse.pClicked.x = 0;
				mouse.pClicked.y = 0;
				new HelpWindow();
			}

			if (menuPanel.clicked
					&& menuPanel.rectangle[4].contains(mouse.pClicked)) {
				menuPanel.clicked = false;
				numberOfEnemies = 5;
				numberOfHearts = 3;
				remainingHearts = 2;
				numberOfStationaryEnemies = 1;
				powerup_count = 1;
				shield_count = 1;
				seconds = 90;
				countEnemy = numberOfEnemies + numberOfStationaryEnemies;
				//here
				File background = new File("Resources/background2.jpg");
				try {
					backGround = (ImageIO.read(background))
							.getSubimage(0, 0, 1080, 660);
				} catch (IOException e) {
					e.printStackTrace();
				}
				reset();
				mainPanel = false;
				play = true;
				gameOver = false;
			}
			if (menuPanel.clicked
					&& menuPanel.rectangle[5].contains(mouse.pClicked)) {
				menuPanel.clicked = false;
				numberOfEnemies = 6;
				numberOfHearts = 2;
				powerup_count = 1;
				shield_count = 1;
				remainingHearts = 2;
				numberOfStationaryEnemies = 2;
				seconds = 70;
				countEnemy = numberOfEnemies + numberOfStationaryEnemies;
				reset();
				mainPanel = false;
				play = true;
				gameOver = false;
				File background = new File("Resources/background.jpg");
				try {
					backGround = (ImageIO.read(background))
							.getSubimage(0, 0, 1080, 660);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (menuPanel.clicked
					&& menuPanel.rectangle[6].contains(mouse.pClicked)) {
				menuPanel.clicked = false;
				numberOfEnemies = 7;
				numberOfHearts = 1;
				remainingHearts = 1;
				numberOfStationaryEnemies = 3;
				powerup_count = 1;
				shield_count = 1;
				seconds = 60;
				countEnemy = numberOfEnemies + numberOfStationaryEnemies;
				reset();
				mainPanel = false;
				play = true;
				gameOver = false;
				File background = new File("Resources/background1.jpg");
				try {
					backGround = (ImageIO.read(background))
							.getSubimage(0, 0, 1920, 1080);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			repaint();
			sleep(60);

		}
	}

	public void result() {
		result.painted = false;
		mouse.pClicked.x = 0;
		mouse.pClicked.y = 0;
		while (resultPanel) {

			for (int i = 0; i < 2; i++) {
				if (result.rectangle[i].contains(mouse.pMoved))
					result.activated[i] = true;
				else
					result.activated[i] = false;
			}

			if (result.rectangle[1].contains(mouse.pClicked))
				System.exit(0);
			if (result.rectangle[0].contains(mouse.pClicked)) {
				mouse.pClicked.x = 0;
				mouse.pClicked.y = 0;
				resultPanel = false;
				mainPanel = true;
			}

			repaint();
			sleep(60);
			//result.painted = true;

		}
	}

	public void reset() {
		powerup.setLocation();
		shield.setLocation();
		powerup.isAlive = true;
		shield.isAlive = true;
		for (int i = 0; i < numberOfHearts; i++) {
			hearts[i].setHealth();
		}

		for (int i = 0; i < numberOfEnemies; i++) {
			enemy[i].isAlive = true;
			enemy[i].setLocation();
		}

		for (int i = 0; i < numberOfStationaryEnemies; i++) {
			stationaryEnemy[i].isAlive = true;
			stationaryEnemy[i].setLocation();
		}

		for (int i = 0; i < numberOfBullets; i++) {
			bullets[i].fired = false;
		}
		player.x = 1080 / 2 - 50;
		player.y = 340 - 40;

	}


//background audio

	public static void backgroundSound() {
        try {
            // Load the audio file
            File soundFile = new File("Resources/Game1.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            // Get a sound clip resource
            Clip clip = AudioSystem.getClip();

            // Open the audio clip and load samples from the audio input stream
            clip.open(audioStream);

            // Start playing the clip
            clip.start();

            // Optionally, loop the clip indefinitely
            clip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
	}
}
  