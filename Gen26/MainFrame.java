import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/*	Diese Klasse ist verantwortlich für das eigentliche Spiel. Hier wird Bewegung, Angriff und 
 *	ein eventueller Sieg gesteuert.	
 */

public class MainFrame extends JFrame implements ActionListener
{
	//Layoutkomponenten
	JPanel contentPane;
	ImageIcon iHeading;
	JLabel [] JLAInfo 		= new JLabel [10];
	JLabel [] JLAInfoV 		= new JLabel [10];
	JLabel lHeading, lEInfo;
	JButton [] JBA 			= new JButton [3];
	Spielfeld Brett;
	Color CBlue 			= new Color (41, 41, 255);
	Color C 				= Color.black;
	Color CExit 			= Color.red;
	Color CEnemy 			= new Color (255, 33, 16);
	Font FPlayer 			= new Font ("Verdana", 1, 16);
	Font F 					= new Font ("Garamond", 1, 14);
	//andere Komponenten
	boolean IsServer;							// Server oder Client
	String [] Player 		= new String [2];	// Feld mit Spielernamen
	ObjectOutputStream Out 	= null;				// Ausgabestrom
	Object [] forSending 	= new Object [2];	// zu verschickendes Objekt
	MouseAdapter MA 		= null;
	KeyAdapter KA 			= null;
	int UC 					= 0;				// (unit-counter) um zu Ermitteln, welche Einheit gerade dran ist
	int [] UnCo 			= new int [1];	
	int P1UnitNum, P2UnitNum;					// Anzahl der Einheiten beider Spieler
	int xy [] 				= new int [3];		// Feld für Koordinaten		
	int MouseX, MouseY;							// Mouskoordinaten
	boolean keyNext 		= true;				// ob "+" auf Tastatur aktiviert ist
	boolean keyEndTurn		= true;				// ob "Enter" auf Tastatur aktiviert ist
	boolean wait;								// ob Spieler gerade wartet
	boolean end 			= false;			// ob bereits ein Sieger feststeht
	Unit [] UnitArray;							// Einheitenfeld
	Operations op;								// Methodensammlung
	Message msg;								// Fehlermeldung

/*********** - Layoutaufbau im Constructor und spezifische Starteinstellungen - ***********/

