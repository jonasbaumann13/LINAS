import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

/* Diese Klasse dient dazu, die Anfangspositionen der Einheiten auf dem Spielfeld zu ermitteln.
 * Dazu setzen die Spieler am Anfang ihre Einheiten auf ihre Seite des Spielfeldes.
 */

public class SetFrame extends JFrame implements ActionListener, MouseListener
{
	//Layoutkomponenten
	JPanel contentPane;
	ImageIcon iHeading;
	JLabel [] JLA;
//	JLabel lHeading, lInfoPlayer, lInfoUnit, lLeftU1;
//	JLabel lInfoPlayerV, lInfoUnitV;
	JLabel [] lLeftU;
	JLabel [] lLeftUV;
	JButton bNext, bReady, bEndGame;
	Spielfeld Brett;
	Color cBlue 	= new Color (41, 41, 255);
	Color C 		= Color.black;
	Color CExit 	= Color.red;
	Font F 			= new Font ("Garamond", 1, 14);
	Font fPlayer 	= new Font ("Verdana", 1, 16);
	
	//andere Komponenten
	KeyAdapter KA 			= null;
	MouseAdapter MA			= null;
	String [] Player 		= new String [2];		// Name der beiden Spieler
	ObjectOutputStream Out 	= null;					// Ausgabestrom
	Object [] forSending 	= new Object [2];		// Objekt, welches bei Bedarf gesendet wird
	int UC;											// (unit-counter) um zu Ermitteln, welche Einheit gerade dran ist
	int MouseX, MouseY;								// Maus-Koordinaten + Anzahl der noch zu setzenden Einheiten
	int [] num 				= {0, 0, 0, 0, 0, 0};
	int P1UnitNum;									// Gesammtanzahl der Einheiten von Spieler 1 (Client!)
	int P2UnitNum;									// Gesammtanzahl der Einheiten von Spieler 2 (Server!)
	int anzUnitFiles;
	boolean turn 			= true;					// Ob gerade am Zug oder nicht
	boolean IsServer;
	Unit [] UnitArray, tmpUA;						// Einheitenfeld
	Unit [] BspU;
	Operations op;									// Methodensammlung
	Message msg;									// Fehlerausgabe

/*********** - Layoutaufbau im Constructor und spezifische Starteinstellungen - ***********/	

