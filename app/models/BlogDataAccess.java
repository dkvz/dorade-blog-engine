package models;

import javax.sql.*;
import java.sql.*;
import java.util.*;

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
	
	/**
	 * Though the numbering of the "id" of article summaries starts at 1,
	 * this method expects earliest "start" value to be 0.
	 * @param start
	 * @param count
	 * @return
	 */
	public List<ArticleSummary> getArticleSummariesDescFromTo(int start, int count) {
		List<ArticleSummary> res = new ArrayList<ArticleSummary>();
		if (start < 0) start = 0;
		
		return res;
	}
	
}
