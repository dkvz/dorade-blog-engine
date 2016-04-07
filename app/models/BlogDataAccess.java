package models;

import javax.sql.*;
import java.sql.*;

import play.db.*;

public class BlogDataAccess {
	
	private static BlogDataAccess bda = new BlogDataAccess();
	
	private BlogDataAccess() {
		// Nothing here.
	}
	
	public static BlogDataAccess getInstance() {
		if(bda == null) {
			bda = new BlogDataAccess();
		}
		return bda;
	}
	
	public User getUser(long id) throws SQLException {
		User usr = null;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE id = ?");
		stmt.setLong(1, id);
        ResultSet rset = stmt.executeQuery();
        if (rset.next()) {
        	usr = new User();
            usr.setId(id);
            usr.setName(rset.getString("name"));
        }
        rset.close();
		return usr;
	}
	
}
