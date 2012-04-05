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
 * Authorisation utility class
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Authorisation
{

    /**
     * Forbidden constructor
     */
    private Authorisation()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * Gets the user's username
     *
     * @param   scanner  Input scanner for stdin
     * @param   argmap   Hashmap of command line arguments and there assigned value
     * @return           The username
     */
    public static String getUsername(final Scanner scanner, final HashMap<String, String> argmap)
    {
	String username = argmap.get("-u");
	if (username == null)
	{
	    System.out.print("Ange användarnamn: ");
	    System.out.print("\033[34m"); //blue echo/print
	    username = scanner.nextLine();
	    System.out.print("\033[33m"); //yellow echo/print
	    System.out.println("Tips: Lägg til argumenten (-u <USERNAME> | -u $USER) nästa gång.");
	    System.out.print("\033[0m"); //default echo/print
	}
	return username;
    }
    
    
    /**
     * Gets the user's password
     *
     * @param   scanner  Input scanner for stdin
     * @param   argmap   Hashmap of command line arguments and there assigned value
     * @return           The password
     */
    public static String getPassword(final Scanner scanner, final HashMap<String, String> argmap)
    {
	String password = argmap.get("-p");
	if (password == null)
	{
	    System.out.print("Ange lösenord: ");
	    System.out.print("\033[0;30;40m"); //black foreground and background for echo/print (invisible in some terminals)
	    Terminal.stty(Terminal.STTY_ECHO_OFF);
	    password = scanner.nextLine();
	    Terminal.stty(Terminal.STTY_ECHO_ON);
	    System.out.print("\033[0;33m"); //yellow echo/print
	    System.out.println("Tips: Lägg till argumenten (-p <PASSWORD> | -p $(echo `cat PASSWORD_FILE`)) nästa gång.");
	    System.out.print("\033[0m"); //default echo/print
	}
	return password;
    }

}
