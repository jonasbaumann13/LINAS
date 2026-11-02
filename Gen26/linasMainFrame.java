import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/*	Diese Klasse ist verantwortlich für das eigentliche Spiel. Hier wird Bewegung, Angriff und 
 *	ein eventueller Sieg gesteuert.	
 */

public class linasMainFrame extends JFrame implements ActionListener, KeyListener, MouseListener
{
	//Layoutkomponenten
	JPanel contentPane;
	ImageIcon iHeading;
	JLabel lHeading, lInfoPlayer, lInfoUnit, lInfoTP, lInfoZP, lInfoZPpF, lInfoZPpS, lInfoDamage, lInfoRW;
	JLabel lInfoPlayerV, lInfoUnitV, lInfoTPV, lInfoZPV, lInfoZPpFV, lInfoZPpSV, lInfoDamageV, lInfoRWV;
	JLabel lEInfoUnit, lEInfoTP, lEInfo;
	JLabel lEInfoUnitV, lEInfoTPV;
	JButton bNext, bEndTurn, bEndGame;
	linasSpielfeld linasBrett;
	Color cBlue = new Color (41, 41, 255);
	Font fPlayer = new Font ("Verdana", 1, 14);
	//andere Komponenten
	String who;					// Server oder Client
	String [] Player = new String [2];		// Feld mit Spielernamen
	ObjectOutputStream Out = null;			// Ausgabestrom
	Object [] forSending = new Object [2];		// zu verschickendes Objekt
	int UC = 0;					// (unit-counter) um zu Ermitteln, welche Einheit gerade dran ist	
	int P1UnitNum, P2UnitNum;			// Anzahl der Einheiten beider Spieler
	int xy [] = new int [3];			// Feld für Koordinaten		
	int MouseX, MouseY;				// Mouskoordinaten
	boolean keyNext = true;				// ob "+" auf Tastatur aktiviert ist
	boolean wait;					// ob Spieler gerade wartet
	boolean end = false;				// ob bereits ein Sieger feststeht
	Unit [] UnitArray;				// Einheitenfeld
	Operations op;					// Methodensammlung
	Message msg;					// Fehlermeldung

/*********** - Layoutaufbau im Constructor und spezifische Starteinstellungen - ***********/

