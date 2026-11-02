import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.* ;

/*	Diese Klasse ist verantwortlich für den Aufbau des ersten Frames und der Verbindung zwischen
 *	Server und Client.
 */

public class linasConection extends JFrame implements ActionListener
{
	// Layoutkomponenten
	JPanel contentPane;
	JTextField TIP;
	JLabel lWelcome1, lWelcome2, lIP, lHeading;
	JButton bServer, bClient;
	ImageIcon iHeading;
	
	// andere Komponenten
	ServerSocket SerSo = null ;
	Socket So = null ;
	String who = "";
	Message msg;								//Klasse für Fehlermeldungen
	
/*********** - Layoutaufbau im Constructor - ***********/	
	
	public linasConection ()					
	{
		lWelcome1 = new JLabel ("Wilkommen bei LINAS! Bitte geben Sie eine IP-Adresse ein, ");
		lWelcome2 = new JLabel ("wenn Sie einem Spiel beitreten wollen.");
		lIP = new JLabel ("IP: ");					TIP = new JTextField (20);
		iHeading = new ImageIcon (linasConection.class.getResource ("/linasHeaderS.gif"));			lHeading = new JLabel (iHeading);
		bServer = new JButton ("Spiel eröffnen");			bClient = new JButton ("Spiel beitreten");
		msg = new Message ();
		
		lHeading.setBounds (25, 20, 340, 65);				lWelcome1.setBounds (20, 120, 350, 20);
		lWelcome2.setBounds (20, 155, 350, 20);
		lIP.setBounds (20, 220, 30, 20);				TIP.setBounds (70, 220, 100, 20);
		bServer.setBounds (20, 300, 150, 20);				bClient.setBounds (200, 300, 150, 20);

		this.setSize (new Dimension (400, 400));			this.setTitle ("Linas Anmeldung");
		contentPane = (JPanel) this.getContentPane ();			contentPane.setLayout (null);
				
		contentPane.add (lHeading);					
		contentPane.add (lWelcome1);					contentPane.add (lWelcome2);
		contentPane.add (lIP);						contentPane.add (TIP);
		contentPane.add (bServer);					contentPane.add (bClient);
		
		bServer.addActionListener (this);				bClient.addActionListener (this);
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
			if (o.equals (bServer))		Server ();		// --> Methode nur für Server
			if (o.equals (bClient))		Client ();		// --> Methode nur für Client
			if (So != null)						//wenn Verbindug vorhanden...
			{
				linasComunication Com = new linasComunication (So, who);
				Com.start ();					//...Thread für Datenübertragung starten
				this.setVisible (false);
			}			
		}

		catch (NullPointerException npe)
		{	System.out.println ("Null Pointer");}					
	}
	
/*********** - auf Verbindung warten und Socket holen (nur Server) - ***********/	
	
	public void Server ()
	{
		try
		{
			this.setVisible (false);
			System.out.println ("");
			System.out.println ("");
			System.out.println ("Warte auf Verbindung auf Port 1111...");
			SerSo = new ServerSocket (1111);			
			So = SerSo.accept ();					//Wartet auf einen Client, um Socket zu bekommen
			who = "server";
		}
		catch(SocketException e)	{System.out.println (e);}
		catch(Exception f)		{System.out.println(f);}			
	} 
	
/*********** - Socket erstellen, um Verbindung mit Server aufzubauen (nur Client) - ***********/	
	
	public void Client ()
	{
		String IP;
		lWelcome1.setText ("");
		if ((IP = TIP.getText ()).equals (""))
		{	msg.msgError ("Bitte eine IP-Adresse eingeben", this);}
		else
		{
			try
			{
				lWelcome2.setText ("Verbindungsaufbau...");
				So = new Socket (IP, 1111);
				who = "client";
			}
			catch(Exception g)	 
			{	
				System.out.println (g);
				lWelcome2.setText ("Verbindungsaufbau fehlgeschlagen!");
			}
		}
	}
}