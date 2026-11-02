import javax.swing.UIManager;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/*	zu startende Klasse, um Linas zu starten	
 */
 
public class Linas
{
/*********** - Constructor, laden des Startbildschirms - ***********/

	public Linas ()
	{
		Menu StartFrame = new Menu ();
		StartFrame.setLocation (100, 100);
		StartFrame.setResizable (false);
		StartFrame.setVisible (true);
	}

/*********** - Main-Methode - ***********/

	public static void main (String[] args)
	{
		try
		{	UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e)
		{	e.printStackTrace ();}
		new Linas ();
	}
}




// ToDo: Keypadsettings, Punkte gleich, SetFrame... Einheitenzahl nicht richtig, MainFrame... Bildwechsel bei Schaden?
// MainFrame ... Reichweite falsch herum