package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class UsersDao {
	private DataSource dataSource;
	
	public UsersDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isEmailInUse(String email) throws SQLException {
		Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            con = dataSource.getConnection();
            pst = con.prepareStatement("SELECT id FROM users WHERE email = ?");
            pst.setString(1,  email);
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
	
	public boolean isAliasInUse(String alias) throws SQLException {
		Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            con = dataSource.getConnection();
            pst = con.prepareStatement("SELECT id FROM users WHERE alias = ?");
            pst.setString(1,  alias);
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

	public boolean createUser(String email, String password, String alias) throws SQLException {
		Connection con = null;
        PreparedStatement pst = null;
          
        try {
            con = dataSource.getConnection();
            pst = con.prepareStatement("INSERT INTO users (email, password, alias) VALUES (?, ?, ?)");
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
