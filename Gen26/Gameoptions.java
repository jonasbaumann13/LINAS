import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;


/*	Diese Klasse ist verantwortlich für den Aufbau des ersten Frames und der Verbindung zwischen
 *	Server und Client.
 */

public class Gameoptions extends JFrame implements ActionListener
{
	// Layoutkomponenten
	JPanel ContentPane;
	JButton BExit, BStart, BFileAdd, BFileDel, BFileInfo;
	JLabel[] LA 			= new JLabel[5];
	ImageIcon IHeading;
	JTextField TFCredit, TFChat;
	JList LiUnits;
	JTextArea TAChat;
	JScrollPane SPChat, SPUnits;
	DefaultListModel LiMUnits;
	JCheckBox CBA[] 		= new JCheckBox[3];
	ExampleFileFilter filter;
	JFileChooser FCUnits;
	Color CBlue				= Color.blue;
	Color CBlack 			= Color.black;
	Color CRed 				= Color.red;
	Font FTimes				= new Font ("Verdana", 1, 10);
	Font FGara				= new Font ("Garamond", 1, 14);
	Font FGaraSmall			= new Font ("Garamond", 1, 10);
		
	// andere Komponenten
	boolean [] BoolSend		= new boolean [1];					// Update
	boolean IsServer;
	Object [] RefreshInfo	= new Object [6];
	Object [] ForSending 	= new Object [2];
	Stack SFile				= new Stack ();
	Stack StackSend			= new Stack ();						// Update
	int AnzUnitFiles		= 6;
	int [] IntSend			= new int [1];						// Update
	int X, Y;
	String NickC			= "";
	String NickS			= "";
	String IP				= "";
	Socket So;
	ServerSocket SerSo 		= null;
	ObjectOutputStream Out 	= null;
	FileOutputStream FOS 	= null;
	BufferedInputStream BIS = null;
	KeyAdapter KA 			= null;
	Unit [] BasicUnitInfo;
	Message Msg;						//Klasse für Fehlermeldungen
	Operations Op;
	Description Des;
	
	
/*********** - Layoutaufbau im Constructor - ***********/	
	
