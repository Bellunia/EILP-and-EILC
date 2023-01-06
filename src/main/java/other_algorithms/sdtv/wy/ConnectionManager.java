package other_algorithms.sdtv.wy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for connection management
 * @author Heiko
 *
 */
public class ConnectionManager {
	private static Connection connection = null;
	
	public static synchronized Connection getConnection() {
		if(connection!=null)
			return connection;
		try {
			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {
			System.out.println("Error: could not load H2 driver");
		}
        try {
        	 connection = DriverManager.getConnection("jdbc:postgresql://localhost/yago_sdtv", "postgres", "wy123");

		} catch (SQLException e) {
			System.out.println("Error: could not initialize database");
		}
        return connection;
	}
}
