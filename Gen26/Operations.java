import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* Methodensammlung
 */
 
public class Operations
{
/*********** - Ob Zahl gerade ist - ***********/	

	public boolean even (int tmp)
	{
		boolean even = true;

		if ((tmp % 2) == 0)
		{	even = true;}
		if ((tmp % 2) == 1)
		{	even = false;}

		return even;
	}

/*********** - Umrechnung von Pixelkoordinaten zu Feldkoordinaten - ***********/	

	public int [] koAngleich (int MX, int MY)
	{
		int [] XY = new int [3];
		int X, Y;
		boolean gerade;

		X = (int) ((MX - 8) / 22);				// Umrechnung in Feldkoordinaten
		gerade = even (X);
		X = (X * 22);						// Eigentlich + 8, aber damit Bild richtig ist, wieder - 8

		if (gerade == false)					// Unterscheidung aufgrund des Hexafeldes nötig!
		{	
			Y = (int) ((MY - 7) / 28);
			XY [2] = Y;
			Y = (Y * 28);					// s. o. ... + - 7
		}

		else
		{	
			Y = (int) ((MY - 21) / 28);
			XY [2] = Y;
			Y = (Y * 28) + 14;				// s. o. ... + 21, - 7
		}

		XY [0] = X;
		XY [1] = Y;
		return XY;
	}

/*********** - Test, ob Feld besetzt ist und gegebenenfalls ID zurückgeben - ***********/	

	public int occupied (int X, int Y, Unit [] UnitArray)
	{
		int occupied = -1;

		for (int i = 0; i < UnitArray.length; i++)
		{
			if ((UnitArray [i].PosX == X) && (UnitArray [i].PosY == Y))
			{	occupied = i;}
		}
		return occupied;
	}

/*********** - Berechnung der Entfernung in Kästchen - ***********/	
	
	public int distance (int X, int Y, Unit [] UnitArray, int UC)
	{
		int distance = 100;
		int tmpY = Y;
		int tmpX = X;
		
		for (int i = 0; i < 40; i++)			// 40 ist ausgewählt worden, da das Spielfeld keine größere Enfernung zulässt
		{
			if ((tmpX == UnitArray [UC].PosX) && (tmpY == UnitArray [UC].PosY))
			{	
				distance = i;
				break;				// da ist mir jetzt nichts als 'n Break eingefallen... sorry
			}
			
			if (UnitArray [UC].PosX != X)		// insgesammt wird schrittweise vorgegangen, bis das Ziel erreicht ist
			{					// die Schritte werden gezählt und zurückgegeben
				if (UnitArray [UC].PosX < X)
				{	tmpX -= 22;}
				if (UnitArray [UC].PosX > X)
				{	tmpX += 22;}
				if (UnitArray [UC].PosY <= Y)
				{	tmpY -= 14;}
				if (UnitArray [UC].PosY > Y)
				{	tmpY += 14;}
			}
			else
			{
				if (UnitArray [UC].PosY < Y)
				{	tmpY -= 28;}
				if (UnitArray [UC].PosY > Y)
				{	tmpY += 28;}
			}
			
			X = tmpX;
			Y = tmpY;
		}
		return distance;
	}
	
/*********** - Schadensberechnung - ***********/

