/**
 * Lab. 3, dbtek12, KTH
 *
 * Mattias Andrée <maandree@kth.se>, Magnus Lundberg
 *
 * COMPILE:  javac -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar *.java
 * EXECUTE:   java -cp .:/info/dbtek12/lib/postgresql-9.1-901.jdbc4.jar Lab3 -u $USER -p $(echo `cat ~/Private/.psqlpw-nestor2.csc.kth.se`)
 */
//default package

import java.sql.*;
import java.util.*;


/**
 * Database façade
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 * @author  Magnus Lundberg
 */
public class Database
{
    /**
     * The SQL driver class
     */
    private static final String SQL_DRIVER = "org.postgresql.Driver";
    
    /**
     * The SQL dialect protocol
     */
    private static final String SQL_PROTOCOL = "postgresql";
    
    /**
     * The SQL host
     */
    private static final String SQL_HOST = "//nestor2.csc.kth.se";
    
    /**
     * The SQL port
     */
    private static final String SQL_PORT = "5432";
    
    /**
     * The SQL database
     */
    private static final String SQL_DATABASE = "$USER"; //"varuhuset"
    
    
    /**
     * The connection string for the database
     */
    private static  final String CONNECTION = "jdbc:" + SQL_PROTOCOL + ':' + SQL_HOST + ':' + SQL_PORT + '/' + SQL_DATABASE;
    
    
    
    /**
     * Forbidden constructor
     */
    private Database()
    {
	//Inhibit instansiation
    }
    
    
    
    /**
     * Checks whether the proper SQL driver is loadable
     *
     * @reeturn  Whether the proper SQL driver is loadable
     */
    public static boolean hasDriver()
    {
	try
	{
	    Class.forName(SQL_DRIVER);
	}
	catch (final ClassNotFoundException err)
	{
	    Terminal.printException(err, "Hittar inte JDBC-drivern");
	    return false;
	}
	return true;
    }
    
    
    /**
     * Querys the database
     *
     * @param   query     The query
     * @param   username  The user's username
     * @param   password  The user's password
     * @param   strings   String to add to the query when preparing
     * @return            A matrix of the result, where the first (index 0) row contains the columns' titles, <code>null</code> is returned on error
     */
    public static String[][] query(final String query, final String username, final String password, final String... strings)
    {
	Connection conn = null;
	ResultSet rs = null;
	
	ResultSetMetaData rsmd = null;
	PreparedStatement pstmt = null;
	int numColumns = 0;
	
	try
	{
	    try
	    {
		conn = DriverManager.getConnection(CONNECTION.replace("$USER", username), username, password);
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Kunde inte ansluta till Varuhusdatabasen"); return null; }
	    
	    
	    try
	    {
		pstmt = conn.prepareStatement(query);
		for (int i = 0, n = strings.length; i < n; i++)
		    pstmt.setString(i + 1, strings[i]);
		rs = pstmt.executeQuery();
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Kunde inte hämta från tabellen"); return null; }
	    
	    
	    try
	    {
		rsmd = rs.getMetaData();
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Kunde inte hämta metadata"); return null; }
	    
	    
	    try
	    {
		numColumns = rsmd.getColumnCount();
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Fick inte reda på antal kolumner"); return null; }
	    
	    
	    
	    try
	    {
		final ArrayDeque<String[]> rows = new ArrayDeque<String[]>();
		String[] row = new String[numColumns];
		for (int i = 1; i <= numColumns; i++)
		    row[i - 1] = (rsmd.getColumnName(i));
		rows.offer(row);
		
		while (rs.next())
		{
		    row = new String[numColumns];
		    for (int i = 1; i <= numColumns; i++)
			row[i - 1] = rs.getString(i);
		    rows.offer(row);
		}
		
		final String[][] rc = new String[rows.size()][];
		int i = 0;
		while ((row = rows.poll()) != null)
		    rc[i++] = row;
		
		return rc;
	    }
	    catch (final SQLException err)
	    {
		Terminal.printException(err, "Kunde inte hämta data ur tabellen");
		return null;
	    }
	}
	finally
	{
	    try
	    {
		if (conn != null)
		    conn.close();
	    }
	    catch (final SQLException err)
	    {
		Terminal.printException(err, "Kunde inte stänga databasanslutningen");
		return null; //At finally, and has returned
	    }
	}
    }
    
    
    /**
     * Updates the database
     *
     * @param   update    The update command
     * @param   username  The user's username
     * @param   password  The user's password
     * @param   strings   String to add to the update when preparing
     * @return            The number of updated rows, <code>-1</code> is returned on error
     */
    public static int update(final String update, final String username, final String password, final String... strings)
    {
	Connection conn = null;
	ResultSet rs = null;
	
	ResultSetMetaData rsmd = null;
	PreparedStatement pstmt = null;
	
	try
	{
	    try
	    {
		conn = DriverManager.getConnection(CONNECTION.replace("$USER", username), username, password);
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Kunde inte ansluta till Varuhusdatabasen"); return -1; }
	    
	    
	    try
	    {
		pstmt = conn.prepareStatement(update);
		for (int i = 0, n = strings.length; i < n; i++)
		    pstmt.setString(i + 1, strings[i]);
	       return  pstmt.executeUpdate();
	    }
	    catch (final SQLException err)
		{ Terminal.printException(err, "Kunde inte uppdatera tabellen"); return -1; }
	}
	finally
	{
	    try
	    {
		if (conn != null)
		    conn.close();
	    }
	    catch (final SQLException err)
	    {
		Terminal.printException(err, "Kunde inte stänga databasanslutningen");
		return -1; //At finally, and has returned
	    }
	}
    }

}