	public linasMainFrame (String who, ObjectOutputStream Out, Unit [] UnitArray)				
	{
		this.UnitArray = UnitArray;
		this.who = who;
		this.Out = Out;
		System.out.println (who);

		iHeading = new ImageIcon (linasMainFrame.class.getResource ("/linasHeader.gif"));	lHeading = new JLabel (iHeading);
		lInfoPlayer = new JLabel ("Spieler:");		lInfoPlayerV = new JLabel ("--");
		lInfoUnit = new JLabel ("Einheit:");		lInfoUnitV = new JLabel ("--");
		lInfoTP = new JLabel ("Trefferpunkte:");	lInfoTPV = new JLabel ("--");
		lInfoZP = new JLabel ("Zugpunkte:");		lInfoZPV = new JLabel ("--");
		lInfoZPpF = new JLabel ("Zugpunkte/Feld");	lInfoZPpFV = new JLabel ("--");
		lInfoZPpS = new JLabel ("Zugpunkte/Schuss");	lInfoZPpSV = new JLabel ("--");
		lInfoDamage = new JLabel ("Feuerkraft:");	lInfoDamageV = new JLabel ("--");
		lInfoRW = new JLabel ("Reichweite:");		lInfoRWV = new JLabel ("--");
		lEInfo = new JLabel ("Markierter Gegner");
		lEInfoUnit = new JLabel ("Einheit:");		lEInfoUnitV = new JLabel ("--");
		lEInfoTP = new JLabel ("TP:");			lEInfoTPV = new JLabel ("--");
		bNext = new JButton ("Weiter");			bEndTurn = new JButton ("--");
		bEndGame = new JButton ("Spiel beenden");	msg = new Message ();
		op = new Operations ();				linasBrett = new linasSpielfeld ();
		
		linasBrett.setSize (580, 580);			linasBrett.addMouseListener (this);
		this.addKeyListener (this);
		this.setTitle ("Linas Beta");			this.setSize (new Dimension (900, 728));		// frame-size (width/height)
		contentPane = (JPanel) this.getContentPane ();	contentPane.setLayout (null);
		
		lHeading.setBounds (175, 20, 550, 65);		linasBrett.setBounds (10, 95, 580, 580);
		lInfoPlayer.setBounds (600, 95, 120, 20);	lInfoPlayerV.setBounds (750, 95, 75, 20);
		lInfoUnit.setBounds (600, 130, 120, 15);	lInfoUnitV.setBounds (750, 130, 75, 15);
		lInfoTP.setBounds (600, 150, 120, 15);		lInfoTPV.setBounds (750, 150, 75, 15);
		lInfoDamage.setBounds (600, 170, 120, 15);	lInfoDamageV.setBounds (750, 170, 75, 15);
		lInfoRW.setBounds (600, 190, 120, 15);		lInfoRWV.setBounds (750, 190, 75, 15);
		lInfoZP.setBounds (600, 215, 120, 15);		lInfoZPV.setBounds (750, 215, 75, 15);
		lInfoZPpF.setBounds (600, 235, 120, 15);	lInfoZPpFV.setBounds (750, 235, 75, 15);
		lInfoZPpS.setBounds (600, 255, 120, 15);	lInfoZPpSV.setBounds (750, 255, 75, 15);
		lEInfo.setBounds (600, 300, 200, 20);
		lEInfoUnit.setBounds (600, 325, 120, 15);	lEInfoUnitV.setBounds (750, 325, 75, 15);
		lEInfoTP.setBounds (600, 345, 120, 15);		lEInfoTPV.setBounds (750, 345, 75, 15);
		bNext.setBounds (600, 480, 125, 30);		bEndTurn.setBounds (750, 480, 125, 30);
		bEndGame.setBounds (675, 645, 125, 30);

		lInfoPlayer.setForeground (cBlue);		lInfoPlayer.setFont (fPlayer);
		lInfoPlayerV.setForeground (cBlue);		lInfoPlayerV.setFont (fPlayer);
		bEndGame.setForeground (Color.red);

		contentPane.add (lHeading);			contentPane.add (linasBrett);
		contentPane.add (bNext);			contentPane.add (bEndTurn);
		contentPane.add (lInfoPlayer);			contentPane.add (lInfoPlayerV);
		contentPane.add (lInfoUnit);			contentPane.add (lInfoUnitV);
		contentPane.add (lInfoTP);			contentPane.add (lInfoTPV);
		contentPane.add (lInfoZP);			contentPane.add (lInfoZPV);
		contentPane.add (lInfoZPpF);			contentPane.add (lInfoZPpFV);
		contentPane.add (lInfoZPpS);			contentPane.add (lInfoZPpSV);
		contentPane.add (lInfoDamage);			contentPane.add (lInfoDamageV);
		contentPane.add (lInfoRW);			contentPane.add (lInfoRWV);
		contentPane.add (lEInfo);
		contentPane.add (lEInfoUnit);			contentPane.add (lEInfoUnitV);
		contentPane.add (lEInfoTP);			contentPane.add (lEInfoTPV);
		contentPane.add (bEndGame);

		bNext.addActionListener (this);
		bEndTurn.addActionListener (this);
		bEndGame.addActionListener (this);

		Player = op.player (UnitArray);
		P1UnitNum = op.UnitNum (UnitArray, 1);
		P2UnitNum = op.UnitNum (UnitArray, 2);			
		
		if (who.equals ("client"))					// nur für Client
		{	
			lInfoPlayerV.setText (Player [0]);
			bEndTurn.setLabel ("Zug beenden");
			UC = 0;
			newInfo ();						// Informationen zur jeweiligen Einheit
			wait = false;
		}
		if (who.equals ("server"))					// nur für Server
		{	
			lInfoPlayerV.setText (Player [1]);
			bEndTurn.setLabel ("Warte auf Client...");
			UC = -1;						// damit keine Einheit angewählt ist
			bEndTurn.setEnabled (false);
			bNext.setEnabled (false);
			wait = true;
		}

		UnitArray = op.visible (UnitArray, P1UnitNum, who);		// Sichtbaren Einheiten für jeden Spieler
		linasBrett.setUnitNum (P1UnitNum, P2UnitNum);			// wichtige Variablen für Spielfeld
		linasBrett.setPics (UnitArray);					// Bilder geladen für Spielfeld
		linasBrett.setzen (UnitArray, UC);
	}					

/*********** - Reaktion auf Buttonklicks - ***********/	

	public void actionPerformed (ActionEvent e)
	{
		if (!wait)
		{
			try
			{
				Object o = e.getSource ();
				if (o.equals (bNext))		Next ();
				if (o.equals (bEndTurn))	EndTurn ();
				if (o.equals (bEndGame))	EndGame ();
				newInfo ();					// neue Info der aktuellen Einheit
				linasBrett.setzen (UnitArray, UC);
			}

			catch (NullPointerException npe)
			{	System.out.println ("Null Pointer");}
		}
	}

/*********** - Anwählen der nächsten Einheit - ***********/	

