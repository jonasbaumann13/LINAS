import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* Das Spielfeld ist das graphische Herzstück des Spieles.
 * Hier wird alles gezeichnet und auf den Bildschirm gebracht, was auf dem Spielfeld passiert
 */

public final class Spielfeld extends JComponent
{
    @java.io.Serial
    private static final long serialVersionUID = 1L;

	transient Image hexa 			= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/Spielbrett.gif"));
	transient Image ESelected 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/Selected.gif"));
	transient Image Turn 			= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/Turn.gif"));
	transient Image MG 			= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/MG.gif"));
	transient Image AT 			= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/AT.gif"));
	transient Image Mortar 		= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/LR.gif"));
	//Schadensmodelle
	transient Image perCent100 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/100perCent.gif"));
	transient Image perCent75 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/75perCent.gif"));
	transient Image perCent50 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/50perCent.gif"));
	transient Image perCent25 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/25perCent.gif"));
	transient Image perCent00 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/00perCent.gif"));
	transient Image destroyed 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/Schrott.gif"));

	transient Image Shadow 		= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/ShadowOfWar.gif"));
	transient Image Explosion 	= Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Pics/Explosion2.gif"));
	transient Image [] IMG;
	Color cBlue 		= new Color (41, 41, 255);
	String [] ArmourType;
	
	int UN1, UN2, UC;							// Anzahler Einheiten + unit-counter
	int [][] border;							// Rand der eigenen Einheiten
	int X = 0;									// Positionen
	int Y = 0;
	boolean IsServer;
	boolean setShadowP1 = false;				// ob und welche blauen Linien gezeichnet werden sollen
	boolean setShadowP2 = false;
	boolean shot 		= false;				// ob ein Schuss gezeichnet werden soll
	boolean main;
	transient Operations op 		= new Operations ();	// Methodensammlung
	Unit [] UA;									// Einheitenfeld
			
/*********** - Einheitenanzahl setzen - ***********/
	
	public void setUnitNum (int P1UnitNum, int P2UnitNum, boolean m, boolean IS, String [] ArmourType)
	{
		UN1 			= P1UnitNum;
		UN2 			= P2UnitNum;
		IMG 			= new Image [(UN1 + UN2)];
		border 			= new int [(UN1 + UN2)][4];
		main 			= m;
		IsServer		= IS;
		this.ArmourType = ArmourType;
	}
	
/*********** - Bilder setzen - ***********/
	
