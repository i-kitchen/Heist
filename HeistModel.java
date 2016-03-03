/**
 * HeistModel.java
 *
 * File:
 *	$Id: HeistModel.java,v 1.1 2014/12/05 03:58:30 igk2718 Exp $
 *
 * Revisions:
 *	$Log: HeistModel.java,v $
 *	Revision 1.1  2014/12/05 03:58:30  igk2718
 *	Created classes and began implementing some methods
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

/**
 * Class definition for the model of a Heist game.
 *
 * @author atd: Aaron T Deever
 */

public class HeistModel extends Observable{

	/**
	 * The dimension of the square museum room, measured in units of
	 * the number of cells, each cell having an associated alarm 
	 * connected to it.
	 */
	private int dim;

	/**
	 * The total number of alarms in the museum room.
	 */
	private int numAlarms;

	/**
	 * The refresh rate of the alarm pattern, measured in milliseconds.
	 * Each alarm pattern index is incremented with this frequency.
	 */
	private int refreshRate;
	
	/**
	 * The list of alarms in raster scan (1-dimensional) order.
	 */
	private List<Alarm> alarms;

	/**
	 * The number of moves made in the game.
	 */
	private int moveCount;

	/**
	 * The location of the thief.  The thief begins at the bottom 
	 * left corner of the room.  He must return to that 
	 * location with the jewels to win the game.
	 */
	private int thiefLocation;

	/**
	 * The location of the jewels.  The jewels are stored at the 
	 * top right corner of the room.  This value doesn't change.  
	 * The variable jewelsStolen is used to identify if the 
	 * jewels have been stolen and possibly moved.
	 */
	private final int jewelsLocation;
	
	/**
	 * An indication whether the thief has stolen the jewels.  
	 * Once the thief makes it to the location of the jewels, 
	 * he has stolen the jewels.
	 */
	private boolean jewelsStolen;
	
	/**
	 * Game status.  A value of 0 indicates that the thief has 
	 * lost the game, by virtue of setting off an alarm.
	 * A value of 1 indicates that the game is still active.  
	 * A value of 2 indicates that the thief has won the game,
	 * by virtue of stealing the jewels and returning to the bottom 
	 * left corner (the exit) of the museum room.
	 */
	private int gameStatus;
	
	/**
	 * An indication of whether the thief has used his 
	 * electro-magnetic pulse (EMP).
	 * He is only able to use it once during the game to disable 
	 * the alarm at his current location.
	 */
	private boolean empUsed;
	
	/**
	 * Construct a HeistModel object.
	 * @param filename name of the input file containing game 
	 * specifications
	 * @throws FileNotFoundException 
	 * 
	 */
	public HeistModel(String filename) throws FileNotFoundException 
	{
		File configFile = new File(filename);
		Scanner in = new Scanner(configFile);
		
		this.dim = in.nextInt(); // read dim of room
		this.numAlarms = this.dim * this.dim;
		this.refreshRate = in.nextInt();  // measured in milliseconds
		this.thiefLocation = this.dim * (this.dim - 1);
		this.jewelsLocation = this.dim-1;
		this.jewelsStolen = false;
		this.moveCount = 0;
		this.gameStatus = 1;
		this.empUsed = false;
		
		alarms = new ArrayList<Alarm>();
		
		for (int n = 0; n < numAlarms; n++) 
		{
			List<Boolean> pattern = new ArrayList<Boolean>();
			int num = in.nextInt(); // length of pattern for this alarm
			for (int k = 0; k < num; k++) 
				pattern.add(in.nextBoolean());
			
			alarms.add(new Alarm(pattern));
		}
		
		in.close();
		
	}//END Constructor

	/**
	 * Select a cell to move to.  Only allow move if it
	 * is a 4-neighbor of current location.  Otherwise 
	 * ignore the requested move.  Only process the
	 * move if the game is still active.
	 * 
	 * @param n An integer referring to the nth cell
	 * (corresponding to a 1-dimensional representation 
	 * of the 2-dimensional grid).
	 *
	 */
	public void selectCell(int n) 
	{
		int currentLoc = thiefLocation;
		int diff = n - currentLoc;
		
		if(gameStatus != 1) 
			return;
		
		boolean validMove = false;
		// make sure it's a 4-nbr and doesn't wrap 
		// around the vertical edge
		int currCol = currentLoc % dim;
		if(((diff == 1) && (currCol < dim - 1)) ||
				((diff == -1) && (currCol > 0)) || 
				(diff == dim) || (diff == -dim)) { 
			validMove = true;
		}
		
		if(validMove == false)
			return;
		
		moveCount++;
		
		thiefLocation = n;
		
		if(thiefLocation == jewelsLocation) {
			jewelsStolen = true;
		}
		
		if(alarms.get(thiefLocation).isActive()) {
			gameStatus = 0;
		}
		
		if(thiefLocation == dim*(dim-1) && jewelsStolen == true)
			gameStatus = 2;
		
		setChanged();
		notifyObservers();
		
	}//END selectCell

