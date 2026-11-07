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

public class Comunication extends Thread
{
	String IP				= "";
	String NickS			= "";
	String NickC			= "";
	String Command;								// ankommende Befehl
	Object [] o;
	Object [] forSending 	= new Object [2];
	ObjectInputStream In;				// Eingabestrom
	ObjectOutputStream Out;				// Ausgabestrom
	FileInputStream FIS 	= null;
	FileOutputStream FOS 	= null;
	Socket So				= null;
	ServerSocket SerSo		= null;
	
	boolean IsServer		= false;
	int [] DataArr 			= null;
	int picCount1 			= 0;
	int picCount2 			= 0;
	int anzUnitFiles;
	int xy [] 				= new int [3];		// nur für Koordinaten eines Schusses nötig
	int whichFrame 			= 0;				// um den Aufbau der Frames zu steuern
	int [] UnCo 			= new int [1];
	int x, y;
	Unit [] basicUnitInfo;
	Unit [] UnitArray 		= null;				// Feld mit Einheiten
	PrepareFrame PFrame;						// \
	SetFrame SFrame;							//  - die drei Frames
	MainFrame MFrame;							// /
	Message msg;								// Klasse für Fehlermeldungen
	Gameoptions go;
	
/*********** - Constructor - ***********/	
		
	public Comunication (boolean IsServer, String Nick, String IP, int x, int y)
	{
		this.IsServer			= IsServer;
		this.IP					= IP;
		this.x 					= x;
		this.y 					= y;
		
		if (IsServer)	{NickS 	= Nick;}
		else			{NickC 	= Nick;}
		msg 				= new Message ();
		
		if (IsServer)
		{
			go = new Gameoptions (NickS, IsServer, x, y);
			go.setLocation (x, y);
			go.setResizable (false);
			go.setVisible (true);
		}		
	}
		
/*********** - Prozess für Kommunikation - ***********/	
	
