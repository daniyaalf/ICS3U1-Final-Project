import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
// Pacman (but more difficult)
// Aswin and Daniyaal
// For our final project, we made pacman with a few changes. First we made the computer smarter, so that it can follow the player around which makes it harder for the player to win
// we also added a powerup which will allow the player to control the ghosts by using the arrow keys
// when all the star powerups are collected, the ghosts will be programmed to follow the arrow keys
public class backupPacman extends JPanel implements KeyListener, MouseListener {
	//These are the important images used throughout the game
	public static Image character, pacBg, wallImage;
	public static Image ghostImage1, ghostImage2, ghostImage3, ghostImage4;
	//We used an array for the pellets in order to minimize the amount of code needed with for loops
	public static Image[] pellets = new Image[80];
	public static Image[] powerUps = new Image[4];

	boolean left, right, up, down, intersect = true;
	boolean ghostLeft[] = { false, false, false, false };
	boolean ghostRight[] = { false, false, false, false };
	boolean ghostUp[] = { false, false, false, false };
	boolean ghostDown[] = { false, false, false, false };

	Thread thread;
	//Length of the frame and the threat
	public static int frameLength = 800;
	public static int charLength = 50;

	//Pacman and the ghosts' positions
	public static int xPos = 375;
	public static int yPos = 625;
	public static int[] xPosGhost = { 305, 405, 305, 405 };
	public static int[] yPosGhost = { 205, 205, 295, 295 };

	//Speed of the characters
	int speed = 5;
	int ghostSpeed = 5;
	//This counter is used in order to ensure that pop up boxes within the run() method do not pop up repeatedly
	int runCounter = 0;
	//The boolean arrays here are use din order to determine whether or not the user got the corresponding pellet or power up
	boolean[] gotPellet = new boolean[80];
	boolean[] gotPowerUp = new boolean[4];
	//The score starts at 0 and the amount of lives starts at 3
	public static int score = 0;
	public static int lives = 3;
	//This boolean is for determining whether or not pacman is alive. If dead is set to true, new game can be called with it as a parameter to reset
	//the entire game fully.
	public static boolean dead = false;
	//For whether or not pacman has gotten a power up
	public static boolean powerup = false;
	//The various JComponents that make up the screen are here
	public static JFrame board;
	public static backupPacman mainPanel;
	public static JMenu fileMenu;
	public static JMenuItem newGameItem;
	public static JMenuBar menuBar;
	//For the score and lives remaining, which are shown on the menu bar
	public static JLabel livesLabel, scoreLabel;
	JLabel space;
	//If this is the name of the audio file, it'll be looped infinitely
	public final static String bgMusic = "bgMusic.wav";

	//Hitboxes for pacman, with once of htem being teh same size as the character's image and the other being slightly smaller for other purposes
	public static Rectangle hitbox;
	public static Rectangle hitbox2;
	//For ghost hitboxes
	Rectangle[] ghostHitbox = new Rectangle[4];
	//Other hitboxes (purpose shown in variable name)
	public static Rectangle[] wallsHitbox = new Rectangle[26];
	public static Rectangle[] pelletHitbox = new Rectangle[80];
	public static Rectangle[] powerUpHitbox = new Rectangle[4];

	//30 FPS is set with the thread and two timers are used in different parts of this program, such as to determine how long a power up is activated
	int FPS = 30;
	int timer = 0;
	int timer2 = 0;
	//If the user collides with a wall, they are returned to their last x or y position to ensure they do not go through it
	int lastXPos = 0;
	int lastYPos = 0;
	//Same collision method with ghosts
	int lastxPosGhost[] = { 0, 0, 0, 0 };
	int lastyPosGhost[] = { 0, 0, 0, 0 };
	//Counters used in the ghost methods below
	int ghostCounter[] = { 0, 0, 0, 0 };
	int ghostCounter2[] = { 0, 0, 0, 0 };
	
	//Equal is for equal coords (for collisions), 
	boolean equal[] = { true, true, true, true };
	//So that the ghost follows the user up
	boolean followUp[] = { false, false, false, false };
	//For whether or not the ghost can be eaten or if they pose a threat to the user
	boolean hostile[] = { true, true, true, true };

	public backupPacman() {
		setVisible(true);
		//Plays background music
		try {
			playSound("assets/" + bgMusic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		board = new JFrame("PACMAN");
		board.setLocation(100, 100);
		board.setPreferredSize(new Dimension(frameLength, frameLength + 20));
		board.setVisible(true);

		String placeHolder = " ";
		//Formatting the labels on the menubar so that they provide the user with useful information
		livesLabel = new JLabel("Lives: " + lives);
		scoreLabel = new JLabel("Score: " + score + "/80");
		space = new JLabel(String.format("%20s", placeHolder));
		fileMenu = new JMenu("File");
		newGameItem = new JMenuItem("New Game");
		menuBar = new JMenuBar();
		newGameItem.setEnabled(true);
		fileMenu.add(newGameItem);
		menuBar.add(fileMenu);
		menuBar.add(space);
		menuBar.add(livesLabel);
		menuBar.add(space);
		menuBar.add(scoreLabel);
		//When new game is pressed, a completely new game begins
		newGameItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newGame(true);
				dead = false;
				validate();
				repaint();
			}
		});

