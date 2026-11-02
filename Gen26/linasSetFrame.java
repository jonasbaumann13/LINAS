import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

/* Diese Klasse dient dazu, die Anfangspositionen der Einheiten auf dem Spielfeld zu ermitteln.
 * Dazu setzen die Spieler am Anfang ihre Einheiten auf ihre Seite des Spielfeldes.
 */

public class linasSetFrame extends JFrame implements ActionListener, MouseListener
{
	//Layoutkomponenten
	JPanel contentPane;
	ImageIcon iHeading;
	JLabel lHeading, lInfoPlayer, lInfoUnit;
	JLabel lInfoPlayerV, lInfoUnitV;
	JLabel lLeftU1, lLeftU2, lLeftU3, lLeftU4, lLeftU2V, lLeftU3V, lLeftU4V;
	JButton bNext, bReady, bEndGame;
	linasSpielfeld linasBrett;
	Color cBlue = new Color (41, 41, 255);
	Font fPlayer = new Font ("Verdana", 1, 14);
	
	//andere Komponenten
	String [] Player = new String [2];		// Name der beiden Spieler
	String who;					// Server oder Client
	ObjectOutputStream Out = null;			// Ausgabestrom
	Object [] forSending = new Object [2];		// Objekt, welches bei Bedarf gesendet wird
	int UC;						// (unit-counter) um zu Ermitteln, welche Einheit gerade dran ist
	int MouseX, MouseY, num1, num2, num3;		// Maus-Koordinaten + Anzahl der noch zu setzenden Einheiten
	int P1UnitNum;					// Gesammtanzahl der Einheiten von Spieler 1 (Client!)
	int P2UnitNum;					// Gesammtanzahl der Einheiten von Spieler 2 (Server!)
	boolean turn = true;				// Ob gerade am Zug oder nicht
	Unit [] UnitArray, tmpUA;			// Einheitenfeld
	Operations op;					// Methodensammlung
	Message msg;					// Fehlerausgabe

/*********** - Layoutaufbau im Constructor und spezifische Starteinstellungen - ***********/	

	public linasSetFrame (String who, ObjectOutputStream Out, Unit [] UnitArray)
	{
		this.UnitArray = UnitArray;
		this.who = who;
		this.Out = Out;
	
		iHeading = new ImageIcon (linasSetFrame.class.getResource ("/linasHeader.gif"));		lHeading = new JLabel (iHeading);
		lInfoPlayer = new JLabel ("Spieler:");			lInfoPlayerV = new JLabel ("");
		lInfoUnit = new JLabel ("Einheit:");			lInfoUnitV = new JLabel ("");
		lLeftU1 = new JLabel ("Verfügbare Einheiten:");		lLeftU2 = new JLabel ("Panzer:");
		lLeftU3 = new JLabel ("Artillerie:");			lLeftU4 = new JLabel ("Späher:");
		lLeftU2V = new JLabel ("--");				lLeftU3V = new JLabel ("--");
		lLeftU4V = new JLabel ("--");				bEndGame = new JButton ("Spiel beenden");
		bNext = new JButton ("Weiter");				bReady = new JButton ("");
		op = new Operations ();					linasBrett = new linasSpielfeld ();
		msg = new Message ();
				
		linasBrett.setSize (580, 580);				linasBrett.addMouseListener (this);
		this.setTitle ("Linas Einheiten setzen");		this.setSize (new Dimension (900, 728));
		contentPane = (JPanel) this.getContentPane ();		contentPane.setLayout (null);
		
		lHeading.setBounds (175, 20, 550, 65);			linasBrett.setBounds (10, 95, 580, 580);
		lInfoPlayer.setBounds (600, 95, 120, 20);		lInfoPlayerV.setBounds (750, 95, 75, 20);
		lInfoUnit.setBounds (600, 130, 120, 15);		lInfoUnitV.setBounds (750, 130, 75, 15);
		lLeftU1.setBounds (600, 170, 150, 15);
		lLeftU2.setBounds (600, 190, 75, 15);			lLeftU2V.setBounds (750, 190, 75, 15);
		lLeftU3.setBounds (600, 210, 75, 15);			lLeftU3V.setBounds (750, 210, 75, 15);
		lLeftU4.setBounds (600, 230, 75, 15);			lLeftU4V.setBounds (750, 230, 75, 15);
		bNext.setBounds (600, 480, 125, 30);			bReady.setBounds (750, 480, 125, 30);
		bEndGame.setBounds (675, 645, 125, 30);

		lInfoPlayer.setForeground (cBlue);			lInfoPlayer.setFont (fPlayer);
		lInfoPlayerV.setForeground (cBlue);			lInfoPlayerV.setFont (fPlayer);
		bEndGame.setForeground (Color.red);

		contentPane.add (lHeading);				contentPane.add (linasBrett);
		contentPane.add (bNext);				contentPane.add (bReady);
		contentPane.add (lInfoPlayer);				contentPane.add (lInfoPlayerV);
		contentPane.add (lLeftU1);				contentPane.add (bEndGame);
		contentPane.add (lLeftU2);				contentPane.add (lLeftU2V);
		contentPane.add (lLeftU3);				contentPane.add (lLeftU3V);
		contentPane.add (lLeftU4);				contentPane.add (lLeftU4V);
		contentPane.add (lInfoUnit);				contentPane.add (lInfoUnitV);

		bNext.addActionListener (this);
		bReady.addActionListener (this);
		bReady.setEnabled (false);
		bEndGame.addActionListener (this);
		
		Player = op.player (UnitArray);
		P1UnitNum = op.UnitNum (UnitArray, 1);
		P2UnitNum = op.UnitNum (UnitArray, 2);
			
		linasBrett.setUnitNum (P1UnitNum, P2UnitNum);				// wichtige Variablen für linasSpielfeld
		linasBrett.setPics (UnitArray);						// Bilder der Einheiten in linasSpielfeld setzen
		
		if (who.equals ("client"))						// nur für Client
		{	
			lInfoUnitV.setText (UnitArray [P1UnitNum].type);
			linasBrett.newBoard (1);					// spezifische erlaubte Felder, auf die er setzen kann
			bReady.setLabel ("Fertig");
			lInfoPlayerV.setText (Player [0]);
			UC = 0;								// (seine Einheiten gehen im Feld von 0 bis P1UnitNum!)
		}
		if (who.equals ("server"))						// nur für Server
		{	
			turn = false;							// nicht am Zug
			lInfoUnitV.setText (UnitArray [P2UnitNum].type);
			linasBrett.newBoard (2);					// spezifische erlaubte Felder, auf die er setzen kann
			bReady.setLabel ("Warte auf Client...");
			lInfoPlayerV.setText (Player [1]);
			UC = P1UnitNum;							// (seine Einheiten gehen im Feld von P1UnitNum bis zum Schluss!)
		}
		numStart ();
	}

/*********** - Reaktion auf Buttonklicks - ***********/		
	