	public void Next ()
	{
		int tmp = 0;
		do				// bis eine "lebende", noch bewegbare Einheit gefunden ist
		{
			tmp++;			// für Abbruch falls nicht
			UC++;
			if ((who.equals("client"))&&(UC >= P1UnitNum))
			{	UC = 0;}
			if ((who.equals("server"))&&(UC >= UnitArray.length))
			{	UC = P1UnitNum;}
		}	while (((UnitArray [UC].TP <= 0) || (!op.ZPLeft (UnitArray, UC))) && (tmp <= UnitArray.length));
		if (tmp > P1UnitNum)
		{				// Anwählen der Einheit mit dem geringsten Feldplatz, aber lebend
			if (who.equals ("client"))
			{
				for (int i1 = (P1UnitNum - 1); i1 >= 0; i1--)
				{	if (UnitArray [i1].TP > 0)	UC = i1;}
			}
			if (who.equals ("server"))
			{
				for (int i1 = (UnitArray.length - 1); i1 >= P1UnitNum; i1--)
				{	if (UnitArray [i1].TP > 0)	UC = i1;}
			}
			bNext.setEnabled (false);
			keyNext = false;
		}
	}
	
/*********** - Runde beendet - ***********/	

	public void EndTurn ()
	{
		send ("EndTurn");					// dies dem anderen Spieler mitteilen
		if (who.equals ("client"))	bEndTurn.setLabel ("Warte auf Server...");
		if (who.equals ("server"))	bEndTurn.setLabel ("Warte auf Client...");
		UC = -1;						// keine Einheit angewählt lassen
		bEndTurn.setEnabled (false);
		bNext.setEnabled (false);
		newEnemyInfo (-1);					// kein Gegner angewählt
		wait = true;
		linasBrett.setzen (UnitArray, UC);			// noch mal graphische Darstellung dafür, dass nichts angewählt ist
		newInfo ();						// keine Info
	}
	
/*********** - Runde beginnen - ***********/	

	public void startTurn (Unit [] UA)
	{
		if (who.equals ("client"))	UC = 0;
		if (who.equals ("server"))	UC = P1UnitNum;
		while ((UnitArray [UC].TP <= 0) && (UC <= UnitArray.length))		// Auswählen der nächsten lebenden Einheit
		{	UC++;}
				
		bEndTurn.setLabel ("Zug beenden");
		UnitArray = UA;
		wait = false;
		bEndTurn.setEnabled (true);
		bNext.setEnabled (true);
		newEnemyInfo (-1);					// kein Gegner angewählt
		UnitArray = op.newRound (UnitArray);			// neue Zugpunkte vergeben
		UnitArray = op.visible (UnitArray, P1UnitNum, who);	// Sichtbarkeit setzen
		linasBrett.setzen (UnitArray, UC);			// alles zeichnen
		newInfo ();						// neue Info
		keyNext = true;						// "+" wieder betätigbar
	}

/*********** - Spiel beenden - ***********/

	public void EndGame ()
	{	
		if (!end)	send ("runAway");
		else		send ("quit");
	}

/*********** - Auswählen und Schießen - ***********/