		//The wall's hitboxes are depicted in the code below
		// Top layer
		wallsHitbox[0] = new Rectangle(50, 50, 80, 40);
		wallsHitbox[1] = new Rectangle(180, 50, 150, 40);
		wallsHitbox[2] = new Rectangle(380, 50, 40, 85);
		wallsHitbox[3] = new Rectangle(470, 50, 150, 40);
		wallsHitbox[4] = new Rectangle(670, 50, 70, 40);
		// Second layer
		wallsHitbox[5] = new Rectangle(50, 145, 80, 40);
		wallsHitbox[6] = new Rectangle(180, 145, 40, 150);
		wallsHitbox[7] = new Rectangle(260, 145, 280, 40);
		wallsHitbox[8] = new Rectangle(580, 145, 40, 150);
		wallsHitbox[9] = new Rectangle(670, 145, 70, 40);
		// Third layer
		wallsHitbox[10] = new Rectangle(80, 235, 40, 200);
		wallsHitbox[11] = new Rectangle(260, 245, 40, 180);
		wallsHitbox[12] = new Rectangle(500, 245, 40, 180);
		wallsHitbox[13] = new Rectangle(680, 235, 40, 200);
		//Middle of screen
		wallsHitbox[14] = new Rectangle(300, 385, 200, 40);
		wallsHitbox[15] = new Rectangle(170, 350, 40, 205);
		wallsHitbox[16] = new Rectangle(590, 350, 40, 205);
		//Below middle but not button
		wallsHitbox[17] = new Rectangle(50, 485, 70, 70);
		wallsHitbox[18] = new Rectangle(680, 485, 60, 70);
		wallsHitbox[19] = new Rectangle(305, 475, 190, 40);

		wallsHitbox[20] = new Rectangle(305, 675, 190, 40);
		wallsHitbox[21] = new Rectangle(50, 675, 190, 40);
		wallsHitbox[22] = new Rectangle(560, 675, 180, 40);
		
		wallsHitbox[23] = new Rectangle(305, 605, 190, 20);
		wallsHitbox[24] = new Rectangle(50, 605, 190, 20);
		wallsHitbox[25] = new Rectangle(560, 605, 180, 20);