	public MainFrame (boolean IsServer, ObjectOutputStream Out, Unit [] UnitArray)				
	{
		this.UnitArray 	= UnitArray;
		this.IsServer	= IsServer;
		this.Out 		= Out;

		iHeading 		= new ImageIcon (AppPaths.asset("Pics/linasHeader.gif"));	lHeading = new JLabel (iHeading);
		lEInfo 			= new JLabel (I18n.text("Selected enemy"));
		for (int l1 = 0; l1 < 10; l1++)
		{
			JLAInfo [l1] 	= new JLabel ("");
			JLAInfoV [l1] 	= new JLabel ("--");
		}
		JLAInfo [0].setText (I18n.text("Player:"));
		JLAInfo [1].setText (I18n.text("Unit:"));
		JLAInfo [2].setText (I18n.text("Hitpoints:"));
		JLAInfo [3].setText (I18n.text("Firepower:"));
		JLAInfo [4].setText (I18n.text("Range:"));
		JLAInfo [5].setText (I18n.text("Action points:"));
		JLAInfo [6].setText (I18n.text("Points/move"));
		JLAInfo [7].setText (I18n.text("Points/shot"));
		JLAInfo [8].setText (I18n.text("Unit:"));
		JLAInfo [9].setText (I18n.text("HP:"));
		
		JBA [0] = new JButton (I18n.text("Next"));						JBA [1] = new JButton ("--");
		JBA [2] = new JButton (I18n.text("End game"));				msg = new Message ();
		op = new Operations ();									Brett = new Spielfeld ();
		
		Brett.setSize (580, 580);			
		this.setTitle ("Linas Beta");							this.setSize (new Dimension (900, 728));	// frame-size (width/height)
		contentPane = (JPanel) this.getContentPane ();			contentPane.setLayout (null);
		contentPane.setBackground (new Color (213, 222, 242));
		
		lHeading.setBounds (175, 20, 550, 65);					Brett.setBounds (10, 95, 580, 580);
		contentPane.add (lHeading);								contentPane.add (Brett);
		JLAInfo [0].setBounds (600, 95, 120, 20);				JLAInfoV [0].setBounds (750, 95, 100, 20);
		JLAInfo [0].setForeground (CBlue);						JLAInfoV [0].setForeground (CBlue);
		JLAInfo [0].setFont (FPlayer);							JLAInfoV [0].setFont (FPlayer);
		contentPane.add (JLAInfo [0]);							contentPane.add (JLAInfoV [0]);
		int y = 130;
		for (int l2 = 1; l2 < 10; l2++)
		{
			JLAInfo [l2].setBounds (600, y, 120, 15);
			JLAInfo [l2].setForeground (C);
			JLAInfo [l2].setFont (F);
			contentPane.add (JLAInfo [l2]);
			JLAInfoV [l2].setBounds (750, y, 75, 15);
			JLAInfoV [l2].setForeground (C);
			JLAInfoV [l2].setFont (F);
			contentPane.add (JLAInfoV [l2]);
			y += 20;
			if (l2 == 4)	{y += 5;}
			if (l2 == 7)	{y += 70;}
		}
		lEInfo.setBounds (600, 320, 200, 20);
		lEInfo.setForeground (CEnemy);
		lEInfo.setFont (F);
		contentPane.add (lEInfo);
		JBA [0].setBounds (600, 480, 125, 30);					JBA [1].setBounds (750, 480, 125, 30);
		JBA [2].setBounds (675, 645, 125, 30);
		for (int b1 = 0; b1 < 3; b1++)
		{
			JBA [b1].setForeground (C);
			JBA [b1].setFont (F);
			JBA [b1].setFocusable (false);
			contentPane.add (JBA [b1]);
			JBA [b1].addActionListener (this);
		}
		JBA [2].setForeground (CExit);
		
		Brett.addMouseListener (MA = new MouseAdapter ()
		{
			public void mouseClicked (MouseEvent e)
			{	
				if ((e.getButton () == 3) && (!wait))			// rechte Maustaste = Schuss
				{	rightButton (e);}
				if ((e.getButton () == 1) && (!wait))			// linke Maustaste = anwählen
				{	leftButton (e);}
			}
		});
		
        KeyBindings.install(getRootPane(), false, command -> {
            if (!wait) move(command);
        });

		Player = op.player (UnitArray);
		P1UnitNum = op.UnitNum (UnitArray, 1);
		P2UnitNum = op.UnitNum (UnitArray, 2);			
		
		if (!IsServer)								// nur für Client
		{	
			JLAInfoV [0].setText (Player [0]);
			JBA [1].setLabel (I18n.text("End turn"));
			UC = 0;
			newInfo ();											// Informationen zur jeweiligen Einheit
			wait = false;
		}
		if (IsServer)								// nur für Server
		{	
			JBA [0].setEnabled (false);
			JBA [1].setEnabled (false);
			JBA [1].setLabel (I18n.text("Waiting for client..."));
			JLAInfoV [0].setText (Player [1]);
			keyNext 	= false;
			keyEndTurn 	= false;
			UC 			= -1;									// damit keine Einheit angewählt ist
			wait 		= true;
		}

		UnitArray = op.UnitVisibility (UnitArray, P1UnitNum, IsServer);						// Sichtbaren Einheiten für jeden Spieler
		Brett.setUnitNum (P1UnitNum, P2UnitNum, true, IsServer, UnitArray [0].ArmourType);	// wichtige Variablen für Spielfeld
		Brett.setPics (UnitArray);														// Bilder geladen für Spielfeld
		Brett.setzen (UnitArray, UC);
		Brett.grabFocus ();
	}					

/*********** - Reaktion auf Buttonklicks - ***********/	