	public SetFrame (boolean IsServer, ObjectOutputStream Out, Unit [] ConUnitArray, Unit [] ConBspU, int anzUnitFiles)
	{
		UnitArray 			= ConUnitArray;
		this.IsServer		= IsServer;
		this.Out 			= Out;
		BspU				= ConBspU;
		this.anzUnitFiles 	= anzUnitFiles;
		
	//******---------******\\
		JLA 	= new JLabel [6];
		lLeftU 	= new JLabel [anzUnitFiles];
		lLeftUV = new JLabel [anzUnitFiles];
		op 		= new Operations ();	
		
	//******graphische Instanzierung******\\
		for (int j0 = 0; j0 < 6; j0++)
		{	JLA [j0] 		= new JLabel ("");}
		for (int j = 0; j < anzUnitFiles; j++)
		{
			lLeftU [j] 		= new JLabel (BspU [j].type + " (" + BspU [j].DamType + "):");
			lLeftUV [j] 	= new JLabel ("--");
		}
		iHeading 	= new ImageIcon ("Pics/linasHeader.gif");		JLA [0].setIcon (iHeading);
		JLA [1].setText ("Spieler:");
		JLA [3].setText ("Einheit:");
		JLA [5].setText ("Verfügbare Einheiten:");
		bEndGame 	= new JButton ("Spiel beenden");
		bNext 		= new JButton ("Weiter");						bReady 	= new JButton ("");
		Brett 		= new Spielfeld ();								msg 	= new Message ();
		
	//******Koordinaten der Objekte******\\
		JLA [0].setBounds (175, 20, 550, 65);			Brett.setBounds (10, 95, 580, 580);
		JLA [1].setBounds (600, 95, 120, 20);			JLA [2].setBounds (750, 95, 125, 20);
		JLA [3].setBounds (600, 130, 120, 15);			JLA [4].setBounds (750, 130, 125, 15);
		JLA [5].setBounds (600, 180, 150, 15);
		int y = 210;
		for (int k = 0; k < anzUnitFiles; k++)
		{
			lLeftU [k].setBounds (600, y, 140, 15);
			lLeftUV [k].setBounds (780, y, 75, 15);
			y += 20;
		}
		bNext.setBounds (600, 480, 135, 25);			bReady.setBounds (750, 480, 135, 25);
		bEndGame.setBounds (675, 645, 140, 25);
		
	//******---------******\\
		Brett.setSize (580, 580);						Brett.addMouseListener (this);
		this.setTitle ("Linas Einheiten setzen");		this.setSize (new Dimension (900, 728));
		contentPane = (JPanel) this.getContentPane ();	contentPane.setLayout (null);
		contentPane.setBackground (new Color (213, 222, 242));
		bNext.setFocusable (false);						bNext.setForeground (C);
		bNext.setFont (F);
		bReady.setFocusable (false);					bReady.setForeground (C);
		bReady.setFont (F);
		bEndGame.setFocusable (false);					bEndGame.setForeground (CExit);
		bEndGame.setFont (F);

		for (int k1 = 0; k1 < 6; k1++)
		{
			JLA [k1].setForeground (C);
			JLA [k1].setFont (F);
			contentPane.add (JLA [k1]);
		}
		for (int k2 = 0; k2 < anzUnitFiles; k2++)
		{
			lLeftU [k2].setForeground (C);
			lLeftU [k2].setFont (F);
			contentPane.add (lLeftU [k2]);
			lLeftUV [k2].setForeground (C);
			lLeftUV [k2].setFont (F);
			contentPane.add (lLeftUV [k2]);
		}
		JLA [1].setForeground (cBlue);				JLA [1].setFont (fPlayer);
		JLA [2].setForeground (cBlue);				JLA [2].setFont (fPlayer);
		bEndGame.setForeground (Color.red);
	
	//******Hinzufügen der Objekte******\\
		contentPane.add (Brett);					contentPane.add (bReady);
		contentPane.add (bNext);					contentPane.add (bEndGame);

		bNext.addActionListener (this);
		bReady.addActionListener (this);
		bReady.setEnabled (false);
		bEndGame.addActionListener (this);
		
		Player 		= op.player (UnitArray);
		P1UnitNum 	= op.UnitNum (UnitArray, 1);
		P2UnitNum 	= op.UnitNum (UnitArray, 2);
		
		Brett.setUnitNum (P1UnitNum, P2UnitNum, false, IsServer, BspU[0].ArmourType);		// wichtige Variablen für Spielfeld
		Brett.setPics (UnitArray);						// Bilder der Einheiten in Spielfeld setzen
		Brett.grabFocus ();
		
	//******Spezifische Anfangseinstellungen******\\
		if (!IsServer)						// nur für Client
		{	
			UC = 0;
			JLA [4].setText (UnitArray [UC].type + " (" + UnitArray [UC].DamType + ")");
			Brett.newBoard (1);							// spezifische erlaubte Felder, auf die er setzen kann
			bReady.setLabel ("Fertig");
			JLA [2].setText (Player [0]);				// (seine Einheiten gehen im Feld von 0 bis P1UnitNum!)
		}
		if (IsServer)						// nur für Server
		{	
			UC 		= P1UnitNum;						// (seine Einheiten gehen im Feld von P1UnitNum bis zum Schluss!)
			turn 	= false;							// nicht am Zug
			JLA [4].setText (UnitArray [P2UnitNum].type + " (" + UnitArray [UC].DamType + ")");
			Brett.newBoard (2);							// spezifische erlaubte Felder, auf die er setzen kann
			bReady.setLabel ("Warte auf Client");
			JLA [2].setText (Player [1]);
		}
		numStart ();
		
		
		//******KeyListener******\\
		this.addKeyListener (KA = new KeyAdapter ()
		{
			public void keyPressed (KeyEvent e)
			{	
				int C = e.getKeyCode ();
				if (C==107)	Next ();				
			}			
		});
	}

/*********** - Reaktion auf Buttonklicks - ***********/		
	
	public void actionPerformed (ActionEvent e)
	{
		try
		{
			Object o = e.getSource ();
			
			if (o.equals (bNext))		Next ();
			if (o.equals (bReady))		Ready ();
			if (o.equals (bEndGame)) 	EndGame ();
			
		//	Brett.setzen (UnitArray, UC);				// Einheiten auch graphisch setzen
		}										
		
		catch (NullPointerException npe)
		{	System.out.println ("Null Pointer");}
	}

/*********** - nächste Einheit auswählen - ***********/	