	public void run ()
	{
		try
		{
			if (IsServer && (So==null))
			{	
				System.out.println ("\n\n Warte auf Verbindung 1111... \n\n");
				SerSo 	= new ServerSocket (1111);			
				So 		= SerSo.accept ();							//Wartet auf einen Client, um Socket zu bekommen
				if (So!=null)
				{
					Out	= new ObjectOutputStream (So.getOutputStream ());
					In 	= new ObjectInputStream (So.getInputStream ());
					go.setOOS (Out);
				}
			}
					
			if (!IsServer && (So==null))
			{
				System.out.println ("\n\n Verbindungsaufbau mit " + IP + "...\n\n");
				So 	= new Socket (IP, 1111);
				if (So!=null)
				{
					Out	= new ObjectOutputStream (So.getOutputStream ());
					In 	= new ObjectInputStream (So.getInputStream ());
					
					go = new Gameoptions (NickC, IsServer, x, y);
					go.setLocation (x, y);
					go.setResizable (false);
					go.setVisible (true);
					go.setOOS (Out);
				}
			}
				
			while (true)
			{
				while ((o = (Object []) In.readObject ()) != null)		// solange ein Objekt ankommt
				{
					Command = o[0].toString();
					System.out.println ("Incomming message: " + Command);
					
					
					if (
						(Command.equals ("ServerNick"))		||	(Command.equals ("ClientNick"))	 ||
						(Command.equals ("sendSettings"))	||	(Command.equals ("chat"))		 ||
						(Command.equals ("ClientAccepts"))	||
						(Command.equals ("sendUnitData")) 	|| 	(Command.equals ("gotUnitData")) ||
					 	(Command.equals ("sendUnitPics"))	|| 	(Command.equals ("noMorePics"))  ||
					 	(Command.equals ("Flame"))			|| 	(Command.equals ("EnemyFire"))	
					   )
					{
						if (Command.equals ("ServerNick"))				// Server schickt seinen Nick
						{
							go.setServerNick (o[1].toString());
						}
						//----------------------------------\\
						
						if (Command.equals ("ClientNick"))				// Client schickt seinen Nick
						{
							go.setClientNick (o[1].toString());
						}
						//----------------------------------\\
						
						if (Command.equals ("sendSettings"))			// Server schickt Einstellungen
						{
							go.setSettings (o[1]);
						}
						//----------------------------------\\
						
						if (Command.equals ("chat"))					// Server schickt Einstellungen
						{
							go.sendMsg (1, (String) o[1]);
						}
						//----------------------------------\\
						
						if (Command.equals ("ClientAccepts"))			// Client ist einverstanden
						{
							go.clientAccepts();
						}
						//----------------------------------\\
						
						if (Command.equals ("sendUnitData"))			// nur Client (Server hat Einheitendatei geschickt)
						{	
							basicUnitInfo = (Unit []) o [1];
							send ("gotUnitData");
						}
						//----------------------------------\\
					
						if (Command.equals ("gotUnitData"))				// nur Server (Client hat Einheiteninfo bekommen)
						{
							DataArr = null;
							if (picCount2 < 5)							// Anzahl der Schadensmodelle
							{
								if (basicUnitInfo[picCount1].IsServer)	FIS = new FileInputStream (AppPaths.temporary(basicUnitInfo[picCount1].pictureAS[picCount2]));
								else																		FIS = new FileInputStream (AppPaths.temporary(basicUnitInfo[picCount1].pictureAC[picCount2]));
								DataArr = new int [FIS.available()];
								for (int i0 = 0; i0 < DataArr.length; i0++)
								{	DataArr [i0] = FIS.read ();}
								FIS.close ();
								FIS = null;
								picCount2++;
							}
							else	
							{
								DataArr = new int [0];
								picCount1++;
								picCount2 = 0;
							}
							System.out.println ("["+picCount1+"]["+picCount2+"]");
							if (picCount1 < anzUnitFiles*2)		send ("sendUnitPics");
							else
							{
								send ("noMorePics");
								nextFrame ();
							}
						}
						//----------------------------------\\
						
						if (Command.equals ("sendUnitPics"))			// nur Client (Server hat Bilder geschickt)
						{		
							DataArr = (int []) o[1];
							if (DataArr.length > 0)
							{	
								if (basicUnitInfo[picCount1].IsServer)	FOS = new FileOutputStream (AppPaths.temporary(basicUnitInfo[picCount1].pictureAS[picCount2]), true);
								else																		FOS = new FileOutputStream (AppPaths.temporary(basicUnitInfo[picCount1].pictureAC[picCount2]), true);
								for (int i = 0; i < DataArr.length; i++)
								{	FOS.write (DataArr [i]);}
								picCount2++;
								FOS.close ();
								FOS = null;
							}
							else
							{	
								picCount1++;
								picCount2 = 0;
							}
							System.out.println ("["+picCount1+"]["+picCount2+"]");
							send ("gotUnitData");						// Schleife, um restlichen Bilder auch zu bekommen
						}
						//----------------------------------\\
						
						if (Command.equals ("noMorePics"))				// Ende der Bilderschickschleife
						{												// und Spielbeginn
							nextFrame ();
						}
						//----------------------------------\\							
					
						if (Command.equals ("Flame"))					// Wenn ein Schuss vermerkt wurde, ist das
						{												// zweite Objekt ein int-Feld!
							xy = (int []) o[1];
							MFrame.flame (xy [0], xy [1]);				// der wartende Spieler will die Flamme ja
						}												// auch sehen!
						//----------------------------------\\
						
						if (Command.equals ("EnemyFire"))
						{
							UnCo = (int []) o[1];
							MFrame.newEnemyInfo (UnCo [0]);
						}
					}
					
					// fuer alle Befehle, bei welchen UnitArray geschickt wird

					else	UnitArray = (Unit []) o[1];
					
					if (Command.equals ("ServerAccepts"))
					{	
						basicUnitInfo = UnitArray;
						anzUnitFiles = UnitArray.length;
						nextFrame ();
						send ("ClientGotIt");
					}
					if (Command.equals ("ClientGotIt"))
					{
						basicUnitInfo = UnitArray;
						anzUnitFiles = UnitArray.length;
						nextFrame ();
					}				
					if (Command.equals ("ClientReady"))	PFrame.ClientReady (UnitArray);		// Client ist mit Wählen der Einheiten fertig
					if (Command.equals ("ServerReady"))					// Server nun auch
					{	
						//PFrame.setUnitArray (UnitArray);				// PrepareFrame bekommt das Feld des Servers, da es ihm gleich geschickt werden soll
						send ("NextFrame");								// Server soll nächstes Frame aufbauen (bekommt auch vollständiges Einheitenfeld)
						nextFrame ();									// Client baut nächstes Frame auf
					}
					
					if (Command.equals ("ClientSet"))	SFrame.ClientReady (UnitArray);		// Server bekommt bescheid, dass Client seine Einheiten gesetzt hat
					if (Command.equals ("ServerSet"))					// Client bekommt bescheid, dass Server seine Einheiten gesetzt hat
					{
						SFrame.setUnitArray (UnitArray);				// wieder, um Server das nun erneuerte Feld zu geben
						SFrame.send ("NextFrame");						// nächstes Frame soll aufgebaut werden (bei Server)
						nextFrame ();									// Client baut nächstes Frame auf
					}
					if (Command.equals ("Move"))		MFrame.refresh (UnitArray);			// nach einem Zug die neuen Positionen für den, der nicht am Zug ist
					if (Command.equals ("EndTurn"))		MFrame.startTurn (UnitArray);		// Zug wurde beendet, der andere ist dran
					if (Command.equals ("NextFrame"))	nextFrame ();	// das nächste Frame wird aufgebaut
					if (Command.equals ("won"))		MFrame.sieg (false);// Es wird mitgeteilt, das der andere gesiegt hat, man selbst hat verloren
					if (Command.equals ("runAway"))						// Falls ein Spieler vor der Entscheidung das Spiel verlässt	
					{
						MFrame.send ("quit");							// Spiel des Spielers, der es beenden wollte wird beendet
						System.out.println ("The enemy runs away!");// Trost für den, der nun allein dasitzt
						msg.msgInform ("The enemy runs away!", MFrame);
						Command = "quit";								// Hier wird das Spiel dann auch beendet
						sleep (200);						
					}
					if (Command.equals ("quit"))						// Beenden des Spieles
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
		catch (IOException e1)				{System.out.println (e1);}
		catch (ClassNotFoundException e2)	{System.out.println (e2);}
		catch (Exception e3)				{System.out.println (e3);}
	}
	
/*********** - Aufbau des jeweils nächsten Frames - ***********/		

	public void nextFrame ()
	{
		
		whichFrame++;
		switch (whichFrame)
			{
				case 1:		x = go.getBounds ().x;
						y = go.getBounds ().y;
						PFrame = new PrepareFrame (Out, IsServer, basicUnitInfo, anzUnitFiles, go.getCredits(), go.getNick());					
						PFrame.setLocation (x, y);
						PFrame.setResizable (false);
						PFrame.setVisible (true);
						go.setVisible (false);	
						break;
							
				case 2: 	x = PFrame.getBounds ().x;
						y = PFrame.getBounds ().y;
						SFrame = new SetFrame (IsServer, Out, UnitArray, PFrame.getBspU(), anzUnitFiles);	// damit er an die anderen Frames übergeben werden kann			
						SFrame.setLocation (x, y);
						SFrame.setResizable (false);
						SFrame.setVisible (true);
						PFrame.setVisible (false);
						break;

				case 3: 	x = SFrame.getBounds ().x;
						y = SFrame.getBounds ().y;
						SFrame.setVisible (false);
						MFrame = new MainFrame (IsServer, Out, UnitArray);					
						MFrame.setLocation (x, y);
						MFrame.setResizable (false);
						MFrame.setVisible (true);
						break;
			}
	}
	
/*********** - Versenden der Server-Einheiten - ***********/
	
	public void send (String cmd)
	{
		try
		{
			forSending [0] = cmd;
			forSending [1] = UnitArray;
			
			if (cmd.equals ("gotUnitData"))		forSending [1] = null;
			if (cmd.equals ("noMorePics"))		forSending [1] = null;
			if (cmd.equals ("sendUnitData"))	forSending [1] = basicUnitInfo;
			if (cmd.equals ("sendUnitPics"))	forSending [1] = DataArr;
 
			Out.reset ();												// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject (forSending);								// in OutputStream schreiben
			Out.flush ();												// alles im OutputStram verschicken
		}
		catch (IOException e4)		{System.out.println (e4);}
	}
}