	public void actionPerformed (ActionEvent e)
	{
		if (!wait)
		{
			try
			{
				Object o = e.getSource ();
				if (o.equals (JBA [0]))		Next ();
				if (o.equals (JBA [1]))		EndTurn ();
				if (o.equals (JBA [2]))		EndGame ();
				newInfo ();										// neue Info der aktuellen Einheit
				Brett.setzen (UnitArray, UC);
			}

			catch (NullPointerException npe)
			{	System.out.println ("Null Pointer");}
		}
	}

/*********** - Anwählen der nächsten Einheit - ***********/	

	public void Next ()
	{
		int tmp = 0;
		do														// bis eine "lebende", noch bewegbare Einheit gefunden ist
		{
			tmp++;												// für Abbruch falls nicht
			UC++;
			if (!IsServer&&(UC >= P1UnitNum))
			{	UC = 0;}
			if (IsServer&&(UC >= UnitArray.length))
			{	UC = P1UnitNum;}
		}	while (((UnitArray [UC].TP <= 0) || (!op.ZPLeft (UnitArray, UC))) && (tmp <= UnitArray.length));
		if (tmp > P1UnitNum)
		{														// Anwählen der Einheit mit dem geringsten Feldplatz, aber lebend
			if (!IsServer)
			{
				for (int i1 = (P1UnitNum - 1); i1 >= 0; i1--)
				{	if (UnitArray [i1].TP > 0)	UC = i1;}
			}
			if (IsServer)
			{
				for (int i1 = (UnitArray.length - 1); i1 >= P1UnitNum; i1--)
				{	if (UnitArray [i1].TP > 0)	UC = i1;}
			}
			JBA [0].setEnabled (false);
			keyNext = false;
		}
	}
	
/*********** - Runde beendet - ***********/	

	public void EndTurn ()
	{
		send ("EndTurn");										// dies dem anderen Spieler mitteilen
		if (!IsServer)		JBA [1].setLabel (I18n.text("Waiting for server"));
		if (IsServer)		JBA [1].setLabel (I18n.text("Waiting for client"));
		UC 			= -1;										// keine Einheit angewählt lassen
		JBA [0].setEnabled (false);
		JBA [1].setEnabled (false);
		keyNext 	= false;
		keyEndTurn 	= false;
		newEnemyInfo (-1);										// kein Gegner angewählt
		wait 		= true;
		Brett.setzen (UnitArray, UC);							// noch mal graphische Darstellung dafür, dass nichts angewählt ist
		newInfo ();												// keine Info
	}
	
/*********** - Runde beginnen - ***********/	

	public void startTurn (Unit [] UA)
	{
		if (!IsServer)		UC = 0;
		if (IsServer)		UC = P1UnitNum;
		while ((UnitArray [UC].TP <= 0) && (UC <= UnitArray.length))		// Auswählen der nächsten lebenden Einheit
		{	UC++;}
				
		JBA [1].setLabel (I18n.text("End turn"));
		UnitArray 	= UA;
		wait 		= false;
		JBA [0].setEnabled (true);
		JBA [1].setEnabled (true);
		newEnemyInfo (-1);										// kein Gegner angewählt
		UnitArray 	= op.newRound (UnitArray);					// neue Zugpunkte vergeben
		UnitArray 	= op.UnitVisibility (UnitArray, P1UnitNum, IsServer);			// Sichtbarkeit setzen
		Brett.setzen (UnitArray, UC);							// alles zeichnen
		newInfo ();												// neue Info
		keyNext 	= true;											// "+" wieder betätigbar
		keyEndTurn 	= true;										// "Enter" wieder betätigbar
	}

/*********** - Spiel beenden - ***********/

	public void EndGame ()
	{	
		if (!end)	send ("runAway");
		else		send ("quit");
	}

/*********** - Schießen - ***********/

