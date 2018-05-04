import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;

public class Ex3
{
  private int pollRun = 0; //sets pollrun to zero when first opened
  private int explorerMode = 1;
  private int RandCount = 0;
  private RobotData robotData = new RobotData();
  public void controlRobot(IRobot robot) 
  {
    if (pollRun == 0) // if this is the first move on the first run then reset the data in the array.
    {	
      if (robot.getRuns() == 0)
	robotData = new RobotData();
    }   
    exploreControl(robot); // Calls exploreControl
    pollRun++; // increment poll run
  }
  private int nonwallExits (IRobot robot) //counts number of nonwall exits
  {
    int NonWalls = 0;
    for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++) // A simple for loop that itterates through every relative direction and counts the number of exits surrounding it
    {    
      if (robot.look(i) != IRobot.WALL)
	NonWalls++;
    }
    return NonWalls;
  }
  
  private void Corridor(IRobot robot) // This is the method to be called when the robot is in a corridor. Makes it face either 
  {
    if( robot.look(IRobot.AHEAD) != IRobot.WALL )
      robot.face(IRobot.AHEAD);
    else if (robot.look(IRobot.LEFT) != IRobot.WALL)
      robot.face(IRobot.LEFT);
    else
      robot.face(IRobot.RIGHT); 
  }
  
  private void DeadEnd(IRobot robot) // This is the method to be called when the robot is at a dead-end
  {
    int i = 0;
    for (i = IRobot.AHEAD; i <= IRobot.LEFT; i++) // for loop itterates through every relative direction, while the if statements checks each relative direction to see if its a wall
   {	  
    if (robot.look(i) != IRobot.WALL)
    {
      robot.face(i);
    }	
   }
  }
  
  private void JunctionOrCrossroad(IRobot robot) // merged these methods together as they're the exact same 
  {
    int direction = 0;
    int randno = 0;
   if (CheckPassage(robot) != 0)
   {
      do
      {  
	randno = (int) Math.floor(Math.random()*3); //selects a random passage exit
	if(randno == 0)		
	  direction = IRobot.LEFT;
	else if(randno == 1)		
	  direction = IRobot.RIGHT;
	else if(randno == 2)		
	  direction = IRobot.AHEAD;
      }
      while (robot.look(direction) != IRobot.PASSAGE); 
      robot.face(direction);   
    }
    else
      BacktrackControl(robot);
  }
  private int CheckPassage(IRobot robot) // counts number of passage exits
  {
     int count = 0;
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.PASSAGE)
	count++;
     }
     return count;
  }
  
  private int beenbeforeExits(IRobot robot) //Counts number of beenbefore exits
  {
     int count = 0;
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.BEENBEFORE)
	count++;
     }
     return count;
  }
  
  public void reset() //resets data in arraylist, sets pollrun to 0 and sets explorermode to 1
  {
    robotData.ResetJunctionCounter();
    pollRun = 0;
    explorerMode = 1;
  } 	
  
  public void exploreControl(IRobot robot)
  {
    int direction;
    int exits = nonwallExits(robot);								//calls nonwallexits and makes int exits equal to it.
    {
      switch (exits)										//Just a simple switch statement that decides which method to visit depending on how many exits there are surrounding the robot.
      {
	case 1: DeadEnd(robot);									//calls deadend method
		explorerMode = 0;								//sets explorerMode equal to 0.
		break;    
	case 2: Corridor(robot);								//calls corridor method
		break;
	case 3:
	case 4: if (robotData.NewJunction(robot.getLocation().x, robot.getLocation().y))
		{                                                                               //if the junction is new, add it to arraylist, select a random passage and set explorer mode to 1. Otherwise, go to backtrack
		  System.out.println("New junction");
		  robotData.AddJunction(robot);
		  JunctionOrCrossroad(robot);
		  explorerMode = 1;
		}
		else
		  BacktrackControl(robot);	
      }
    }
  }
  public void BacktrackControl(IRobot robot)
  {
    int randno = 0;
    int direction = 0;
    System.out.println(explorerMode);
    switch (explorerMode)
    {
      case 1: robot.face(IRobot.BEHIND); //In the case that it has arrived at an old junction from a new one, turn around
	      System.out.println("Turning around");
	      explorerMode = 0;
	      break;	    
      case 0:				//In the case that it has arrived at an old junction from another old junction, if there are no passages, backtrack. Otherwise select a random passage and set explorer modde to 1
	      if (CheckPassage(robot) == 0)
	      {
		switch (robotData.SearchJunction(robot.getLocation().x, robot.getLocation().y))
		{
		  case IRobot.NORTH: robot.setHeading(IRobot.SOUTH);
			  break;
		  case IRobot.EAST:  robot.setHeading(IRobot.WEST);
			  break;
		  case IRobot.SOUTH: robot.setHeading(IRobot.NORTH);
			  break;
		  case IRobot.WEST:  robot.setHeading(IRobot.EAST);
			  break;
		}
		System.out.println("Backtracking");
		explorerMode = 0;
	      }
	      else
	      {
		JunctionOrCrossroad(robot);
		explorerMode = 1;
		System.out.println("Choosing passage");
	      }
	      break;
    }   
  }
 }
 