	public void actionPerformed (ActionEvent e)
	{
		try
		{
			Object o = e.getSource ();
			
			if (o.equals (bNext))	Next ();
			if (o.equals (bReady))	Ready ();
			if (o.equals (bEndGame)) EndGame ();
			
			linasBrett.setzen (UnitArray, UC);				// Einheiten auch graphisch setzen
		}										
		
		catch (NullPointerException npe)
		{	System.out.println ("Null Pointer");}
	}

/*********** - nächste Einheit auswählen - ***********/	

	public void Next ()
	{	
		int tmp = 0;								// um Endlosschleife zu verhindern
		do 
		{
			UC++;
			tmp++;
			if ((who.equals ("client")) && (UC >= P1UnitNum))
			{	UC = 0;}
			if ((who.equals ("server")) && (UC >= (UnitArray.length)))
			{	UC = P1UnitNum;}
		}	while ((UnitArray [UC].visible == true) && (tmp < (UnitArray.length * 2)));
											// bis Einheit gefunden, die noch nicht gesetzt wurde
		lInfoUnitV.setText (UnitArray [UC].type);
	}
	
/*********** - alle Einheiten wunschgemäß verteilt - ***********/		
	
	public void Ready ()
	{
		if (who.equals ("client"))				// nur Client
		{
			bReady.setText ("Warte auf Host...");
			bReady.setEnabled (false);
			send ("ClientSet");				// sagt, dass er alles gesetzt hat
		}
		if (who.equals ("server"))				// nur Server
		{	
			send ("ServerSet");				// sagt, dass er alles gesetzt hat
		}
	}
	
/*********** - Setzen einer Einheit per Mausklick auf Spielfeld - ***********/
	
