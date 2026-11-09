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
        if (args.length > 0 && args[0].equals("--rl-server")) {
            try {
                if (args.length > 2) throw new IllegalArgumentException("Usage: Linas --rl-server [port]");
                int port = args.length == 2 ? Integer.parseInt(args[1]) : 8765;
                if (port < 0 || port > 65535) throw new IllegalArgumentException("Port must be 0..65535");
                RlServer.run(port);
            } catch (Exception e) {
                System.err.println("Cannot start RL server: " + e.getMessage());
                System.exit(1);
            }
            return;
        }
		try
		{	UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e)
		{	e.printStackTrace ();}
		new Linas ();
	}
}




// ToDo: Keypadsettings, Punkte gleich, SetFrame... Einheitenzahl nicht richtig, MainFrame... Bildwechsel bei Schaden?
// MainFrame ... Reichweite falsch herum