/* 
 * HeistGUI.java
 *
 * Version:
 *        $Id: HeistGUI.java,v 1.7 2014/12/09 22:06:41 igk2718 Exp $
 *
 * Revisions:
 *        $Log: HeistGUI.java,v $
 *        Revision 1.7  2014/12/09 22:06:41  igk2718
 *        Fixed documentation
 *
 *        Revision 1.6  2014/12/09 22:05:21  igk2718
 *        Made heistGUI look better
 *
 *        Revision 1.5  2014/12/05 19:01:41  igk2718
 *        Added documentation
 *
 *        Revision 1.4  2014/12/05 18:56:16  igk2718
 *        Got game working
 *
 *        Revision 1.3  2014/12/05 18:46:21  igk2718
 *        created the bottom panel and implemented it's buttons
 *
 *        Revision 1.2  2014/12/05 18:27:53  igk2718
 *        Created tiles and alarms
 *
 *        Revision 1.1  2014/12/05 17:56:17  igk2718
 *        Added a class that creates the game info bar
 *
 *        Revision 1.2  2014/12/05 04:48:42  igk2718
 *        Created the game board with no current functionality
 *
 *        Revision 1.1  2014/12/05 03:58:29  igk2718
 *        Created classes and began implementing some methods
 *
 */

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Builds the GUI for the Heist game and also listens for the user input
 *
 * @author igk2718: Ian Kitchen
 */
public class HeistGUI
{
	private final HeistModel model;
	private Timer timer;
	
	/**
	 * Constructor builds the view
	 * 
	 * @param model - the model for the game board
	 */
	public HeistGUI(HeistModel model)
	{
		this.model = model;
		
		//build the frame and main panel
		JFrame frame = new JFrame("Heist Game");
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		
		//Add the game's info bar
		main.add(createGameInfoBar(model), BorderLayout.NORTH);
		
		//Create the game cells
		JPanel floor = new JPanel();
		floor.setLayout(new GridLayout(model.getDim(), model.getDim()));
		ArrayList<JButton> tiles = createTiles(model);
		for(JButton b: tiles)
			floor.add(b);
		main.add(floor, BorderLayout.CENTER);
		
		//Create the lower game panel
		JPanel gameOptions = new JPanel();
		gameOptions.setLayout(new BorderLayout());
		JLabel enterExit = new JLabel("ENTER / EXIT");
		gameOptions.add(enterExit, BorderLayout.WEST);
		JPanel optionButtons = new JPanel();
		optionButtons.add(EMP(model));
		optionButtons.add(reset(model));
		gameOptions.add(optionButtons, BorderLayout.EAST);
		main.add(gameOptions, BorderLayout.SOUTH);
		
		//Finish the frame elements
		beginTimer();
		Container pane = frame.getContentPane();
		pane.add(main);
		frame.pack();
		frame.setSize(450, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}//END 
	
	/**
	 * creates the top info bar of the game and adds appropriate listener and 
	 * observer
	 * 
	 * @param model - the model containing the game data
	 * @return the label containing game information
	 */
	private JLabel createGameInfoBar(HeistModel model)
	{
		//create the label for the game info
		final JLabel status = new JLabel("Moves: 0");
		status.setOpaque(true);
		
		//Add an observer for the start 
		model.addObserver(new Observer() {
	        public void update(Observable o, Object arg) {
                HeistModel model = (HeistModel)o;
                
                //The user lost
                if(model.getGameStatus() == 0) 		                 
                    status.setText("Moves: " + model.getMoveCount()
            				     + " GAME OVER: YOU TRIGGERED AN ALARM!");
                
                //The user is currently playing	  
                else if(model.getGameStatus() == 1)
                    status.setText("Moves: " + model.getMoveCount());
            	      
                //The user has won    
                else
                    status.setText("Moves: " + model.getMoveCount()
            	    			   + " YOU WIN!!");
                }//END update
            });//END Observer
		
		return status;
		
	}//END createGameInfoBar
	
	/**
	 * creates the tiles, alarms and implements listeners and observers for each
	 * button
	 * 
	 * @param model - the model containing the game data
	 * @return the array of tile buttons
	 */
	private ArrayList<JButton> createTiles(final HeistModel model)
	{
		//create array of buttons
		ArrayList<JButton> tiles = new ArrayList<>();
		
		//create image icons of the thief, jewels and escaping thief
		final Icon robber = new ImageIcon("Thief.JPG");
		final Icon jewels = new ImageIcon("Jewels.JPG");
		final Icon win = new ImageIcon("Escape.JPG");
		int numTiles = model.getDim() * model.getDim();
		
	    for(int i = 0; i < numTiles; i++)
	    {
	    	final int currentTile = i;
	    	final JButton tile = new JButton();
	    	tile.setContentAreaFilled(false);
	    	tile.setOpaque(true);
	    	
	    	//add an action listener for a tile being clicked
	    	tile.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e)
	    		{
	    			model.selectCell(currentTile);
	    		}
	    	});
	  
