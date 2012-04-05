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
 * This is the main class of the program
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Lab3
{
    /**
     * Number of less that needs to be exeeded for 'less' to be used
     */
    private static final int LESS_ON = 50;
    
    
    
    /**
     * Forbidden constructor
     */
    private Lab3()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * This is the main entry point of the program
     *
     * @param  args  Command line arguments
     */
    public static void main(final String... args)
    {
	if (Database.hasDriver() == false)
	    return;
	
	
	final HashMap<String, String> argmap = new HashMap<String, String>();
	for (int i = 0, n = args.length; i < n; i++)
	{
	    final String arg = args[i];
	    if      (arg.equals("-p"))	  argmap.put(arg, args[++i]);  //password
	    else if (arg.equals("-u"))	  argmap.put(arg, args[++i]);  //username
	    else		          argmap.put(arg, null);
	}
	
	final Scanner scanner = new Scanner(System.in);
	final String username = Authorisation.getUsername(scanner, argmap);
	final String password = Authorisation.getPassword(scanner, argmap);
	
	
	for (;;)
	{
	    int action = 2;
	    try
	    {
		action = Menu.start("Vad vill du göra?"
		            ,"Lista alla kommandon (avslutas med q)"   // :0
		            ,"Utför kommandon (avslutas med tom rad)"  // :1
		            ,"Avsluta programmet"                       // :2
			);
	    }
	    catch (final IOException err)
	    {
		System.out.println("Vad vill du göra?\n");
		System.out.println("  0: Lista alla kommandon (avslutas med q)");
		System.out.println("  1: Utför kommandon (avslutas med tom rad)");
		System.out.println("  2: Avsluta programmet");
		System.out.print(":");
		final String line = scanner.nextLine();
		try
		{
		    action = Integer.parseInt(line);
		}
		catch (final Throwable ierr)
		{
		    //No int
		}
		System.out.println();
	    }
	    
	    switch (action)
	    {
		case 0:
		    Terminal.page(Command.getAllCommands());
		    break;
		    
		case 1:
		    command(scanner, username, password);
		    break;
		    
	        default:
		    System.out.println("Happy hacking!");
		    return;
	    }
	}
    }
    
    
    
    /**
     * Lets the user do some commands, until an empty line is feed
     *
     * @param  scanner   The input scanner
     * @param  username  The user's username
     * @param  password  The user's password
     */
    public static void command(final Scanner scanner, final String username, final String password)
    {
	for (;;)
	{
	    System.out.println();
	    System.out.print("\033[32m"); //green print
	    System.out.print("Skrivt ditt kommando: ");
	    System.out.print("\033[0m"); //default echo/print
	    final String command = scanner.nextLine();
	    if (command.isEmpty())
		break;
	    
	    if (query(command, username, password) == false)
	    {
		System.out.print("\033[31m"); //red print
		System.out.println("Kallar du det där ett kommando?");
		System.out.print("\033[0m"); //default print
	    }
	}
    }
    
    /**
     * Parses a query command and querys the database
     * 
     * @param   command   The command the user wants to execute
     * @param   username  The user's username
     * @param   password  The user's password
     * @return            Whether the command was recongised
     */
    public static boolean query(final String command, final String username, final String password)
    {
	final StringBuilder out = new StringBuilder();
	final ArrayList<String[][]> results = new ArrayList<String[][]>();
	String[][] result = null;
	String query = null;
	
	
	if (command.toLowerCase().startsWith("!"))
	    result = Database.query(command.substring(1) + (command.endsWith(";") ? "" : ";"), username, password);
	else
	{
	    final ArrayList<String[]> commands = Command.getSQLCommand(command);
	    if (commands.isEmpty())
		return false;
	    
	    int index = 0;
	    if (commands.size() > 1)
		try
		{
		    final String[] items = new String[commands.size() + 1];
		    for (final String[] item : commands)
			items[index++] = item[0];
		    final int abort = index;
		    items[abort] = "AVBRYT";
		    index = Menu.start("Tvetydligt...", items);
		    if (index == abort)
			return true;
		}
		catch (final IOException err)
		    { index = commands.size() - 1; }
	    
	    final String[] elements = commands.get(index);
	    index = 1;
	    final int n = elements.length;
	    final String[] strs = new String[elements.length - 2];
	    while (index < n)
	    {
		int ptr = 0;
		query = elements[index++];
		while (query.contains("@"))
		{
		    final int at    = query.indexOf('@');
		    final int dot   = query.indexOf('.', at);
		    final int space = query.indexOf(' ', dot);
		    
		    final String beginning = query.substring(0, at);
		    final String end       = query.substring(space);
		    final String midIndex  = query.substring(at + 1, dot);
		    final String midColumn = query.substring(dot + 1, space);
		    
		    int column = 0;
		    for (final String candidate : results.get(Integer.parseInt(midIndex))[0])
			if (candidate.equalsIgnoreCase(midColumn) == false)
			    column++;
			else
			    break;
		    
		    query = beginning + results.get(Integer.parseInt(midIndex))[1][column] + end;
		}
		while (index < n)
		{
		    if (elements[index] == null)
		    {
			index++;
			break;
		    }
		    strs[ptr++] = elements[index++];
		}
		final String[] params = new String[ptr];
		System.arraycopy(strs, 0, params, 0, ptr);
		results.add(Database.query(query, username, password, params));
	    }
	}
	if (result != null)
	    results.add(result);
	
	outer:
	    for (int i = 1; i < results.size(); i++)
	    {
		final String[] lastHeads = results.get(i - 1)[0];
		final String[] curHeads = results.get(i)[0];
		
		if (lastHeads.length != curHeads.length)
		    continue;
		
		for (int j = 0, m = lastHeads.length; j < m; j++)
		    if (lastHeads[j].equals(curHeads[j]) == false)
			continue outer;
		
		final String[][] table = new String[results.get(i - 1).length + results.get(i).length - 1][];
		System.arraycopy(results.get(i - 1), 0, table, 0, results.get(i - 1).length);
		System.arraycopy(results.get(i), 1, table, results.get(i - 1).length, results.get(i).length - 1);
		results.set(i, table);
		results.remove(i);
		i--;
	    }
	
	for (final String[][] table : results)
	{
	    out.append(Tables.printTable(table));
	    out.append("Tabellen innehåller " + (table.length - 1) + " rader.\n");
	}
	    
	Terminal.printMuch(out.toString(), LESS_ON);
	
	return true;
    }
    
}