	public void rightButton (MouseEvent e)
	{
		int distance, damage, tmpAD, tmpUC, rwMod;
		int [] ADVictims = new int [UnitArray.length];
				
		xy = op.koAngleich (e.getX (), e.getY ());				// modifizierte Mauskoordinaten
		tmpUC = op.occupied (xy [0], xy [1], UnitArray);		// gucken, wer getroffen würde
		distance = op.distance (xy [0], xy [1], UnitArray, UC);	// Distanz zum Ziel
		rwMod = op.rwMod (distance, UnitArray, UC);

		if (rwMod != 0)											// Distanz muss in Reichweite sein
		{
			if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP[2])	// Es müssen noch genug Zugpunkte vorhanden sein
			{
				damage = op.damage (distance, rwMod, UnitArray, UC);	// Schadensberechnung
				System.out.println ("Damage: " + damage);
				
				UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[2];			// Abziehen der Zugpunkte
									
				if ((tmpUC != -1) && (UnitArray [tmpUC].TP > 0))		// Ziel muss vorhanden sein und noch Trefferpunkte besitzen	
				{
					UnitArray [tmpUC].TP -= op.ArmourMod (UnitArray, damage, UC, tmpUC);	// Schaden (noch mal durch Rüstung modifiziert) wird von Trefferpunkten abgezogen					
					UnitArray = op.DamMod (UnitArray, tmpUC, P1UnitNum);					// dadurch mögliche Veränderungen werden berechnet
				}
					
				if (UnitArray [UC].RW[2] != 0)					// Wenn es eine Einheit mit Gebietsschaden ist (z.B. Artillerie)
				{	
					ADVictims = op.AreaDamageMod (UnitArray, UC, xy [0], xy [1]);	// Opfer des Gebietsschadens werden berechnet

					for (int i = 0; i < UnitArray.length; i++)
					{
						if ((ADVictims [i] != 0) && (UnitArray [i].TP > 0))			// Ausmaß des Gebietsschadens berechnet
						{
							tmpAD 				 = (int) (damage * Math.pow (UnitArray [UC].AreaDamageMod, ADVictims [i]));	// Gebietsschaden berechnet
							UnitArray [i].TP 	-= op.ArmourMod (UnitArray, tmpAD, UC, i);;	// Schaden von TP abgezogen
							UnitArray 			 = op.DamMod (UnitArray, i, P1UnitNum);		// dadurch mögliche Veränderungen werden berechnet
						}
					}
				}
			
				UnitArray = op.UnitVisibility (UnitArray, P1UnitNum, IsServer);	// erneut Sicht berechnet (zerstörte Einheiten sehen nichts mehr)			
				newInfo ();
				newEnemyInfo (tmpUC);
				Brett.setPics (UnitArray);
				send ("Move");							// Feld an anderen Spieler senden
				Brett.setzen (UnitArray, UC);			// graphische realisierung
				flame (xy [0], xy [1]);					// Flamme setzen
				send ("EnemyFire");
				send ("Flame");							// Flamme bei anderem Spieler setzen
				lastDestroyed ();						// testen ob aktueller Spieler gewonnen hat
			}
		}
	}
	
/*********** - Auswählen - ***********/
	
	public void leftButton (MouseEvent e)
	{
		xy = op.koAngleich (e.getX (), e.getY ());					// Koordinaten modifizieren
		int tmpUC = op.occupied (xy [0], xy [1], UnitArray);		// test, ob Feld besetzt ist

		if ((tmpUC != -1) && (UnitArray [tmpUC].visible == true) && (UnitArray [tmpUC].TP > 0))		// wenn vorhanden, sichtbar, lebend
		{
			if ((!IsServer && (tmpUC < P1UnitNum)) || (IsServer && (tmpUC >= P1UnitNum)))
			{														// eigene Einheit angeklickt
				if (UnitArray [tmpUC].TP > -1)
				{
					UC = tmpUC;							// --> angewählt
					newInfo ();
				}
			}
			else
			{	newEnemyInfo (tmpUC);}					// feindliche Einheit angeklickt und angewählt
			send ("Move");
			Brett.setzen (UnitArray, UC);				// graphische Realisierung
		}
	}

