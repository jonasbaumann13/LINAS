import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

/* In dieser Klasse wird von den Spielern festgelegt, mit wie vielen von jeder Sorte an Einheiten
 * Sie gegen den anderen Spieler antreten wollen. Jeder hat eine bestimmte Anzahl an Punkten und muss
 * davon die unterschiedlich teuren Einheiten kaufen.
 * Außerdem wird dann das komplette Einheitenfeld erstellt, welches also die Einheiten beider Spieler
 * enthält
 */

public class linasPrepareFrame extends JFrame implements ActionListener
{
	//Layoutkomponenten
	JPanel contentPane;
	JLabel lNumberPan, lNumberArt, lNumberAuf, lName, lHeading, lPoints;
	JTextField TName;
	JButton bStart, bNumberPanP, bNumberArtP, bNumberAufP, bNumberPanM, bNumberArtM, bNumberAufM;
	ImageIcon iHeading, iArrayT, iArrayB;
	
	//andere Komponenten
	String Name = "", who;					// Name des Spielers und ob Server oder Client
	Socket So;
	ObjectOutputStream Out = null;
	Object [] forSending = new Object [2];			// Objekt, welches bei Bedarf gesendet wird	
	int points = 550;					// Anzahl der Punkte um Einheiten zu "kaufen"
	int UnitNum = 0;					// Anzahl der Einheiten eines Spielers
	int NumPan = 0;						// Anzahl der Panzer
	int NumArt = 0;						// Anzahl der Artillerie
	int NumAuf = 0;						// Anzahl der Aufklärer
	Unit [] UnitArray, tmpUA;				// tmpUA ist für das spätere zusammenschließen der beiden Felder (von Spieler 1 und 2)
	Message msg;						// Klasse für Fehlermeldungen
	Operations op;						// Methodensammlung

/*********** - Layoutaufbau im Constructor und spezifische Starteinstellungen - ***********/

	public linasPrepareFrame (Socket So, String who)
	{
		this.So = So;
		this.who = who;
		try
		{	Out = new ObjectOutputStream (So.getOutputStream());}
		catch (IOException e1)	{	System.out.println (e1);}
		
		iHeading = new ImageIcon (linasPrepareFrame.class.getResource ("/linasHeaderS.gif"));			lHeading = new JLabel (iHeading);
		iArrayT = new ImageIcon (linasPrepareFrame.class.getResource ("/ArrayTop.gif"));			iArrayB = new ImageIcon (linasPrepareFrame.class.getResource ("/ArrayBottom.gif"));
		lNumberPan = new JLabel ("Panzer (50): 0");			lNumberArt = new JLabel ("Artillerie (80): 0");	
		lNumberAuf = new JLabel ("Aufklärer (35): 0");			lPoints = new JLabel ("Punkte: 540");
		bNumberPanP = new JButton (iArrayT);				bNumberPanM = new JButton (iArrayB);
		bNumberArtP = new JButton (iArrayT);				bNumberArtM = new JButton (iArrayB);
		bNumberAufP = new JButton (iArrayT);				bNumberAufM = new JButton (iArrayB);
		lName = new JLabel ("Nick:");					TName = new JTextField (10);
		bStart = new JButton ("");					msg = new Message ();
		op = new Operations ();
		
		lNumberPan.setBounds (20, 165, 200, 20);			bNumberPanP.setBounds (200, 161, 18, 12);
										bNumberPanM.setBounds (200, 177, 18, 12);
		lNumberArt.setBounds (20, 205, 200, 20);			bNumberArtP.setBounds (200, 201, 18, 12);
										bNumberArtM.setBounds (200, 217, 18, 12);
		lNumberAuf.setBounds (20, 245, 200, 20);			bNumberAufP.setBounds (200, 241, 18, 12);
										bNumberAufM.setBounds (200, 257, 18, 12);
		lName.setBounds (20, 285, 200, 20);				TName.setBounds (200, 285, 100, 20);
		lHeading.setBounds (25, 20, 340, 65);				bStart.setBounds (200, 335, 150, 20);
		lPoints.setBounds (20, 125, 100, 20);

		this.setSize (new Dimension (400, 400));			this.setTitle ("Linas Vorbereitungen");
		contentPane = (JPanel) this.getContentPane ();			contentPane.setLayout (null);
				
		contentPane.add (lNumberPan);					contentPane.add (lNumberArt);
		contentPane.add (lNumberAuf);					contentPane.add (lPoints);
		contentPane.add (bNumberPanP);					contentPane.add (bNumberPanM);
		contentPane.add (bNumberArtP);					contentPane.add (bNumberArtM);
		contentPane.add (bNumberAufP);					contentPane.add (bNumberAufM);
		contentPane.add (lName);					contentPane.add (TName);
		contentPane.add (lHeading);					contentPane.add (bStart);
		
		bStart.addActionListener (this);
		bNumberPanP.addActionListener (this);				bNumberPanM.addActionListener (this);
		bNumberArtP.addActionListener (this);				bNumberArtM.addActionListener (this);
		bNumberAufP.addActionListener (this);				bNumberAufM.addActionListener (this);
		
		// Spezifische Anfangseinstellungen
		if (who.equals ("client"))	bStart.setLabel ("Bestätigen");
		if (who.equals ("server"))	
		{
			bStart.setLabel ("Warte auf Client...");
			bStart.setEnabled (false);
		}
	}
	
/*********** - Programmende, wenn das Fenster geschlossen wird - ***********/	
	
