import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

/*	Diese Klasse ist verantwortlich für Menue
 */

public class Menu extends JFrame implements ActionListener
{
	// Layoutkomponenten
	JPanel contentPane;
	JButton [] JBA 			= new JButton [4];
	JLabel JLHeading;
	ImageIcon iHeading;
	Color C 				= Color.black;
	Color CExit 			= Color.red;
	Font F 					= new Font ("Garamond", 1, 14);
		
	// andere Komponenten
	String Nick				= "";
	Socket So 				= null;
	ObjectOutputStream Out 	= null;
	ObjectInputStream In 	= null;
	Message msg;						//Klasse für Fehlermeldungen
	Comunication Com;
		
/*********** - Layoutaufbau im Constructor - ***********/	
	
	public Menu ()					
	{
		this.setSize (new Dimension (400, 400));					this.setTitle ("Linas");
		contentPane = (JPanel) this.getContentPane ();				contentPane.setLayout (null);
		contentPane.setBackground (new Color (213, 222, 242));		msg = new Message ();
		JLHeading = new JLabel ("");
		contentPane.add (JLHeading);
			
		for (int b = 0; b < 4; b++)
		{
			JBA [b] = new JButton ("");
			JBA [b].setFont (F);
			JBA [b].setForeground (C);
			JBA [b].setFocusable (false);
			JBA [b].addActionListener (this);
			contentPane.add (JBA [b]);
		}
		JBA [3].setForeground (CExit);

		iHeading = new ImageIcon ("Pics/linasHeaderS.gif");
		JLHeading.setIcon (iHeading);
		JBA [0].setLabel ("Create Game");					JBA [1].setLabel ("Join Game");
		JBA [2].setLabel ("Keypad Configuration");			JBA [3].setLabel ("Exit");
		
		JLHeading.setBounds (25, 20, 340, 65);
		JBA [0].setBounds (115, 150, 170, 25);
		JBA [1].setBounds (115, 200, 170, 25);
		JBA [2].setBounds (115, 250, 170, 25);
		JBA [3].setBounds (115, 300, 170, 25);
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
			if (o.equals (JBA [0]))		create ();				// --> Methode nur für Server
			if (o.equals (JBA [1]))		join ();				// --> Methode nur für Client
			if (o.equals (JBA [2]))		keyConfigure ();		// --> Tastenbelegung
			if (o.equals (JBA [3]))		System.exit (0);		// Beenden
		}
		
		catch (NullPointerException npe)	{System.out.println ("Null Pointer");}					
	}
	
/*********** - open Options (just server) - ***********/	
	
	public void create ()
	{
		try
		{
			Nick 	= msg.msgInput ("Please enter your name.", this);
			this.setVisible (false);
			Com 	= new Comunication (true, Nick, "", this.getBounds ().x, this.getBounds ().y);
			Com.start ();
		}
		catch (Exception g)			{System.out.println (g);}
	} 
	
/*********** - asks for IP (just client) - ***********/	
	
	public void join ()
	{
		String IP	= "";
		
		try
		{
			Nick 	= msg.msgInput ("Please enter your name.", this);
			IP 		= msg.msgInput ("Please enter the server IP.", this);
		}
		catch (Exception g)	 		{System.out.println (g);}
				
		Com 		= new Comunication (false, Nick, IP, this.getBounds ().x, this.getBounds ().y);
		Com.start ();									//...Thread für Datenübertragung starten
		this.setVisible (false);
	}
		
/*********** - Tastenbelegung - ***********/	
	
	public void keyConfigure ()
	{
		
	}
}