	public void setPics (Unit [] UnitArray)
	{
		UA = UnitArray;
		try
		{	
			for (int i = 0; i < UA.length; i++)
			{	IMG [i] = Toolkit.getDefaultToolkit().getImage (AppPaths.asset("Units/" + UnitArray [i].Picture));}
		}
		catch (NullPointerException e1)		{System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Operation failed", e1);}
	}
	
/*********** - nicht setzbare Felder setzen - ***********/
	
	public void newBoard (int Player)
	{
		if (Player == 1)
		{	setShadowP1 = true;}
		else
		{	
			setShadowP1 = false;
			setShadowP2 = true;
		}
		
		for (Unit element : UA)
		{	element.visible = false;}
		repaint ();
	}

/*********** - Aktualisierung der globalen Variablen und anschließender repaint Aufruf - ***********/

	public void setzen (Unit [] UnitArray, int UnitCounter)
	{
		UC 		= UnitCounter;
		UA 		= UnitArray;
		shot 	= false;
		repaint ();
	}
	
/*********** - Flamme setzen - ***********/

	public void setFlame (int tmpX, int tmpY)
	{
		X 		= tmpX;
		Y 		= tmpY;
		shot 	= true;
		repaint ();
	}
	
/*********** - eigentliche graphische Ausgabe - ***********/

	public void paintComponent (Graphics Brett)
	{
		boolean tmpVisible;
		int DamPro;
		Brett.drawImage (hexa, 0, 0, this);
		Brett.setColor (cBlue);
						
		for (int n = 0; n < UA.length; n++)		// setzt alle sichtbaren Einheiten
		{	
			border [n][0] = 0;
			border [n][1] = -1;
			border [n][2] = -1;
			if ((UA [n].visible) && (UA [n].TP > 0))
			{	
				Brett.drawImage (IMG [n], UA [n].PosX, UA [n].PosY, this);
				if (UA [n].ESelected)			border [n][0] = 1;
				if (UC == n)				border [n][0] = 2;
				border [n][1] = UA [n].PosX - 1;
				border [n][2] = UA [n].PosY;
			}
		}
		
		if (setShadowP1)						// für Spieler 1 Verticale Linien - zeigt Feld an, auf welches nicht gesetzt werden darf 
		{			
			for (int k1 = 0; k1 < 560; k1 += 44)
			{
				for (int k2 = 14; k2 < 480; k2 += 28)
				{	Brett.drawImage (Shadow, k1, k2, this);}
			}
			for (int k3 = 22; k3 < 560; k3 += 44)
			{
				for (int k4 = 0; k4 < 460; k4 += 28)
				{	Brett.drawImage (Shadow, k3, k4, this);}
			}
		}
		if (setShadowP2)						// für Spieler 2 Verticale Linien - zeigt Feld an, auf welches nicht gesetzt werden darf 
		{			
			for (int k5 = 0; k5 < 560; k5 += 44)
			{
				for (int k6 = 98; k6 < 550; k6 += 28)
				{	Brett.drawImage (Shadow, k5, k6, this);}
			}
			for (int k7 = 22; k7 < 560; k7 += 44)
			{
				for (int k8 = 84; k8 < 550; k8 += 28)
				{	Brett.drawImage (Shadow, k7, k8, this);}
			}
		}
		
		if (main)
		{
			for (int m1 = 0; m1 < 560; m1 += 44)
			{
				for (int m2 = 14; m2 < 550; m2 += 28)
				{
					tmpVisible = op.PointVisibility (UA, UN1, IsServer, m1, m2, 0);
					if (!tmpVisible)
					{	Brett.drawImage (Shadow, m1, m2, this);}
				}
			}
				
			for (int m3 = 22; m3 < 560; m3 += 44)
			{
				for (int m4 = 0; m4 < 550; m4 += 28)
				{
					tmpVisible = op.PointVisibility (UA, UN1, IsServer, m3, m4, 0);
					if (!tmpVisible)
					{	Brett.drawImage (Shadow, m3, m4, this);}
				}
			}
		}
		
		for (int n2 = 0; n2 < UA.length; n2++)
		{
			if (border [n2][2] != -1)
			{	
				if (border [n2][0] == 1)	Brett.drawImage (ESelected, border [n2][1], border [n2][2], this);
				if (border [n2][0] == 2)	Brett.drawImage (Turn, border [n2][1], border [n2][2], this);
			}
		}
				
		for (int o = 0; o < UA.length; o++)			// setzt die zur Einheit dazugehörigen Lebensppunkte (grün-roter Balkn)
		{	
			if ((UA [o].visible) && (UA [o].obstacleAD))
			{
				DamPro = (UA [o].TP * 100) / UA [o].basicTP;
				
				if (DamPro == 100)
				{	Brett.drawImage (perCent100, UA [o].PosX, UA [o].PosY, this);}
				if ((DamPro < 100) && (DamPro >= 75))
				{	Brett.drawImage (perCent75, UA [o].PosX, UA [o].PosY, this);}
				if ((DamPro < 75) && (DamPro >= 50))
				{	Brett.drawImage (perCent50, UA [o].PosX, UA [o].PosY, this);}
				if ((DamPro < 50) && (DamPro >= 25))
				{	Brett.drawImage (perCent25, UA [o].PosX, UA [o].PosY, this);}
				if ((DamPro < 25) && (UA [o].TP > 0))
				{	Brett.drawImage (perCent00, UA [o].PosX, UA [o].PosY, this);}
				if (UA [o].TP <= 0)
				{	Brett.drawImage (destroyed, UA [o].PosX, UA [o].PosY, this);}		// setzt Schrotthaufen
			}
			if ((UA [o].visible) && (UA [o].TP > 0))
			{
				if (!IsServer && (o < UN1))
				{
					if (UA [o].DamType.equals (ArmourType [0]))	Brett.drawImage (MG, UA [o].PosX, UA [o].PosY, this);
					if (UA [o].DamType.equals (ArmourType [1]))	Brett.drawImage (AT, UA [o].PosX, UA [o].PosY, this);
					if (UA [o].DamType.equals (ArmourType [2]))	Brett.drawImage (Mortar, UA [o].PosX, UA [o].PosY, this);
				}
				if (IsServer && (o >= UN1))
				{
					if (UA [o].DamType.equals (ArmourType [0]))	Brett.drawImage (MG, UA [o].PosX, UA [o].PosY, this);
					if (UA [o].DamType.equals (ArmourType [1]))	Brett.drawImage (AT, UA [o].PosX, UA [o].PosY, this);
					if (UA [o].DamType.equals (ArmourType [2]))	Brett.drawImage (Mortar, UA [o].PosX, UA [o].PosY, this);
				}
			}
		}
		
		if (shot)
		{	Brett.drawImage (Explosion, X, Y, this);}	// setzt die Explosion
	}
}