	public int damage (int distance, Unit [] UnitArray, int UC)
	{
		int percent = (int) (Math.random () * 100);			// der Zufallsgenerator
		int damage = 0;	
		int tmpDist = distanceMod (UnitArray, UC, distance);		// Der Schaden ist aber auch von der Reichweite abhängig
		
		// Die folgende Liste ist Teil der Regeln und wird vielleicht noch modifiziert		
		if (percent == 100)
		{	damage = UnitArray [UC].Dam;}
						
		if ((percent < 100) && (percent > 95))
		{	
			switch (tmpDist)
			{
				case 1:	damage = UnitArray [UC].Dam;			break;
				case 2: damage = UnitArray [UC].Dam;			break;
				case 3: damage = (int) (UnitArray [UC].Dam * 0.75);	break;
			}
		}

		if ((percent < 96) && (percent > 75))
		{
			switch (tmpDist)
			{
				case 1:	damage = UnitArray [UC].Dam;			break;
				case 2: damage = (int) (UnitArray [UC].Dam * 0.75);	break;
				case 3: damage = (int) (UnitArray [UC].Dam * 0.5);	break;
			}
		}
			
		if ((percent < 76) && (percent > 50))
		{
			switch (tmpDist)
			{
				case 1:	damage = (int) (UnitArray [UC].Dam * 0.75);	break;
				case 2: damage = (int) (UnitArray [UC].Dam * 0.5);	break;
				case 3: damage = (int) (UnitArray [UC].Dam * 0.5);	break;
			}
		}	
			
		if ((percent < 51) && (percent > 25))
		{	
			switch (tmpDist)
			{
				case 1:	damage = (int) (UnitArray [UC].Dam * 0.5);	break;
				case 2: damage = (int) (UnitArray [UC].Dam * 0.5);	break;
				case 3: damage = (int) (UnitArray [UC].Dam * 0.25);	break;
			}
		}
		
		if ((percent < 26) && (percent > 5))
		{	
			switch (tmpDist)
			{
				case 1:	damage = (int) (UnitArray [UC].Dam * 0.5);	break;
				case 2: damage = (int) (UnitArray [UC].Dam * 0.25);	break;
				case 3: damage = 0;					break;
			}
		}
		
		if ((percent < 6) && (percent > 0))
		{	
			switch (tmpDist)
			{
				case 1:	damage = (int) (UnitArray [UC].Dam * 0.25);	break;
				case 2: damage = 0;					break;
				case 3: damage = 0;					break;
			}
		}
			
		if (percent == 0)
		{	damage = 0;}

		return damage;
	}
	
/*********** - Test, wer alles vom Gebietsschaden betroffen ist - ***********/	
	
	public int [] AreaDamageMod (Unit [] UnitArray, int UC, int X, int Y)
	{
		int [] ADVictims = new int [UnitArray.length];				// Feld der betroffenen
		int distance;
		for (int i = 1; i <= UnitArray [UC].AreaDamageRW; i++)
		{
			for (int j = 0; j < UnitArray.length; j++)
			{
				distance = distance (X, Y, UnitArray, j);
				if (distance == i)
				{	ADVictims [j] = i;}				// Vermerk über Entfernung vom Zentrum	
			}
		}
		return ADVictims;
	}
	
/*********** - Welche Distanz entspricht welcher Reichweitenkategorie bei der Einheit - ***********/	
	
	public int distanceMod (Unit [] UnitArray, int UC, int distance)
	{
		int tmpDist = 0;							// --> Reichweite (nah = 1, mittel = 2, fern = 3)
		if (UnitArray [UC].type.equals ("Panzer"))
		{	
			if (distance == 1) 			{tmpDist = 1;}
			if (distance == 2) 			{tmpDist = 2;}
			if (distance == 3) 			{tmpDist = 3;}
		}
		if (UnitArray [UC].type.equals ("Artillerie"))
		{	
			if (distance < 7) 			{tmpDist = 1;}
			if ((distance >= 7) && (distance < 10))	{tmpDist = 2;}
			if (distance >= 10) 			{tmpDist = 3;}
		}
		if (UnitArray [UC].type.equals ("Aufklärer"))
		{	
			if (distance == 1) 			{tmpDist = 1;}
			if (distance == 2) 			{tmpDist = 2;}
		}
		return tmpDist;
	}		

/*********** - Modifikation der Standardwerte in Abhängigkeit zu den Trefferpunkten - ***********/	

