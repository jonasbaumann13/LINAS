import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.* ;

/* Durch diesen Thread kommunizieren Client und Server. Es wird stets ein Objektfeld übergeben,
 * welches aus 2 Objekten besteht. Das erste ist ein Befehl, um den nächsten Schritt zu bestimmen.
 * Das zweite ist das Feld, in welchem die aktuellen Daten der einzelnen Einheiten stehen.
 * Der Thread ist weiterhin für den Aufbau der Frames verantwortlich.
 */

public class linasComunication extends Thread
{
	Socket So = null;
	String who = "";
	String Command;							// ankommende Befehl
	Object o;
	Object [] Income = new Object [2];				// ankommendes Objektfeld
	ObjectInputStream In = null;					// Eingabestrom
	ObjectOutputStream Out = null;					// Ausgabestrom
	int xy [] = new int [3];					// nur für Koordinaten eines Schusses nötig
	int whichFrame = 0;						// um den Aufbau der Frames zu steuern
	Unit [] UnitArray;						// Feld mit Einheiten
	linasPrepareFrame PFrame;					// \
	linasSetFrame SFrame;						//  - die drei Frames
	linasMainFrame MFrame;						// /
	Message msg;							// Klasse für Fehlermeldungen
	
/*********** - Constructor - ***********/	
		
	public linasComunication (Socket tmpSo, String tmpwho)
	{
		So = tmpSo;
		who = tmpwho;
		msg = new Message ();
	}
	
/*********** - Prozess für Kommunikation - ***********/	
	
	public void run ()
	{
		nextFrame ();						// Aufbau des ersten Frames
		try
		{
			In = new ObjectInputStream (So.getInputStream());	// Erstellen des Eingabestroms
			while (true)
			{
				while ((o = In.readObject ()) != null)		// solange ein Objekt ankommt
				{
					Income = (Object []) o;
					Command = (String) Income [0];
					
					if (Command.equals ("Flame"))		// Wenn ein Schuss vermerkt wurde, ist das
					{					// zweite Objekt ein int-Feld!
						xy = (int []) Income [1];
						MFrame.flame (xy [0], xy [1]);	// der wartende Spieler will die Flamme ja
					}					// auch sehen!
					else	UnitArray = (Unit []) Income [1];
					
					
					
					if (Command.equals ("ClientReady"))	PFrame.ClientReady (UnitArray);		// Client ist mit Wählen der Einheiten fertig
					if (Command.equals ("ServerReady"))						// Server nun auch
					{	
						PFrame.setUnitArray (UnitArray);					// PrepareFrame bekommt das Feld des Servers, da es ihm gleich geschickt werden soll
						PFrame.send ("NextFrame");						// Server soll nächstes Frame aufbauen (bekommt auch vollständiges Einheitenfeld)
						nextFrame ();								// Client baut nächstes Frame auf
					}
					
					if (Command.equals ("ClientSet"))	SFrame.ClientReady (UnitArray);		// Server bekommt beschei, dass Client seine Einheiten gesetzt hat
					if (Command.equals ("ServerSet"))						// Client bekommt beschei, dass Server seine Einheiten gesetzt hat
					{
						SFrame.setUnitArray (UnitArray);		// wieder, um Server das nun erneuerte Feld zu geben
						SFrame.send ("NextFrame");			// nächstes Frame soll aufgebaut werden (bei Server)
						nextFrame ();					// Client baut nächstes Frame auf
					}
					if (Command.equals ("Move"))		MFrame.refresh (UnitArray);		// nach einem Zug die neuen Positionen für den, der nicht am Zug ist
					if (Command.equals ("EndTurn"))		MFrame.startTurn (UnitArray);		// Zug wurde beendet, der andere ist dran
					if (Command.equals ("NextFrame"))	nextFrame ();				// das nächste Frame wird aufgebaut
					if (Command.equals ("won"))		MFrame.sieg (false);			// Es wird mitgeteilt, das der andere gesiegt hat, man selbst hat verloren
					if (Command.equals ("runAway"))							// Falls ein Spieler vor der Entscheidung das Spiel verlässt	
					{
						MFrame.send ("quit");					// Spiel des Spielers, der es beenden wollte wird beendet
						System.out.println ("Ihr Gegner ist geflohen!");	// Trost für den, der nun allein dasitzt
						msg.msgInform ("Ihr Gegner ist geflohen!", MFrame);
						Command = "quit";					// Hier wird das Spiel dann auch beendet
						sleep (5000);						
					}
					if (Command.equals ("quit"))					// Beenden des Spieles
					{
						MFrame.send ("quit");
						sleep (200);
						PFrame.closeStream ();
						In.close ();
						MFrame.setVisible (false);
						System.exit (0);
					}						
					sleep (100);
				}
				sleep (100);
			}
		}
		catch (IOException e1)			{System.out.println (e1);}
		catch (ClassNotFoundException e2)	{System.out.println (e2);}
		catch (Exception e3)			{System.out.println (e3);}
	}
	
/*********** - Aufbau des jeweils nächsten Frames - ***********/		

	public void nextFrame ()
	{
		
		whichFrame++;
		switch (whichFrame)
			{
				case 1:		PFrame = new linasPrepareFrame (So, who);					
						PFrame.setLocation (100, 100);
						PFrame.setVisible (true);	
						break;
													// Da der OutputStream in linasPrepareFrame
				case 2: 	Out = PFrame.getOOS ();					// geöffnet wurde, muss er hier abgeholt werden
						SFrame = new linasSetFrame (who, Out, UnitArray);	// damit er an die anderen Frames übergeben werden kann			
						SFrame.setLocation (50, 0);
						SFrame.setVisible (true);
						PFrame.setVisible (false);
						break;

				case 3: 	SFrame.setVisible (false);
						MFrame = new linasMainFrame (who, Out, UnitArray);					
						MFrame.setLocation (50, 0);
						MFrame.setVisible (true);
						break;
			}
	}
}