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
	public List<ArticleSummary> getArticleSummariesDescFromTo(long start, int count, String tags) throws SQLException {
		return this.getArticleSummariesFromTo(start, count, tags, "desc");
	}
	
	public List<Article> getShortsFromTo(long start, int count, String tags, String order) throws SQLException {
		List<Article> res = new ArrayList<>();
		if (start < 0) start = 0;
		// This all should be refactored as it's used in the next method as well.
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			String sql = "SELECT articles.id, articles.title, " +
				"articles.article_url, articles.thumb_image, articles.date, " +
				"articles.user_id, articles.summary, articles.content FROM articles ";
			String[] tagsA = null;
			if (tags != null && !tags.isEmpty()) {
				sql = sql.concat(", article_tags, tags WHERE articles.published = 1 AND articles.short = 1");
				tagsA = tags.split(",");
				for (int a = 0; a < tagsA.length; a++) {
					sql = sql.concat(" AND tags.name = ?");
				}
				// Adding the join code:
				sql = sql.concat(" AND (tags.id = article_tags.tag_id AND " +
						"article_tags.article_id = articles.id) ");
			} else {
				sql = sql.concat("WHERE articles.published = 1 AND articles.short = 1 ");
			}
			if (order.toLowerCase().contains("asc")) {
				sql = sql.concat("ORDER BY articles.id ASC ");
			} else {
				sql = sql.concat("ORDER BY articles.id DESC ");
			}
			sql = sql.concat("LIMIT ? OFFSET ?");
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			int pos = 0;
			if (tagsA != null && tagsA.length > 0) {
				for (pos = 0; pos < tagsA.length; pos++) {
					stmt.setString(pos + 1, tagsA[pos]);
				}
			}
			stmt.setInt(pos + 1, count); // LIMIT clause value
			stmt.setLong(pos + 2, start); // OFFSET is start
			ResultSet rset = stmt.executeQuery();
			if (rset != null) {
				// For SQLite the date is an integer (or a long I suppose).
				while (rset.next()) {
					ArticleSummary sum = this.getArticleSummaryFromRset(rset, conn);
					Article art = new Article(sum);
					art.setContent(rset.getString("content"));
					res.add(art);
				}
			}
			stmt.close();
		} finally {
			conn.close();
		}
		return res;
	}
	
	public List<ArticleSummary> getArticleSummariesFromTo(long start, int count, String tags, String order) throws SQLException {
		List<ArticleSummary> res = new ArrayList<ArticleSummary>();
		if (start < 0) start = 0;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		// I could do this in a single statement but going to do it in two.
		// I'm using limit and offset, which are supported by PostgreSQL and MySQL (normally) but
		// not most other databases.
		try {
			String sql = "SELECT articles.id, articles.title, " +
				"articles.article_url, articles.thumb_image, articles.date, " +
				"articles.user_id, articles.summary FROM articles ";
			String[] tagsA = null;
			if (tags != null && !tags.isEmpty()) {
				sql = sql.concat(", article_tags, tags WHERE articles.published = 1 AND articles.short = 0");
				tagsA = tags.split(",");
				for (int a = 0; a < tagsA.length; a++) {
					sql = sql.concat(" AND tags.name = ?");
				}
				// Adding the join code:
				sql = sql.concat(" AND (tags.id = article_tags.tag_id AND " +
						"article_tags.article_id = articles.id) ");
			} else {
				sql = sql.concat("WHERE articles.published = 1 AND articles.short = 0 ");
			}
			if (order.toLowerCase().contains("asc")) {
				sql = sql.concat("ORDER BY articles.id ASC ");
			} else {
				sql = sql.concat("ORDER BY articles.id DESC ");
			}
			sql = sql.concat("LIMIT ? OFFSET ?");
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			int pos = 0;
			if (tagsA != null && tagsA.length > 0) {
				for (pos = 0; pos < tagsA.length; pos++) {
					stmt.setString(pos + 1, tagsA[pos]);
				}
			}
			stmt.setInt(pos + 1, count); // LIMIT clause value
			stmt.setLong(pos + 2, start); // OFFSET is start
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
				if (rset.next()) {
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
		sum.setId(rset.getLong("id"));
		// Find comment count:
		long comCount = this.getCommentCount(sum.getId());
		if (comCount < 0) comCount = 0;
		sum.setCommentsCount(comCount);
		long dateVal = rset.getLong("date");
		java.util.Date date = new java.util.Date(dateVal * 1000);
		sum.setDate(date);
		sum.setSummary(rset.getString("summary"));
		sum.setArticleURL(rset.getString("article_url"));
		sum.setThumbImage(rset.getString("thumb_image"));
		sum.setTitle(rset.getString("title"));
		// Get the tags.
		List<ArticleTag> artTags = new ArrayList<ArticleTag>();
		try {
			PreparedStatement st = conn.prepareStatement("SELECT tags.name, tags.id, tags.main_tag FROM article_tags, tags " +
					"WHERE article_tags.article_id = ? AND article_tags.tag_id = tags.id");
			st.setLong(1, sum.getId());
			ResultSet tags = st.executeQuery();
			while (tags.next()) {
				ArticleTag t = new ArticleTag();
				t.setId(tags.getLong("id"));
				t.setName(tags.getString("name"));
				int mainT = tags.getInt("main_tag");
				if (mainT > 0) {
					t.setMainTag(true);
				} else {
					t.setMainTag(false);
				}
				artTags.add(t);
			}
			st.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		sum.setTags(artTags);
		return sum;
	}
	
	public List<ArticleTag> getAllTags() throws SQLException {
		List<ArticleTag> ret = null;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tags");
			ret = new ArrayList<ArticleTag>();
			while (rs.next()) {
				ArticleTag t = new ArticleTag();
				t.setId(rs.getLong("id"));
				t.setName(rs.getString("name"));
				int mainT = rs.getInt("main_tag");
				if (mainT > 0) {
					t.setMainTag(true);
				} else {
					t.setMainTag(false);
				}
				ret.add(t);
			}
			rs.close();
		} finally {
			conn.close();
		}
		return ret;
	}
	
	public long getCommentCount(long articleID) throws SQLException {
		long ret = 0l;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM comments WHERE article_id = ?");
			stmt.setLong(1, articleID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				ret = rs.getLong(1);
			}
		} finally {
			conn.close();
		}
		return ret;
	}
	
	/**
	 * getArticleCount returns the number of articles, marked as published or not
	 * with the given tag list (comma separated)
	 * The authoring tool dorade-blog-authoring has a better version of this method.
	 * And of many other methods.
	 * @param published if true, will look for articles marked as published only
	 * @param tags comma separated String of tags for this article ; set to empty String or null to disable
	 * @return the amount of articles corresponding to the criteria (long)
	 * @throws SQLException
	 */
	public long getArticleCount(boolean published, String tags) throws SQLException {
		return this.getArticleOrShortCount(published, tags, false);
	}
	
	public long getShortCount(boolean published, String tags) throws SQLException {
		return this.getArticleOrShortCount(published, tags, true);
	}
	
	private long getArticleOrShortCount(boolean published, String tags, boolean shorts) throws SQLException {
		long ret = 0l;
		DataSource ds = DB.getDataSource();
		Connection conn = ds.getConnection();
		List<String> where = new ArrayList<>();
		try {
			PreparedStatement stmt = null;
			String sql = "SELECT count(*) FROM articles";
			ResultSet rs;
			String[] tagsA = null;
			if (published) {
				where.add("articles.published = 1");
			}
			if (shorts) {
				where.add("articles.short = 1");
			} else {
				where.add("articles.short = 0");
			}
			if (tags != null && !tags.isEmpty()) {
				tagsA = tags.split(",");
				sql = sql.concat(", tags, article_tags");
				for (int a = 0; a < tagsA.length; a++) {
					where.add("tags.name = ?");
				}
				where.add("(tags.id = article_tags.tag_id AND article_tags.article_id = articles.id)");
			}
			if (where.size() > 0) {
				sql = sql.concat(" WHERE ");
				sql = sql.concat(String.join(" AND ", where));
			}
			stmt = conn.prepareStatement(sql);
			// Set the parameters of the statement:
			if (tagsA != null) {
				for (int i = 1; i <= tagsA.length; i++) {
					// I originally meant the list to use long as index
					// but arrays use int, so yeah... I'm using int.
					// F*** consistency.
					stmt.setString(i, tagsA[i - 1]);
				}
			}
			// Set rs somewhere around here
			rs = stmt.executeQuery();
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