	public Unit [] DamMod (Unit [] UnitArray, int UC)
	{
		int DamPro;
		DamPro = (int) ((UnitArray [UC].TP * 100) / UnitArray [UC].basicTP);
		
		if ((UnitArray [UC].type).equals ("Panzer"))
		{
			if ((DamPro < 100) && (DamPro > 75))
			{	UnitArray [UC].ZPpF = 3;}
			if ((DamPro < 76) && (DamPro > 50))
			{	
				UnitArray [UC].ZPpF = 3;
				UnitArray [UC].ZPpS = 5;
			}
			if ((DamPro < 51) && (DamPro > 25))
			{	
				UnitArray [UC].ZPpF = 4;
				UnitArray [UC].ZPpS = 5;
			}
			if ((DamPro < 26) && (DamPro > 0))
			{	
				UnitArray [UC].ZPpF = 4;
				UnitArray [UC].ZPpS = 9;
			}
		}
		
		if ((UnitArray [UC].type).equals ("Artillerie"))
		{
			if ((DamPro < 100) && (DamPro > 75))
			{	UnitArray [UC].ZPpF = 4;}
			if ((DamPro < 76) && (DamPro > 50))
			{	
				UnitArray [UC].ZPpF = 5;
				UnitArray [UC].Dam = 75;
			}
			if ((DamPro < 51) && (DamPro > 25))
			{	
				UnitArray [UC].ZPpF = 5;
				UnitArray [UC].Dam = 50;
			}
			if ((DamPro < 26) && (DamPro > 0))
			{	
				UnitArray [UC].ZPpF = 6;
				UnitArray [UC].Dam = 50;
			}
		}
		
		if ((UnitArray [UC].type).equals ("Aufklärer"))
		{
			if ((DamPro < 100) && (DamPro > 75))
			{	
				UnitArray [UC].basicZP = 16;
				UnitArray [UC].ZP = 16;
				if (UnitArray [UC].ZP > 16)	
				{	UnitArray [UC].ZP = 16;}
			}
			if ((DamPro < 76) && (DamPro > 50))
			{	
				UnitArray [UC].basicZP = 16;
				UnitArray [UC].ZPpF = 3;
				UnitArray [UC].ZPpS = 4;
				if (UnitArray [UC].ZP > 16)	
				{	UnitArray [UC].ZP = 16;}
			}
			if ((DamPro < 51) && (DamPro > 25))
			{	
				UnitArray [UC].basicZP = 16;
				UnitArray [UC].ZPpF = 4;
				UnitArray [UC].ZPpS = 5;
				if (UnitArray [UC].ZP > 16)	
				{	UnitArray [UC].ZP = 16;}
			}
			if ((DamPro < 26) && (DamPro > 0))
			{	
				UnitArray [UC].basicZP = 12;
				UnitArray [UC].ZPpF = 4;
				UnitArray [UC].ZPpS = 5;
				if (UnitArray [UC].ZP > 12)	
				{	UnitArray [UC].ZP = 12;}
			}
		}
		if (DamPro <= 0)
		{
			UnitArray [UC].ZPpF = 99;
			UnitArray [UC].ZPpS = 99;
			UnitArray [UC].sight = -1;
		}
		return UnitArray;
	}
	
/*********** - Auffüllen der Zugpunkte - ***********/	
	
	public Unit [] newRound (Unit [] UnitArray)
	{
		for (int i = 0; i < UnitArray.length; i++)
		{	UnitArray [i].ZP = UnitArray [i].basicZP;}
		return UnitArray;
	}
	
/*********** - Sichtbarkeit jeder Einheit - ***********/	
	