	public Gameoptions (String Nick, boolean IsServer, int X, int Y)					
	{
		this.IsServer			= IsServer;
		if (IsServer)	{NickS 	= Nick;}
		else			{NickC 	= Nick;}
		this.X					= X;
		this.Y					= Y;
		
		Op = new Operations ();
			
		this.setSize (new Dimension (570, 450));					this.setTitle ("Linas");
		ContentPane = (JPanel) this.getContentPane ();				ContentPane.setLayout (null);
		ContentPane.setBackground (new Color (213, 222, 242));		Msg 		= new Message ();
		CBA[0]		= new JCheckBox ("Fog of war");
		CBA[1]		= new JCheckBox ("Transfer own units");			CBA[2]		= new JCheckBox ("Transfer own pictures");
		IHeading	= new ImageIcon (AppPaths.asset("Pics/linasHeaderS.gif"));		LiMUnits	= new DefaultListModel ();
		BExit 		= new JButton ("Exit");							ContentPane.add (BExit);						
		BStart 		= new JButton ("Accept");						ContentPane.add (BStart);
		TFCredit	= new JTextField (10);							ContentPane.add (TFCredit);
		TFChat		= new JTextField (100);							ContentPane.add (TFChat);
		TAChat		= new JTextArea ();
		LiUnits		= new JList (LiMUnits);
		SPChat		= new JScrollPane (TAChat);						ContentPane.add (SPChat);
		SPUnits		= new JScrollPane (LiUnits);					ContentPane.add (SPUnits);
		BFileAdd	= new JButton ("Add");			if (IsServer)	ContentPane.add (BFileAdd);
		BFileDel	= new JButton ("X");			if (IsServer)	ContentPane.add (BFileDel);
		BFileInfo	= new JButton ("?");			if (IsServer)	ContentPane.add (BFileInfo);
		

		TAChat.setFont (FTimes);
		BExit.setFont (FGara);
		BExit.setForeground (CRed);
		BStart.setFont (FGara);
		BFileAdd.setFont (FGaraSmall);
		BFileDel.setFont (FGara);
		BFileInfo.setFont (FGara);
		
		for (int l = 0; l < 5; l++)
		{
			LA[l] = new JLabel ("");
			LA[l].setFont (FGara);
			LA[l].setForeground (CBlack);
			ContentPane.add (LA[l]);			
		}
		
		for (int c = 0; c < 3; c++)
		{
			CBA[c].setFont (FGara);
			CBA[c].setForeground (CBlack);
			if ((IsServer)||(c==0))					ContentPane.add (CBA[c]);			
		}
		
		LA[1].setText ("Credits available");
		LA[2].setText ("Units for this game:");
		LA[3].setForeground (CBlue);
		LA[4].setForeground (CRed);
		LA[4].setHorizontalAlignment (LA[4].RIGHT);
		
		
		TAChat.setLineWrap (true);
		TAChat.setWrapStyleWord(true);
		TAChat.setEditable (false);
		TAChat.setFocusable (false);
		TAChat.setForeground (CBlack);
		SPChat.setVerticalScrollBarPolicy(SPChat.VERTICAL_SCROLLBAR_AS_NEEDED);
		SPChat.setHorizontalScrollBarPolicy(SPChat.HORIZONTAL_SCROLLBAR_NEVER);
		SPUnits.setVerticalScrollBarPolicy(SPUnits.VERTICAL_SCROLLBAR_AS_NEEDED);
		SPUnits.setHorizontalScrollBarPolicy(SPUnits.HORIZONTAL_SCROLLBAR_NEVER);
		
		BExit.setFocusable (false);
		BStart.setFocusable (false);
		BFileAdd.setFocusable (false);
		BFileDel.setFocusable (false);
		BFileInfo.setFocusable (false);
		LA[0].setIcon (IHeading);
		
						LA[0].setBounds 	(125, 20, 340, 65);			
		TFCredit.setBounds 	(10, 100, 50, 20);			LA[2].setBounds		(300, 100, 150, 20);
		LA[1].setBounds 	(70, 100, 150, 25);			SPUnits.setBounds	(300, 130, 225, 85);
		CBA[0].setBounds 	(10, 130, 160, 25);			BFileAdd.setBounds	(300, 225, 51, 20);
		CBA[1].setBounds 	(10, 190, 160, 25);			BFileDel.setBounds	(355, 225, 51, 20);
		CBA[2].setBounds 	(10, 220, 160, 25);			BFileInfo.setBounds (410, 225, 51, 20);
		LA[3].setBounds		(12, 260, 160, 25);			TFChat.setBounds	(300, 335, 225, 20);
		LA[4].setBounds		(98, 260, 160, 25);			BStart.setBounds	(300, 365, 90, 25);
		SPChat.setBounds	(10, 290, 250, 100);		BExit.setBounds		(435, 365, 90, 25);									
															
														
		BExit.addActionListener (this);
		BStart.addActionListener (this);
		BFileAdd.addActionListener (this);
		BFileDel.addActionListener (this);
		BFileInfo.addActionListener (this);
		TFCredit.addActionListener (this);
		CBA[0].addActionListener (this);
		
		TFChat.addKeyListener (KA = new KeyAdapter ()
		{
			public void keyReleased (KeyEvent e)	{}
			public void keyTyped (KeyEvent e)		{}
			public void keyPressed (KeyEvent e)
			{	
				if (e.getKeyCode () == 10)		
				{
					if (Out != null)		send ("chat", TFChat.getText());
					sendMsg (0, TFChat.getText());
					TFChat.setText ("");
				}
			}
		});
		
		if (IsServer)
		{
			BStart.setEnabled (false);
			BStart.setLabel ("Waiting");
			LA[3].setText (NickS);
		}
		else
		{
			TFCredit.setFocusable (false);
			LA[4].setText (NickC);
			CBA[0].setEnabled (false);
			CBA[1].setEnabled (false);
			CBA[2].setEnabled (false);
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
			if (o.equals (BFileAdd))	addFile ();	update();
			if (o.equals (BFileDel))	delFile ();	update();
			if (o.equals (BFileInfo))	fileInf ();
			if (o.equals (BStart))		beginnG ();
			if (o.equals (BExit))		exit	();
			if (o.equals (TFCredit))	update	();
			if (o.equals (CBA[0]))		update	();
		}
		
		catch (NullPointerException npe)	{System.out.println ("Null Pointer");}					
	}


/*********** - Schliessen des Programmes (noch verbessern!) - ***********/	
	
