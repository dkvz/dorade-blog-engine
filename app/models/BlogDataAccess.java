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
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE id = ?");
			stmt.setLong(1, id);
	        ResultSet rset = stmt.executeQuery();
	        if (rset.next()) {
	        	usr = new User();
	            usr.setId(id);
	            usr.setName(rset.getString("name"));
	        }
	        rset.close();
		} finally {
			conn.close();
		}
		return usr;
	}
	
	public Article getArticleByURL(String articleURL) throws SQLException {
		Article ret = null;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
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
		} finally {
			conn.close();
		}
		return ret;
	}
	
	public List<Comment> getCommentsFromTo(long start, int count, String articleURL) throws SQLException {
		List<Comment> res = new ArrayList<Comment>();
		if (start < 0) start = 0;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		// I could do this in a single statement but going to do it in two.
		// I'm using limit and offset, which are supported by postgre and MySQL (normally) but
		// not most other databases.
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT comments.id, comments.article_id, comments.author, " +
					"comments.comment, comments.date FROM comments, articles WHERE articles.article_url = ? AND articles.id = comments.article_id ORDER BY comments.id ASC " +
					"LIMIT ? OFFSET ?");
			stmt.setString(1, articleURL);
			stmt.setInt(2, count);
			stmt.setLong(3, start);
			ResultSet rset = stmt.executeQuery();
			if (rset != null) {
				while(rset.next()) {
					Comment comment = new Comment();
					comment.setAuthor(rset.getString("author"));
					comment.setComment(rset.getString("comment"));
					// Parse the date, which is a timestamp.
					comment.setDate(new java.util.Date(rset.getLong("date") * 1000));
					res.add(comment);
				}
			}
			stmt.close();
		} finally {
			conn.close();
		}
		return res;
	}
	
	/**
	 * Though the numbering of the "id" of article summaries starts at 1,
	 * this method expects earliest "start" value to be 0.
	 * @param start
	 * @param count
	 * @return
	 */
	public List<ArticleSummary> getArticleSummariesDescFromTo(long start, int count) throws SQLException {
		List<ArticleSummary> res = new ArrayList<ArticleSummary>();
		if (start < 0) start = 0;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		// I could do this in a single statement but going to do it in two.
		// I'm using limit and offset, which are supported by postgre and MySQL (normally) but
		// not most other databases.
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT id, title, article_url, thumb_image, " +
					"date, user_id, summary FROM articles " +
					"ORDER BY id DESC LIMIT ? OFFSET ?");
			// The comments are not working so comment count is constant 0.
			stmt.setInt(1, count); // LIMIT clause value
			stmt.setLong(2, start); // OFFSET is start
			ResultSet rset = stmt.executeQuery();
			if (rset != null) {
				// For SQLite the date is an integer (or a long I suppose).
				while (rset.next()) {
					ArticleSummary sum = this.getArticleSummaryFromRset(rset, conn); 
					res.add(sum);
				}
			}
			stmt.close();
		} finally {
			conn.close();
		}
		return res;
	}
	
	public long getArticleIdFromUrl(String url) throws SQLException {
		long res = -1;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT id FROM articles WHERE article_url = ?");
			// The comments are not working so comment count is constant 0.
			stmt.setString(1, url);
			ResultSet rset = stmt.executeQuery();
			if (rset != null) {
				// For SQLite the date is an integer (or a long I suppose).
				while (rset.next()) {
					res = rset.getLong("id");
				}
			}
			stmt.close();
		} finally {
			conn.close();
		}
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
	
	public long getCommentCount() throws SQLException {
		long ret = 0l;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM comments");
			if (rs.next()) {
				ret = rs.getLong(1);
			}
		} finally {
			conn.close();
		}
		return ret;
	}
	
	public long getArticleCount() throws SQLException {
		long ret = 0l;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM articles");
			if (rs.next()) {
				ret = rs.getLong(1);
			}
		} finally {
			conn.close();
		}
		return ret;
	}
	
	public void insertComment(Comment comment) throws SQLException {
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO comments " +
					"(article_id, author, comment, date, client_ip) VALUES (?, ?, ?, ?, ?)");
			stmt.setLong(1, comment.getArticleId());
			stmt.setString(2, comment.getAuthor());
			stmt.setString(3, comment.getComment());
			if (comment.getDate() == null) {
				java.util.Date now = new java.util.Date();
				comment.setDate(now);
			}
			long stamp = comment.getDate().getTime() / 1000;
			stmt.setLong(4, stamp);
			stmt.setString(5, comment.getClientIP());
			stmt.executeUpdate();
		} finally {
			conn.close();
		}
	}
	
}
