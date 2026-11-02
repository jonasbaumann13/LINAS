import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* Das Spielfeld ist das graphische Herzstück des Spieles.
 * Hier wird alles gezeichnet und auf den Bildschirm gebracht, was auf dem Spielfeld passiert
 */

public class linasSpielfeld extends JComponent
{
	Image hexa = getToolkit ().getImage (linasSpielfeld.class.getResource ("/Spielbrett.gif"));
	Image ESelected = getToolkit ().getImage (linasSpielfeld.class.getResource ("/Selected.gif"));
	Image Turn = getToolkit ().getImage (linasSpielfeld.class.getResource ("/Turn.gif"));
	//Schadensmodelle
	Image perCent100 = getToolkit ().getImage (linasSpielfeld.class.getResource ("/100perCent.gif"));
	Image perCent75 = getToolkit ().getImage (linasSpielfeld.class.getResource ("/75perCent.gif"));
	Image perCent50 = getToolkit ().getImage (linasSpielfeld.class.getResource ("/50perCent.gif"));
	Image perCent25 = getToolkit ().getImage (linasSpielfeld.class.getResource ("/25perCent.gif"));
	Image perCent00 = getToolkit ().getImage (linasSpielfeld.class.getResource ("/00perCent.gif"));
	Image destroyed = getToolkit ().getImage (linasSpielfeld.class.getResource ("/Schrott.gif"));
	
	Image Explosion = getToolkit ().getImage (linasSpielfeld.class.getResource ("/Explosion2.gif"));
	Image [] IMG;
	Color cBlue = new Color (41, 41, 255);
	
	int UN1, UN2, UC;						// Anzahler Einheiten + unit-counter
	int X = 0;							// Positionen
	int Y = 0;
	boolean drawLineP1 = false;					// ob und welche blauen Linien gezeichnet werden sollen
	boolean drawLineP2 = false;
	boolean shot = false;						// ob ein Schuss gezeichnet werden soll
	Operations op = new Operations ();				// Methodensammlung
	Unit [] UA;							// Einheitenfeld
			
/*********** - Einheitenanzahl setzen - ***********/
	
	public void setUnitNum (int P1UnitNum, int P2UnitNum)
	{
		UN1 = P1UnitNum;
		UN2 = P2UnitNum;
		IMG = new Image [(UN1 + UN2)];
	}
	
/*********** - Bilder setzen - ***********/
	
	public void setPics (Unit [] UnitArray)
	{
		UA = UnitArray;
		try
		{	for (int i = 0; i < UN1; i++)
			{				
				if ((UA [i].type).equals ("Panzer"))
				{	IMG [i] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/PanzerPlayer1.gif"));}

				if ((UA [i].type).equals ("Artillerie"))
				{	IMG [i] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/ArtilleriePlayer1.gif"));}
				
				if ((UA [i].type).equals ("Aufklärer"))
				{	IMG [i] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/AufklaererPlayer1.gif"));}
			}

			for (int j = UN1; j < UA.length; j++)
			{
				if ((UA [j].type).equals ("Panzer"))
				{	IMG [j] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/PanzerPlayer2.gif"));}

				if ((UA [j].type).equals ("Artillerie"))
				{	IMG [j] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/ArtilleriePlayer2.gif"));}
				
				if ((UA [j].type).equals ("Aufklärer"))
				{	IMG [j] = getToolkit ().getImage (linasSpielfeld.class.getResource ("/AufklaererPlayer2.gif"));}
			}
		}
		catch (NullPointerException e)
		{	System.out.println (e);}
	}
	
/*********** - nicht setzbare Felder setzen - ***********/
	
	public void newBoard (int Player)
	{
		if (Player == 1)
		{	drawLineP1 = true;}
		else
		{	
			drawLineP1 = false;
			drawLineP2 = true;
		}
		
		for (int i = 0; i < UA.length; i++)
		{	UA [i].visible = false;}
		repaint ();
	}

/*********** - Aktualisierung der globalen Variablen und anschließender repaint Aufruf - ***********/

	public void setzen (Unit [] UnitArray, int UnitCounter)
	{
		UC = UnitCounter;
		UA = UnitArray;
		shot = false;
		repaint ();
	}
	
/*********** - Flamme setzen - ***********/

	public void setFlame (int tmpX, int tmpY)
	{
		X = tmpX;
		Y = tmpY;
		shot = true;
		repaint ();
	}
	
/*********** - eigentliche graphische Ausgabe - ***********/

	public void paintComponent (Graphics Brett)
	{
		int DamPro;
		Brett.drawImage (hexa, 0, 0, this);
		Brett.setColor (cBlue);
		if (drawLineP1)						// für Spieler 1 Verticale Linien - zeigt Feld an, auf welches nicht gesetzt werden darf 
		{			
			for (int j = 13; j < 564; j += 44)
			{	Brett.fillRect (j, 15, 3, 474);}
			for (int k = 35; k < 564; k += 44)
			{	Brett.fillRect (k, 1, 3, 474);}
		}
		if (drawLineP2)						// für Spieler 2 Verticale Linien - zeigt Feld an, auf welches nicht gesetzt werden darf 
		{			
			for (int l = 13; l < 564; l += 44)
			{	Brett.fillRect (l, 99, 3, 474);}
			for (int m = 35; m < 564; m += 44)
			{	Brett.fillRect (m, 85, 3, 474);}
		}

		for (int i1 = 0; i1 < UA.length; i1++)			// setzt alle sichtbaren Einheiten
		{	
			if ((UA [i1].visible == true) && (UA [i1].TP > 0))
			{	
				Brett.drawImage (IMG [i1], UA [i1].PosX, UA [i1].PosY, this);
				
				if (UA [i1].ESelected == true)		// setzt rote Umrandung
				{	Brett.drawImage (ESelected, UA [i1].PosX - 1, UA [i1].PosY, this);}
				if (UC == i1)				// setzt blaue Umrandung
				{	Brett.drawImage (Turn, UA [i1].PosX - 1, UA [i1].PosY, this);}
			}
		}
		
		for (int i2 = 0; i2 < UA.length; i2++)			// setzt die zur einheit dazugehörigen Lebensppunkte (grün-roter Balkn)
		{	
			if (UA [i2].visible == true)
			{
				DamPro = (int) ((UA [i2].TP * 100) / UA [i2].basicTP);
				
				if (DamPro == 100)
				{	Brett.drawImage (perCent100, UA [i2].PosX, UA [i2].PosY, this);}
				if ((DamPro < 100) && (DamPro >= 75))
				{	Brett.drawImage (perCent75, UA [i2].PosX, UA [i2].PosY, this);}
				if ((DamPro < 75) && (DamPro >= 50))
				{	Brett.drawImage (perCent50, UA [i2].PosX, UA [i2].PosY, this);}
				if ((DamPro < 50) && (DamPro >= 25))
				{	Brett.drawImage (perCent25, UA [i2].PosX, UA [i2].PosY, this);}
				if ((DamPro < 25) && (DamPro > 0))
				{	Brett.drawImage (perCent00, UA [i2].PosX, UA [i2].PosY, this);}
				if (DamPro <= 0)
				{	Brett.drawImage (destroyed, UA [i2].PosX, UA [i2].PosY, this);}		// setzt Schrotthaufen
			}
		}
		
		if (shot)
		{	Brett.drawImage (Explosion, X, Y, this);}	// setzt die Explosion
	}
}