	public void mousePressed (MouseEvent e)				
	{
		boolean ready = true;					// ob alle gesetzt wurden
		int [] xy = new int [3];				

		if ((e.getX() < 572) && (e.getY() < 565))		// im Spielfeld
		{
			xy = op.koAngleich (e.getX(), e.getY());	// Feld mit umgerechneten Mauskoordinaten
			if (((who.equals ("client")) && (xy [2] > 16)) || ((who.equals ("server")) && (xy [2] < 3)))
			{						// Koordinaten innerhalb des vorgeschriebenen bereiches
				int tmpUC = op.occupied (xy [0], xy [1], UnitArray);		// ob Feld besetzt
				if ( tmpUC == -1)						// wenn Feld nicht besetzt
				{
					UnitArray [UC].PosX = xy [0];
					UnitArray [UC].PosY = xy [1];
					numMod ();
					UnitArray [UC].visible = true;
					linasBrett.setzen (UnitArray, UC);			// Einheit mit neuen Koordinaten setzen
				}
				else								// wemm Feld besetzt
				{	
					UC = tmpUC;						// anwählen der angeklickten Einheit
					linasBrett.setzen (UnitArray, UC);
				}
			}
			else						// Koordinaten nicht im richtigen Bereich
			{	msg.msgError ("Bitte auf den freien Flächen setzen.", this);}
		}
		
		ready = op.setAll (UnitArray, P1UnitNum, who);		// test, ob alle Einheiten gesetzt sind
		if (turn)
		{	if (ready)	bReady.setEnabled (true);}	
	}

/*********** - Methoden die mit aufgeschrieben werden müssen (wegen implements Action- bzw Mouselistener) - ***********/

	public void mouseClicked (MouseEvent e)
	{
	}


	public void mouseReleased (MouseEvent e)
	{
	}


	public void mouseEntered (MouseEvent e)
	{
	}


	public void mouseExited (MouseEvent e)
	{
	}
	
/*********** - Programmende, wenn das Fenster geschlossen wird - ***********/	
	
	protected void processWindowEvent (WindowEvent e)
	{
		super.processWindowEvent (e);
		if (e.getID () == WindowEvent.WINDOW_CLOSING)
		{	System.exit (0);}							
	}
	
/*********** - Programmende (Schließen Button) - ***********/	
	
	public void EndGame ()
	{
		System.exit (0);
	}
	
/*********** - Wieviele Einheiten noch zu setzen sind - ***********/	
	
	public void UnitsToSet ()
	{
		lLeftU2V.setText ("" + num1);
		lLeftU3V.setText ("" + num2);
		lLeftU4V.setText ("" + num3);
	}
	
/*********** - stets die gesetzte Einheit abziehen - ***********/		
	
	public void numMod ()
	{
		if (UnitArray [UC].visible == false)
		{
			if (UnitArray [UC].type.equals ("Panzer"))
			{	num1--;}
			if (UnitArray [UC].type.equals ("Artillerie"))
			{	num2--;}
			if (UnitArray [UC].type.equals ("Aufklärer"))
			{	num3--;}
			UnitsToSet ();
		}
	}
	
/*********** - Anfänglichen Einheiten zum setzen - ***********/		
	
	public void numStart ()
	{
		if (who.equals ("client"))
		{
			num1 = op.number (UnitArray, Player [0], "Panzer");
			num2 = op.number (UnitArray, Player [0], "Artillerie");
			num3 = op.number (UnitArray, Player [0], "Aufklärer");
		}
		if (who.equals ("server"))
		{
			num1 = op.number (UnitArray, Player [1], "Panzer");
			num2 = op.number (UnitArray, Player [1], "Artillerie");
			num3 = op.number (UnitArray, Player [1], "Aufklärer");
		}
		UnitsToSet ();
	}
	
/*********** - Einheiten-Feld ersetzen (erneuern) - ***********/
	
	public void setUnitArray (Unit [] UA)
	{
		UnitArray = UA;
	}
	
/*********** - für Server: Client ist fertig mit setzen - ***********/
	
	public void ClientReady (Unit [] UA)
	{
		UnitArray = op.ModUnitArray (UnitArray, UA, P1UnitNum);		// Server-Feld bekommt Daten des Client-Feldes
		bReady.setLabel ("Spiel beginnen");
		turn = true;
		boolean ready = op.setAll (UnitArray, P1UnitNum, who);		// Test, ob Server schon alle gesetzt hat
		if (ready)
		{	bReady.setEnabled (true);}
	}
	
/*********** - Senden des Befehles und des Feldes - ***********/
	
	public void send (String Command)
	{
		forSending [0] = (Object) Command;
		forSending [1] = (Object) UnitArray;
		try
		{	
			Out.reset ();					// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject ((Object) forSending);		// in OutputStream schreiben
			Out.flush ();					// alles im OutputStram verschicken
		}
		catch (IOException e2)		{System.out.println (e2);}
	}
}