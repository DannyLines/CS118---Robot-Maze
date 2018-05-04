import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;

public class Ex1
{
  private int pollRun = 0; //sets pollrun to zero when first opened
  private int explorerMode = 1;
  private RobotData robotData = new RobotData();
  public void controlRobot(IRobot robot) 
  {
    if ((robot.getRuns() == 0) && (pollRun == 0)) // if this is the first move on the first run then reset the data in the array.
    {
      robotData = new RobotData();
    }
    pollRun++; // increment poll run
    exploreControl(robot); // Calls exploreControl
    
  }
  
  private int nonwallExits (IRobot robot)
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
      do
      {  
	randno = (int) Math.floor(Math.random()*3); //selects a random passage exit.
	if(randno == 0)		
	  direction = IRobot.LEFT;
	else if(randno == 1)		
	  direction = IRobot.RIGHT;
	else if(randno == 2)		
	  direction = IRobot.AHEAD;
      }
      while (robot.look(direction) != IRobot.PASSAGE);      
      robot.face(direction);     //faces direction
  }
 
  private int CheckPassage(IRobot robot) //counts number of passage exits
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.PASSAGE)
	count++;
     }
     return count;
  }
  
  private int beenbeforeExits(IRobot robot) //counts number of beenbefore exits
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.BEENBEFORE)
	count++;
     }
     return count;
  }
  
  public void reset() //Calls the reset junctioncounter method, resets pollrun and sets explorer mode to 1.
  {
    robotData.ResetJunctionCounter();
    pollRun = 0;
    explorerMode = 1;
  } 	
  
  public void exploreControl(IRobot robot)
  {
    int direction;
    int exits = nonwallExits(robot);
    switch (exits)	//Just a simple switch statement that decides which method to visit depending on how many exits there are surrounding the robot.
    {
      case 1: DeadEnd(robot);
	      explorerMode = 0;
	      break;    
      case 2: Corridor(robot);
	      break;
      case 3:  					// If the number of beenbefore blocks is less than 2, then add the junction and print it. This check is to ensure a junction it had visited before isn't added again. 
      case 4: if (beenbeforeExits(robot) < 2)
	      {
		robotData.AddJunction(robot);
		robotData.PrintJunction(robot);
	      }
	      BacktrackControl(robot);
	      break;
    }
  }
  public void BacktrackControl(IRobot robot) //If theres a passage exit, calls junctionorcrossroad, otherwise it backtracks
  {
    if (CheckPassage(robot) != 0)
      JunctionOrCrossroad(robot);
    else 
    { 
      switch (robotData.SearchJunction(robot.getLocation().x, robot.getLocation().y)) //calls searchjunction passing current x and y to get the prior heading when previously encountered this junction.
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
  ArrayList<JunctionRecorder> junctions = new ArrayList<JunctionRecorder>();
  private int JunctionCounter = 0;
  
  public RobotData()
  {    
  }
  public void AddJunction(IRobot robot) // This method adds a junction to the array. It passes the current x,y and heading into the junctionrecorder class/method and stores it in the array.
  {
      JunctionRecorder temp = new JunctionRecorder(robot.getLocation().x,robot.getLocation().y, robot.getHeading()); //creates an object called temp of junctionrecorder with current x,yand heading
      junctions.add(temp);			//adds this object to the array list
      JunctionCounter++;			//increments junction counter
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
  
  public void PrintJunction(IRobot robot) // This is a set of nested if statements that will print out the number of the junction, x cooridinate y coordinate and heading.
  {   
      if(junctions.get(JunctionCounter -1).GetHeading() == IRobot.NORTH)
	System.out.println("Junction " + JunctionCounter + " (x = " + junctions.get(JunctionCounter -1).GetJunctionX() + ", y = " + junctions.get(JunctionCounter -1).GetJunctionY() + ") Heading north");
      else if(junctions.get(JunctionCounter -1).GetHeading() == IRobot.EAST)
	System.out.println("Junction " + JunctionCounter + " (x = " + junctions.get(JunctionCounter -1).GetJunctionX() + ", y = " + junctions.get(JunctionCounter -1).GetJunctionY() + ") Heading east");
      else if(junctions.get(JunctionCounter -1).GetHeading() == IRobot.SOUTH)
	System.out.println("Junction " + JunctionCounter + " (x = " + junctions.get(JunctionCounter -1).GetJunctionX() + ", y = " + junctions.get(JunctionCounter -1).GetJunctionY() + ") Heading south");
      else if(junctions.get(JunctionCounter -1).GetHeading() == IRobot.WEST)
	System.out.println("Junction " + JunctionCounter + " (x = " + junctions.get(JunctionCounter -1).GetJunctionX() + ", y = " + junctions.get(JunctionCounter -1).GetJunctionY() + ") Heading west");
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


