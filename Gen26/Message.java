import java.awt.*;
import javax.swing.*;

class Message
{
	public void msgError(String meldung, JFrame parentFrame)
	{
		JOptionPane.showMessageDialog(parentFrame, meldung, I18n.text("Error"), JOptionPane.ERROR_MESSAGE);
	}// msgError
   
	public void msgInform(String meldung, JFrame parentFrame)
	{
		JOptionPane.showMessageDialog(parentFrame, meldung, I18n.text("Information"), JOptionPane.INFORMATION_MESSAGE);
	}// msgInform
   
	public String msgInput(String meldung, JFrame parentFrame)
	{
		return JOptionPane.showInputDialog(parentFrame, meldung, I18n.text("Input"), JOptionPane.QUESTION_MESSAGE);
	}// msgInput
	
	public int msgConfirm(String meldung, JFrame parentFrame) throws HeadlessException
	{
		return JOptionPane.showConfirmDialog(parentFrame, meldung, I18n.text("Confirmation"), JOptionPane.YES_NO_OPTION);
	}
}