class JunctionRecorder
{
  private int X;
  private int Y;
  private int Heading;
      
  public JunctionRecorder(int JunctionX, int JunctionY, int PriorHeading) // Makes X, Y and Heading equal to the values passed into the method.
  {
      X = JunctionX;	
      Y = JunctionY;
      Heading = PriorHeading;
  }
  public int GetJunctionX() // Returns X
  {
      return X;
  }
  public int GetJunctionY() // Returns y
  {
      return Y;
  }
  public int GetHeading() // returns heading
  {
      return Heading;
  }
}

class RobotData
{
  ArrayList<JunctionRecorder> junctions = new ArrayList<JunctionRecorder>(); //creates arraylist of type junctionrecorder (objects).
  private int JunctionCounter = 0;
  public RobotData() 
  {
  }
  public void AddJunction(IRobot robot) // This method adds a junction to the array. It passes the current x,y and heading into the junctionrecorder class/method and stores it in the array.
  {
      JunctionRecorder temp = new JunctionRecorder(robot.getLocation().x,robot.getLocation().y, robot.getHeading()); //creates an object of the junction recorder class with the current x y and heading
      junctions.add(temp); // adds this temp to the arraylist
      JunctionCounter++; //
  }
  public int GetJunctionCounter() // This method returns the value of junction counter, an integer value.
  {
      return JunctionCounter;
  }
  public void ResetJunctionCounter() // This resets the JunctionCounter to 0 and clears the array.
  {
      JunctionCounter = 0;
      junctions.clear();
  }
  
  public boolean NewJunction(int x, int y) //checks to see if a junction is new by comparing passed in x and y coords with coords already in the arraylist. Returns true if new.
  {
    boolean newJunction = true; 
    for (int i = 0; i < junctions.size(); i++) 
    {
      if (junctions.get(i).GetJunctionX() == x && junctions.get(i).GetJunctionY() == y) 
      {
	newJunction = false;
      }	    
    }
    return newJunction;
  }
  
  public int SearchJunction(int x, int y) // This utilises a for loop which looks through all the elements of the array to find a match with the x and y coordinates passed to it.
  {
      int PriorHeading = 0; 
      for (int i = 0; i < junctions.size(); i++) 
      {
	if (junctions.get(i).GetJunctionX() == x && junctions.get(i).GetJunctionY() == y) 
	{
	  PriorHeading = junctions.get(i).GetHeading();
	}	    
      }
      return PriorHeading; // Returns the prior heading when it first reached the junction; if it hasnt visited this junction before then it returns 0.
  }
}


