import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

/* In dieser Klasse wird von den Spielern festgelegt, mit wie vielen von jeder Sorte an Einheiten
 * Sie gegen den anderen Spieler antreten wollen. Jeder hat eine bestimmte Anzahl an Punkten und muss
 * davon die unterschiedlich teuren Einheiten kaufen.
 * Au�erdem wird dann das komplette Einheitenfeld erstellt, welches also die Einheiten beider Spieler
 * enth�lt
 */

public class Description extends JFrame implements ActionListener
{
	//Layoutkomponenten
	JPanel contentPane;
	JLabel [] JLAInfo;
	JLabel [] JLAInfoV;
	JLabel lHeading;
	JTextArea TADescr;
	JScrollPane SPDescr;
	JButton bLast, bNext, bBack;
	ImageIcon iHeading, iUnit, iNext, iLast;
	Color C 		= Color.black;
	Color CExit 	= Color.red;
	Color CBlue 	= new Color (41, 41, 255);
	Font F 			= new Font ("Garamond", 1, 14);
	Font FKursiv 	= new Font ("Monotype Corsiva", 1, 18);
	Font FP 		= new Font ("Verdana", 1, 16);

	//andere Komponenten
	int UC;
	Unit [] bspU;
	Message msg;						// Klasse f�r Fehlermeldungen
	Operations op;						// Methodensammlung
	