/*********** - Bewegung, realisiert durch Tastatur und I18n.text("Next") durch "+"- ***********/

	public void move (int keyCode)
	{
		int X, Y;										// Koordinaten
		switch (keyCode)
		{
			case 97:	
					X = UnitArray [UC].PosX - 22;
					Y = UnitArray [UC].PosY + 14;
					  
			// Num 1
					if ((UnitArray [UC].PosX < 5) | (UnitArray [UC].PosY > 540) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{	
							UnitArray [UC].PosX = X;
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;

			case 98:	X = UnitArray [UC].PosX;
					Y = UnitArray [UC].PosY + 28;
			// Num 2
					if ((UnitArray [UC].PosY > 520) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{ 	
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;

			case 99:	X = UnitArray [UC].PosX + 22;
					Y = UnitArray [UC].PosY + 14;
			// Num 3
					if ((UnitArray [UC].PosX > 530) | (UnitArray [UC].PosY > 540) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{
							UnitArray [UC].PosX = X;
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;

			case 100:	X = UnitArray [UC].PosX - 22;
					Y = UnitArray [UC].PosY - 14;
			// Num 4
					if ((UnitArray [UC].PosX < 5) | (UnitArray [UC].PosY < 10) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{
							UnitArray [UC].PosX = X;
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;

			case 101:	X = UnitArray [UC].PosX;
					Y = UnitArray [UC].PosY - 28;
			// Num 5
					if ((UnitArray [UC].PosY < 20) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{	
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;

			case 102:	X = UnitArray [UC].PosX + 22;
					Y = UnitArray [UC].PosY - 14;
			// Num 6
					if ((UnitArray [UC].PosX > 530) | (UnitArray [UC].PosY < 10) | (op.occupied (X, Y, UnitArray) != -1))
					{}		// im Feld und nicht besetzt
					else
					{	
						if (UnitArray [UC].ZP[0] >= UnitArray [UC].ZP [1])
						{
							UnitArray [UC].PosX = X;
							UnitArray [UC].PosY = Y;
							UnitArray [UC].ZP[0] -= UnitArray [UC].ZP[1];
						}
					}
					break;
					
			case 107:	if (keyNext) {Next ();}		break;	
			// Plus			--> nächste Einheit
			
			case 10:	if (keyEndTurn) {EndTurn ();}	break;
			// Enter		--> Zug beenden
		}
		UnitArray = op.UnitVisibility (UnitArray, P1UnitNum, IsServer);	// Sichtbarkeit
		newInfo ();							// neue Info
		send ("Move");						// Bewegung dem Mitspieler senden
		Brett.setzen (UnitArray, UC);		// selbst Bewegung realisiern
	}
	
/*********** - Neuzeichnen der Einheiten für inaktiven Spieler - ***********/

	public void refresh (Unit [] UA)
	{
		UnitArray = UA;
		UnitArray = op.UnitVisibility (UnitArray, P1UnitNum, IsServer);
		newEnemyInfo (-1);					// kein Feind angewählt
		Brett.setPics (UnitArray);
		Brett.setzen (UnitArray, UC);
	}
	
/*********** - Flamme setzen - ***********/

	public void flame (int X, int Y)
	{
		if (wait)
		{
			if (op.PointVisibility (UnitArray, P1UnitNum, IsServer, X, Y, 0))
			{	Brett.setFlame (X, Y);}
		}
		else	Brett.setFlame (X, Y);
	}
	
/*********** - Informationen über aktuelle Einheit - ***********/
	
	public void newInfo ()
	{
		if (!wait)
		{
			JLAInfoV [1].setText (UnitArray [UC].getDisplayName() + " (" + UnitArray [UC].DamType + ")");
			JLAInfoV [2].setText ("" + UnitArray [UC].TP);
			JLAInfoV [3].setText ("" + UnitArray [UC].Dam);
			JLAInfoV [4].setText (UnitArray [UC].RW[0] + " - " + UnitArray [UC].RW[1]);
			JLAInfoV [5].setText ("" + UnitArray [UC].ZP[0]);
			JLAInfoV [6].setText ("" + UnitArray [UC].ZP[1]);
			JLAInfoV [7].setText ("" + UnitArray [UC].ZP[2]);
		}
		else		// nicht am Zug --> keine aktuelle Einheit
		{
			for (int l3 = 1; l3 < 8; l3++)
			{	JLAInfoV [l3].setText ("--");} 
		}
	}
	
/*********** - Informationen über feindliche angewählte Einheit - ***********/
	
	public void newEnemyInfo (int tmpUC)
	{
		for (int j = 0; j < UnitArray.length; j++)	// ersteinmal alle auf nichtausgewählt stellen
		{	UnitArray [j].ESelected = false;}
		
		if (tmpUC == -1)							// keiner ausgewählt
		{
			lEInfo.setText (I18n.text("Selected enemy"));
			JLAInfoV [8].setText ("--");
			JLAInfoV [9].setText ("--");
		}
		
		else										// ausgewählter Gegner
		{
			if (tmpUC < P1UnitNum)
			{	lEInfo.setText (I18n.text("Selected enemy (") + Player [0] + ")");}
			else
			{	lEInfo.setText (I18n.text("Selected enemy (") + Player [1] + ")");}
			JLAInfoV [8].setText ("" + UnitArray [tmpUC].getDisplayName());
			JLAInfoV [9].setText ("" + UnitArray [tmpUC].TP);
			UnitArray [tmpUC].ESelected = true;
		}
	}
	
/*********** - Programmende, wenn das Fenster geschlossen wird - ***********/
	
	protected void processWindowEvent (WindowEvent e)
	{
		super.processWindowEvent (e);
		if (e.getID () == WindowEvent.WINDOW_CLOSING)
		{	System.exit (0);}							
	}
	
/*********** - Wenn Gewinner feststeht - ***********/
	
	public void sieg (boolean won)
	{
		end 		= true;							// damit man normal beenden kann (ohne "runAway" zu senden)
		JBA [0].setEnabled (false);
		JBA [1].setEnabled (false);
		this.removeMouseListener (MA);
		this.removeKeyListener (KA);
		UnitArray 	= op.seeAll (UnitArray);
		UnitArray 	= op.UnitVisibility (UnitArray, P1UnitNum, IsServer);
		Brett.setzen (UnitArray, UC);
		if (won)
		{	
			send ("won");							// dem anderen Spieler dies mitteilen
			msg.msgInform (I18n.text("You are victorious!"), this);		// und natürlich dem Gewinner!
		}
		if (!won)
		{	msg.msgInform (I18n.text("You have been defeated!"), this);}
	}
	
/*********** - testen, ob man die letzte feindliche Einheit zerstört hat - ***********/
	
	public void lastDestroyed ()
	{
		boolean lD = true;
		if (!IsServer)
		{
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{	if (UnitArray [i2].TP > 0)	lD = false;}
		}
		
		if (IsServer)
		{
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{	if (UnitArray [i1].TP > 0)	lD = false;}
		}
		
		if (lD)		sieg (true);					// wenn, dann Sieg verkünden (s.o.)
	}
	
/*********** - Senden des Befehles und des Feldes - ***********/	
	
	public void send (String Command)
	{
		UnCo [0] = UC;
		forSending [0] = (Object) Command;
		forSending [1] = (Object) UnitArray;
		
		if (Command.equals ("Flame"))		forSending [1] = (Object) xy;
		if (Command.equals ("EnemyFire"))	forSending [1] = (Object) UnCo;
					
		try
		{	
			Out.reset ();							// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject ((Object) forSending);	// in OutputStream schreiben
			Out.flush ();							// alles im OutputStram verschicken
		}
		catch (IOException e2)		{System.out.println (e2);}
	}
}