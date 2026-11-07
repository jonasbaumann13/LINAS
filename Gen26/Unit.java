import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* In dieser Klasse werden die Daten jeder einzelnen Einheit festgehalten
 */
 
public class Unit implements Serializable, Cloneable
{
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

		RWtype 			= new int [rwTypeA[0].length];
		for (int i = 0; i < rwTypeA[0].length; i++)
		{	RWtype[i] = rwTypeA[0][i];}
	}
	
	public Object clone ()
	{
		Unit o = null;
		try
		{o = (Unit) super.clone();}
		catch(CloneNotSupportedException e) 
		{e.printStackTrace();}
		
		o.ZP 			= new int [3];
		o.ZP[0]			= ZP[0];
		o.ZP[1]			= ZP[1];
		o.ZP[2]			= ZP[2];
		o.RW 			= new int [3];
		o.RW[0]			= RW[0];
		o.RW[1]			= RW[1];
		o.RW[2]			= RW[2];
		o.RWtype		= new int [RWtype.length];
		for (int i0=0; i0<RWtype.length; i0++)		{o.RWtype[i0] = RWtype[i0];}
		
		o.Armour		= new double [3];
		o.Armour[0]		= Armour[0];
		o.Armour[1]		= Armour[1];
		o.Armour[2]		= Armour[2];
		
		o.tpA			= new int [tpA.length];
		for (int i1=0; i1<tpA.length; i1++)			{o.tpA[i1] = tpA[i1];}
		
		o.damA			= new int [damA.length];
		for (int i2=0; i2<damA.length; i2++)		{o.damA[i2] = damA[i2];}
		
		o.sightA		= new int [sightA.length];
		for (int i3=0; i3<sightA.length; i3++)		{o.sightA[i3] = sightA[i3];}
		
		o.camouflageA	= new int [camouflageA.length];
		for (int i4=0; i4<camouflageA.length; i4++)	{o.camouflageA[i4] = camouflageA[i4];}
		
		o.zpA			= new int [5][3];
		for (int i5a=0; i5a<5; i5a++)
		{
			for (int i5b=0; i5b<3; i5b++)
			{	o.zpA[i5a][i5b] = zpA[i5a][i5b];}
		}
		
		o.rwA			= new int [5][3];
		for (int i6a=0; i6a<5; i6a++)
		{
			for (int i6b=0; i6b<3; i6b++)
			{	o.rwA[i6a][i6b] = rwA[i6a][i6b];}
		}
				
		o.rwTypeA		= new int [5][rwA[0][1]];
		for (int i7a=0; i7a<5; i7a++)
		{
			for (int i7b=0; i7b<rwA[0][1]; i7b++)
			{	o.rwTypeA[i7a][i7b] = rwTypeA[i7a][i7b];}
		}
		
		o.armourA		= new double [5][3];
		for (int i8a=0; i8a<5; i8a++)
		{
			for (int i8b=0; i8b<3; i8b++)
			{	o.armourA[i8a][i8b] = armourA[i8a][i8b];}
		}
		
		o.admA			= new double [admA.length];
		for (int i9=0; i9<admA.length; i9++)		{o.admA[i9] = admA[i9];}
		
		o.damTypeA		= new String [damTypeA.length];
		for (int i10=0; i10<damTypeA.length; i10++)	{o.damTypeA[i10] = damTypeA[i10];}
		
		o.pictureAS		= new String [pictureAS.length];
		for (int i11=0; i11<pictureAS.length; i11++){o.pictureAS[i11] = pictureAS[i11];}
		
		o.pictureAC		= new String [pictureAC.length];
		for (int i12=0; i12<pictureAC.length; i12++){o.pictureAC[i12] = pictureAC[i12];}
		
		return o;
	}
}