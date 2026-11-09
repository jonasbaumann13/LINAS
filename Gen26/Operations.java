import java.io.*;
import java.util.Stack;

/* Methodensammlung
 */
 
public class Operations
{
/*********** - Umdrehen eines Stack - ***********/	
	public <T> Stack<T> reverse (Stack<T> tmp)
	{
		Stack<T> tmp2 = new Stack<>();
		while (!tmp.empty())
		{tmp2.push (tmp.pop());}
		return tmp2;
	}
	
/*********** - Ob Zahl gerade ist - ***********/	
	public boolean even (int tmp)
	{
        return (tmp % 2) != 1;
	}

/*********** - Umrechnung von Pixelkoordinaten zu Feldkoordinaten - ***********/	
	public int [] koAngleich (int MX, int MY)
	{
		int [] XY = new int [3];
		int X, Y;
		boolean gerade;

		X 			= (MX - 8) / 22;	// Umrechnung in Feldkoordinaten
		gerade 		= even (X);
		X 			= (X * 22);					// Eigentlich + 8, aber damit Bild richtig ist, wieder - 8

		if (!gerade)					// Unterscheidung aufgrund des Hexafeldes nötig!
		{	
			Y 		= (MY - 7) / 28;
			XY [2] 	= Y;
			Y 		= (Y * 28);					// s. o. ... + - 7
		}

		else
		{	
			Y 		= (MY - 21) / 28;
			XY [2] 	= Y;
			Y 		= (Y * 28) + 14;			// s. o. ... + 21, - 7
		}

		XY [0] 		= X;
		XY [1] 		= Y;
		return XY;
	}

/*********** - Test, ob Feld besetzt ist und gegebenenfalls ID zurückgeben - ***********/	
	public int occupied (int X, int Y, Unit [] UnitArray)
	{
		int occupied = -1;

		for (int i = 0; i < UnitArray.length; i++)
		{
			if ((UnitArray [i].PosX == X) && (UnitArray [i].PosY == Y))
			{	occupied = i;}
		}
		return occupied;
	}

/*********** - Berechnung der Entfernung in Kästchen - ***********/	
	public int distance (int X, int Y, Unit [] UnitArray, int UC)
	{
		int distance = 100;
		int tmpY = Y;
		int tmpX = X;
		
		for (int i = 0; i < 40; i++)			// 40 ist ausgewählt worden, da das Spielfeld keine größere Enfernung zulässt
		{
			if ((tmpX == UnitArray [UC].PosX) && (tmpY == UnitArray [UC].PosY))
			{	
				distance = i;
				break;							// da ist mir jetzt nichts als 'n Break eingefallen... sorry
			}
			
			if (UnitArray [UC].PosX != X)		// insgesammt wird schrittweise vorgegangen, bis das Ziel erreicht ist
			{									// die Schritte werden gezählt und zurückgegeben
				if (UnitArray [UC].PosX < X)
				{	tmpX -= 22;}
				if (UnitArray [UC].PosX > X)
				{	tmpX += 22;}
				if (UnitArray [UC].PosY <= Y)
				{	tmpY -= 14;}
				if (UnitArray [UC].PosY > Y)
				{	tmpY += 14;}
			}
			else
			{
				if (UnitArray [UC].PosY < Y)
				{	tmpY -= 28;}
				if (UnitArray [UC].PosY > Y)
				{	tmpY += 28;}
			}
			
			X = tmpX;
			Y = tmpY;
		}
		return distance;
	}
	
/*********** - Berechnung der Reichweitenmodifikation - ***********/	
	public int rwMod (int distance, Unit [] UnitArray, int UC)
	{
		int rwMod = 0;
		for (int i = 0; i < UnitArray [UC].RWtype.length; i++)
		{
			if ((distance-1) == i)	rwMod = UnitArray [UC].RWtype[i];
		}
		return rwMod;
	}
	


