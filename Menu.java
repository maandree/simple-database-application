/**
 * Lab. 3, dbtek12, KTH
 *
 * Mattias Andrée <maandree@kth.se>, Magnus Lundberg
 *
 * COMPILE: javac -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar *.java
 * EXECUTE:  java -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar Lab3 -u $USER -p $(echo `cat ~/Private/.psqlpw-nestor2.csc.kth.se`)
 */
//default package

import java.io.*;
import java.util.*;


/**
 * Interative menu class
 */
public class Menu
{
    /**
     * Forbidden constructor
     */
    private Menu()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * Creats a menu and lets the users select one items
     * 
     * @param   title  The menu title
     * @param   items  Menu list items
     * @return         The selected items index, -1 on end of stream
     */
    public static int start(final String title, final String... items) throws IOException
    {
	final String itty = " < " + (new File("/dev/stdin")).getCanonicalPath();
	final String otty = " > " + (new File("/dev/stdout")).getCanonicalPath();
	final String etty = " 2> " + (new File("/dev/stderr")).getCanonicalPath();
	final String atty = itty + otty + etty;
	
	int selected = 0;
	exec("stty -icanon -echo" + atty);
	
	try
	{
	    System.out.print("\033c");
	    System.out.print("\033[?25l");
	    System.out.println("\033[1m" + title + "\033[0m\n");
		
	    for (int i = 0, n = items.length; i < n; i++)
	    {
		if (i == selected)
		    System.out.print("\033[1;34m> ");
		else
		    System.out.print("  ");
		System.out.print(items[i]);
		if (i == selected)
		    System.out.print("\033[0m");
		System.out.println();
	    }
	    
	    for (int d = 0;;)
	    {
		d = System.in.read();
		if (d == -1)
		    return -1;
		if (d == 0x41) //up (sloppy)
		{
		    int prev = selected;
		    selected--;
		    if (selected == -1)
			selected += items.length;
		    System.out.print("\0337\033[" + (prev + 3) + ";0H  ");
		    System.out.print(items[prev]);
		    System.out.print("\033[" + (selected + 3) + ";0H\033[1;34m> ");
		    System.out.print(items[selected]);
		    System.out.print("\033[0m\0338");
		}
		if (d == 0x42) //down (sloppy)
		{
		    int prev = selected;
		    selected++;
		    if (selected == items.length)
			selected = 0;
		    System.out.print("\0337\033[" + (prev + 3) + ";0H  ");
		    System.out.print(items[prev]);
		    System.out.print("\033[" + (selected + 3) + ";0H\033[1;34m> ");
		    System.out.print(items[selected]);
		    System.out.print("\033[0m\0338");
		}
		if (d == 10)
		{
		    System.out.print("\033[?25l");
		    exec("stty -icanon -echo" + atty);
		    return selected;
		}
	    }
	}
	finally
	{
	    System.out.print("\033[?25h\033c");
	    exec("stty icanon echo" + atty);
	    return selected;
	}
    }
    
    
    
    /**
     * Runs a bash command (a program)
     * 
     * @param   cmd          The command, can be a program pipeline with tty redirections
     * @throws  IOException  On total failure
     */
    private static String exec(final String cmd) throws IOException
    {
	final Process process = (new ProcessBuilder("/bin/sh", "-c", cmd)).start();
	final InputStream stream = process.getInputStream();
	String rcs = new String();
	
	int c;
	while (((c = stream.read()) != '\n') && (c != -1))
	    rcs += (char)c;
	
	return rcs;
    }
    
}
