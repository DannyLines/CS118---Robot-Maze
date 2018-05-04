import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;

public class GrandFinale
{
  private int pollRun = 0; //sets pollrun to zero when first opened
  private int explorerMode = 1;
  private int RandCount = 0;
  private RobotData robotData = new RobotData();
  private Stack<Integer> DirectionList = new Stack<Integer>();
  private Stack<Integer> ImprovedRoute = new Stack<Integer>();
  public void controlRobot(IRobot robot) 
  {
    
    if ((pollRun == 0) && (robot.getRuns() == 0))
    {
      robotData = new RobotData();
      DeadEnd(robot);
      //DirectionList.push(robot.getHeading());
    }
    else if (robot.getRuns() != 0)
      FollowRoute(robot); //calls the followroute method if it isnt the first run through the maze
    else  
      exploreControl(robot); // Calls exploreControl
      
    pollRun++; // increment poll run
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
      robot.face(i);	
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
	randno = (int) Math.floor(Math.random()*3); // chooses a random passage
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
  
  private int CheckPassage(IRobot robot) // simple method that counts the number of passages adjacent to the robot
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.PASSAGE)
	count++;
     }
     return count;
  }
  
  private int beenbeforeExits(IRobot robot) // Counts number of beenbefore adjacent to the robot
  {
     int count = 0;
    
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++)
     {
	if (robot.look(i) == IRobot.BEENBEFORE)
	count++;
     }
     return count;
  }
  
  public void reset() // resets junction counter, calls transfer stack, resets pollrun and sets explorerMode to 1
  {
    robotData.ResetJunctionCounter();
    TransferStack();
    pollRun = 0;
    explorerMode = 1;
  } 
  
  private void TransferStack()	// transfers the junctions stored in the DirectionList to the ImprovedRoute stack
  {
    while(!DirectionList.empty())
    {
      ImprovedRoute.push(DirectionList.pop());
    }
  }
  
  private void FollowRoute(IRobot robot) //This directs the robot using the junction directions stored in the stack ImprovedRoute
  {
    int exits = nonwallExits(robot); 
    if (exits <= 2)
    {
      System.out.println("moving to next junction");
      if(exits == 1) 
      {
	System.out.println("deadend");
	DeadEnd(robot);
      }
      else if (exits == 2)
      {
	Corridor(robot);
	System.out.println("corridor");
      }
    }
    else if ((exits == 3 || exits == 4) && (!ImprovedRoute.empty()))
    {
      System.out.println("Choosing best path");
      System.out.println("The best direction is " + ImprovedRoute.peek() );
      DirectionList.push(ImprovedRoute.peek());
      robot.setHeading(ImprovedRoute.pop());
    }
  }
  public void exploreControl(IRobot robot) //This is called on the first run. Takes it through the maze
  {
    System.out.println("In explore control");
    int direction;
    int exits = nonwallExits(robot);
    {
      switch (exits)										//Just a simple switch statement that decides which method to visit depending on how many exits there are surrounding the robot.
      {
	case 1: DeadEnd(robot);
	System.out.println("case 1");
		explorerMode = 0;
		break;    
	case 2: Corridor(robot);
	System.out.println("case 2");
		break;
	case 3:
	case 4: System.out.println("case 3/4");
		if (robotData.NewJunction(robot.getLocation().x, robot.getLocation().y))	// If the number of beenbefore blocks is less than 2, then add the junction and print it. This check is to ensure a junction it had visited before isn't added again		
		{
		  System.out.println("New junction");
		  robotData.AddJunction(robot);
		  JunctionOrCrossroad(robot);
		  System.out.println("Storing " + robot.getHeading());
		  DirectionList.push(robot.getHeading());
		  explorerMode = 1;
		}
		else
		  BacktrackControl(robot);
		
      }
    }
  }
  public void BacktrackControl(IRobot robot)
  {
    System.out.println("In backtrackControl");
    int randno = 0;
    int direction = 0;
    System.out.println("Explorer mode is " + explorerMode);
    switch (explorerMode)
    {
      case 1: robot.face(IRobot.BEHIND);
	      System.out.println("Turning around");
	      explorerMode = 0;
	      break;	    
      case 0: 
	      System.out.println("Removed " + DirectionList.peek());
	      DirectionList.pop();
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
		DirectionList.push(robot.getHeading());
		System.out.println(robot.getHeading() + "Stored to stack");
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
  ArrayList<JunctionRecorder> junctions = new ArrayList<JunctionRecorder>(); // Creates an arraylist of type Junctionrecorder (objects)
  private int JunctionCounter = 0;	//sets junction counter to 0
  
  public RobotData() // This is a blank contructor
  {     
  }
  public void AddJunction(IRobot robot) // This method adds a junction to the array. It passes the current x,y and heading into the junctionrecorder class/method and stores it in the array.
  {
      //Junctions[JunctionCounter] = new JunctionRecorder(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
      JunctionRecorder temp = new JunctionRecorder(robot.getLocation().x,robot.getLocation().y, robot.getHeading()); // creates an object, apssing through current x y and heading
      junctions.add(temp); 	// adds this new object to the array
      JunctionCounter++;	//increments junctioncounter
  }
  public int GetJunctionCounter() // This method returns the value of junction counter, an integer value.
  {
      return JunctionCounter;
  }
  public void ResetJunctionCounter() // This resets the JunctionCounter to 0 and clears the array.
  {
      JunctionCounter = 0;	//resets junction counter
      junctions.clear();	//clears the array list
  }
  
  public boolean NewJunction(int x, int y) //This method determines whether a junction is beenbefore by iteratting through the arraylist looking for match coords with those passed to it
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


