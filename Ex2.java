import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;
import java.util.Stack;

public class Ex2
{
  private int pollRun = 0;
  private int explorerMode = 1;
  private RobotData2 robotData = new RobotData2();
  public void controlRobot(IRobot robot) 
  {
    if ((robot.getRuns() == 0) && (pollRun == 0))
    {
      robotData = new RobotData2();
    }
    pollRun++;
    exploreControl(robot);  
  }
  
  private int nonwallExits (IRobot robot)
  {
    int NonWalls = 0;
    for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
    {    
      if (robot.look(i) != IRobot.WALL)
	NonWalls++;
    }
    return NonWalls;
  }
  
  private void Corridor(IRobot robot)
  {
    if( robot.look(IRobot.AHEAD) != IRobot.WALL )
      robot.face(IRobot.AHEAD);
    else if (robot.look(IRobot.LEFT) != IRobot.WALL)
      robot.face(IRobot.LEFT);
    else
      robot.face(IRobot.RIGHT); 
  }
  
  private void DeadEnd(IRobot robot)
  {
    int i = 0;
    for (i = IRobot.AHEAD; i <= IRobot.LEFT; i++) // for loop checks every relative direction, while if statements checks each relative direction to see if its a wall
   {  
    if (robot.look(i) != IRobot.WALL)
      robot.face(i);
   }
  }
  
  private void JunctionOrCrossroad(IRobot robot) // merged these methods together as they're the exact same 
  {
    int direction = 0;
    int randno = 0;
      do
      {  
	randno = (int) Math.floor(Math.random()*3);
	if(randno == 0)		
	  direction = IRobot.LEFT;
	else if(randno == 1)		
	  direction = IRobot.RIGHT;
	else if(randno == 2)		
	  direction = IRobot.AHEAD;
      }
      while (robot.look(direction) != IRobot.PASSAGE); //while the direction its facing isnt equal to a passage, keep reselcting a direction.
      robot.face(direction);   //face direction
    
  }
 
  private int CheckPassage(IRobot robot) //checks number of passage exits adjacent to robot
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.PASSAGE)
	count++;
     }
     return count;
  }
  
  private int beenbeforeExits(IRobot robot) //checks number of beenbefore exits adjacent to the robot
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.BEENBEFORE)
	count++;
     }
     return count;
  }
  
  public void reset() 
  {
    robotData.ResetJunctions();
    pollRun = 0;
    explorerMode = 1;
  } 	
  
  public void exploreControl(IRobot robot)
  {
    int direction;
    int exits = nonwallExits(robot);
    switch (exits) //simple switch, if 1 exit, then at deadend so call deadend method. if 2, call corridor. If 3 and beenbefore is <2 i.e.its a new junction the add it to the stack same fo
    {
      case 1: DeadEnd(robot);
	      explorerMode = 0;
	      break;    
      case 2: Corridor(robot);
	      break;
      case 3: 
      case 4: if (beenbeforeExits(robot) < 2)
	      {
		robotData.AddJunction(robot);
		robotData.PrintJunction(robot);
	      }
	      BacktrackControl(robot);
	      break;
    }
  }
  public void BacktrackControl(IRobot robot) //if the junction has passages it calls junctionorcrossroad method
  {
    if (CheckPassage(robot) != 0)
    {
      JunctionOrCrossroad(robot);
    }
    else //if the junction doesnt have a passage, it backtracks
    { 
      switch (robotData.SearchJunction())
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
      System.out.println("Popped junction");
      System.out.println(robotData.SearchJunction()); //peeks at top element on stack
      robotData.popJunction(); //pops top element on stack
    }
  }
}
class JunctionRecorder //
{
  private int Heading;
      
  public JunctionRecorder(int PriorHeading)
  {
      Heading = PriorHeading; //sets heading to value passed into it
  }
  public int GetHeading()
  {
      return Heading;
  }
}

class RobotData2
{
  Stack <JunctionRecorder> junctions = new Stack <JunctionRecorder>(); //creates stack of type junction recorder (object)
  private int JunctionCounter = 0;
  public RobotData2()
  {
  }
  public void AddJunction(IRobot robot)
  {
      JunctionRecorder temp = new JunctionRecorder(robot.getHeading());
      junctions.push(temp);
      System.out.println(temp.GetHeading());
      JunctionCounter++;
  }
  public int GetJunctionCounter() //returns junction counter
  {
      return JunctionCounter;
  }
  public void ResetJunctions() //resets the stack.
  {
      JunctionCounter = 0;
      while (!junctions.empty())
      {
	junctions.pop();
      }
  }
  public void popJunction() //pops the top element on stack so long as it isnt empty
  {
    if(!junctions.empty())
      junctions.pop();
  }
  public void PrintJunction(IRobot robot) //simple debugging prints heading etc.
  {   
      if(junctions.peek().GetHeading() == IRobot.NORTH)
	System.out.println("Junction " + JunctionCounter + " Heading north");
      else if(junctions.peek().GetHeading() == IRobot.EAST)
	System.out.println("Junction " + JunctionCounter + " Heading east");
      else if(junctions.peek().GetHeading() == IRobot.SOUTH)
	System.out.println("Junction " + JunctionCounter + " Heading south");
      else if(junctions.peek().GetHeading() == IRobot.WEST)
	System.out.println("Junction " + JunctionCounter + " Heading west");
  }
  
  public int SearchJunction() //looks at top heading on stack
  {
    return junctions.peek().GetHeading();
  }
} 