		// PELLETS, pacman at 375 625
		// First column from the left, but pelletHitbox[0] is first to the left of first
		// row
		pelletHitbox[0] = new Rectangle(70, 20, 12, 12);
		int graphicsCounter = 0;
		for (int i = 1; i < 14; i++) {
			pelletHitbox[i] = new Rectangle(18, 65 + graphicsCounter, 12, 12);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;

		// First ROW
		for (int i = 14; i < 27; i++) {
			pelletHitbox[i] = new Rectangle(120 + graphicsCounter, 20, 12, 12);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;

		// Right side perimeter
		for (int i = 27; i < 40; i++) {
			pelletHitbox[i] = new Rectangle(758, 65 + graphicsCounter, 12, 12);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;

		// Bottom row
		for (int i = 40; i < 54; i++) {
			pelletHitbox[i] = new Rectangle(70 + graphicsCounter, 735, 12, 12);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;

		// 2nd Row
		for(int i = 54; i < 66; i++) {
			if(i == 60)
				graphicsCounter+=100;
			pelletHitbox[i] = new Rectangle(70+graphicsCounter, 115, 12, 12);
			graphicsCounter+=50;
		}
		graphicsCounter = 0;

		// Row above pacman's spawn
		for (int i = 66; i < 80; i++) {
			pelletHitbox[i] = new Rectangle(70 + graphicsCounter, 575, 12, 12);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		
		//Power up hitboxes
		powerUpHitbox[0] = new Rectangle(18, 20, 12, 12);
		powerUpHitbox[1] = new Rectangle(758, 20, 12, 12);
		powerUpHitbox[2] = new Rectangle(18, 735, 12, 12);
		powerUpHitbox[3] = new Rectangle(758, 735, 12, 12);

		//This gets each image's file location/name
		pacBg = Toolkit.getDefaultToolkit().getImage("assets/pacBg.jpg");
		character = Toolkit.getDefaultToolkit().getImage("assets/pacRight.png");
		wallImage = Toolkit.getDefaultToolkit().getImage("assets/wallImage.jpg");
		ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/ghost1.png");
		ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/ghost2.png");
		ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/ghost3.png");
		ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/ghost4.png");
		// pacPellet = Toolkit.getDefaultToolkit().getImage("assets/pacPellet.png");

		//This sets the textures for each pellet/power up
		for (int i = 0; i < pellets.length; i++)
			pellets[i] = Toolkit.getDefaultToolkit().getImage("assets/pacPellet.png");
		for (int i = 0; i < powerUps.length; i++)
			powerUps[i] = Toolkit.getDefaultToolkit().getImage("assets/powerUp.png");

		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);

		//Starts thread
		thread = new Thread();
		thread.start();

	}

	// Description: Takes in the file name of an audio clip to be played as a
	// string, and plays it using Java's AudioInputStream class
	// Parameters: The location of the file as a string and what its called in that
	// location
	// Return: Returns the clip to be played
	public static Clip playSound(String fileName) throws Exception {
		Clip clip = null;
		try {
			// Makes a new file using the name (and/or location) of the file
			File audioFile = new File(fileName);
			if (audioFile.exists()) {
				AudioInputStream sound = AudioSystem.getAudioInputStream(audioFile);
				clip = AudioSystem.getClip();
				clip.open(sound);
				// Opens and starts playing the clip
				clip.start();
				// This ensures that the background music will constantly play whether or not a
				// new game is started so that there is no overlapping
				if (fileName.indexOf(bgMusic) != -1)
					clip.loop(Clip.LOOP_CONTINUOUSLY);
			} else
				throw new RuntimeException("File not found"); // A try catch statement is required in case a file cannot
			// be found
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clip;
	}

	//Description: Used to either start a new life if the parameter is true, or start a completely new game for the user
	//Parameters: Whether or not the user is dead, or if they clicked the button "New Game"
	//Return: N/A
	public void newGame(boolean dead) {
		//Character's position is reset
		xPos = 375;
		yPos = 625;
		//Boolean for whether or not the powerup is activated is set to false
		powerup = false;

		//Ghosts' positions are reset
		xPosGhost[0] = 305;
		xPosGhost[1] = 405;
		xPosGhost[2] = 305;
		xPosGhost[3] = 405;
		yPosGhost[0] = 205;
		yPosGhost[1] = 205;
		yPosGhost[2] = 295;
		yPosGhost[3] = 295;

		//Ghost images are reset
		ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/ghost1.png");
		ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/ghost2.png");
		ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/ghost3.png");
		ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/ghost4.png");
		
		//Counter used for pop up boxes in run() is reset, as well as the speeds
		runCounter = 0;
		speed = 5;
		ghostSpeed = 5;
		if (dead) {
			//Score and timers reset
			score = 0;
			timer2 = 0;
			for (int i = 0; i < pellets.length; i++)
				pellets[i] = Toolkit.getDefaultToolkit().getImage("assets/pacPellet.png");

			for (int i = 0; i < gotPellet.length; i++)
				gotPellet[i] = false;
			for (int i = 0; i < powerUps.length; i++) {
				powerUps[i] = Toolkit.getDefaultToolkit().getImage("assets/powerUp.png");
				hostile[i] = true;
			}
			for (int i = 0; i < gotPowerUp.length; i++)
				gotPowerUp[i] = false;
			lives = 3;
			livesLabel.setText("Lives: " + lives);
			scoreLabel.setText("Score: " + score + "/80");
		}
		validate();
		repaint();
	}
	
	//Description: This method is constantly run throughout the program in order to update the program with new information
	//Parameters and return: None
	public void run() {
		while (true) {
			// main game loop for whether or not they have reached a score of 80, as well as whether or not the runCounter is 0
			if (score == 80 && runCounter == 0) {
				runCounter++;
				JOptionPane.showMessageDialog(this, "YOU WIN! GO TO FILE > NEW TO START A NEW GAME!", "You Win!",
						JOptionPane.WARNING_MESSAGE);
				speed = 0;
				ghostSpeed = 0;
			}
			// main game loop for whether or not the user has died
			if (lives == 0 && runCounter == 0) {
				runCounter++;
				dead = true;
				JOptionPane.showMessageDialog(this, "GO TO FILE > NEW TO START A NEW GAME!", "Game Over",
						JOptionPane.WARNING_MESSAGE);
				speed = 0;
				ghostSpeed = 0;

			}
			//Setting all of the hitboxes repeatedly based on the movement of the characters
			hitbox = new Rectangle(xPos, yPos, charLength, charLength);
			hitbox2 = new Rectangle(xPos + 10, yPos + 10, charLength - 20, charLength - 20);
			ghostHitbox[0] = new Rectangle(xPosGhost[0], yPosGhost[0], charLength - 10, charLength - 10);
			ghostHitbox[1] = new Rectangle(xPosGhost[1], yPosGhost[1], charLength - 10, charLength - 10);
			ghostHitbox[2] = new Rectangle(xPosGhost[2], yPosGhost[2], charLength - 10, charLength - 10);
			ghostHitbox[3] = new Rectangle(xPosGhost[3], yPosGhost[3], charLength - 10, charLength - 10);
			
			//Increasing the values of the timer
			timer++;
			move();
			//Setting the big powerup to true if the user got all four of the smaller ones
			if (gotPowerUp[0] && gotPowerUp[1] && gotPowerUp[2] && gotPowerUp[3] && timer2 != 300) {
				powerup = true;
			}
			//Moving the ghost
			ghostMove();
			this.repaint();
			//Adding delay
			try {
				Thread.sleep(1000 / FPS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Description: Used in order to contain all of the major graphics components, such as walls, pellets, the character and other visual parts
	//Parameters: The graphics object so that different textures can be drawn on to the screen
	//Return: None
	//To find what drawing refers to what, the same order can be found in the constructor for the corresponding hitboxes
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		//Drawing the character based on where they are
		g.drawImage(pacBg, 0, 0, 800, 800, this);
		g.drawImage(character, xPos, yPos, charLength, charLength, this);
		g.drawImage(ghostImage1, xPosGhost[0], yPosGhost[0], charLength - 10, charLength - 10, this);
		g.drawImage(ghostImage2, xPosGhost[1], yPosGhost[1], charLength - 10, charLength - 10, this);
		g.drawImage(ghostImage3, xPosGhost[2], yPosGhost[2], charLength - 10, charLength - 10, this);
		g.drawImage(ghostImage4, xPosGhost[3], yPosGhost[3], charLength - 10, charLength - 10, this);

		//Drawing all of the walls
		g.drawImage(wallImage, 50, 50, 80, 40, this);
		g.drawImage(wallImage, 180, 50, 150, 40, this);
		g.drawImage(wallImage, 380, 50, 40, 85, this);
		g.drawImage(wallImage, 470, 50, 150, 40, this);
		g.drawImage(wallImage, 670, 50, 70, 40, this);

		g.drawImage(wallImage, 50, 145, 80, 40, this);
		g.drawImage(wallImage, 180, 145, 40, 150, this);
		g.drawImage(wallImage, 260, 145, 280, 40, this);
		g.drawImage(wallImage, 580, 145, 40, 150, this);
		g.drawImage(wallImage, 670, 145, 70, 40, this);

		g.drawImage(wallImage, 80, 235, 40, 200, this);
		g.drawImage(wallImage, 260, 245, 40, 180, this);
		g.drawImage(wallImage, 500, 245, 40, 180, this);
		g.drawImage(wallImage, 680, 235, 40, 200, this);

		g.drawImage(wallImage, 300, 385, 200, 40, this);
		g.drawImage(wallImage, 170, 350, 40, 205, this);
		g.drawImage(wallImage, 590, 350, 40, 205, this);

		g.drawImage(wallImage, 50, 485, 70, 70, this);
		g.drawImage(wallImage, 680, 485, 60, 70, this);
		g.drawImage(wallImage, 305, 475, 190, 40, this);

		g.drawImage(wallImage, 305, 675, 190, 40, this);
		g.drawImage(wallImage, 50, 675, 190, 40, this);
		g.drawImage(wallImage, 560, 675, 180, 40, this);

		g.drawImage(wallImage, 305, 605, 190, 20, this);
		g.drawImage(wallImage, 50, 605, 190, 20, this);
		g.drawImage(wallImage, 560, 605, 180, 20, this);

		g.setColor(Color.BLUE);
		//Drawing rectangles for the outlines of the walls
		g.drawRect(50, 50, 80, 40);
		g.drawRect(180, 50, 150, 40);
		g.drawRect(380, 50, 40, 85);
		g.drawRect(470, 50, 150, 40);
		g.drawRect(670, 50, 70, 40);

		g.drawRect(50, 145, 80, 40);
		g.drawRect(180, 145, 40, 150);
		g.drawRect(260, 145, 280, 40);
		g.drawRect(580, 145, 40, 150);
		g.drawRect(670, 145, 70, 40);

		g.drawRect(80, 235, 40, 200);
		g.drawRect(260, 245, 40, 180);
		g.drawRect(500, 245, 40, 180);
		g.drawRect(680, 235, 40, 200);

		g.drawRect(300, 385, 200, 40);
		g.drawRect(170, 350, 40, 205);
		g.drawRect(590, 350, 40, 205);

		g.drawRect(50, 485, 70, 70);
		g.drawRect(680, 485, 60, 70);
		g.drawRect(305, 475, 190, 40);

		g.drawRect(305, 675, 190, 40);
		g.drawRect(50, 675, 190, 40);
		g.drawRect(560, 675, 180, 40);

		g.drawRect(305, 605, 190, 20);
		g.drawRect(50, 605, 190, 20);
		g.drawRect(560, 605, 180, 20);
		
		g.setColor(Color.RED);
		// shows where the pacman cannot go but where the ghost can go
		g.drawLine(222, 145, 222, 185);
		g.drawLine(222, 245, 222, 295);

		g.drawLine(258, 145, 258, 185);
		g.drawLine(258, 245, 258, 295);

		g.drawLine(542, 145, 542, 185);
		g.drawLine(542, 245, 542, 295);

		g.drawLine(578, 145, 578, 185);
		g.drawLine(578, 245, 578, 295);

		//Loops for drawing all of the pellets, same order as in constructor
		g.drawImage(pellets[0], 70, 20, 12, 12, this);
		int graphicsCounter = 0;
		for (int i = 1; i < 14; i++) {
			g.drawImage(pellets[i], 18, 65 + graphicsCounter, 12, 12, this);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		for (int i = 14; i < 27; i++) {
			g.drawImage(pellets[i], 120 + graphicsCounter, 20, 12, 12, this);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		for (int i = 27; i < 40; i++) {
			g.drawImage(pellets[i], 758, 65 + graphicsCounter, 12, 12, this);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		for (int i = 40; i < 54; i++) {
			g.drawImage(pellets[i], 70 + graphicsCounter, 735, 12, 12, this);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		for(int i = 54; i < 66; i++) {
			if(i == 60)
				graphicsCounter+=100;
			g.drawImage(pellets[i], 70+graphicsCounter, 115, 12, 12, this);
			graphicsCounter+=50;
		}
		graphicsCounter = 0;
		for (int i = 66; i < 80; i++) {
			g.drawImage(pellets[i], 70 + graphicsCounter, 575, 12, 12, this);
			graphicsCounter += 50;
		}
		graphicsCounter = 0;
		//Drawing the power up locations
		g.drawImage(powerUps[0], 18, 20, 20, 20, this);
		g.drawImage(powerUps[1], 758, 20, 20, 20, this);
		g.drawImage(powerUps[2], 18, 735, 20, 20, this);
		g.drawImage(powerUps[3], 758, 735, 20, 20, this);
	
	}

	public static void main(String[] args) {
		mainPanel = new backupPacman();
		board.setJMenuBar(menuBar);
		board.add(mainPanel);
		board.addKeyListener((KeyListener) mainPanel);
		board.pack();
		board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		board.setResizable(false);
		//Running the main game
		mainPanel.run();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	//Description: This method is used to check whether or not the user has collided with the pellets, and if they did the score is updated
	//Parameters: The parameters used are the rectangle for the pellet (which is in an array), an int for the direction, and an int for which pellet it is (its index)
	//Return: None
	void checkCollisionPel(Rectangle pellet, int direction, int pelletNum) {
		//Used in order to ensure that the collision is correct by altering the rectangle's hitbox for the corresponding user direction (key press)
		Rectangle pellet2 = new Rectangle(pellet);
		if (direction == 1)
			pellet2.x -= speed;
		Rectangle pellet3 = new Rectangle(pellet);
		if (direction == 2)
			pellet3.y -= speed;
		Rectangle pellet4 = new Rectangle(pellet);
		if (direction == 3)
			pellet4.y += speed;
		Rectangle pellet5 = new Rectangle(pellet);
		if (direction == 4)
			pellet5.x += speed;
		//If any of the above rectangles are intersected (or specifically just one since it depends on the direction), and if the pellet hasn't been eaten yet, which is 
		//checked using a boolean array, this if statement updates the score, updates the boolean array, updates the menu bar's score label, and gets rid of the pellet texture
		if (gotPellet[pelletNum] == false && (hitbox.intersects(pellet) || hitbox.intersects(pellet2)
				|| hitbox.intersects(pellet3) || hitbox.intersects(pellet4) || hitbox.intersects(pellet5))) {
			try {
				playSound("assets/pacEat.wav");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			pellets[pelletNum] = Toolkit.getDefaultToolkit().getImage("nothing.png");
			score += 1;
			gotPellet[pelletNum] = true;
			scoreLabel.setText("Score: " + score + "/80");
		}
	}
	//Description: This method is used to check whether or not the user has collided with the powerup, and if they did they get the powerup
		//Parameters: The parameters used are the rectangle for the powerup (which is in an array), an int for the direction, and an int for which powerup it is (its index)
		//Return: None
	void checkCollisionPowerUp(Rectangle powerUp, int direction, int pelletNum) {
		Rectangle pellet2 = new Rectangle(powerUp);
		if (direction == 1)
			pellet2.x -= speed;
		Rectangle pellet3 = new Rectangle(powerUp);
		if (direction == 2)
			pellet3.y -= speed;
		Rectangle pellet4 = new Rectangle(powerUp);
		if (direction == 3)
			pellet4.y += speed;
		Rectangle pellet5 = new Rectangle(powerUp);
		if (direction == 4)
			pellet5.x += speed;
		if (gotPowerUp[pelletNum] == false && (hitbox.intersects(powerUp) || hitbox.intersects(pellet2)
				|| hitbox.intersects(pellet3) || hitbox.intersects(pellet4) || hitbox.intersects(pellet5))) {
			powerUps[pelletNum] = Toolkit.getDefaultToolkit().getImage("nothing.png");
			gotPowerUp[pelletNum] = true;
			try {
				playSound("assets/pacPowerUp.wav");
			} catch (Exception e) {
				e.printStackTrace();
			}
			ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			//
			timer = 0;
			hostile[0] = false;
			hostile[1] = false;
			hostile[2] = false;
			hostile[3] = false;
		}
		if (gotPowerUp[pelletNum] && timer == 300) {
			ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/ghost1.png");
			ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/ghost2.png");
			ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/ghost3.png");
			ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/ghost4.png");
			hostile[0] = true;
			hostile[1] = true;
			hostile[2] = true;
			hostile[3] = true;
		}
		if (timer > 200 && timer < 299 && timer % 3 == 1) {
			if (!hostile[0])
				ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/ghost1.png");
			if (!hostile[1])
				ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/ghost2.png");
			if (!hostile[2])
				ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/ghost3.png");
			if (!hostile[3])
				ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/ghost4.png");
		}
		if (timer > 200 && timer < 299 && timer % 3 == 0) {
			if (!hostile[0])
				ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			if (!hostile[1])
				ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			if (!hostile[2])
				ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
			if (!hostile[3])
				ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/scaredghost.png");
		}
	}

	// For user with wall
	//needs the rectangle for the wall and the direction pacman is facing in an integer value
	// no return
	void checkCollision(Rectangle wall, int direction) {
		Rectangle wall2 = new Rectangle(wall);
		if (direction == 1)
			wall2.x -= speed;
		Rectangle wall3 = new Rectangle(wall);
		if (direction == 2)
			wall3.y -= speed;
		Rectangle wall4 = new Rectangle(wall);
		if (direction == 3)
			wall4.y += speed;
		Rectangle wall5 = new Rectangle(wall);
		if (direction == 4)
			wall5.x += speed;
		if (hitbox.intersects(wall) || hitbox.intersects(wall2) || hitbox.intersects(wall3) || hitbox.intersects(wall4)
				|| hitbox.intersects(wall5)) {
			xPos = lastXPos;
			yPos = lastYPos;
		}

	}

	// For ghosts collision
	// needs the rectangle for the wall, the direction the ghost faces and the index of the ghost(which ghost is being used)
	// no return
	void checkCollision(Rectangle wall, int direction, int j) {
		Rectangle wall2 = new Rectangle(wall);
		if (direction == 1)
			wall2.x -= speed;
		Rectangle wall3 = new Rectangle(wall);
		if (direction == 2)
			wall3.y -= speed;
		Rectangle wall4 = new Rectangle(wall);
		if (direction == 3)
			wall4.y += speed;
		Rectangle wall5 = new Rectangle(wall);
		if (direction == 4)
			wall5.x += speed;

		if ((ghostHitbox[j].intersects(wall) || ghostHitbox[j].intersects(wall2) || ghostHitbox[j].intersects(wall3)
				|| ghostHitbox[j].intersects(wall4) || ghostHitbox[j].intersects(wall5))
				&& (direction == 1 || direction == 4)) {
			ghostCounter2[j]++;
			xPosGhost[j] = lastxPosGhost[j];
			if (yPos < yPosGhost[j] && equal[j]) {
				yPosGhost[j] = lastyPosGhost[j];
				for (int i = 0; i < 26; i++) {
					if (yPosGhost[j] + 40 == wallsHitbox[i].y && xPosGhost[j] >= wallsHitbox[i].x
							&& xPosGhost[j] < wallsHitbox[i].x + wallsHitbox[i].width) {
						ghostCounter[j]++;
					}
				}
				if (ghostCounter[j] >= 1 || followUp[j]) {
					yPosGhost[j] -= ghostSpeed;
					lastyPosGhost[j] = yPosGhost[j];
					followUp[j] = true;
				} else if (ghostCounter[j] < 1) {
					yPosGhost[j] -= ghostSpeed;
					lastyPosGhost[j] = yPosGhost[j];
				}
			} else if (yPos > yPosGhost[j] && equal[j]) {
				yPosGhost[j] = lastyPosGhost[j];

				for (int i = 0; i < 26; i++) {
					if (yPosGhost[j] + 40 == wallsHitbox[i].y && xPosGhost[j] >= wallsHitbox[i].x
							&& xPosGhost[j] < wallsHitbox[i].x + wallsHitbox[i].width) {
						ghostCounter[j]++;
					}
				}
				if (ghostCounter[j] >= 1 || followUp[j]) {
					yPosGhost[j] -= ghostSpeed;
					lastyPosGhost[j] = yPosGhost[j];
					followUp[j] = true;
				} else if (ghostCounter[j] < 1) {
					yPosGhost[j] += ghostSpeed;
					lastyPosGhost[j] = yPosGhost[j];
				}

			} else {
				for (int i = 0; i < 26; i++) {
					if (xPosGhost[j] + 40 == wallsHitbox[i].x && yPosGhost[j] + 40 >= wallsHitbox[i].y
							&& yPosGhost[j] <= wallsHitbox[i].y + wallsHitbox[i].height) {
						// if(yPosGhost[j]+15>wallsHitbox[i].y+wallsHitbox[i].height/2)
						// yPosGhost[j] +=ghostSpeed;
						// else if (yPosGhost[j]+15<=wallsHitbox[i].y+wallsHitbox[i].height/2)
						// yPosGhost[j] -=ghostSpeed;
						yPosGhost[j] -= ghostSpeed;
						break;
					}
					if (xPosGhost[j] - wallsHitbox[i].width == wallsHitbox[i].x && yPosGhost[j] + 40 >= wallsHitbox[i].y
							&& yPosGhost[j] <= wallsHitbox[i].y + wallsHitbox[i].height) {
						yPosGhost[j] -= ghostSpeed;
						break;
					}

				}
				equal[j] = false;

			}
		} else if ((ghostHitbox[j].intersects(wall) || ghostHitbox[j].intersects(wall2)
				|| ghostHitbox[j].intersects(wall3) || ghostHitbox[j].intersects(wall4)
				|| ghostHitbox[j].intersects(wall5)) && (direction == 2 || direction == 3)) {
			ghostCounter2[j]++;
			yPosGhost[j] = lastyPosGhost[j];
		}
	}
	// controls pacman movements based off of keys pressed
		// parameters:none
		// return: nothing
	void move() {
		if (left) {
			
			lastXPos = xPos;
			lastYPos = yPos;
			xPos -= speed;
			if (xPos < 0)
				xPos = lastXPos;
			character = Toolkit.getDefaultToolkit().getImage("assets/pacLeft.png");

			for (int i = 0; i < wallsHitbox.length; i++)
				checkCollision(wallsHitbox[i], 4);
			for (int i = 0; i < 80; i++)
				checkCollisionPel(pelletHitbox[i], 4, i);
			for (int i = 0; i < 4; i++)
				checkCollisionPowerUp(powerUpHitbox[i], 4, i);

		} else if (right) {
			lastXPos = xPos;
			lastYPos = yPos;
			xPos += speed;
			if (xPos > frameLength - charLength - 10)
				xPos = lastXPos;
			character = Toolkit.getDefaultToolkit().getImage("assets/pacRight.png");
			for (int i = 0; i < wallsHitbox.length; i++)
				checkCollision(wallsHitbox[i], 1);
			for (int i = 0; i < 80; i++)
				checkCollisionPel(pelletHitbox[i], 1, i);
			for (int i = 0; i < 4; i++)
				checkCollisionPowerUp(powerUpHitbox[i], 1, i);

		} else if (up) {
			lastXPos = xPos;
			lastYPos = yPos;
			yPos -= speed;
			if (yPos < 0)
				yPos = lastYPos;
			character = Toolkit.getDefaultToolkit().getImage("assets/pacUpRight.png");

			for (int i = 0; i < wallsHitbox.length; i++)
				checkCollision(wallsHitbox[i], 3);
			for (int i = 0; i < 80; i++)
				checkCollisionPel(pelletHitbox[i], 3, i);
			for (int i = 0; i < 4; i++)
				checkCollisionPowerUp(powerUpHitbox[i], 3, i);
		} else if (down) {
			lastXPos = xPos;
			lastYPos = yPos;
			yPos += speed;
			if (yPos > frameLength - charLength - 30.5)
				yPos = lastYPos;
			character = Toolkit.getDefaultToolkit().getImage("assets/pacDownRight.png");
			for (int i = 0; i < wallsHitbox.length; i++)
				checkCollision(wallsHitbox[i], 2);
			for (int i = 0; i < 80; i++)
				checkCollisionPel(pelletHitbox[i], 2, i);
			for (int i = 0; i < 4; i++)
				checkCollisionPowerUp(powerUpHitbox[i], 2, i);

		}
	}
	// controls the ghost movement through pacmans actions. each ghost has an xpos and ypos represented by an array which is changed according to pacmans movement
		// this method also has the powerup included, because if the user gets the powerup, they should be able to control the ghosts
		// parameters: none
		// nothing returned because its a void method
	void ghostMove() {
		if (!powerup)
			// for loop checks for each ghost
			for (int j = 0; j < 4; j++) {
				ghostCounter[j] = 0;
				ghostCounter2[j] = 0;
				if (xPos > xPosGhost[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					xPosGhost[j] += ghostSpeed;
					for (int i = 0; i < wallsHitbox.length; i++) {
						// checks collisions
						checkCollision(wallsHitbox[i], 1, j);

					}
				} else if (xPos < xPosGhost[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					xPosGhost[j] -= ghostSpeed;
					for (int i = 0; i < wallsHitbox.length; i++) {
						// checks collisions
						checkCollision(wallsHitbox[i], 4, j);

					}
				} else if (yPos > yPosGhost[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					yPosGhost[j] += ghostSpeed;
					for (int i = 0; i < wallsHitbox.length; i++) {
						// checks collisions
						checkCollision(wallsHitbox[i], 2, j);

					}
				} else if (yPos < yPosGhost[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					yPosGhost[j] -= ghostSpeed;
					for (int i = 0; i < wallsHitbox.length; i++) {
						// checks collisions
						checkCollision(wallsHitbox[i], 3, j);

					}
				}

				if (ghostCounter2[j] < 1) {
					if (followUp[j]) {
						followUp[j] = false;
					}
					if (!equal[j]) {
						equal[j] = true;
					}
				}
				// if the ghost hits the hitbox, the player dies. however, if the ghosts are vulnerable do to the powerup, when pacman can eat the ghosts
				if (ghostHitbox[j].intersects(hitbox2) && lives > 0 && runCounter == 0) {
					if (hostile[j]) {
						hostile[0] = true;
						hostile[1] = true;
						hostile[2] = true;
						hostile[3] = true;
						lives--;
						try {
							playSound("assets/pacDeath.wav");
						} catch (Exception e) {
							e.printStackTrace();
						}
						livesLabel.setText("Lives: " + lives);

						JOptionPane.showMessageDialog(this, lives + " lives left!!", "Life Lost!",
								JOptionPane.WARNING_MESSAGE);
						runCounter++;
						speed = 0;
						ghostSpeed = 0;
						newGame(dead);
					} else {
						try {
							playSound("assets/pacEat.wav");
						} catch (Exception e) {
							e.printStackTrace();
						}
						yPosGhost[j] = 300;
						hostile[j] = true;
						if (j == 0) {
							ghostImage1 = Toolkit.getDefaultToolkit().getImage("assets/ghost1.png");
							xPosGhost[j] = 305;
							yPosGhost[j] = 205;
						}
						if (j == 1) {
							ghostImage2 = Toolkit.getDefaultToolkit().getImage("assets/ghost2.png");
							xPosGhost[j] = 405;
							yPosGhost[j] = 205;
						}
						if (j == 2) {
							ghostImage3 = Toolkit.getDefaultToolkit().getImage("assets/ghost3.png");
							xPosGhost[j] = 305;
							yPosGhost[j] = 295;
						}
						if (j == 3) {
							ghostImage4 = Toolkit.getDefaultToolkit().getImage("assets/ghost4.png");
							xPosGhost[j] = 405;
							yPosGhost[j] = 295;
						}
					}

				}

			}
		else {
			//the ghosts can be controlled by the user by using arrow keys
			timer = 0;
			timer2++;
			if (timer2 == 1)
				JOptionPane.showMessageDialog(this, "Use the arrow keys to control the ghosts!", "POWER UP!",
						JOptionPane.WARNING_MESSAGE);
			// turns off the powerup
			if (timer2 == 300) {
				powerup = false;

			}
			// moves the ghosts similar to how pacman moves(constantly moving unless collision happens)
			for (int j = 0; j < 4; j++)
				if (ghostUp[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					yPosGhost[j] -= ghostSpeed;
					if (yPosGhost[j] < 0)
						yPosGhost[j] = lastyPosGhost[j];

					ghostUp[j] = true;
					for (int i = 0; i < wallsHitbox.length; i++)
						controlGhosts(wallsHitbox[i], 3, j);
				} else if (ghostDown[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					yPosGhost[j] += ghostSpeed;
					if (yPosGhost[j] > frameLength - charLength - 30.5)
						yPosGhost[j] = lastyPosGhost[j];
					ghostDown[j] = true;
					for (int i = 0; i < wallsHitbox.length; i++)
						controlGhosts(wallsHitbox[i], 2, j);
				} else if (ghostLeft[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					xPosGhost[j] -= ghostSpeed;
					if (xPosGhost[j] < 0)
						xPosGhost[j] = lastxPosGhost[j];
					ghostLeft[j] = true;
					for (int i = 0; i < wallsHitbox.length; i++)
						controlGhosts(wallsHitbox[i], 4, j);
				}

				else if (ghostRight[j]) {
					lastxPosGhost[j] = xPosGhost[j];
					lastyPosGhost[j] = yPosGhost[j];
					xPosGhost[j] += ghostSpeed;
					if (xPosGhost[j] > frameLength - charLength - 10)
						xPosGhost[j] = lastxPosGhost[j];
					for (int i = 0; i < wallsHitbox.length; i++)
						controlGhosts(wallsHitbox[i], 1, j);
				}
		}
	}

	void controlGhosts(Rectangle wall, int direction, int j) {
		Rectangle wall2 = new Rectangle(wall);
		if (direction == 1)
			wall2.x -= speed;
		Rectangle wall3 = new Rectangle(wall);
		if (direction == 2)
			wall3.y -= speed;
		Rectangle wall4 = new Rectangle(wall);
		if (direction == 3)
			wall4.y += speed;
		Rectangle wall5 = new Rectangle(wall);
		if (direction == 4)
			wall5.x += speed;
		if (ghostHitbox[j].intersects(wall) || ghostHitbox[j].intersects(wall2) || ghostHitbox[j].intersects(wall3)
				|| ghostHitbox[j].intersects(wall4) || ghostHitbox[j].intersects(wall5)) {
			xPosGhost[j] = lastxPosGhost[j];
			yPosGhost[j] = lastyPosGhost[j];
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (left) {
			left = !left;
		}
		if (right) {
			right = !right;
		}
		if (up) {
			up = !up;
		}
		if (down) {
			down = !down;
		}
		for (int j = 0; j < 4; j++) {
			if (ghostLeft[j]) {
				ghostLeft[j] = !ghostLeft[j];
			}
			if (ghostRight[j]) {
				ghostRight[j] = !ghostRight[j];
			}
			if (ghostUp[j]) {
				ghostUp[j] = !ghostUp[j];
			}
			if (ghostDown[j]) {
				ghostDown[j] = !ghostDown[j];
			}
		}
		hitbox = new Rectangle(xPos, yPos, charLength, charLength);
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_A) {
			
			left = !left;
		} else if (key == KeyEvent.VK_D) {
			right = !right;
		} else if (key == KeyEvent.VK_W) {
			up = !up;
		}
		else if (key == KeyEvent.VK_S) {
			down = !down;

		}
		// if all the star powerups are eaten, a powerup is unlocked which allows the
		// user to control the ghosts
		// arrows keys used to control the ghosts
		if (powerup) {
			if (key == KeyEvent.VK_UP) {
				for (int j = 0; j < 4; j++) {
					ghostUp[j] = true;
				}
			} else if (key == KeyEvent.VK_DOWN) {
				for (int j = 0; j < 4; j++) {
					ghostDown[j] = true;
				}
			} else if (key == KeyEvent.VK_LEFT) {
				for (int j = 0; j < 4; j++) {
					ghostLeft[j] = true;
				}
			}

			else if (key == KeyEvent.VK_RIGHT) {
				for (int j = 0; j < 4; j++) {
					ghostRight[j] = true;
				}
			}
		}
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}