	public void exit ()
	{
		System.exit (0);
	}
	
	
/*********** - fuegt eine Einheit zum Pool hinzu - ***********/	
	
	public void addFile ()
	{
		if (FCUnits == null)
		{
			FCUnits = new JFileChooser (AppPaths.units ());
			FCUnits.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
			FCUnits.setFileFilter (new ExampleFileFilter ("txt", "Unit files or folders"));
			FCUnits.setAcceptAllFileFilterUsed (false);
		}
		if (FCUnits.showOpenDialog (BFileAdd) == JFileChooser.APPROVE_OPTION)
		{
			File selected = FCUnits.getSelectedFile ();
			FCUnits.setCurrentDirectory (selected.isDirectory () ? selected : selected.getParentFile ());
			addUnitFiles (selected);
		}
	}

	private void addUnitFiles (File file)
	{
		if (file.isDirectory ())
		{
			File [] files = file.listFiles ();
			if (files == null) return;
			Arrays.sort (files);
			for (File child : files)
			{
				if (!java.nio.file.Files.isSymbolicLink (child.toPath ())) addUnitFiles (child);
			}
		}
		else if (file.getName ().toLowerCase (Locale.ROOT).endsWith (".txt") && !SFile.contains (file))
		{
			try (BufferedReader reader = new BufferedReader (new FileReader (file)))
			{
				String line;
				do { line = reader.readLine (); } while (line != null && line.trim ().isEmpty ());
				if (line == null || !line.trim ().startsWith ("Unit:")) return;
				SFile.push (file);
				LiMUnits.addElement (file.getName ());
			}
			catch (IOException e) { Msg.msgError ("Cannot read unit file: " + file, this); }
		}
	}

/*********** - loescht die ausgewaehlte Einheit aus dem Pool - ***********/	
	
	public void delFile ()
	{
		int tmp = LiUnits.getSelectedIndex ();
		if (tmp != -1)
		{
			Object OTmp = SFile.remove (tmp);
			LiMUnits.remove (tmp);
		}
	}


/*********** - Erneuert den Einheitenpool fuer den Client (nur Client) - ***********/

	public void setFileList ()
	{
		File FTmp;
		LiMUnits.clear();		
		while (!SFile.isEmpty())
		{
			FTmp = (File) SFile.pop();
			LiMUnits.addElement (FTmp.getName());
		}
	}


/*********** - Zeigt die Daten der ausgewaehlten Einheit an (nur Server) - ***********/	
	
	public void fileInf ()
	{
		if (LiUnits.getSelectedIndex() != -1)
		{
			Unit [] BspUA = Op.createBspUA (SFile);
			Des = new Description (BspUA, LiUnits.getSelectedIndex());
			Des.setLocation (X, Y);
			Des.setResizable (false);
			Des.setVisible (true);
		}
	}
	
	
/*********** - Zum Chatten - ***********/	
	
	public void sendMsg (int tmp, String Msg)
	{
		if ((IsServer&&(tmp==0)) || (!IsServer&&(tmp==1)))
		{	TAChat.append ("\n" + NickS + ": " + Msg);}
		if ((IsServer&&(tmp==1)) || (!IsServer&&(tmp==0)))	
		{	TAChat.append ("\n" + NickC + ": " + Msg);}
		TAChat.setCaretPosition(TAChat.getDocument().getLength());				// damit immer der neuste Text angezeigt wird
	}


/*********** -  - ***********/	
	
