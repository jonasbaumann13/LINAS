import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

/* In dieser Klasse werden die Daten jeder einzelnen Einheit festgehalten
 */
 
public class Unit implements Serializable
{
	public int PosX = 0, PosY = 0;								// Position im Spielfeld
	public int TP, basicTP, RW, minRW, Dam, ZPpF, ZPpS, ZP, basicZP, sight, AreaDamageRW;
	public double AreaDamageMod;
	public String type, player;
	public boolean visible, ESelected;
	
/*********** - Leer-constructor - ***********/	
	
	public Unit ()
	{	}
	
/*********** - Constructor - ***********/
	
	public Unit (String type, int basicTP, int RW, int minRW, int Dam, int ZPpF, int ZPpS, int basicZP, int sight, int AreaDamageRW, double AreaDamageMod, String player)
	{
		ESelected = false;			// Ob Einheit als Feind markiert wurde (--> rote Umrandung)
		visible = false;			// Ob Einheit von jeweiligem Spieler gesehen werden kann
		TP = basicTP;				// Trefferpunkte
		this.basicTP = basicTP;			// Die anfänglichen Trefferpunkte (Lebenspunkte)
		this.RW = RW;				// maximale Reichweite
		this.minRW = minRW;			// minimale Reichweite
		this.Dam = Dam;				// maximaler Schaden
		this.type = type;			// Art
		this.ZPpF = ZPpF;			// Verbrauch der Zugpunkte pro Feld
		this.ZPpS = ZPpS;			// Verbrauch der Zugpunkte pro Schuss
		ZP = basicZP;				// Zugpunkte
		this.basicZP = basicZP;			// Zugpunkte pro Runde
		this.sight = sight;			// Sichtweite der Einheit
		this.AreaDamageRW = AreaDamageRW;	// Reichweite des Gebietsschadens
		this.AreaDamageMod = AreaDamageMod;	// Veränderung des Schadens mit jedem weiteren vom Zentrum des Schusses entfernten Feld
		this.player = player;			// Besitzer
	}
}