	    	//Add an observer for the tiles 
		    Observer observer = new Observer() {
		    	public void update(Observable o, Object arg) {
		    		HeistModel model = (HeistModel)o;
		    		
		    		//IF the thief is on the current tile, set the thief picture 
		    		//to the tile
		    		if(model.getThiefLocation() == currentTile)
		    			tile.setIcon(model.getAreJewelsStolen() ? win : robber);
		    		
		    		//IF the jewels haven't been stolen and the current tile is
		    		//the jewels location, set the icon to the jewels picture
		    		else if(!model.getAreJewelsStolen() && model.getJewelsLocation() == currentTile)
		    			tile.setIcon(jewels);
		    		
		    		//IF the tile icon is not null, set the tile icon to be null
		    		else if(tile.getIcon() != null)
		    			tile.setIcon(null);
		    		
		    		//IF the current tile alarm is active, set icon to blue
		    		if(model.getAlarms().get(currentTile))
		    			tile.setBackground(Color.BLUE);
		    		
		    		else
		    		    tile.setBackground(Color.WHITE);
		    		
		    	}//END update
		    };//END Observer
		    
		    //update observer
		    observer.update(model, null);
		    
		    //add observer to the model
		    model.addObserver(observer);
		    
		    //add the tile to the list of tiles
		    tiles.add(tile);
		    
	    }//END FOR	    
		
		return tiles;
		
	}//END createTiles
	
	/**
	 * creates the EMP button and adds a listener for when the button is hit
	 * 
	 * @param model - the model containing game data
	 * @return the EMP button
	 */
	private JButton EMP(final HeistModel model)
	{
		//create button
		JButton EMP = new JButton("EMP");
		
		//add the action listener for the EMP button
		EMP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				model.disableAlarm();
			}
		});		
		return EMP;
		
	}//END EMP
	
	/**
	 * creates the reset button for the game and adds a listener for when the 
	 * reset is hit
	 * 
	 * @param model - the model containing game data
	 * @return the reset button
	 */
	private JButton reset(final HeistModel model)
	{
		//create the reset button
		JButton reset = new JButton("RESET");
		
		//Add the listener for the reset button
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				model.reset();
				beginTimer();
			}
		});
		
		return reset;
		
	}//END reset
	
	/**
	 * Starts the game's timer if it is not running. If it is running then it 
	 * resets the timer.
	 */
	public void beginTimer()
	{
		//IF the timer has already been created 
		if(timer != null)
		{
			//IF the timer is currently running
			if(timer.isRunning())
				//restart the timer
		        timer.restart();
		
		    else
		    	//start the timer
			    timer.start();
		}
		else
		{
			//create the timer object and give it it's own action listener
			timer = new Timer(model.getRefreshRate(), new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					model.updateAlarmPattern();
					
				}//END actionPerformed
			});//END timer
			timer.start();
			
		}//END IF
		
	}//END beginTimer
	
	/**
	 * The main method of HeistGUI
	 * 
	 * @param args - the command line arguments
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		if(args.length != 1) 
		{ 
	        String us = "Usage: java Heist <config-file>";
	        System.out.println(us);
	        return;
	    }
	    	
	    new HeistGUI(new HeistModel(args[0]));
	    
	}//END main

}//END Heist