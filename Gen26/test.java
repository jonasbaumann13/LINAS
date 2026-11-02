import javax.swing.UIManager;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/*	zu startende Klasse, um Linas zu starten	
 */
 
public class test
{
	static String s = "";
	
/*********** - Constructor, laden des Startbildschirms - ***********/

	public test ()
	{
		if (s.equals ("Gameoptions"))	
		{
			Gameoptions TestF 	= new Gameoptions ("test", false, 0, 0);
			TestF.setLocation (100, 100);
			TestF.setResizable (false);
			TestF.setVisible (true);
		}

		if (s.equals ("Menu"))			
		{
			Menu TestF 			= new Menu ();
			TestF.setLocation (100, 100);
			TestF.setResizable (false);
			TestF.setVisible (true);
		}

	}

/*********** - Main-Methode - ***********/

	public static void main (String[] args)
	{
		try
		{	UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e)
		{	e.printStackTrace ();}
		s = args [0];
		new test ();
	}
}