    /** Explicit roll supports seeded matches without changing the GUI random source. */
    public int damage(int tmpDist, Unit[] UnitArray, int UC, int percent)
    {
        if (percent < 0 || percent >= 100) throw new IllegalArgumentException("Roll must be 0..99");
        int damage = UnitArray[UC].Dam;
        if (percent > 95) return switch (tmpDist) {
            case 1, 2, 3, 4 -> damage;
            case 5 -> (int) (damage * 0.75);
            default -> 0;
        };
        if (percent > 75) return switch (tmpDist) {
            case 1, 2, 3 -> damage;
            case 4 -> (int) (damage * 0.75);
            case 5 -> (int) (damage * 0.50);
            default -> 0;
        };
        if (percent > 50) return switch (tmpDist) {
            case 1, 2 -> damage;
            case 3 -> (int) (damage * 0.75);
            case 4 -> (int) (damage * 0.50);
            case 5 -> (int) (damage * 0.25);
            default -> 0;
        };
        if (percent > 25) return switch (tmpDist) {
            case 1 -> damage;
            case 2 -> (int) (damage * 0.75);
            case 3 -> (int) (damage * 0.50);
            case 4 -> (int) (damage * 0.25);
            default -> 0;
        };
        if (percent > 5) return switch (tmpDist) {
            case 1 -> (int) (damage * 0.75);
            case 2 -> (int) (damage * 0.50);
            case 3 -> (int) (damage * 0.25);
            default -> 0;
        };
        if (percent > 0) return switch (tmpDist) {
            case 1 -> (int) (damage * 0.50);
            case 2 -> (int) (damage * 0.25);
            default -> 0;
        };
        return 0;
	}
	
/*********** - Einflussnahme von Ruestung auf den Schaden - ***********/	
	public int ArmourMod (Unit [] UnitArray, int dam, int UC, int tmpUC)
	{
		int damage = dam;
		if (UnitArray [UC].DamType.equals (UnitArray [tmpUC].ArmourType [0]))		damage = (int) (dam * UnitArray [tmpUC].Armour [0]);
		if (UnitArray [UC].DamType.equals (UnitArray [tmpUC].ArmourType [1]))		damage = (int) (dam * UnitArray [tmpUC].Armour [1]);
		if (UnitArray [UC].DamType.equals (UnitArray [tmpUC].ArmourType [2]))		damage = (int) (dam * UnitArray [tmpUC].Armour [2]);
		return damage;
	}
	
/*********** - Test, wer alles vom Gebietsschaden betroffen ist - ***********/	
	public int [] AreaDamageMod (Unit [] UnitArray, int UC, int X, int Y)
	{
		int [] ADVictims = new int [UnitArray.length];		// Feld der betroffenen
		int distance;
		for (int i = 1; i <= UnitArray [UC].RW[2]; i++)
		{
			for (int j = 0; j < UnitArray.length; j++)
			{
				distance = distance (X, Y, UnitArray, j);
				if (distance == i)
				{	ADVictims [j] = i;}						// Vermerk über Entfernung vom Zentrum	
			}
		}
		return ADVictims;
	}
	
/*********** - Modifikation der Standardwerte in Abhängigkeit zu den Trefferpunkten - ***********/	
	public void DamMod (Unit [] UnitArray, int UC)
	{
		int dMod = 0;
		for (int i = 0; i < 4; i++)
		{
			if (UnitArray [UC].TP <= UnitArray [UC].tpA[i])
			{	dMod = i+1;}
		}
		
		if (!UnitArray [UC].IsServer) 	UnitArray [UC].Picture 	= 	UnitArray [UC].pictureAC[dMod];
		else							UnitArray [UC].Picture	=	UnitArray [UC].pictureAS[dMod];
		UnitArray [UC].RW[0] 			= 	UnitArray [UC].rwA[dMod][0];
		UnitArray [UC].RW[1] 			= 	UnitArray [UC].rwA[dMod][1];
		UnitArray [UC].RW[2] 			= 	UnitArray [UC].rwA[dMod][2];
		UnitArray [UC].sight 			= 	UnitArray [UC].sightA[dMod];
		UnitArray [UC].camouflage	 	= 	UnitArray [UC].camouflageA[dMod];
		UnitArray [UC].Dam 				= 	UnitArray [UC].damA[dMod];
		UnitArray [UC].AreaDamageMod 	= 	UnitArray [UC].admA[dMod];
		UnitArray [UC].DamType 			= 	UnitArray [UC].damTypeA[dMod];
		UnitArray [UC].Armour[0] 		= 	UnitArray [UC].armourA[dMod][0];
		UnitArray [UC].Armour[1] 		= 	UnitArray [UC].armourA[dMod][1];
		UnitArray [UC].Armour[2] 		= 	UnitArray [UC].armourA[dMod][2];
		UnitArray [UC].basicZP			= 	UnitArray [UC].zpA[dMod][0];
		UnitArray [UC].ZP[1] 			= 	UnitArray [UC].zpA[dMod][1];
		UnitArray [UC].ZP[2] 			= 	UnitArray [UC].zpA[dMod][2];
		
        System.arraycopy(UnitArray[UC].rwTypeA[dMod], 0, UnitArray[UC].RWtype, 0, UnitArray[UC].rwTypeA[dMod].length);
		
		if (UnitArray [UC].ZP[0] > UnitArray [UC].basicZP)	
		{	UnitArray [UC].ZP[0] = UnitArray [UC].basicZP;}
		
		if (UnitArray [UC].TP <= 0)
		{
			if (!UnitArray [UC].obstacleAD)
			{
				UnitArray [UC].PosX += 1;
				UnitArray [UC].PosY += 1;
			}
			
			UnitArray [UC].ZP[1] = 99;
			UnitArray [UC].ZP[2] = 99;
			UnitArray [UC].sight = -1;
		}
	}
	
/*********** - Auffüllen der Zugpunkte - ***********/	
	public Unit [] newRound (Unit [] UnitArray)
	{
		for (Unit element : UnitArray)
		{	element.ZP[0] = element.basicZP;}
		return UnitArray;
	}
	
/*********** - Sichtbarkeit jeder Einheit - ***********/	
	public Unit [] UnitVisibility (Unit [] UnitArray, int P1UnitNum, boolean IsServer)
	{
		for (Unit element : UnitArray)
		{	element.visible = PointVisibility (UnitArray, P1UnitNum, IsServer, element.PosX, element.PosY, element.camouflage);}

		return UnitArray;
	}
	
/*********** - Sichtbarkeit eines Punktes (von einem Spieler) - ***********/
	public boolean PointVisibility (Unit [] UnitArray, int P1UnitNum, boolean IsServer, int x, int y, int camouflage)
	{
		int sightMod;
		int distance;
		boolean tmpVisible = false;
		if (!IsServer)
		{
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{
				sightMod = UnitArray [i1].sight - camouflage;
				distance = distance (x, y, UnitArray, i1);
				if (distance <= sightMod)
				{	tmpVisible = true;}
			}
		}
		else
		{
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{
				sightMod = UnitArray [i2].sight - camouflage;
				if ((sightMod < 1)&&(UnitArray [i2].sight > 0))		sightMod = 1;
				distance = distance (x, y, UnitArray, i2);
				if (distance <= sightMod)
				{	tmpVisible = true;}
			}
		}
		return tmpVisible;
	}
	
/*********** - Ob die Einheit sich noch bewegen kann - ***********/	
	public boolean ZPLeft (Unit [] UnitArray, int UC)
	{
		boolean zpLeft = false;
		int zp = lower (UnitArray [UC].ZP[1], UnitArray [UC].ZP[2]);
		if (UnitArray [UC].ZP[0] >= zp)
		{	zpLeft = true;}
		return zpLeft;
	}
	
/*********** - gibt kleinere Zahl zurueck - ***********/	
	public int lower (int a, int b)
	{
		return Math.min(a, b);
	}
	
/*********** - Name der beiden Spieler herausfinden - ***********/
	public String [] player (Unit [] UnitArray)
	{
		String [] player 	= new String [2];
		player [0] 			= UnitArray [0].player;
		player [1] 			= UnitArray [UnitArray.length - 1].player;
		return player;
	}
	
/*********** - Anzahl der Einheiten des jeweiligen Spielers (p) zurückgeben - ***********/
	public int UnitNum (Unit [] UnitArray, int p)
	{
		int i 	= 0;
		int UN 	= -1;
		while (UnitArray [0].player.equals (UnitArray [i].player))
		{	i++;}

		if (p == 1)	UN = i;
		if (p == 2)	UN = UnitArray.length - i;
		return UN;
	}
	
/*********** - Aufdecken des Spielfeldes, indem jede Einheit hohe Sichtweite bekommt - ***********/	
	public Unit [] seeAll (Unit [] UnitArray)
	{
		for (Unit element : UnitArray)
		{	element.sight = 60;}
		return UnitArray;
	}
	
/*********** - Aktualisieren des Einheitenfeldes (nur für linasSetFrame) - ***********/
	public Unit [] ModUnitArray (Unit [] UA1, Unit [] UA2, int P1UnitNum)
	{
		for (int i = 0; i < P1UnitNum; i++)
		{
			UA1 [i] 		= UA2 [i];
			UA1 [i].visible = false;
		}
		return UA1;
	}
	
/*********** - Einheitenfeld aus Stack erstellen - ***********/
	public Unit [] createBspUA (Stack<?> SFile)
	{
		Unit [] BspUA = new Unit [SFile.size()];
		for (int i = 0; i < SFile.size(); i++)
		{	BspUA[i] = setUnit ((File) SFile.elementAt(i), "none", true);}
		return BspUA;
	}
	
/*********** - bestimmte Einheit erstellen (nur für linasSetFrame) - ***********/
	public Unit setUnit (File sFile, String Name, boolean IsServer)
	{
		Unit UnitType;
		
		linasString [] values 	= new linasString [25];
        String germanName = null, germanDescription = null;
        int n = 0;
        try (BufferedReader BR = new BufferedReader(new InputStreamReader(
                new FileInputStream(sFile), java.nio.charset.Charset.forName("windows-1252"))))
        {
            String line;
            while ((line = BR.readLine()) != null) {
                if (!notEmpty(line)) continue;
                if (line.trim().startsWith("Unit de:")) germanName = new linasString(bigCut(line)).getS(0);
                else if (line.trim().startsWith("Description de:")) germanDescription = new linasString(bigCut(line)).getS(0);
                else if (n < values.length) values[n++] = new linasString(bigCut(line));
            }

			for (linasString element : values)
			{
				for (int i2 = 0; i2 < element.getL(); i2++)
				{	
					if (element.getS(i2).isEmpty())
					{	element.setS(i2, element.getS(0));}
				}
			}

			int [] tpA				=	toIntA (values[2]);
			int [] damA				=	toIntA (values[9]);
			int [] sightA			=	toIntA (values[16]);
			int [] camouflageA		=	toIntA (values[17]);
			int [][] zpA			=	ToInt2A (toD2A (values[18], values[19], values [20]));
			int [][] rwA			=	ToInt2A (toD2A (values[12], values[13], values [14]));
			int [][] rwTypeA		=	ToInt2A (RowToDA (values[15]));
			double [][] armourA		=	toD2A (values[6], values[7], values [8]);
			double [] admA			=	toDA (values[11]);
			String [] damTypeA		=	values[10].getAll();
			
			String [] pictureAS;
			String [] pictureAC;
			pictureAS 				= 	values[22].getAll();
			pictureAC				= 	values[23].getAll();
			
			int price 				= (int) (tD(values[1].getS(0)));
			String typeV 			= 	values[0].getS(0);
			String [] ArmourType 	= 	{values[3].getS(0), values[4].getS(0), values[5].getS(0)};
			String Description 		= 	values [24].getS(0);
			boolean obstacleAD = values[21].getS(0).equalsIgnoreCase("true");

			UnitType = new Unit (price,Name,typeV,ArmourType,Description,obstacleAD,tpA,damA,sightA,camouflageA,zpA,rwA,rwTypeA,armourA,admA,damTypeA,pictureAS,pictureAC,IsServer);
            UnitType.GermanName = germanName;
            UnitType.GermanDescription = germanDescription;
		}
		catch (IOException e)
        {
            throw new java.io.UncheckedIOException("Cannot read unit definition: " + sFile, e);
        }

		return UnitType;
	}
	


/*********** - Umwandlungen - ***********/	
	public int [] toIntA (linasString lS)
	{
		String [] s = lS.getAll ();
		int [] iA 	= new int [s.length];
		for (int i 	= 0; i < s.length; i++)
		{	iA [i] 	= (int) (tD (s [i]));}
		return iA;
	}
	
