import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;

import com.leapmotion.leap.Arm;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

public class GestureListener extends Listener {
	
	//this is the total number of features for 1 hand
	public static final int TOTAL_HAND_FEATURES = 110;
	public static final int TOTAL_FINGER_FEATURES = 15;
	
	//the gesture being performed and the delimiter for the dataset
	private String gestureName, s;
	private boolean paused = true;
	private File newDir, data;
	private PrintWriter out;
	//numbers each gesture performed
	private int counter = 1;
	//this is put into the row of data when a hand is missing
	private String emptyHand = "";
	private String environment = "";
	private GestureView panel;
	
	public GestureListener(String separator, GestureView aPanel) {
		s = separator;
		panel = aPanel;
		//Create this long string once and use it if it's ever necessary
		for(int i = 0; i < TOTAL_HAND_FEATURES; i++)
			emptyHand += "NP"+s;
	}
	
	public void setGesture(String gesture) {
		gestureName = gesture;
		counter = 1;
	}
	
	public void setFilePath(File baseDir) {
		try {   	
    		//This finds the current number of folders in this directory and makes the new directory
    		// name to be the next largest integer.  This is what will store the data
    		ArrayList<String> names = new ArrayList<String>(Arrays.asList(baseDir.list()));
    		Integer nextDirNum = 0;
    		for (String dirName : names) {
    			int currDir = Integer.parseInt(dirName);
    			if (currDir > nextDirNum) 
    				nextDirNum = currDir;
    		}
    		++nextDirNum;
    		newDir = new File( baseDir.getPath() + File.separator + nextDirNum);
    		newDir.mkdir();
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
	}
    
	public synchronized void startRecording() {
		if (paused) {
			// We were paused when user toggled recording, so we will make a new file and
			// start recording the next gesture
			data = new File( newDir.getPath() + File.separator + counter + ".txt");
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(data, true)));
			} catch (IOException e) {
				System.out.println("Could not create folder for data");
				System.exit(1);
			}
			//This is a new file, so label it at the top
			printDataLabels(out);
			paused = false;
			panel.showRecording();
		}
	}
	
	public synchronized void stopRecording() {
		if (!paused) {
			// We were already recording, now we pause and decide whether to keep the data or not
			paused = true;
			out.close();
			panel.pause();
		}
	}
	
	public int keepGesture() {
		panel.updateGestureDisplay(gestureName, counter);
		panel.showStopped();
		return ++counter;
	}
	
	public int discardGesture() {
		System.out.println("Deletion " + (data.delete()?"succesful.":"failed.") );
		panel.showStopped();
		return counter;
	}
	
    public void onConnect(Controller controller) {
    	
    	environment = "Date: ";
    	environment += new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    	environment += " " + controller.devices().get(0).toString();
    	Properties p = System.getProperties();
    	Enumeration<Object> keys = p.keys();
    	while (keys.hasMoreElements()) {
    		String key = (String)keys.nextElement();
    		String value = (String)p.get(key);
    		if (value.contains("LeapDeveloperKit") ) {
    			environment += " Leap SDK: " + value;
    			break;
    		}  
    	}
    }
	
    public synchronized void onFrame(Controller controller) {
        //Update the display for hand count even if you're paused
    	Frame frame = controller.frame();
    	panel.setHandCount( frame.hands().count() );
    	
    	if(paused) 
    		return;
    	
    	// Get the most recent frame and report some basic information
        String dataDump = gestureName +s+
        				  frame.id() +s+ 
        				  frame.timestamp() +s+ 
        				  frame.currentFramesPerSecond() +s+
        				  frame.hands().count() +s+ 
        				  frame.fingers().count() +s;
        //Uncomment this to print some data to the console for debugging purposes
        //System.out.println(dataDump);
        //only printing one hand!  
        
        out.print( printHand(frame.hands().rightmost(), dataDump) + "\n");
	}

	public String printHand(Hand hand, String dataDump) {
		
		if ( !hand.isValid() ) {
			return dataDump + emptyHand;
		}
	    // Hand data
		Vector direction = hand.direction();
	    Vector pos = hand.palmPosition();
	    Vector vel = hand.palmVelocity();
	    Vector normal = hand.palmNormal();
	    Vector wristPos = hand.wristPosition();
    	dataDump += hand.id() +s+
    				(hand.isLeft()?"Left":"Right") +s+
    				hand.confidence() +s+
    				hand.timeVisible() +s+
    				hand.palmWidth() +s+
    				hand.pinchStrength() +s+
    				hand.grabStrength() +s+
    				pos.getX() +s+
    				pos.getY() +s+
    				pos.getZ() +s+
    				direction.getX() +s+
    				direction.getY() +s+        				
    				direction.getZ() +s+
    				vel.getX() +s+
    				vel.getY() +s+
    				vel.getZ() +s+
    				normal.getX() +s+
    				normal.getY() +s+
    				normal.getZ() +s+
    				Math.toDegrees(direction.pitch()) +s+
    				Math.toDegrees(normal.roll()) +s+
    				Math.toDegrees(direction.yaw()) +s+
    				wristPos.getX() +s+
    				wristPos.getY() +s+        				
    				wristPos.getZ() +s;
        // Get arm bone data
        Arm arm = hand.arm();
        Vector armDir = arm.direction();
        Vector elbow = arm.elbowPosition();
        Vector center = arm.center();
        dataDump += arm.width() +s+
        			armDir.getX() +s+
        			armDir.getY() +s+
        			armDir.getZ() +s+
        			elbow.getX() +s+
        			elbow.getY() +s+
        			elbow.getZ() +s+
        			center.getX() +s+
        			center.getY() +s+
        			center.getZ() +s;
        // Get finger data for each finger
        int fingerCount = 0;
        for (Finger finger : hand.fingers()) {
        	++fingerCount;
        	Vector fDir = finger.direction();
        	Vector fPos = finger.tipPosition();
        	Vector fVel = finger.tipVelocity();
        	dataDump += finger.id() +s+
        				finger.type() +s+
        				finger.timeVisible() +s+
        				finger.isExtended() +s+
        				finger.length() +s+
                        finger.width() +s+ 
                        fPos.getX() +s+
                        fPos.getY() +s+
                        fPos.getZ() +s+
                        fDir.getX() +s+
                        fDir.getY() +s+
                        fDir.getZ() +s+
                        fVel.getX() +s+
                        fVel.getY() +s+
                        fVel.getZ() +s;
        }
        //Not all five fingers were present, fill in missing info
        if (fingerCount < 5) 
        	for(; fingerCount < 5; fingerCount++) 
        		for(int i = 0; i < TOTAL_FINGER_FEATURES; i++)
        			dataDump += "NP"+s;
        
        return dataDump;
    }
	
	private void printDataLabels(PrintWriter out) {
		//first put metadata in the top line
		out.println(environment);
		
		String handString = "h_ID" +s+
							 "h_type" +s+ 
							 "h_conf" +s+
							 "h_tVis" +s+
							 "h_palmWidth" +s+
							 "h_pchStr" +s+
							 "h_grpStr" +s+
							 "h_PosX" +s+
							 "h_PosY" +s+
							 "h_PosZ" +s+
							 "h_DirX" +s+
							 "h_DirY" +s+
							 "h_DirZ" +s+
							 "h_VelX" +s+
							 "h_VelY" +s+
							 "h_VelZ" +s+
							 "h_NrmX" +s+
							 "h_NrmY" +s+
							 "h_NrmZ" +s+						
							 "h_Pitch" +s+
							 "h_Roll" +s+
							 "h_Yaw" +s+
							 "wr_PosX" +s+
							 "wr_PosY" +s+
							 "wr_PosZ" +s;
		String armString = "a_Width" +s+
							"a_DirX" +s+
							"a_DirY" +s+
							"a_DirZ" +s+
							"elbowX" +s+
							"elbowY" +s+
							"elbowZ" +s+
							"a_CenX" +s+
							"a_CenY" +s+
							"a_CenZ" +s;		
		
		out.println( 	 "gesture" +s+
					 	 "frameId" +s+ 
				  		 "timestamp" +s+ 
				  		 "framesPerSec" +s+
				  		 "handCount" +s+ 
				  		 "fingerCount" +s+
				  		 handString +
				  		 armString +
				  		 fingerLabel(1) +
				  		 fingerLabel(2) +
				  		 fingerLabel(3) +
				  		 fingerLabel(4) +
				  		 fingerLabel(5) );
	}
	
	private String fingerLabel(int index) {
		String fl = "f"+index+"_";
		String fingerString = fl+"ID" +s+
							  fl+"Type" +s+
							  fl+"Vis" +s+
							  fl+"Ext" +s+
							  fl+"Len" +s+
							  fl+"Wid" +s+ 
							  fl+"PosX" +s+
							  fl+"PosY" +s+
							  fl+"PosZ" +s+
							  fl+"DirX" +s+
							  fl+"DirY" +s+
							  fl+"DirZ" +s+
							  fl+"VelX" +s+
							  fl+"VelY" +s+
							  fl+"VelZ" +s;
		return fingerString;
	}
}