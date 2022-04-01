package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class QueryMaker {
	private static final String INSERT_USER = "INSERT INTO users (email, password, alias) VALUES (?, ?, ?)";

	public static boolean checkStringFieldFromTable(DataSource datasource, String table, String field, String value) throws SQLException {
		Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            con = datasource.getConnection();
            pst = con.prepareStatement(String.format("SELECT %s FROM %s WHERE %s = ?", field, table, field));
            pst.setString(1,  value);
            rs = pst.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (pst != null) {
                pst.close();
            }

            if (con != null) {
                con.close();
            }
        }
	}

	public static boolean insertIntoUserTable(DataSource datasource, String email, String password, String alias) throws SQLException {
		Connection con = null;
        PreparedStatement pst = null;
          
        try {
            con = datasource.getConnection();
            pst = con.prepareStatement(INSERT_USER);
            pst.setString(1, email);
            pst.setString(2, password);
            pst.setString(3, alias);

            int rowsInserted = pst.executeUpdate();
            return rowsInserted > 0;
        } finally {
            if (pst != null) {
                pst.close();
            }

            if (con != null) {
                con.close();
            }
        }
	}
}
