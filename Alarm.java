/**
 * Alarm.java
 *
 * File:
 *	$Id: Alarm.java,v 1.1 2014/12/05 03:58:30 igk2718 Exp $
 *
 * Revisions:
 *	$Log: Alarm.java,v $
 *	Revision 1.1  2014/12/05 03:58:30  igk2718
 *	Created classes and began implementing some methods
 *
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Class definition for an alarm in the Heist game.
 *
 * @author atd: Aaron T Deever
 */

public class Alarm 
{
    /**
     * Flag indicating whether or not the alarm has been 
     * permanently disabled.
     */
    private boolean isDisabled;

    /**
     * Length of repeating alarm activation pattern
     */
    private int patternLength;
    
    /**
     * Current index of the alarm activation pattern
     */
    private int currentPatternIndex;
    
    /**
     * Activation pattern
     */
    private List<Boolean> activationPattern;
    
    /**
     * Construct an Alarm object.  On initialization, the alarm is not
     * permanently disabled and its currentPatternIndex is set to 0.
     * 
     * @param pattern List<Boolean> indicating the activation 
     * pattern for this alarm.
     */
    public Alarm(List<Boolean> pattern) 
    {
    	this.isDisabled = false;
    	this.activationPattern = new ArrayList<Boolean>(pattern);
    	this.currentPatternIndex = 0;
    	this.patternLength = this.activationPattern.size();
    }

    /**
     * Get the flag indicating whether or not the alarm is active.  
     * An alarm that has been disabled by an electro-magnetic 
     * pulse (EMP) is never active.
     *
     * @return A boolean indicating whether or not the alarm is active.
     */
    public boolean isActive() 
    {
    	if(isDisabled)
    		return false;
    	else
    		return activationPattern.get(currentPatternIndex);
    }

    /**
     * Permanently disable this alarm via an electro-magnetic pulse (EMP).
     */
    public void disable() 
    {
    	isDisabled = true;
    }

    /**
     * Update the current alarm pattern index.  Wrap around to 
     * the first index (0) if the end is reached.
     */
    public void incrementPatternIndex() 
    {
    	currentPatternIndex++;
    	if(currentPatternIndex == patternLength)
    		currentPatternIndex = 0;
    }

    /**
     * Reset the alarm.  Remove disabled status if present, 
     * and put the current pattern index back to 0.
     */
    public void resetAlarm() 
    {
    	currentPatternIndex = 0;
    	isDisabled = false;
    	
    }//END resetAlarm
    
}//END Alarm