	/**
	 * Get the alarms as a List of boolean values indicating whether
	 * each alarm is active.
	 *
	 * @return An ArrayList containing booleans representing the alarms
	 */
	public List<Boolean> getAlarms() 
	{
		List<Boolean> currAlarms = new ArrayList<Boolean>();

		for (Alarm a : alarms) 
			currAlarms.add(a.isActive());
				
		return currAlarms;
		
	}//END getAlarms


	/**
	 * Update the current pattern index for all of the alarms.  
	 * Wrap back around to the first index (0)
	 * once the pattern has been completed.  Check to see if
	 * this update catches a stationary thief.  Once the game
	 * has been won or lost, the alarm patterns are no longer updated.
	 */
	public void updateAlarmPattern() 
	{ 	
		if(gameStatus != 1) 
			return;
		
		for (int n = 0; n < numAlarms; n++) 
			alarms.get(n).incrementPatternIndex();
				
		// now check if this catches a stationary thief
		if(alarms.get(thiefLocation).isActive()) 
			gameStatus = 0;
				
		setChanged();
		notifyObservers();
		
	}//END updateAlarmPattern
	
	/**
	 * Get the number of moves the thief has made.
	 *
	 * @return An integer that represents the number of moves.
	 */
	public int getMoveCount() 
	{		
		return moveCount;
		
	}//END getMoveCount
	
	/**
	 * Get the thief's location.
	 * 
	 * @return the thief's location
	 */
	public int getThiefLocation() 
	{ 
		return thiefLocation;
		
	}//END getThiefLocation
	
	/** 
	 * Get the jewels location.
	 * 
	 * @return the jewels location
	 */
	public int getJewelsLocation()
	{ 
		return jewelsLocation;
		
	}//END getJewelsLocation
	
	/**
	 * Get indication if jewels have been stolen.
	 * 
	 * @return boolean indicating if jewels have been stolen.
	 */
	public boolean getAreJewelsStolen() 
	{ 
		return jewelsStolen;
		
	}//END getAreJewelsStolen
	
	/**
	 * Get current game status.  0 is thief has lost.  1 is game is active.
	 * 2 is thief has won (stolen the jewels and returned to the exit).
	 * 
	 * @return game status
	 */
	public int getGameStatus() 
	{ 
		return gameStatus;
		
	}//END getGameStatus
	
	/**
	 * Get indication if electro-magnetic pulse (EMP) has already been used.
	 * 
	 * @return boolean indicating if EMP has been used.
	 */
	public boolean getEMPUsed() 
	{
		return empUsed;
		
	}//END getEMPUsed
	
	/**
	 * Get refresh rate (how often alarm pattern changes in milliseconds).
	 * 
	 * @return number of milliseconds between changes in alarm pattern
	 */
	public int getRefreshRate() 
	{
		return refreshRate;
		
	}//END getRefreshRate
	
	/**
	 * Get museum room dimension
	 * 
	 * @return dimension of room
	 */
	public int getDim() 
	{ 
		return dim;
		
	}//END getDim
	
	/**
	 * Disable the alarm at the thief's current location via EMP.
	 * Only has effect one time.  Only has effect if the game
	 * is still active.
	 */
	public void disableAlarm() 
	{ 
		if(empUsed || gameStatus != 1) 
			return;
		
		alarms.get(thiefLocation).disable();
		empUsed = true;
		setChanged();
		notifyObservers();
		
	}//END disableAlarm
	
	/**
	 * Reset the game back to initial conditions.
	 */
	public void reset() 
	{

		// doesn't matter what current status is - just reset
		thiefLocation = dim * (dim - 1);
		jewelsStolen = false;
		moveCount = 0;
		gameStatus = 1;
		empUsed = false;
		
		// reset alarms
		for (int n = 0; n < numAlarms; n++) {
			alarms.get(n).resetAlarm();
		}
		
		setChanged();
		notifyObservers();
		
	}//END reset

}//END HeistModel