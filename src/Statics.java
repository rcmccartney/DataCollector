import java.io.File;


public class Statics {

	public static final String[] descriptions = {
		"Tap:\nLift only your index finger in an arch then extend\nit straight forward while flicking your wrist as if\nyou were tapping loudly on a window that is located\na few inches above the Leap Conroller.\nDo this motion only once.",
		"Two-finger tap:\nPerform the same gesture as the Tap,\nonly with two fingers outstretched.\nDo this motion only once.",
		"Swipe left-to-right:\nMove your entire outstretched palm along the X-axis left to right\nkeeping your palm parallel to the floor.",
		"Grab object:\nStart with all fingers extended,\nthen close your hand into\na fist as if squeezing a ball.\nDo this motion only once.",
		"Release object:\nRelease an imaginary ball that you are squeezing.\nYour hand should go from a fist into an\noutsretched palm parallel to the floor.\nDo this motion only once.",
		"Pinch:\nStart with index finger and thumb fully extended\nand other fingers folded. Bring your index finger\nand thumb together while slightly curling them in\nlike crab pinchers. Make sure the fingers are visible\nabove the Leap Motion Controller.",
		"Wipe:\nWith all fingers outstretched, wave\nhand from left to right several times\nas if wiping a window with a cloth.",
		"Checkmark:\nMake a checkmark sign with one\nfinger going from left to right.",
		"Figure 8:\nDraw the number 8 with one finger\neither clockwise or counter-clockwise.",
		"Little e:\nDraw a lower case 'e' with one finger.",
		"Upper-Case E:\nDraw an upper-case E with four\nswipes from a single finger.",
		"Upper-Case F:\nDraw an upper-case F with three\nswipes from a single finger."
	};
	
	public static final String[] movies = {
		"movies"+File.separator+"tap.MOV",
		"movies"+File.separator+"two_finger_tap.MOV",
		"movies"+File.separator+"swipe.MOV",
		"movies"+File.separator+"grab_motion_begin.MOV",
		"movies"+File.separator+"grab_motion_end.MOV",
		"movies"+File.separator+"pinch.MOV",
		"movies"+File.separator+"wipe_clean.MOV",
		"movies"+File.separator+"checkmark.MOV",
		"movies"+File.separator+"eight.MOV",
		"movies"+File.separator+"lower_case_e.MOV",
		"movies"+File.separator+"upper_case_e.MOV",
		"movies"+File.separator+"upper_case_f.MOV",
	};
	
	//Separator used in printing the file
	public static final String separator = " ";
	
	//Name of the gestures to be performed
	public static enum Gestures {Tap, Tap2, Swipe, Grab, Release, 
								Pinch, Wipe, CheckMark, Figure8, e,
								capE, F};
	
	//number of gesture repeats for each user
	public static final int COUNT = 10;
	
	//location to print the files to
	public static final String basePath = System.getProperty("user.home") + File.separator + "Desktop" +
									File.separator + "LeapData" + File.separator;

}
