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
		String input = JOptionPane.showInputDialog(parentFrame, meldung, "Eingabe", JOptionPane.QUESTION_MESSAGE);
		return input;
	}// msgInput
	
	public int msgConfirm(String meldung, JFrame parentFrame) throws HeadlessException
	{
		int answer = JOptionPane.showConfirmDialog(parentFrame, meldung, "Bestätigung", JOptionPane.YES_NO_OPTION);
		return answer;
	}
}