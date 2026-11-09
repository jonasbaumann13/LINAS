import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;


public class linasString
{
	public String [] s;

	public linasString (String [] s)
	{
		this.s = s;			// Ob Einheit von jeweiligem Spieler gesehen werden kann
	}
	
	public int getL ()
	{
		return s.length;
	}
	
	public String getS (int i)
	{
		return s [i];
	}
	
	public void setS (int i, String a)
	{
		s [i] = a;
	}
	

	
	public String getMaxS ()
	{
		String newS = "";
		for (String value : s)
        {
            if (value.length() > newS.length()) newS = value;
        }
		return newS;
	}
	
	public String [] getAll ()
	{
		return s;
	}
	
	public String [] last ()
	{
		String [] newS = new String [s.length-1];
		System.arraycopy(s, 1, newS, 0, s.length - 1);
		return newS;
	}		
}