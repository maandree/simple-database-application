/**
 * Lab. 3, dbtek12, KTH
 *
 * Mattias Andrée <maandree@kth.se>, Magnus Lundberg
 *
 * COMPILE:  javac -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar *.java
 * EXECUTE:   java -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar Lab3 -u $USER -p $(echo `cat ~/Private/.psqlpw-nestor2.csc.kth.se`)
 * TEST:      java -cp . Command
 */
//default package

import java.util.regex.*;
import java.util.*;
import java.io.*;


/**
 * This is the main class of the program
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Command
{
    /**
     * Forbidden constructor
     */
    private Command()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * List of possible commands from the user
     */
    private static final ArrayList<String> inCommands = new ArrayList<String>();

    /**
     * List of possible commands from the user converted to regular expression
     */
    private static final ArrayList<String> regexCommands = new ArrayList<String>();

    /**
     * List of commands to the database, indexwise paired with {@link #inCommands}
     */
    private static final ArrayList<String> outCommands = new ArrayList<String>();
    
    
    
    /**
     * Class initialiser
     */
    static
    {
	InputStream is = null;
	try
	{
	    is = new BufferedInputStream(new FileInputStream(new File("simple_commands")));
	    final Scanner sc = new Scanner(is);
	    
	    String command = null;
	    StringBuilder sql = new StringBuilder();
	    
	    while (sc.hasNext())
	    {
		final String line = sc.nextLine().replace('\t', ' ');
		if (line.isEmpty())
		    continue;
		if (line.charAt(0) == ' ')
		{
		    sql.append(line);
		    sql.append('\n');
		}
		else
		{
		    if (command != null)
		    {
			while (command.endsWith(" "))
			    command = command.substring(0, command.length() - 1);
			inCommands.add(command);
			outCommands.add(sql.toString());
			regexCommands.add(toRegex(command));
		    }
		    command = line;
		    sql = new StringBuilder();
		}
	    }
	    
	    if (command != null)
	    {
		while (command.endsWith(" "))
		    command = command.substring(0, command.length() - 1);
		inCommands.add(command);
		outCommands.add(sql.toString());
		regexCommands.add(toRegex(command));
	    }
	}
	catch (final Throwable err)
	{
	    Terminal.printException(err, "Kunde inte ladda in enkla kommandon från filen simple_commands");
	}
	finally
	{
	    if (is != null)
	        try
		{
		    is.close();
		}
		catch (final Throwable err)
		{
		    //Ignore error
		}
	}
    }
    
    
    
    /**
     * This is a test entry point of the program for testing
     * the simple commands; which are loaded and what their
     * corresponding SQL commands are.
     *
     * @param  args  Command line arguments (unused)
     */
    public static void main(final String... args)
    {
	final Scanner sc = new Scanner(System.in);
	for (;;)
	{
	    final String cmd = sc.nextLine();
	    if (cmd.isEmpty())
	    {
		System.out.print("\033c");
		continue;
	    }
	    if (cmd.equals("q"))
		return;
	    if (cmd.equals("?"))
	    {
		Terminal.page(getAllCommands());
		continue;
	    }
	    
	    final ArrayList<String[]> result = getSQLCommand(cmd);
	    if (result.isEmpty())
		System.out.println("\033[31m" + "Det finns inget sådant kommando");
	    else
		for (final String[] elements : result)
		{
		    int index = 0;
		    final int n = elements.length;
		    System.out.print("\033[33m");  System.out.println(elements[index++]);
		    while (index < n)
		    {
			System.out.print("\033[32m");  System.out.println(elements[index++]);
			System.out.print("\033[35m");
			while (index < n)
			{
			    if (elements[index] == null)
			    {
				index++;
				break;
			    }
			    System.out.println("? = " + elements[index++]);
			}
		    }
		}
	    System.out.print("\033[0m");
	}
    }
    
    
    
    /**
     * Returns a string with all commands
     *
     * @return  All commands
     */
    public static String getAllCommands()
    {
	final StringBuilder rc = new StringBuilder();
	
	for (final String line : inCommands)
	{
	    rc.append(line);
	    rc.append('\n');
	}
	
	rc.append("vad har $[{, $}] och $ skrivit tillsammans\n");
	rc.append("lägg till artikeln $ till # med författaren #\n");
	rc.append("lägg till artikeln $ till # med författarna #[{, #}] och #\n");
	rc.append("lägg till artikeln $ till #/# för # med författaren #\n");
	rc.append("lägg till artikeln $ till #/# för # med författarna #[{, #}] och #\n");
	
	return rc.toString();
    }
    
    /**
     * Converts a simple pattern to regular expression
     *
     * @param   pattern  The simple pattern
     * @return           The pattern in regular expression
     */
    private static String toRegex(final String pattern)
    {
	final StringBuilder regex = new StringBuilder("\\A");
	for (int i = 0, n = pattern.length(); i < n; i++)
	{
	    final char c = pattern.charAt(i);
	    if (c == '$')
		regex.append("(.+)");
	    else if (c == '#')
		regex.append("(\\d+)");
	    else if (('a' <= c) && (c <= 'z'))  regex.append("[" + c + (char)(c - 'a' + 'A') + "]");
	    else if (('A' <= c) && (c <= 'Z'))  regex.append("[" + (char)(c - 'A' + 'a') + c + "]");
	    else if ((c == 'å') || (c == 'Å'))  regex.append("[åÅ]");
	    else if ((c == 'ä') || (c == 'Ä'))  regex.append("[äÄ]");
	    else if ((c == 'ö') || (c == 'Ö'))  regex.append("[öÖ]");
	    else if ("<>()[]{}\\!?+-=^*|.".indexOf(c) >= 0)
	    {
		regex.append('\\');
		regex.append(c);
	    }
	    else
		regex.append(c);
	}
	regex.append("\\z");
	return regex.toString();
    }
    
    /**
     * Converts a regular expression to regular expression with ignored casing
     *
     * @param   pattern  The pattern in regular expression
     * @return           The pattern in regular expression with ignored casing
     */
    private static String ignoreCaseRegex(final String pattern)
    {
	final StringBuilder regex = new StringBuilder();
	for (int i = 0, n = pattern.length(); i < n; i++)
	{
	    final char c = pattern.charAt(i);
	    if (c == '\\')
	    {
		i++;
		regex.append(c);
		regex.append(pattern.charAt(i));
	    }
	    else if (('a' <= c) && (c <= 'z'))  regex.append("[" + c + (char)(c - 'a' + 'A') + "]");
	    else if (('A' <= c) && (c <= 'Z'))  regex.append("[" + (char)(c - 'A' + 'a') + c + "]");
	    else if ((c == 'å') || (c == 'Å'))  regex.append("[åÅ]");
	    else if ((c == 'ä') || (c == 'Ä'))  regex.append("[äÄ]");
	    else if ((c == 'ö') || (c == 'Ö'))  regex.append("[öÖ]");
	    else
		regex.append(c);
	}
	return regex.toString();
    }
    
    /**
     * Greedy group capturing procedure (problems with subgroup capturing in Java 1.6)
     * 
     * @param   text     The feed text
     * @param   pattern  The matching pattern
     * @return           Captured groups
     */
    private static ArrayList<String> getGroups(final String text, final String pattern)
    {
	final ArrayList<String> groups = new ArrayList<String>();
	String t = text;
	String p = pattern;
	
	for (;;)
	{
	    int hash   = p.indexOf('#');
	    int dollar = p.indexOf('$');
	    if ((hash < 0) && (dollar < 0))
		break;
	    
	    if (hash   < 0)  hash   >>>= 1;
	    if (dollar < 0)  dollar >>>= 1;

	    if (p.length() == 1)
	    {
		groups.add(t);
		break;
	    }
	    
	    t = t.substring(hash < dollar ? hash : dollar);
	    p = p.substring(hash < dollar ? hash : dollar);
	    
	    if (hash < dollar)
	    {
		int index = 0;
		while ((index < t.length()) && ('0' <= t.charAt(index)) && (t.charAt(index) <= '9'))
		    index++;
		groups.add(Integer.toString(Integer.parseInt(t.substring(0, index), 10)));
		t = t.substring(index);
		p = p.substring(1);
	    }
	    else
	    {
		int nextHash   = p.indexOf('#', 1);
		int nextDollar = p.indexOf('$', 1);
		if ((nextHash < 0) && (nextDollar < 0))
		{
		    groups.add(t.substring(0, t.length() - p.length() + 1));
		    break;
		}
		
		if (nextHash   < 0)  nextHash   >>>= 1;
		if (nextDollar < 0)  nextDollar >>>= 1;
		final int next = nextHash < nextDollar ? nextHash : nextDollar;
		final String part = p.substring(1, next);
		
		int index = t.indexOf(part);
		final String regex = toRegex(p.substring(1));
		while (t.substring(index).matches(regex) == false)
		    index = t.indexOf(part, index + 1);
		groups.add(t.substring(0, index));
		t = t.substring(index);
		p = p.substring(1);
	    }
	}
	
	return groups;
    }
    
    /**
     * Gets the SQL command for the input command
     *
     * @param   cmd  The input command
     * @return       List of possibilities {input parsing, SQL command, SQL command strings...}
     */
    public static ArrayList<String[]> getSQLCommand(final String cmd)
    {
	final ArrayList<String[]> result = new ArrayList<String[]>();
	
	//simple patterns
	int index = 0;
	for (final String regex : regexCommands)
	{
	    if (cmd.matches(regex))
	    {
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(cmd);
		    
		final String out = outCommands.get(index);
		final StringBuilder sql = new StringBuilder();
		final ArrayList<String> strs = new ArrayList<String>();
		
		final ArrayList<String> groups = getGroups(cmd, inCommands.get(index));
		
		String incmd = inCommands.get(index);
		for (int pos = 0, group = 0;;)
		{
		    int hash   = incmd.indexOf('#', pos);
		    int dollar = incmd.indexOf('$', pos);
		    if ((hash < 0) && (dollar < 0))
			break;

		    if (hash   < 0)  hash   >>>= 1;
		    if (dollar < 0)  dollar >>>= 1;
		    
		    if (hash < dollar)
			incmd = incmd.substring(0, hash) + "\033[2m" + groups.get(group) + "\033[22m" + incmd.substring(hash + 1);
		    else
			incmd = incmd.substring(0, dollar) + "\033[4m" + groups.get(group) + "\033[24m" + incmd.substring(dollar + 1);
		    
		    pos = (hash < dollar ? hash : dollar) + groups.get(group++).length();
		}
		
		int group = 0;
		char c;
		for (int i = 0, n = out.length(); i < n; i++)
		    if ((c = out.charAt(i)) == '?')
		    {
			sql.append(c);
			
			String specgroup = new String();
			while (('0' <= out.charAt(i + 1)) && (out.charAt(i + 1) <= '9'))
			    specgroup += out.charAt(++i);
			
			if (specgroup.isEmpty())
			    strs.add(groups.get(group++));
			else
			    strs.add(groups.get(Integer.parseInt(specgroup)));
		    }
		    else if (c == '#')
		    {
			String specgroup = new String();
			while (('0' <= out.charAt(i + 1)) && (out.charAt(i + 1) <= '9'))
			    specgroup += out.charAt(++i);
			
			if (specgroup.isEmpty())
			    sql.append(groups.get(group++));
			else
			    sql.append(groups.get(Integer.parseInt(specgroup)));
		    }
		    else
			sql.append(c);
		
		final String[] elements = new String[strs.size() + 2];
		elements[0] = incmd;
		elements[1] = sql.toString();
		int i = 2;
		for (final String str : strs)
		    elements[i++] = str;
		result.add(elements);
	    }
	    index++;
	}
	
	//complex patterns
	if (cmd.matches(ignoreCaseRegex("%Avad har (.+)(, (.+))* och (.+) skrivit tillsammans%z".replace("%", "\\"))))
	    result.add(parseQueryWrote(cmd));
	else if (cmd.matches(ignoreCaseRegex("%Alägg till artikeln (.+) till (%d+) med författaren (%d+)%z".replace("%", "\\"))))
	    result.add(parseAddArticle(cmd, "författaren", false));
	else if (cmd.matches(ignoreCaseRegex("%Alägg till artikeln (.+) till (%d+) med författarna (%d+)(, (%d+))* och (%d+)%z".replace("%", "\\"))))
	    result.add(parseAddArticle(cmd, "författarna", true));
	else if (cmd.matches(ignoreCaseRegex("%Alägg till artikeln (.+) till (%d+)/(%d+) för (%d+) med författaren (%d+)%z".replace("%", "\\"))))
	    result.add(parseAddNrArticle(cmd, "författaren", false));
	else if (cmd.matches(ignoreCaseRegex("%Alägg till artikeln (.+) till (%d+)/(%d+) för (%d+) med författarna (%d+)(, (%d+))* och (%d+)%z".replace("%", "\\"))))
	    result.add(parseAddNrArticle(cmd, "författarna", true));
	
	return result;
    }
    
    /**
     * Parses the command: vad har $[{, $}] och $ skrivit tillsammans
     *
     * @param   cmd  The input command
     * @return       Possibility {input parsing, SQL command, SQL command strings...}
     */
    private static String[] parseQueryWrote(final String cmd)
    {
	//vad har ${, $} och $ skrivit tillsammans
	
	String t = cmd.substring("vad har ".length());
	t = t.substring(0, t.length() - " skrivit tillsammans".length());
	final String listing = t;

	final ArrayList<String> args = parseListing(listing);
	
	//Parsing display
	String parsed = "vad har ";
	for (int i = 0, n = args.size(); i < n; i++)
	    if (i == 0)           parsed += "\033[4m" + args.get(i) + "\033[24m";
	    else if (i + 1 == n)  parsed += " och \033[4m" + args.get(i) + "\033[24m";
	    else                  parsed += ", \033[4m" + args.get(i) + "\033[24m";
	parsed += " skrivit tillsammans";
	
	//SQL command
	String sql = "    SELECT * FROM artikel NATURAL JOIN\n    (\n";
	for (int i = 0, n = args.size(); i < n; i++)
	{
	    if (i > 0)
		sql += "      INTERSECT\n";
	    sql += "      (\n";
	    sql += "        SELECT artid FROM artikel NATURAL JOIN skrev NATURAL JOIN författare\n";
	    sql += "        WHERE författare.namn = ?\n";
	    sql += "      )\n";
	}
	sql += "    ) AS foobar;\n";
	
	//Return:  parsed . sql . @args
	String[] rc = new String[args.size() + 2];
	rc[0] = parsed;
	rc[1] = sql;
	int index = 2;
	for (final String arg : args)
	    rc[index++] = arg;
	
	return rc;
    }
    
    /**
     * Parses the command: lägg till artikeln $ till # med (författaren #|författarna #[{, #} och #])
     *
     * @param   cmd       The input command
     * @param   plursing  The the constant literal separating the the possibilites
     * @param   plur      Whether the pattern matches the with one with listing in the end
     * @return            Possibility {input parsing, SQL command, SQL command strings..., [null, SQL command, SQL command strings...]*}
     */
    private static String[] parseAddArticle(final String cmd, final String plursing, final boolean plur)
    {
	//lägg till artikel $ till # med %plursing #[[{, #}] och #]
	
	String t = cmd.substring("lägg till artikeln ".length());
	final String listing = t.substring(t.toLowerCase().lastIndexOf(plursing + ' ') + (plursing + ' ').length());
	t = t.substring(0, t.toLowerCase().lastIndexOf(" med " + plursing + ' '));
	//$ till #
	
	final int pos = t.toLowerCase().lastIndexOf(" till ");
	final ArrayList<String> args = new ArrayList<String>();
	args.add(t.substring(0, pos));
	args.add(t.substring(pos + " till ".length()));
	if (plur)
	    args.addAll(parseListing(listing));
	else
	    args.add(listing);
	
	//Parsing display
	String parsed = "lägg till artikeln ";
	for (int i = 0, n = args.size(); i < n; i++)
	    if (i == 0)           parsed += "\033[4m" + args.get(i) + "\033[24m till ";
	    else if (i == 1)      parsed += "\033[2m" + args.get(i) + "\033[22m med " + plursing + ' ';
	    else if (i == 2)      parsed += "\033[2m" + args.get(i) + "\033[22m";
	    else if (i + 1 == n)  parsed += " och \033[2m" + args.get(i) + "\033[22m";
	    else                  parsed += ", \033[2m" + args.get(i) + "\033[22m";
	
	//Return:  parsed . sql . p@args . [null . sql]*
	String[] rc = new String[3 + (args.size() - 2) * 2];
	rc[0] = parsed;
	rc[1] = "    INSERT INTO artikel (arttitel, nrid) VALUES (?, #) RETURNING *;\n".replace("#", args.get(1));
	rc[2] = args.get(0);
	int index = 3;
	for (int i = 2, n = args.size(); i < n; i++)
	{
	    rc[index++] = null;
	    rc[index++] = "    INSERT INTO skrev (artid, fid) VALUES (@0.artid, #) RETURNING *;\n".replace("#", args.get(i));
	}
	
	return rc;
    }
    
    /**
     * Parses the command: lägg till artikeln $ till #/# för # med (författaren #|författarna #[{, #} och #])
     *
     * @param   cmd       The input command
     * @param   plursing  The the constant literal separating the the possibilites
     * @param   plur      Whether the pattern matches the with one with listing in the end
     * @return            Possibility {input parsing, SQL command, SQL command strings..., [null, SQL command, SQL command strings...]*}
     */
    private static String[] parseAddNrArticle(final String cmd, final String plursing, final boolean plur)
    {
	//lägg till artikel $ till #/# för # med %plursing #[[{, #}] och #]
	
	String t = cmd.substring("lägg till artikeln ".length());
	final String listing = t.substring(t.toLowerCase().lastIndexOf(plursing + ' ') + (plursing + ' ').length());
	t = t.substring(0, t.toLowerCase().lastIndexOf(" med " + plursing + ' '));
	//$ till #/# för #

	final ArrayList<String> args = new ArrayList<String>();
	int pos = t.toLowerCase().lastIndexOf(" till ");
	args.add(t.substring(0, pos));
	t = t.substring(pos + " till ".length());
	//#/# för #
	pos = t.toLowerCase().lastIndexOf("/");
	args.add(t.substring(0, pos));
	t = t.substring(pos + "/".length());
	//# för #
	pos = t.toLowerCase().lastIndexOf(" för ");
	args.add(t.substring(0, pos));
	args.add(t.substring(pos + " för ".length()));
	if (plur)
	    args.addAll(parseListing(listing));
	else
	    args.add(listing);
	
	//Parsing display
	String parsed = "lägg till artikeln ";
	for (int i = 0, n = args.size(); i < n; i++)
	    if (i == 0)           parsed += "\033[4m" + args.get(i) + "\033[24m till ";
	    else if (i == 1)      parsed += "\033[2m" + args.get(i) + "\033[22m/";
	    else if (i == 2)      parsed += "\033[2m" + args.get(i) + "\033[22m för ";
	    else if (i == 3)      parsed += "\033[2m" + args.get(i) + "\033[22m med " + plursing + ' ';
	    else if (i == 4)      parsed += "\033[2m" + args.get(i) + "\033[22m";
	    else if (i + 1 == n)  parsed += " och \033[2m" + args.get(i) + "\033[22m";
	    else                  parsed += ", \033[2m" + args.get(i) + "\033[22m";
	
	//First SQL command
	String sql = "    INSERT INTO artikel (arttitel, nrid) VALUES (?,\n";
	sql += "    (\n";
	sql += "      SELECT nrid FROM nummer WHERE pubår = " + args.get(1);
	sql += " AND årnr = " + args.get(2);
	sql += " AND tsid = " + args.get(3);
	sql += "\n    )) RETURNING *;\n";
	
	//Return:  parsed . sql . p@args . [null . sql]*
	String[] rc = new String[3 + (args.size() - 4) * 2];
	rc[0] = parsed;
	rc[1] = sql;
	rc[2] = args.get(0);
	int index = 3;
	for (int i = 4, n = args.size(); i < n; i++)
	{
	    rc[index++] = null;
	    rc[index++] = "    INSERT INTO skrev (artid, fid) VALUES (@0.artid, #) RETURNING *;\n".replace("#", args.get(i));
	}
	
	return rc;
    }
    
    /**
     * Parses an item listing of the pattern:  $[[{, $}] och $]
     *
     * @param   list  The item listing
     * @return        A list with all the items
     */
    private static ArrayList<String> parseListing(final String list)
    {
	final ArrayList<String> rc = new ArrayList<String>();
	
	final int pos = list.toLowerCase().lastIndexOf(" och ");
	final String[] first = list.substring(0, pos).split(", ");
	for (final String item : first)
	    rc.add(item);
	rc.add(list.substring(pos + " och ".length()));
	
	return rc;
    }
    
}

