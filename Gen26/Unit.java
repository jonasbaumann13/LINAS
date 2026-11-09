import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* In dieser Klasse werden die Daten jeder einzelnen Einheit festgehalten
 */
 
public class Unit implements Serializable, Cloneable
{
    // Preserve compatibility with the previous implicitly generated network UID.
    @java.io.Serial
    private static final long serialVersionUID = 8456601742033046694L;
	public int PosX, PosY;
	public int TP, basicTP, Dam, basicZP, sight, camouflage;
	public int price;
	public int [] ZP 			= new int [3];
	public int [] RW 			= new int [3];
	public int [] RWtype;
	public double [] Armour 	= new double [3];
	public double AreaDamageMod;
	public String type, player;
	public String [] ArmourType = new String [3];
	public String DamType;
	public String Picture;
	public String Description;
    public String GermanName, GermanDescription;

    public String getDisplayName() {
        return I18n.isGerman() && GermanName != null && !GermanName.trim().isEmpty() ? GermanName : type;
    }

    public String getDescription() {
        return I18n.isGerman() && GermanDescription != null && !GermanDescription.trim().isEmpty()
            ? GermanDescription : Description;
    }
	public boolean visible, ESelected, obstacleAD, IsServer;
	
	
	public int [] tpA;
	public int [] damA;
	public int [] sightA;
	public int [] camouflageA;
	public int [][] zpA;
	public int [][] rwA;
	public int [][] rwTypeA;
	public double [][] armourA;
	public double [] admA;
	public String [] damTypeA;
	public String [] pictureAS, pictureAC;

	
/*********** - Leer-constructor - ***********/	
	
	public Unit ()
	{	}
	
/*********** - Constructor - ***********/
	
	public Unit (int price, String player, String type, String[] ArmourType, String Description, boolean obstacleAD, int[] tpA, int[] damA, int[] sightA, int[] camouflageA, int[][] zpA, int[][] rwA, int[][] rwTypeA, double[][] armourA, double[] admA, String[] damTypeA, String[] pictureAS, String[] pictureAC, boolean IsServer)
	{
		this.price 			= 	price;		// Preis der Einheit
		this.player 		= 	player;		// Besitzer
		this.type 			= 	type;
		this.ArmourType 	= 	ArmourType;
		this.Description 	= 	Description;
		this.obstacleAD		=	obstacleAD;
						
		this.tpA			=	tpA;
		this.damA			=	damA;
		this.sightA			=	sightA;
		this.camouflageA	=	camouflageA;
		this.zpA			=	zpA;
		this.rwA			=	rwA;
		this.rwTypeA		=	rwTypeA;
		this.armourA		=	armourA;
		this.admA			=	admA;
		this.damTypeA		=	damTypeA;
		this.pictureAS		=	pictureAS;
		this.pictureAC		=	pictureAC;
		this.IsServer		=	IsServer;			
		
		if (IsServer)		Picture 		= 	pictureAS[0];		// Pfad des Bildes
		else				Picture			=	pictureAC[0];
		basicTP 		= 	tpA[0];				// Die anfänglichen Trefferpunkte (Lebenspunkte)
		TP 				= 	basicTP;			// Trefferpunkte
		RW[0] 			= 	rwA[0][0];			// maxRW
		RW[1] 			= 	rwA[0][1];			// minRW
		RW[2] 			= 	rwA[0][2];			// AreaDamageRW
		sight 			= 	sightA[0];			// Sichtweite der Einheit
		camouflage 		= 	camouflageA[0];		// Grad der Tarnung
		Dam 			= 	damA[0];			// maximaler Schaden
		AreaDamageMod 	= 	admA[0];			// Veränderung des Schadens mit jedem weiteren vom Zentrum des Schusses entfernten Feld
		DamType 		= 	damTypeA[0];		// Schadenstyp der Waffe
		Armour[0] 		= 	armourA[0][0];		// Rüstungstyp 1
		Armour[1] 		= 	armourA[0][1];		// Rüstungstyp 2
		Armour[2] 		= 	armourA[0][2];		// Rüstungstyp 3
		ZP[0] 			= 	zpA[0][0];			// ZP
		ZP[1] 			= 	zpA[0][1];			// ZP/Feld
		ZP[2] 			= 	zpA[0][2];			// ZP/Schuss
		basicZP 		= 	ZP [0];				// Zugpunkte pro Runde
		PosX 			= 	0;					// Position im Spielfeld
		PosY 			= 	0;					// Position im Spielfeld
		ESelected 		= 	false;				// Ob Einheit als Feind markiert wurde (--> rote Umrandung)
		visible 		= 	false;				// Ob Einheit von jeweiligem Spieler gesehen werden kann

		RWtype = rwTypeA[0].clone();
	}
	
	public Object clone ()
	{
		Unit o;
		try
		{o = (Unit) super.clone();}
		catch(CloneNotSupportedException e) 
		{throw new AssertionError("Unit implements Cloneable", e);}
		
        o.ZP = ZP.clone();
        o.RW = RW.clone();
        o.RWtype = RWtype.clone();
        o.Armour = Armour.clone();
        o.ArmourType = ArmourType.clone();
        o.tpA = tpA.clone();
        o.damA = damA.clone();
        o.sightA = sightA.clone();
        o.camouflageA = camouflageA.clone();
        o.admA = admA.clone();
        o.damTypeA = damTypeA.clone();
        o.pictureAS = pictureAS.clone();
        o.pictureAC = pictureAC.clone();
        o.zpA = Arrays.stream(zpA).map(int[]::clone).toArray(int[][]::new);
        o.rwA = Arrays.stream(rwA).map(int[]::clone).toArray(int[][]::new);
        o.rwTypeA = Arrays.stream(rwTypeA).map(int[]::clone).toArray(int[][]::new);
        o.armourA = Arrays.stream(armourA).map(double[]::clone).toArray(double[][]::new);

		return o;
	}
}