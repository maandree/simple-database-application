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
import java.io.*;


/**
 * Terminal utility class
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Terminal
{
    /**
     * Do not echo input, useful for passwords and graphics
     */
    public static final String STTY_ECHO_OFF   = "-echo";
    
    /**
     * Do echo input, standard
     */
    public static final String STTY_ECHO_ON    = "echo";
    
    /**
     * Retrieve characters when typed, useful for menus and other graphics
     */
    public static final String STTY_ICANON_OFF = "-icanon";
    
    /**
     * Retrieve characters on new line, standard
     */
    public static final String STTY_ICANON_ON  = "icanon";
    
    
    
    /**
     * Forbidden constructor
     */
    private Terminal()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * Sets some settings to the TTY
     *
     * @param  flags  Flags to use (settings to use/not use)
     */
    public static void stty(final String... flags)
    {
	try
	{
	    final StringBuilder cmd = new StringBuilder("stty");
	    for (final String flag : flags)
	    {
		cmd.append(' ');
		cmd.append(flag);
	    }
	    cmd.append(" > /dev/null 2> /dev/null < ");
	    cmd.append((new File("/dev/stdout")).getCanonicalPath());
	    
	    final Process process = (new ProcessBuilder("/bin/sh", "-c", cmd.toString())).start();
	    final InputStream stream = process.getInputStream();
	    
	    for (;;)
		if (stream.read() == -1)
		    break;
	}
	catch (final Throwable err)
	{
	    //Ignore exception, this is not importent and does work if you have coreutils and use a tty
	}
    }
    
    
    /**
     * Prints information about a cought exception
     *
     * @param  err      The exception
     * @param  message  Description without final punctuation
     */
    public static void printException(final Throwable err, final String message)
    {
	System.err.print("\033[1;31m"); //bold read echo/print
	System.err.println("FEL: " + message + '!');
	System.err.print("\033[0;35m"); //bold lilac echo/print
	System.err.println(err);
	System.err.print("\033[0m"); //default echo/print
    }
    
    
    /**
     * Prints lines to the terminal using a pager if necessary
     *
     * @param  text  The text to print
     * @param  less  Whether to print using 'less' if the number of lines exeeds the value
     */
    public static void printMuch(final String text, final int less)
    {
	int lines = 1 + text.length() - text.replace("\n", "").length();
	
	if ((lines > less) == false)
	    System.out.print(text);
	else
	    page(text);
    }

    /**
     * Prints lines to the terminal using a pager
     *
     * @param  text  The text to print
     */
    public static void page(final String text)
    {
	try
	{
	    String cmd = "less -r > " + (new File("/dev/stdout")).getCanonicalPath();
	    
	    final Process process = (new ProcessBuilder("/bin/sh", "-c", cmd)).start();
	    final InputStream stream = process.getErrorStream();
	    final OutputStream out = process.getOutputStream();
	    
	    out.write(text.getBytes("UTF-8"));
	    out.flush();
	    out.close();
	    
	    for (;;)
		if (stream.read() == -1)
		    break;
	}
	catch (final Throwable err)
	{
	    if (err.toString().equals("java.io.IOException: Broken pipe") == false)
		System.out.print(text);
	}
    }

}