	public Unit [] visible (Unit [] UnitArray, int P1UnitNum, String who)
	{
		int distance;
		for (int k = 0; k < UnitArray.length; k++)
		{	UnitArray [k].visible = false;}				// ersteinmal alle auf false
		
		if (who.equals("client"))
		{
			for (int l = 0; l < P1UnitNum; l++)
			{	
				if (UnitArray [l].TP > 0)			// die eigenen lebenden sind sichtbar
				{	UnitArray [l].visible = true;}
			}
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{
				for (int j1 = 0; j1 < UnitArray.length; j1++)
				{
					distance = distance (UnitArray [j1].PosX, UnitArray [j1].PosY, UnitArray, i1);
					if (distance <= UnitArray [i1].sight)		// die, welche im Sichtfeld irgendeiner eigenen Einheit sind sind auch sichtbar
					{	UnitArray [j1].visible = true;}		// (zerstörte können nicht sehen, da "sight" auf -1 gestellt wurde)
				}
			}
		}
		if (who.equals("server"))					// das gleiche für die andere Seite
		{
			for (int m = P1UnitNum; m < UnitArray.length; m++)
			{
				if (UnitArray [m].TP > 0)
				{	UnitArray [m].visible = true;}
			}
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{
				for (int j2 = 0; j2 < UnitArray.length; j2++)
				{
					distance = distance (UnitArray [j2].PosX, UnitArray [j2].PosY, UnitArray, i2);
					if (distance <= UnitArray [i2].sight)
					{	UnitArray [j2].visible = true;}
				}
			}
		}
		
		return UnitArray;
	}
	
/*********** - Ob die Einheit sich noch bewegen kann - ***********/	
	
	public boolean ZPLeft (Unit [] UnitArray, int UC)
	{
		boolean zpLeft = true;
		if (UnitArray [UC].ZP < UnitArray [UC].ZPpF)
		{	zpLeft = false;}
		return zpLeft;
	}
	
/*********** - Name der beiden Spieler herausfinden - ***********/
	
	public String [] player (Unit [] UnitArray)
	{
		String player [] = new String [2];
		player [0] = UnitArray [0].player;
		player [1] = UnitArray [UnitArray.length - 1].player;
		return player;
	}
	
/*********** - Anzahl der Einheiten des jeweiligen Spielers (p) zurückgeben - ***********/
	
	public int UnitNum (Unit [] UnitArray, int p)
	{
		int i = 0;
		int UN = -1;
		while (UnitArray [0].player.equals (UnitArray [i].player))
		{	i++;}

		if (p == 1)	UN = i;
		if (p == 2)	UN = UnitArray.length - i;
		return UN;
	}
	
/*********** - Aktualisieren des Einheitenfeldes (nur für linasSetFrame) - ***********/
	
	public Unit [] ModUnitArray (Unit [] UA1, Unit [] UA2, int P1UnitNum)
	{
		for (int i = 0; i < P1UnitNum; i++)
		{
			UA1 [i] = UA2 [i];
			UA1 [i].visible = false;
		}
		return UA1;
	}
	
/*********** - bestimmte Einheit erstellen (nur für linasSetFrame) - ***********/
	
	public Unit setUnit (int type, String Name)
	{
		Unit UnitType = new Unit ();
		switch (type)
		{
			case 1:	UnitType = new Unit ("Panzer", 100, 3, 1, 100, 2, 4, 9, 4, 0, -1, Name);	break;
			case 2:	UnitType = new Unit ("Artillerie", 75, 12, 4, 100, 3, 6, 10, 4, 1, 0.5, Name);	break;
			case 3:	UnitType = new Unit ("Aufklärer", 50, 2, 1, 10, 2, 4, 20, 6, 0, -1, Name);	break;
		}
		return UnitType;
	}
	
/*********** - test, ob alle Einheiten gesetzt wurden (nur für linasSetFrame) - ***********/	
	
	public boolean setAll (Unit [] UnitArray, int P1UnitNum, String who)
	{
		boolean ready = true;
		if (who.equals ("client"))
		{
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{	if (UnitArray [i1].visible == false)	ready = false;}
		}
		if (who.equals ("server"))
		{
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{	if (UnitArray [i2].visible == false)	ready = false;}
		}
		return ready;
	}
	
/*********** - Anzahl der Einheiten einer Art und eines Spieler (nur für linasSetFrame) - ***********/	
	
	public int number (Unit [] UnitArray, String Player, String Unit)
	{
		int num = 0;
		
		for (int i = 0; i < UnitArray.length; i++)
		{
			if ((UnitArray [i].player.equals (Player)) && (UnitArray [i].type.equals (Unit)))
			{	num++;}
		}
		return num;
	}
}