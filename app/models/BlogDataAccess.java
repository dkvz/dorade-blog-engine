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
	
	public Article getArticleByURL(String articleURL) throws SQLException {
		Article ret = null;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM articles WHERE article_url = ?");
		stmt.setString(1, articleURL);
		stmt.setMaxRows(1);
		ResultSet rset = stmt.executeQuery();
		if (rset != null && rset.next()) {
			ArticleSummary sum = this.getArticleSummaryFromRset(rset, conn);
			if (sum.getId() >= 0l) {
				ret = new Article(sum);
				ret.setContent(rset.getString("content"));
			}
		}
		stmt.close();
		return ret;
	}
	
	/**
	 * Though the numbering of the "id" of article summaries starts at 1,
	 * this method expects earliest "start" value to be 0.
	 * @param start
	 * @param count
	 * @return
	 */
	public List<ArticleSummary> getArticleSummariesDescFromTo(int start, int count) throws SQLException {
		List<ArticleSummary> res = new ArrayList<ArticleSummary>();
		if (start < 0) start = 0;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		// I could do this in a single statement but going to do it in two.
		// I'm using limit and offset, which are supported by postgre and MySQL (normally) but
		// not most other databases.
		PreparedStatement stmt = conn.prepareStatement("SELECT title, article_url, thumb_image, " +
				"date, user_id, summary FROM articles " +
				"ORDER BY id DESC LIMIT ? OFFSET ?");
		// The comments are not working so comment count is constant 0.
		stmt.setInt(1, count); // LIMIT clause value
		stmt.setInt(2, start); // OFFSET is start
		ResultSet rset = stmt.executeQuery();
		if (rset != null) {
			// For SQLite the date is an integer (or a long I suppose).
			while (rset.next()) {
				ArticleSummary sum = this.getArticleSummaryFromRset(rset, conn); 
				res.add(sum);
			}
		}
		stmt.close();
		return res;
	}
	
	private ArticleSummary getArticleSummaryFromRset(ResultSet rset, Connection conn) throws SQLException {
		ArticleSummary sum = new ArticleSummary();
		// Get the author:
		String author = "Anonymous";
		try {
			PreparedStatement st = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
			st.setInt(1, rset.getInt("user_id"));
			st.setMaxRows(1);
			ResultSet usrSet = st.executeQuery();
			if (usrSet.next()) {
				author = usrSet.getString("name");
			}
			st.close();
		} catch (SQLException ex) {
			// Do nothing, author already set to Anonymous by default.
			ex.printStackTrace();
		}
		sum.setAuthor(author);
		sum.setCommentsCount(0);
		long dateVal = rset.getLong("date");
		java.util.Date date = new java.util.Date(dateVal * 1000);
		sum.setDate(date);
		sum.setId(rset.getLong("id"));
		sum.setSummary(rset.getString("summary"));
		sum.setArticleURL(rset.getString("article_url"));
		sum.setThumbImage(rset.getString("thumb_image"));
		sum.setTitle(rset.getString("title"));
		// Get the tags.
		List<ArticleTag> artTags = new ArrayList<ArticleTag>();
		try {
			PreparedStatement st = conn.prepareStatement("SELECT tags.name FROM article_tags, tags, articles " +
					"WHERE article_tags.article_id = ? AND article_tags.tag_id = tags.id");
			st.setLong(1, sum.getId());
			ResultSet tags = st.executeQuery();
			while (tags.next()) {
				ArticleTag t = new ArticleTag();
				t.setId(tags.getLong("id"));
				t.setName(tags.getString("name"));
				artTags.add(t);
			}
			st.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		sum.setTags(artTags);
		return sum;
	}
	
}