	public Description (Unit [] bspU, int UC)
	{
		this.bspU 	= bspU;
		this.UC 	= UC;
				
	//******---------******\\
		
		op 			= new Operations ();
		msg 		= new Message ();
		JLAInfo 	= new JLabel [17];
		JLAInfoV 	= new JLabel [17];
									
	//******graphische Instanzierung******\\
		
		iHeading 		= new ImageIcon ("Pics/linasHeaderS.gif");
		iUnit 			= new ImageIcon ("Units/" + bspU [UC].Picture);
		iNext 			= new ImageIcon ("Pics/ArrayNext.gif");
		iLast 			= new ImageIcon ("Pics/ArrayLast.gif");
		lHeading 		= new JLabel (iHeading);
		bLast 			= new JButton (iLast);				bLast.setFocusable (false);
		bNext 			= new JButton (iNext);				bNext.setFocusable (false);
		bBack 			= new JButton ("zur�ck");			bBack.setFocusable (false);
		JLAInfo [16] 	= new JLabel ();
		for (int i1 = 0; i1 < 16; i1++)
		{
			JLAInfo [i1] = new JLabel ();
			JLAInfo [i1].setFont (F);
			JLAInfo [i1].setForeground (C);
			JLAInfoV [i1] = new JLabel ();
			JLAInfoV [i1].setFont (F);
			JLAInfoV [i1].setForeground (C);
			JLAInfoV [i1].setHorizontalAlignment (4);
		}
		JLAInfo [16].setIcon (iUnit);
		JLAInfoV [16] = new JLabel ();
		JLAInfoV [16].setFont (FP);
		JLAInfoV [16].setForeground (CBlue);
		
		TADescr = new JTextArea ();
		TADescr.setLineWrap (true);
		TADescr.setWrapStyleWord(true);
		TADescr.setEditable (false);
		TADescr.setFocusable (false);
		TADescr.setFont (FKursiv);
		TADescr.setForeground (C);
		SPDescr = new JScrollPane (TADescr);
		
		JLAInfo [0].setText ("Cost:");
		JLAInfo [1].setText ("Hitpoints:");
		JLAInfo [2].setText ("Armour for " + bspU [UC].ArmourType [0] + ":");
		JLAInfo [3].setText ("Armour for " + bspU [UC].ArmourType [1] + ":");
		JLAInfo [4].setText ("Armour for " + bspU [UC].ArmourType [2] + ":");
		JLAInfo [5].setText ("Maximum Damage:");
		JLAInfo [6].setText ("Damage type:");
		JLAInfo [7].setText ("Factor for area damage:");
		JLAInfo [8].setText ("range:");								////////////////////////////
		JLAInfo [9].setText ("Radius des Gebietsschadens:");
		JLAInfo [10].setText ("Sichtweite:");
		JLAInfo [11].setText ("Tarnung:");
		JLAInfo [12].setText ("Zugpunkte pro Runde:");
		JLAInfo [13].setText ("Zugpunkte pro Feld:");
		JLAInfo [14].setText ("Zugpunkte pro Angriff:");
		JLAInfo [15].setText ("Hindernis nach dem Tod?");
		
		
	//******Koordinaten der Objekte******\\
	
		lHeading.setBounds (105, 20, 340, 65);
		bLast.setBounds (250, 100, 14, 13);
		bNext.setBounds (275, 100, 14, 13);
		bBack.setBounds (445, 495, 50, 20);
		JLAInfo [16].setBounds (20, 122, 30, 30);
		JLAInfoV [16].setBounds (60, 125, 200, 20);
		SPDescr.setBounds (20, 360, 495, 75);
		
		int x = 20;
		int y = 160;
		for (int j1 = 0; j1 < 16; j1++)
		{
			JLAInfo [j1].setBounds (x, y, 185, 15);
			JLAInfoV [j1].setBounds ((x+150), y, 75, 15);
			y += 20;
			if (j1 == 7)
			{
				x = 290;
				y = 160;
			}
			if (j1 == 0)	y += 5;
			if (j1 == 4)	y += 5;
			if (j1 == 11)	y += 5;
			if (j1 == 14)	y += 5;			
		}
		
		
	//******---------******\\
		
		this.setSize (new Dimension (560, 520));				this.setTitle ("Linas Beschreibungen");
		contentPane = (JPanel) this.getContentPane ();			contentPane.setLayout (null);
		contentPane.setBackground (new Color (213, 222, 242));
		
	//******Hinzuf�gen der Objekte******\\
		for (int l1 = 0; l1 < 17; l1++)
		{
			contentPane.add (JLAInfo [l1]);
			contentPane.add (JLAInfoV [l1]);
		}
		contentPane.add (bLast);
		contentPane.add (bNext);
		contentPane.add (lHeading);					
		contentPane.add (SPDescr);
		
		info ();
		
		bNext.addActionListener (this);
		bLast.addActionListener (this);
	}
	
	
	public void actionPerformed (ActionEvent e)
	{
		Object o = e.getSource ();
		if (o.equals (bBack))		goBack ();
		if (o.equals (bLast))		UC--;
		if (o.equals (bNext))		UC++;
		if (UC > bspU.length - 1)	UC = 0;
		if (UC < 0)					UC = bspU.length - 1;
		info ();			
	}
	
	
	public void goBack ()
	{
		this.setVisible (false);
	}
	
	
	public void info ()
	{
		JLAInfo [16].setIcon (new ImageIcon ("Units/" + bspU [UC].Picture));
		JLAInfoV [0].setText (bspU [UC].price + "");
		JLAInfoV [1].setText (bspU [UC].TP + "");
		JLAInfoV [2].setText (op.perCent (bspU [UC].Armour [0]) + "%");
		JLAInfoV [3].setText (op.perCent (bspU [UC].Armour [1]) + "%");
		JLAInfoV [4].setText (op.perCent (bspU [UC].Armour [2]) + "%");
		JLAInfoV [5].setText (bspU [UC].Dam + "");
		JLAInfoV [6].setText (bspU [UC].DamType);
		JLAInfoV [7].setText (bspU [UC].AreaDamageMod + "");
		if (bspU [UC].AreaDamageMod == -1)	JLAInfoV [7].setText ("--");
		JLAInfoV [8].setText (bspU [UC].RW [0] + " - " + bspU [UC].RW [1]);
		JLAInfoV [9].setText (bspU [UC].RW [2] + "");
		JLAInfoV [10].setText (bspU [UC].sight + "");
		JLAInfoV [11].setText (bspU [UC].camouflage + "");
		JLAInfoV [12].setText (bspU [UC].ZP [0] + "");
		JLAInfoV [13].setText (bspU [UC].ZP [1] + "");
		JLAInfoV [14].setText (bspU [UC].ZP [2] + "");
		if (bspU [UC].obstacleAD)	JLAInfoV [15].setText ("Ja");
		else				JLAInfoV [15].setText ("Nein");
		JLAInfoV [16].setText (bspU [UC].type + " (" + bspU [UC].DamType + ")");
		TADescr.setText (bspU [UC].Description);
	}
	
	
	public void setUC (int tmpUC)
	{
		UC = tmpUC;
		info ();
	}
}