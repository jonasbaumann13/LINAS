import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

class Message
{
	public void msgError(String meldung, JFrame parentFrame)
	{
		JOptionPane.showMessageDialog(parentFrame, meldung, "Fehler", JOptionPane.ERROR_MESSAGE);
	}// msgError
   
	public void msgInform(String meldung, JFrame parentFrame)
	{
		JOptionPane.showMessageDialog(parentFrame, meldung, "Information", JOptionPane.INFORMATION_MESSAGE);
	}// msgInform
   
	public String msgInput(String meldung, JFrame parentFrame) throws IOException
	{
		String name = JOptionPane.showInputDialog(parentFrame, meldung, "Eingabe", JOptionPane.QUESTION_MESSAGE);
		return name;
	}// msgInput
}