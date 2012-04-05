/**
 * Lab. 3, dbtek12, KTH
 *
 * Mattias Andrée <maandree@kth.se>, Magnus Lundberg
 *
 * COMPILE:  javac -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar *.java
 * EXECUTE:   java -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar Lab3 -u $USER -p $(echo `cat ~/Private/.psqlpw-nestor2.csc.kth.se`)
 */
//default package

import java.util.*;


/**
 * Table utility class
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Tables
{
    /**
     * Forbidden constructor
     */
    private Tables()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * Prints a nice table to a string
     *
     * @param   table  The table, the first row should be the column's titles
     * @return         The table as a string as it should be printed to the terminal
     */
    public static String printTable(final String[][] table)
    {
	final StringBuilder out = new StringBuilder();
	int n = table[0].length;
	
	final String nullString = "(null)";
	
	//Get column width
	final int[] widths = new int[n];
	for (final String[] row : table)
	    for (int i = 0; i < n; i++)
		if (widths[i] < (row[i] == null ? nullString : row[i]).length())
		    widths[i] = (row[i] == null ? nullString : row[i]).length();
	    
	//Fix cell width
	int maxwidth = 0;
	for (final int width : widths)
	    if (maxwidth < width)
		maxwidth = width;
	final char[] fillChars = new char[maxwidth];
	final char[] horzChars = new char[maxwidth];
	for (int i = 0; i < maxwidth; i++)
	{
	    fillChars[i] = ' ';
	    horzChars[i] = '─';
	}
	final String fill =  new String(fillChars);
	final String horz = (new String(horzChars)) + "──";
	for (final String[] row : table)
	    for (int i = 0; i < n; i++)
		if (row[i] != null)
		    row[i] = (row[i] + fill).substring(0, widths[i]);
	    
	//Print table
	for (int i = 0; i < n; i++)
	{
	    out.append(i == 0 ? '┌' : '┬');
	    out.append(horz.substring(0, widths[i] + 2));
	}
	out.append("┐\n");
	int j = -1;
	for (final String[] row : table)
	    if (++j == 0)
	    {
		for (int i = 0; i < n; i++)
		    out.append("\033[0m│\033[1m " + row[i] + ' '); //table[0][*] can not be (null)
		out.append("\033[0m│\n");
		if (table.length > 2)
		{
		    for (int i = 0; i < n; i++)
		    {
			out.append(i == 0 ? '├' : '┼');
			out.append(horz.substring(0, widths[i] + 2));
		    }
		    out.append("┤\n");
		}
	    }
	    else
	    {
		for (int i = 0; i < n; i++)
		    if (row[i] == null)
		    {
			String filled = nullString;
			filled += fill;
			filled = filled.substring(0, widths[i]);
			out.append("\033[0m│ " + filled + ' '); //print (null) in whith instead of coloured
		    }
		    else
		        out.append("\033[0m│\033[" + (31 + (j % 3)) + "m " + row[i] + ' ');
		out.append("\033[0m│\n");
	    }
	for (int i = 0; i < n; i++)
	{
	    out.append(i == 0 ? '└' : '┴');
	    out.append(horz.substring(0, widths[i] + 2));
	}
	out.append("┘\n");
	
	return out.toString();
    }
    
}