	public void mousePressed (MouseEvent e)
	{
		if ((e.getModifiers () == 4) && (!wait))			// rechte Maustaste = Schuss	
		{
			int distance, damage, tmpAD, tmpUC;
			int [] ADVictims = new int [UnitArray.length];
				
			xy = op.koAngleich (e.getX (), e.getY ());			// modifizierte Mauskoordinaten
			tmpUC = op.occupied (xy [0], xy [1], UnitArray);		// gucken, wer getroffen würde
			distance = op.distance (xy [0], xy [1], UnitArray, UC);		// Distanz zum Ziel

			if ((distance >= UnitArray [UC].minRW) && (distance <= UnitArray [UC].RW))		// Distanz muss in Reichweite sein
			{
				if (UnitArray [UC].ZP >= UnitArray [UC].ZPpS)				// Es müssen noch genug Zugpunkte vorhanden sein
				{
					damage = op.damage (distance, UnitArray, UC);			// Schadensberechnung
					UnitArray [UC].ZP -= UnitArray [UC].ZPpS;			// Abziehen der Zugpunkte
										
					if ((tmpUC != -1) && (UnitArray [tmpUC].TP > 0))		// Ziel muss vorhanden sein und noch Trefferpunkte besitzen	
					{
						UnitArray [tmpUC].TP -= damage;				// Schaden wird von Trefferpunkten abgezogen
						UnitArray = op.DamMod (UnitArray, tmpUC);		// dadurch mögliche Veränderungen werden berechnet
					}
					
					if (UnitArray [UC].AreaDamageRW != 0)				// Wenn es eine Einheit mit Gebietsschaden ist (z.B. Artillerie)
					{	
						ADVictims = op.AreaDamageMod (UnitArray, UC, xy [0], xy [1]);	// Opfer des Gebietsschadens werden berechnet

						for (int i = 0; i < UnitArray.length; i++)
						{
							if ((ADVictims [i] != 0) && (UnitArray [i].TP > 0))	// Ausmaß des Gebietsschadens berechnet
							{
								tmpAD = (int) (damage * Math.pow (UnitArray [UC].AreaDamageMod, ADVictims [i]));	// Gebietsschaden berechnet
								UnitArray [i].TP -= tmpAD;			// Schaden von TP abgezogen
								UnitArray = op.DamMod (UnitArray, i);		// dadurch mögliche Veränderungen werden berechnet
							}
						}
					}
				
					UnitArray = op.visible (UnitArray, P1UnitNum, who);	// erneut Sicht berechnet (zerstörte Einheiten sehen nichts mehr)			
					newInfo ();
					newEnemyInfo (tmpUC);
					send ("Move");						// Feld an anderen Spieler senden
					linasBrett.setzen (UnitArray, UC);			// graphische realisierung
					linasBrett.setFlame (xy [0], xy [1]);			// Flamme setzen
					send ("Flame");						// Flamme bei anderem Spieler setzen
					lastDestroyed ();					// testen ob aktueller Spieler gewonnen hat
				}
			}
		}
	
		if ((e.getModifiers () == 16) && (!wait))	 		// linke Maustaste = anwählen
		{
			xy = op.koAngleich (e.getX (), e.getY ());		// Koordinaten modifizieren
			int tmpUC = op.occupied (xy [0], xy [1], UnitArray);	// test, ob Feld besetzt ist

			if ((tmpUC != -1) && (UnitArray [tmpUC].visible == true) && (UnitArray [tmpUC].TP > 0))		// wenn vorhanden, sichtbar, lebend
			{
				if (((who.equals("client")) && (tmpUC < P1UnitNum)) || ((who.equals("server")) && (tmpUC >= P1UnitNum)))
				{							// eigene Einheit angeklickt
					if (UnitArray [tmpUC].TP > -1)
					{
						UC = tmpUC;				// --> angewählt
						newInfo ();
					}
				}
				else
				{	newEnemyInfo (tmpUC);}				// feindliche Einheit angeklickt und angewählt

				send ("Move");
				linasBrett.setzen (UnitArray, UC);			// graphische Realisierung
			}
		}
	}
	
/*********** - Neuzeichnen der Einheiten für inaktiven Spieler - ***********/

	public void refresh (Unit [] UA)
	{
		UnitArray = UA;
		UnitArray = op.visible (UnitArray, P1UnitNum, who);
		newEnemyInfo (-1);						// kein Feind angewählt
		linasBrett.setzen (UnitArray, UC);
	}
	
/*********** - Flamme setzen - ***********/

