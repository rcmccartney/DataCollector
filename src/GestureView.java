
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

/*
 * 
 * For Leap, don't need to use a Listener class.  Can run it 
 * from the main execution thread if you want as well, using 
 * controller.frame method
 * 
 * Total of 116 features collected per Frame of data
 * 
 */
public class GestureView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_WIDTH = 800; //Width of screen
	public static final int DEFAULT_HEIGHT = 600; //Height of screen
	public static final float ASPECT_RAT = ((float) DEFAULT_WIDTH) / DEFAULT_HEIGHT;
	public static final int START_X = 150; //Starting x position
	public static final int START_Y = 100; // Starting y position
	public static final int DEFAULT_INTERVAL = 10;

	protected JFrame frame;
	protected BufferedImage imageRecord, imageStop, imageWait, leapImg, currentImage;
	//instrShowing is true when instructions for each gesture are being displayed
	protected int handCount;
	protected JLabel handDisplay, recordInstructions, gestureDisplay;
	protected JTextArea instructions;
	protected JPanel instructionPanel, textPanel;
	protected GestureController controller; 
	protected Timer timer;
	protected int[] baseDimensions, currDimensions;
	
	
	public GestureView(GestureController controller) {
		super();
		this.controller = controller;
		this.setLayout(new BorderLayout());
		this.setBackground(Color.DARK_GRAY);
		try {
			imageRecord = ImageIO.read(new File("images" + File.separator + "red.png"));
			imageStop = ImageIO.read(new File("images" + File.separator + "green.png"));
			imageWait = ImageIO.read(new File("images" + File.separator + "yellow.png"));
			leapImg = ImageIO.read(new File("images" + File.separator + "offset.jpg"));
		} 
		catch (IOException ex) {
			System.err.println("Could not load image files.");
			System.exit(1);
		}
		currentImage = imageStop;
		this.buildInstructionDisplay();
		this.buildCenterInstructions();
		this.buildFrame(controller);
	    timer = new Timer(DEFAULT_INTERVAL, new TimeListener());
		int w = this.getWidth();
		int h = (int) (this.getHeight()*ASPECT_RAT); 
		baseDimensions = new int[]{ 2*w/8, this.getHeight()/2 - (h/4), w/2, h/2 };
		currDimensions = baseDimensions.clone();

		//CODE TO MAKE A CONSOLE LIKE STDIN FOR VIEWING OUTPUT
		//text = new JTextArea(15,30);
    	//aPanel.add(new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
        //       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    	//this.setSize(500, 500);
        //PrintStream con=new PrintStream(new TextAreaOutputStream(text));
		//System.setOut(con);
        //System.setErr(con);
	}
	
	private void buildFrame(GestureController controller) {
		//Build a frame and add this (a JPanel) to it
		frame = new JFrame("Gesture Writer");
		frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		frame.setLocation(START_X, START_Y);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(this);
    	//frame.pack();
    	frame.setVisible(true);
    	frame.setAlwaysOnTop(true);
    	//start the keylistener for user input
    	frame.addKeyListener(controller);
	}
	
	private void buildInstructionDisplay() {
		
		//make the hand count display on top
		handDisplay = new JLabel("<html>Hands in view: <b><font color=\"red\">0</font></b></html>");
		handDisplay.setFont(new Font("Serif", 1, 40));
		this.add(handDisplay, BorderLayout.NORTH);
		//make the instructions and current gesture display on bottom
		recordInstructions = new JLabel("Hold 's' to record a gesture");
		recordInstructions.setFont(new Font("Serif", 1, 20));
		recordInstructions.setForeground(Color.BLACK);
		// this displays the current gesture being recorded
		gestureDisplay = new JLabel(Statics.Gestures.values()[0].toString() +
									" gesture: 0 of " + Statics.COUNT + " completed");
		gestureDisplay.setFont(new Font("Serif", 1, 20));
		gestureDisplay.setForeground(Color.BLACK);
		// bottom panel to hold the instructions and current recording information
		textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout(5,5));
		textPanel.setBackground(Color.DARK_GRAY);
		textPanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createRaisedBevelBorder(), 
							BorderFactory.createLoweredBevelBorder()) );
		textPanel.add(recordInstructions, BorderLayout.NORTH);
		textPanel.add(gestureDisplay, BorderLayout.SOUTH);
		this.add(textPanel, BorderLayout.SOUTH);	
	}
	
	private void buildCenterInstructions() {
		//Special instructions appear in the center of the screen to transition between each gesture, as well
		//as first-time instructions when the program first starts up to explain how it works
		instructions = new JTextArea("\n\nStart the Leap Diagnostic Visualizer to see hand while recording.\n\n"
						+ "Available Commands:\n"
						+ "Press 's' to start and stop a gesture.\n"
						+ "Once recorded, press 'a' to keep the gesture.\n"
						+ "Press 'r' to remove it due to errors.\n"
						+ "At any time, press 'p' to replay the last gesture video.\n"
						+ "\n\nPress 'c' to continue...");
		instructions.setFont(new Font("Serif", 1, 20));
		instructions.setEnabled(false);
		instructions.setDisabledTextColor(Color.BLACK);
		instructions.setOpaque(false);
		instructionPanel = new JPanel();
		instructionPanel.setBackground(Color.WHITE);
		instructionPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		instructionPanel.add(instructions, BorderLayout.CENTER );
		this.add(instructionPanel, BorderLayout.CENTER);
		controller.instrShowing = true;
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g2d); 
		g2d.drawImage(currentImage, currDimensions[0], currDimensions[1],
					currDimensions[2], currDimensions[3], null);
	}

	public void setHandCount(int aCount) {
		if (aCount != handCount) {
			handCount = aCount;
			if (handCount != 0)
				handDisplay.setText("Hands in view: " + handCount);
			else
				handDisplay.setText("<html>Hands in view: <b><font color=\"red\">0</font></b></html>");
			this.repaint();
		}
	}
	
	public void play(final String file) {
		controller.videoPlaying = true;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				playVideo(file);
			}
		});
		while (controller.videoPlaying == true)
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
		frame.remove(frame.getContentPane());
		frame.setContentPane(this);
		frame.setVisible(true);
	}
	
	private void playVideo(String file) {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		frame.setContentPane(mediaPlayerComponent);
		frame.setVisible(true);
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		mediaPlayerComponent.getMediaPlayer().playMedia(file);
		try { Thread.sleep(6000); } catch (InterruptedException e) { }
		controller.videoPlaying = false;
	}
	
	public void updateInstructionDisplay(String message) {
		controller.instrShowing = true;
		instructionPanel.setVisible(true);
		instructions.setText("\n\n" + message + "\n\nPress 'p' to replay the video.\nPress 'c' to continue...");
		this.setBackground(Color.DARK_GRAY);
		textPanel.setBackground(Color.DARK_GRAY);
	}
	
	public void updateGestureDisplay(String gesture, int count) {
		gestureDisplay.setText(gesture + " gesture: " + count + " of " + Statics.COUNT + " completed");
	}
	
	public void showLeapHeight() {
		currentImage = leapImg;
		currDimensions = new int[]{ 0, 0, this.getWidth(), this.getHeight() };
		controller.leapDisplayed = true;
		instructionPanel.setVisible(false);
		recordInstructions.setText("Press 'c' to continue.");
		this.repaint();
	}
	
	public void showRecording() {
		currentImage = imageRecord;
		recordInstructions.setText("Release 's' to stop recording.");
		currDimensions = baseDimensions.clone();
		timer.start();
	}
	
	public void showStopped() {
		currentImage = imageStop;
		currDimensions = baseDimensions.clone();
		timer.stop();
		recordInstructions.setText("Hold 's' to record a gesture");
		this.repaint();
	}
	
	public void pause() {
		controller.decisionMade = false;
		currDimensions = baseDimensions.clone();
		timer.stop();
		currentImage = imageWait;
		recordInstructions.setText("Press 'a' to accept or 'r' to reject the recorded gesture");
		this.repaint();
	}
	
	private class TimeListener implements ActionListener {
		
		//private int size = -1;
		private int xDir = 3;
		private int yDir = 2;
		//private int steps = 100;
		//private int i = 0;
		
		public void actionPerformed(ActionEvent e) {
			
			//if (++i % steps == 0)
			//	size *= -1;
			GestureView.this.currDimensions[0] += xDir;
			GestureView.this.currDimensions[1] += yDir;
			//GestureView.this.currDimensions[2] += size;
			//GestureView.this.currDimensions[3] += size;
			if (currDimensions[0] < 0 ||  
					currDimensions[0] + currDimensions[2] > GestureView.this.getWidth())
				xDir *= -1;
			if (currDimensions[1] < 0 ||  
					currDimensions[1] + currDimensions[3] > GestureView.this.getHeight())
				yDir *= -1;
			GestureView.this.repaint();
		}
	}
}