	public double [] toDA (linasString lS)
	{
		String [] s 	= lS.getAll ();
		double [] dA 	= new double [s.length];
		for (int i 		= 0; i < s.length; i++)
		{	dA [i] 		= tD (s [i]);}
		return dA;
	}
	
	public double [][] toD2A (linasString lS1, linasString lS2, linasString lS3)
	{
		double [][] d2A = new double [5][3];
		for (int i = 0; i < 5; i++)
		{	
			d2A [i][0] 	= tD (lS1.getS (i));
			d2A [i][1] 	= tD (lS2.getS (i));
			d2A [i][2] 	= tD (lS3.getS (i));
		}
		return d2A;
	}
	
	public int [][] ToInt2A (double [][] d2A)
	{
		int [][] i2A = new int [5][d2A[0].length];
		for (int i1 = 0; i1 < 5; i1++)
		{
			for (int i2 = 0; i2 < d2A[0].length; i2++)
			{	i2A [i1][i2] = (int) d2A [i1][i2];}
		}
		return i2A;
	}
	
	public double [][] RowToDA (linasString lS)
	{
		int l = (lS.getMaxS()).split (",").length;
		int [] iA;
		double [][] d2A = new double [5][l];
		for (int i1 = 0; i1 < 5; i1++)
		{
			iA = cutKomma (lS.getS(i1), l);
			for (int i2 = 0; i2 < l; i2++)
			{	d2A [i1][i2] = iA [i2];}
		}
		return d2A;
	}
	
