import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.leapmotion.leap.Controller;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;


public class GestureController extends KeyAdapter {

	protected GestureListener listener;
	// LeapMotion controller
	protected Controller controller;
	protected GestureView view;
	
	//Lock used to halt the thread that changes gestures
	protected static Object lock = new Object();
	protected String lastPlayed;
	//decisionMade is used to know when the user has decided to keep or discard a gesture
	//firsTime is to know whether the user is reading the first-time instructions that 
	//show up when you first start the game.
	//finished is to know we are done & can quit
	protected boolean decisionMade, firstTime, continueOn, instrShowing,
					finished, videoPlaying, leapDisplayed;

	
	public GestureController() {
		view = new GestureView(this);
		//start the leap motion listener
		controller = new Controller();
    	listener = new GestureListener(Statics.separator, view);
    	controller.addListener(listener);
    	decisionMade = true; 
    	firstTime = true;
		finished = false;
		videoPlaying = false;
		continueOn = false;
		leapDisplayed = false;
	}
	
	public void keyReleased(KeyEvent e) {
		
		if (videoPlaying)
			return;

		if ( e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
			// swing deadlocks if you call invokeLater from an action listener
			if (lastPlayed != "") {
				new Thread(new Runnable() {
					public void run() {
						view.play(lastPlayed);	
					}
				}).start();
			}
		}
		else if (instrShowing && (e.getKeyChar() == 'c' || e.getKeyChar() == 'C')) {
			if (finished) {
		    	controller.removeListener(listener);
		    	System.exit(1);
			}
			//Not first time then remove instructions and go back to recording gestures
			if (!firstTime) {
				instrShowing = false;
				view.instructionPanel.setVisible(false);
				view.setBackground(Color.WHITE);
				view.textPanel.setBackground(Color.LIGHT_GRAY);
			}
			//firstTime instructions set to false, so now a gesture instruction will be shown 
			else {
				if (leapDisplayed) {
					firstTime = false;
					leapDisplayed = false;
					synchronized (lock) { lock.notify(); }
					view.instructionPanel.setBackground(Color.LIGHT_GRAY);
					view.currentImage = view.imageStop;
					view.currDimensions = view.baseDimensions;
					view.recordInstructions.setText("Hold 's' to record a gesture");
				}
				else {
					leapDisplayed = true;
					view.showLeapHeight();
				}
			}
		}
		//!decisionMade tells us we are waiting for a decision to keep or remove a gesture 
		else if(!decisionMade && (e.getKeyChar() == 'a' || e.getKeyChar() == 'A')) {
			decisionMade = true; 
			int currCount = listener.keepGesture();
			// currCount is the number of times this gesture has been performed, if 
			//we are at the total needed then release the waiting thread
			if (currCount > Statics.COUNT) {
				//Release the thread to start the next gesture
				continueOn = true;
				synchronized (lock) { lock.notify(); }
			}
		}
		else if(!decisionMade && (e.getKeyChar() == 'r' || e.getKeyChar() == 'R')) {
			//No need to check the count, as this gesture wasn't kept so we
			//haven't increased it
			decisionMade = true; 
			listener.discardGesture();
		}
		//we are in the recording phase, user can stop or start a gesture
		else if (!instrShowing && decisionMade) {
			if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
				listener.stopRecording();
			}
		}
		//any time the user can hit Q to quit
		else {
			if (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q') {
		    	//Here, the user quit early
		    	controller.removeListener(listener);
		    	//visualizer.destroy();
		    	System.exit(1);
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (videoPlaying)
			return;
		//we are in the recording phase, user can stop or start a gesture
		if (!instrShowing && decisionMade) {
			if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
				listener.startRecording();
			}
		}
	}
	
