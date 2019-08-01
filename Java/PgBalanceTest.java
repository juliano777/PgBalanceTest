import java.io.Console;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
//import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.postgresql.util.PSQLException;

public class PgBalanceTest{

	public static void main(String[] args) throws ParseException {

		// Options
		Options options = new Options();

		/*
		 * 
		 * https://paquier.xyz/postgresql-2/postgres-10-libpq-read-write/
		 * 
		 * https://jdbc.postgresql.org/documentation/head/connect.html
		 * 
		 */

		// add -h option
		options.addOption("h", true, "Host[:port]");

		// add -d option
		options.addOption("d", true, "Database");

		// add -U option
		options.addOption("U", true, "User");

		// add -c option
		options.addOption("c", true, "SQL Command");

		// add -w option
		options.addOption("w", false, "Prompt password");
		
		// add -b option
		options.addOption("b", false, "Load balance");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		String HOST = cmd.getOptionValue("h", null);
		String DB = cmd.getOptionValue("d", "postgres");
		String USERNAME = cmd.getOptionValue("U", "postgres");
		String SQL = cmd.getOptionValue("c", "SELECT 'OK'");
		Boolean ASKPASS = (cmd.hasOption("w") ? true : false);
		Boolean LOAD_BALANCE = (cmd.hasOption("b") ? true : false);
		String str_conn = (
				HOST == null ?
				String.format("jdbc:postgresql:%s", DB)
				: String.format("jdbc:postgresql://%s/%s", HOST, DB)
				);
		
		if (LOAD_BALANCE && HOST != null) {str_conn += "?loadBalanceHosts=true";}

		Console console = System.console();
		String pw = null;

		Properties props = new Properties();
		props.setProperty("user", USERNAME);
		props.setProperty("ssl", "false");
		props.setProperty("ApplicationName", "Foo");

		if (ASKPASS) {
			pw = String.valueOf(console.readPassword("\nEnter password: "));
			props.setProperty("password", pw);
		}
		
		try {
			Connection CONN = DriverManager.getConnection(str_conn, props);
			Statement stmt = CONN.createStatement();
			
			if (LOAD_BALANCE) {
				str_conn += "?loadBalanceHosts=true";
				ResultSet rs = stmt.executeQuery(SQL);								
				
				if (rs != null && rs.next()) {
					System.out.println(rs.getString(1));
					rs.close();
				}
			} else {
				stmt.execute(SQL);
			}
			
			stmt.close();			
			CONN.close();
		}

		catch (Exception e) {
			System.out.println("Error:\n");
			e.printStackTrace();
		}
	}
}