	public int [] cutKomma (String s, int l)
	{
		String [] sA = s.split (",");
		int [] iA = new int [l];
		for (int i = 0; i < sA.length; i++)
		{	iA [i] = (int) (tD (sA [i]));}
		return iA;
	}
	
	public double tD (String s)								
	{
		return Double.parseDouble(s);
	}

/*********** - String != ""? - ***********/	
	public boolean notEmpty (String s)
	{
		return !s.isEmpty();
	}
	
/*********** - String bei @ zerlegen - ***********/		
	public String [] bigCut (String s)
	{
		String [] sA = s.split ("@");
		for (int i = 0; i < sA.length; i++)
		{	sA [i] = cutOf (sA [i]);}
		return sA;
	}
	
/*********** - Nur den String zwischen <> herausfiltern - ***********/		
	public String cutOf (String s)
	{	
		return s.substring ((s.indexOf ("<") + 1), s.indexOf (">"));
	}
	
/*********** - test, ob alle Einheiten gesetzt wurden (nur für linasSetFrame) - ***********/	
	public boolean setAll (Unit [] UnitArray, int P1UnitNum, boolean IsServer)
	{
		boolean ready = true;
		if (!IsServer)
		{
			for (int i1 = 0; i1 < P1UnitNum; i1++)
			{	if (!UnitArray [i1].visible) { ready = false; break; }}
		}
		if (IsServer)
		{
			for (int i2 = P1UnitNum; i2 < UnitArray.length; i2++)
			{	if (!UnitArray [i2].visible) { ready = false; break; }}
		}
		return ready;
	}
	
/*********** - Anzahl der Einheiten einer Art und eines Spieler (nur für linasSetFrame) - ***********/	
	public int number (Unit [] UnitArray, String Player, String Unit)
	{
		int num = 0;
		
		for (Unit element : UnitArray)
		{
			if ((element.player.equals (Player)) && ((element.type + element.DamType).equals (Unit)))
			{	num++;}
		}
		return num;
	}
	
/*********** - Double in Prozent - ***********/	
	public int perCent (double a)
	{
		return (int) (100 - 100*a);
	}
}