	public void beginnG ()
	{
		if (!IsServer)		send ("ClientAccepts", "");
		else
		{	
			if ((TFCredit.getText().equals(""))||((int)Integer.valueOf(TFCredit.getText()).intValue()==0))
			{	Msg.msgError ("Please enter a number for the available credits.", this);}
			else 
			{
				BasicUnitInfo = Op.createBspUA (SFile);
				send ("ServerAccepts", "");
			}
		}
	}


/*********** - Der Server darf jetzt auch auf Ready klicken (nur Server) - ***********/	
	
	public void clientAccepts ()
	{
		BStart.setEnabled (true);
		BStart.setLabel ("Start");
	}
		
		
/*********** - kopiert die gebrauchten Einheiten in einen tmp Ordner (in construction!!!) - ***********/
	
	public void copyUnitFiles ()
	{
		
	}
	
	
/*********** - gibt dem Client die Einstellungsaenderungen (nur Server) - ***********/
	
	public void update ()
	{
		if ((Out != null)&&(IsServer))
		{
			BoolSend [0] 	= CBA [0].isSelected();
			if (TFCredit.getText().equals(""))	IntSend [0] = 0;
			else								IntSend [0] = (int) Integer.valueOf(TFCredit.getText()).intValue();
			StackSend		= SFile;
	
			send ("sendSettings", "");
		}
	}
	
	
/*********** - setzt die Einstellungen vom Server (nur Client) - ***********/
	
	public void setSettings (Object O)
	{
		Object [] Refresh = (Object []) O;
		BoolSend 	= (boolean []) Refresh[0];
		IntSend 	= (int []) Refresh[1];
		CBA [0].setSelected(BoolSend[0]);
		TFCredit.setText("" + IntSend[0]);
		SFile = Op.reverse ((Stack) Refresh[2]);
		setFileList ();
	}
	
	
/*********** - gibt Spiel-Credits zurueck - ***********/

	public int getCredits ()
	{
		int credit = (int) Integer.valueOf(TFCredit.getText()).intValue();
		return credit;
	}
	
/*********** - gibt den Nick zurueck - ***********/

	public String getNick ()
	{
		String Nick;
		if (IsServer)	Nick = NickS;
		else			Nick = NickC;
		return Nick;
	}
	
/*********** - setzt den Ausgabestrom - ***********/
	
	public void setOOS (ObjectOutputStream Out)
	{
		this.Out = Out;
		TAChat.setForeground (CBlack);
		TAChat.append ("\nConection created\n");
		if (IsServer)		send ("ServerNick", NickS);
	}
	
	
/*********** - setzt Server Nickname (nur Client) - ***********/
	
	public void setServerNick (String Nick)
	{
		NickS = Nick;
		LA[3].setText (NickS);
		send ("ClientNick", NickC);
	}
	
	
/*********** - setzt Client Nickname (nur Server) - ***********/
	
	public void setClientNick (String Nick)
	{
		NickC = Nick;
		LA[4].setText (NickC);
		update ();
	}
	
	
/*********** - Senden des Befehles und des Feldes - ***********/	
	
	public void send (String Command, String Message)
	{
		ForSending [0] = (Object) Command;
		ForSending [1] = (Object) Message;
		
		if (Command.equals ("sendSettings"))
		{
			RefreshInfo [0] = (Object) BoolSend;
			RefreshInfo [1] = (Object) IntSend;
			RefreshInfo [2] = (Object) StackSend;
			
			ForSending [1] = (Object) RefreshInfo;
		}
		
		if (Command.equals ("ServerAccepts"))	ForSending [1] = (Object) BasicUnitInfo;
		
		try
		{	
			Out.reset ();							// damit vorhandene Daten im OutputStream erst gelöscht und nicht mitgesendet werden
			Out.writeObject ((Object) ForSending);	// in OutputStream schreiben
			Out.flush ();							// alles im OutputStram verschicken
		}
		catch (IOException e2)		{System.out.println (e2);}
	}
}