	public void flame (int X, int Y)
	{
		linasBrett.setFlame (X, Y);
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


	public void keyReleased (KeyEvent e)
	{
	}


	public void keyTyped (KeyEvent e)
	{
	}

/*********** - Bewegung, realisiert durch Tastatur und Weiter durch "+"- ***********/

	public void keyPressed (KeyEvent e)
	{
		int keyCode = e.getKeyCode ();
		int X, Y;									// Koordinaten
		if ((((keyCode >= 97) && (keyCode <= 102)) || (keyCode == 107)) && (!wait))
		{
			switch (keyCode)
			{
				case 97:	X = UnitArray [UC].PosX - 22;
						Y = UnitArray [UC].PosY + 14;
				// Num 1
						if ((UnitArray [UC].PosX < 5) | (UnitArray [UC].PosY > 540) | (op.occupied (X, Y, UnitArray) != -1))
						{}		// im Feld und nicht besetzt
						else
						{	
							if (op.ZPLeft (UnitArray, UC))
							{
								UnitArray [UC].PosX = X;
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
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
							if (op.ZPLeft (UnitArray, UC))
							{ 	
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
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
							if (op.ZPLeft (UnitArray, UC))
							{
								UnitArray [UC].PosX = X;
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
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
							if (op.ZPLeft (UnitArray, UC))
							{
								UnitArray [UC].PosX = X;
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
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
							if (op.ZPLeft (UnitArray, UC))
							{	
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
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
							if (op.ZPLeft (UnitArray, UC))
							{
								UnitArray [UC].PosX = X;
								UnitArray [UC].PosY = Y;
								UnitArray [UC].ZP -= UnitArray [UC].ZPpF;
							}
						}
						break;
						
				case 107:	if (keyNext) {Next ();}		break;	
				// Plus			--> nächste Einheit
			}
			UnitArray = op.visible (UnitArray, P1UnitNum, who);	// Sichtbarkeit
			newInfo ();						// neue Info
			send ("Move");						// Bewegung dem Mitspieler senden
			linasBrett.setzen (UnitArray, UC);			// selbst Bewegung realisiern
		}
	}					
	
/*********** - Informationen über aktuelle Einheit - ***********/
	
	public void newInfo ()
	{
		if (!wait)
		{
			lInfoUnitV.setText (UnitArray [UC].type);
			lInfoTPV.setText ("" + UnitArray [UC].TP);
			lInfoZPV.setText ("" + UnitArray [UC].ZP);
			lInfoZPpFV.setText ("" + UnitArray [UC].ZPpF);
			lInfoZPpSV.setText ("" + UnitArray [UC].ZPpS);
			lInfoDamageV.setText ("" + UnitArray [UC].Dam);
			lInfoRWV.setText (UnitArray [UC].minRW + " - " + UnitArray [UC].RW);
		}
		else		// nicht am Zug --> keine aktuelle Einheit
		{
			lInfoUnitV.setText ("--");
			lInfoTPV.setText ("--");
			lInfoZPV.setText ("--");
			lInfoZPpFV.setText ("--");
			lInfoZPpSV.setText ("--");
			lInfoDamageV.setText ("--");
			lInfoRWV.setText ("--");
		}
	}
	
/*********** - Informationen über feindliche angewählte Einheit - ***********/
	
	public void newEnemyInfo (int tmpUC)
	{
		for (int j = 0; j < UnitArray.length; j++)		// ersteinmal alle auf nichtausgewählt stellen
		{	UnitArray [j].ESelected = false;}
		
		if (tmpUC == -1)					// keiner ausgewählt
		{
			lEInfo.setText ("Markierter Gegner");
			lEInfoUnitV.setText ("--");
			lEInfoTPV.setText ("--");
		}
		
		else							// ausgewählter Gegner
		{
			if (tmpUC < P1UnitNum)
			{	lEInfo.setText ("Markierter Gegner (" + Player [0] + ")");}
			else
			{	lEInfo.setText ("Markierter Gegner (" + Player [1] + ")");}		
			lEInfoUnitV.setText ("" + UnitArray [tmpUC].type);
			lEInfoTPV.setText ("" + UnitArray [tmpUC].TP);
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
		end = true;							// damit man normal beenden kann (ohne "runAway" zu senden)
		bNext.setEnabled (false);
		bEndTurn.setEnabled (false);
		linasBrett.removeMouseListener (this);
		this.removeKeyListener (this);
		if (won)
		{	
			send ("won");						// dem anderen Spieler dies mitteilen
			msg.msgInform ("Du hast gewonnen!!!", this);		// und natürlich dem Gewinner!
		}
		if (!won)
		{	msg.msgInform ("Du hast verloren!!!", this);}
	}
	
/*********** - testen, ob man die letzte feindliche Einheit zerstört hat - ***********/
	
	public void lastDestroyed ()
	{
		boolean lD = true;
		if (who.equals ("client"))
		{
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{	if (UnitArray [i2].TP > 0)	lD = false;}
		}
		
		if (who.equals ("server"))
		{
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{	if (UnitArray [i1].TP > 0)	lD = false;}
		}
		
		if (lD)		sieg (true);					// wenn, dann Sieg verkünden (s.o.)
	}
	
/*********** - Senden des Befehles und des Feldes - ***********/	
	
	public void send (String Command)
	{
		forSending [0] = (Object) Command;
		if (Command.equals ("Flame"))	forSending [1] = (Object) xy;
		else				forSending [1] = (Object) UnitArray;
		try
		{	
			Out.reset ();					// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject ((Object) forSending);		// in OutputStream schreiben
			Out.flush ();					// alles im OutputStram verschicken
		}
		catch (IOException e2)		{System.out.println (e2);}
	}
}