	public void Next ()
	{	
		int tmp = 0;									// um Endlosschleife zu verhindern
		do 
		{
			UC++;
			tmp++;
			if (!IsServer && (UC >= P1UnitNum))
			{	UC = 0;}
			if (IsServer && (UC >= (UnitArray.length)))
			{	UC = P1UnitNum;}
		}	while ((UnitArray [UC].visible == true) && (tmp < (UnitArray.length)));
														// bis Einheit gefunden, die noch nicht gesetzt wurde
		JLA [4].setText (UnitArray [UC].type + " (" + UnitArray [UC].DamType + ")");
		Brett.setzen (UnitArray, UC);					// Einheiten auch graphisch setzen
	}
	
/*********** - Setzen einer Einheit per Mausklick auf Spielfeld - ***********/
	
	public void mousePressed (MouseEvent e)				
	{
		boolean ready 	= true;							// ob alle gesetzt wurden
		int [] xy 		= new int [3];				

		if ((e.getX() < 572) && (e.getY() < 565))		// im Spielfeld
		{
			xy = op.koAngleich (e.getX(), e.getY());	// Feld mit umgerechneten Mauskoordinaten
			if ((!IsServer && (xy [2] > 16)) || (IsServer && (xy [2] < 3)))
			{											// Koordinaten innerhalb des vorgeschriebenen bereiches
				int tmpUC = op.occupied (xy [0], xy [1], UnitArray);		// ob Feld besetzt
				if ( tmpUC == -1)						// wenn Feld nicht besetzt
				{
					UnitArray [UC].PosX 	= xy [0];
					UnitArray [UC].PosY 	= xy [1];
					numMod ();
					UnitArray [UC].visible 	= true;
					Brett.setzen (UnitArray, UC);		// Einheit mit neuen Koordinaten setzen
				}
				else									// wemm Feld besetzt
				{	
					UC = tmpUC;							// anwählen der angeklickten Einheit
					JLA [4].setText (UnitArray [UC].type + " (" + UnitArray [UC].DamType + ")");
					Brett.setzen (UnitArray, UC);
				}
			}
			else										// Koordinaten nicht im richtigen Bereich
			{	msg.msgError ("Bitte auf den freien Flächen setzen.", this);}
		}
		
		ready = op.setAll (UnitArray, P1UnitNum, IsServer);	// test, ob alle Einheiten gesetzt sind
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
	
/*********** - alle Einheiten wunschgemäß verteilt - ***********/		
	
	public void Ready ()
	{
		if (!IsServer)						// nur Client
		{
			bReady.setText ("Warte auf Host...");
			bReady.setEnabled (false);
			send ("ClientSet");							// sagt, dass er alles gesetzt hat
		}
		if (IsServer)						// nur Server
		{	
			send ("ServerSet");							// sagt, dass er alles gesetzt hat
		}
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
		for (int i = 0; i < lLeftUV.length; i++)
		{	lLeftUV [i].setText ("" + num [i]);}
	}
	
/*********** - stets die gesetzte Einheit abziehen - ***********/		
	
	public void numMod ()
	{
		if (UnitArray [UC].visible == false)
		{
			for (int i = 0; i < anzUnitFiles; i++)
			{
				if ((UnitArray [UC].type + UnitArray [UC].DamType).equals (BspU [i].type + BspU [i].DamType))
				{	num [i]--;}
			}
			UnitsToSet ();
		}
	}
	
/*********** - Anfänglichen Einheiten zum setzen - ***********/		
	
	public void numStart ()
	{
		if (!IsServer)
		{
			for (int i1 = 0; i1 < anzUnitFiles; i1++)
			{	num [i1] = op.number (UnitArray, Player [0], (BspU [i1].type + BspU [i1].DamType));}
		}
		if (IsServer)
		{
			for (int i2 = 0; i2 < anzUnitFiles; i2++)
			{	num [i2] = op.number (UnitArray, Player [1], (BspU [i2].type + BspU [i2].DamType));}
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
		boolean ready = op.setAll (UnitArray, P1UnitNum, IsServer);		// Test, ob Server schon alle gesetzt hat
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
			Out.reset ();								// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject ((Object) forSending);		// in OutputStream schreiben
			Out.flush ();								// alles im OutputStram verschicken
		}
		catch (IOException e2)		{System.out.println (e2);}
	}
}