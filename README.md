# DataCollector
Collects gesture data from a Leap Motion Controller

## Data collection
This GUI was used to collect data from RIT students as research into 
gesture recognition with the Leap Motion controller.  Info on Leap Motion 
can be found here: http://leapmotion.com/

The data that was collected (approx 1.5 GB, with over 9 thousand individual 
gesture instances) is located here: http://spiegel.cs.rit.edu/~hpb/LeapMotion/

The goal of collecting this data was to create a gesture recognition engine
that can segment, classify, and parse gestures in an online fashion as they
are seen.  Our current research on this topic can be seen in other repos. 

## Running 
To run the jar on Windows, just double click the jar file or use 
$ java -jar DataCollector.jar

The Windows libraries and Jar files required to interface with the Leap
Motion controller have been packaged into this repo.  Often, users
find it helpful to run the Leap Motion Visualizer while they are 
performing the gestures in this GUI, as a way to get visual feedback
on the gesture being performed.