	public void displayGesture(Statics.Gestures gesture, File baseDir) throws InterruptedException {
		continueOn = false;
    	listener.setFilePath(baseDir);
    	listener.setGesture(gesture.toString());  
    	String message = "Perform the " + gesture + " gesture.\n\n" + Statics.descriptions[gesture.ordinal()] + "\n";
    	lastPlayed = Statics.movies[gesture.ordinal()];
    	view.play(lastPlayed);
    	view.updateInstructionDisplay(message);
    	view.updateGestureDisplay(gesture.toString(), 0);
    	//wait until this gesture has been performed required number of times
    	while (!continueOn) { synchronized(lock) { lock.wait(); } }
    }
	
	// Implementing Fisher–Yates shuffle
	private static void shuffleArray(int[] ar) {
	    Random rnd = new Random();
	    int index, temp;
	    for (int i = ar.length - 1; i > 0; i--) {
	      index = rnd.nextInt(i + 1);
	      // Simple swap
	      temp = ar[index];
	      ar[index] = ar[i];
	      ar[i] = temp;
	    }
	}  
	
	/**
	 * Main thread of execution, it starts each new gesture by calling recordGesture then waits
	 * 
	 * @param args ignored
	 * @throws InterruptedException 
	 */
    public static void main(String[] args) throws InterruptedException {
		
    	// before we used Leap Motion visualizer in this way
		// String[] cmd = { "C:\\Program Files (x86)\\Leap Motion\\Core Services\\VisualizerApp.exe" };
		//	visualizer = Runtime.getRuntime().exec(cmd, null, new File("C:\\Program Files (x86)\\Leap Motion\\Core Services"));
		//	visualizer.waitFor();
    	
    	// see if we are using first or second half of the gestures (cuts down on time 
    	// required for each user)
		// We were paused when user toggled recording, so we will make a new file and
		// start recording the next gesture
    	File dir = new File(Statics.basePath);
		File type = new File(dir.getPath() + File.separator + ".temp");
		int half = 0;
		try {
			if (type.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(type));
				// increment by one whatever the user did last time
				half = (Integer.parseInt(br.readLine()) + 1) % 2;
				br.close();
			}
		} catch (NumberFormatException | IOException e) {
			System.out.println(e);
			//choose even or odd randomly
			Random rand = new Random();
			half = rand.nextInt(2);
		}
		
    	// write the half of gestures we did so we know next time to do other half
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir.getPath() + File.separator + ".temp")));
			out.print(half);
			out.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	
    	//make sure we can find VLC dll's
    	NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");
    	NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "MacOS"+File.separator+"lib");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

    	GestureController gc = new GestureController();
    	while(gc.firstTime) { synchronized(lock) { lock.wait(); } }  

    	// the main thread of execution that makes the new directory, displays each gesture and waits
    	// half is either zero or one.  if it is zero, then the user will do gestures 0-5 (in random
    	// order).  Otherwise, they will do gestures 6-11 in random order
    	// only perform half the gestures to speed up the process
    	Statics.Gestures[] gestures = Statics.Gestures.values();
    	/*int[] indices = new int[ gestures.length/2 ];  //if # gestures is odd this will be a bug 
    	if (half == 0) {
    		for(int i = 0; i < indices.length; i++)
    			indices[i] = i;
    	}
    	else {
    		for(int i = 0; i < indices.length; i++)
    			indices[i] = i + indices.length;
    	}*/
    	//changed it back to all gestures for our internal collection
    	int[] indices = new int[ gestures.length ];
		for(int i = 0; i < indices.length; i++)
			indices[i] = i;
    	//now shuffle the order you will do the gestures
    	shuffleArray(indices);	
    	for( int i = 0; i < indices.length; i++) { 
    		Statics.Gestures gesture = gestures[ indices[i] ]; 
    		File baseDir = new File(Statics.basePath + gesture.toString());
    		if (!(baseDir.exists() && baseDir.isDirectory()))
    			baseDir.mkdirs();
    		gc.displayGesture(gesture, baseDir);
    	}
    	
    	gc.view.updateInstructionDisplay("Thanks for participating in our study!\n\n\t-hpb\n");
    	gc.finished = true;
    	gc.view.instructionPanel.setBackground(Color.WHITE);
    }
}