	protected void processWindowEvent (WindowEvent e)
	{
		super.processWindowEvent (e);
		if (e.getID () == WindowEvent.WINDOW_CLOSING)
		{	System.exit (0);}							
	}											

/*********** - Reaktion auf Buttonklicks - ***********/		
	
	public void actionPerformed (ActionEvent e)
	{
		try
		{
			Object o = e.getSource ();
			if (o.equals (bStart))
			{
				Name = TName.getText ();
				if (who.equals ("client"))	beginC ();
				if (who.equals ("server"))	beginS ();
			}
			if (o.equals (bNumberPanP))	NumMod ("Pan", "P");
			if (o.equals (bNumberPanM))	NumMod ("Pan", "M");
			if (o.equals (bNumberArtP))	NumMod ("Art", "P");
			if (o.equals (bNumberArtM))	NumMod ("Art", "M");
			if (o.equals (bNumberAufP))	NumMod ("Auf", "P");
			if (o.equals (bNumberAufM))	NumMod ("Auf", "M");
		}
		catch (NullPointerException npe)
		{	System.out.println ("Null Pointer");}
	}
	
/*********** - "Kaufen" der Einheiten - ***********/		
	
	public void NumMod (String type, String PorM)
	{
		if ((type.equals ("Pan")) && (PorM.equals ("P")) && (points > 49))		{NumPan++; points -= 50;}
		if ((type.equals ("Pan")) && (PorM.equals ("M")) && (NumPan > 0))		{NumPan--; points += 50;}
		if ((type.equals ("Art")) && (PorM.equals ("P")) && (points > 79))		{NumArt++; points -= 80;}
		if ((type.equals ("Art")) && (PorM.equals ("M")) && (NumArt > 0))		{NumArt--; points += 80;}
		if ((type.equals ("Auf")) && (PorM.equals ("P")) && (points > 34))		{NumAuf++; points -= 35;}
		if ((type.equals ("Auf")) && (PorM.equals ("M")) && (NumAuf > 0))		{NumAuf--; points += 35;}

		lNumberPan.setText ("Panzer (50): " + NumPan);
		lNumberArt.setText ("Artillerie (75): " + NumArt);
		lNumberAuf.setText ("Aufklärer (30): " + NumAuf);
		lPoints.setText ("Punkte: " + points);
	}				
				
/*********** - Wenn der Client mit seinen Einstellungen fertig ist - ***********/	
	
	public void beginC ()
	{
		UnitNum = NumPan + NumArt + NumAuf;
		if ((UnitNum > 0) && !(Name.equals ("")))			// Kontrolle, ob alles ausgefüllt ist
		{
			creatArray (0);						// Einheiten-Feld erstellen
			send ("ClientReady");
						
			bNumberPanP.setEnabled (false);
			bNumberPanM.setEnabled (false);
			bNumberArtP.setEnabled (false);
			bNumberArtM.setEnabled (false);
			bNumberAufP.setEnabled (false);
			bNumberAufM.setEnabled (false);
			bStart.setEnabled (false);
			bStart.setLabel ("Warte auf Host...");
		}
	}

/*********** - Bescheid, dass Client fertig ist - ***********/		
		
	public void ClientReady (Unit [] UA)
	{
		tmpUA = UA;						// Einheiten-Feld vom Client
		bStart.setLabel ("Spiel beginnen");
		bStart.setEnabled (true);
	}
	
/*********** - Wenn der Server mit seinen Einstellungen fertig ist - ***********/	
	
	public void beginS ()
	{
		int l = tmpUA.length;					// Länge des Feldes des Clients
		UnitNum = NumPan + NumArt + NumAuf;
		if ((UnitNum > 0) && !(Name.equals ("")))
		{			
			creatArray (l);					// Feld des Servers erstellen
			addArray ();					// beide Felder zu einem verpacken
			send ("ServerReady");				// fertiges Feld und Bescheid zum Client senden
		}
	}
	
/*********** - Feld mit den jeweiligen Einheiten erstellen - ***********/	
	
	public void creatArray (int l)
	{
		UnitArray = new Unit [(l + UnitNum)];
		for (int i = l; i < (l + NumPan); i++)				{UnitArray [i] = op.setUnit (1, Name);}
		for (int j = (l + NumPan); j < (l + NumPan + NumArt); j++)	{UnitArray [j] = op.setUnit (2, Name);}
		for (int k = (l + NumPan + NumArt); k < (l + UnitNum); k++)	{UnitArray [k] = op.setUnit (3, Name);}
	}	
	
/*********** - Feld mit den jeweiligen Einheiten erstellen - ***********/
	
	public void addArray ()
	{
		for (int i = 0; i < tmpUA.length; i++)
		{	UnitArray [i] = tmpUA [i];}
	}
	
/*********** - Einheiten-Feld ersetzen (erneuern) - ***********/	

	public void setUnitArray (Unit [] UA)
	{
		UnitArray = UA;
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
	
/*********** - Ausgabestrom schließen - ***********/
	
	public void closeStream ()
	{
		try	{Out.close ();}
		catch (IOException e3)		{System.out.println (e3);}
	}
	
/*********** - OutputStream zurückgeben - ***********/	
	
	public ObjectOutputStream getOOS ()
	{	
		return Out;
	}
}