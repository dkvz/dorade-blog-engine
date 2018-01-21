package controllers;

import play.*;
import play.libs.Json;
import play.mvc.*;

import java.text.*;
import java.util.*;

import views.html.*;
import models.*;
import java.sql.SQLException;

import org.apache.commons.lang3.StringEscapeUtils;

public class Application extends Controller {
	
	public static final int MAX_COMMENT_LENGTH = 2000;
	public static final int MAX_AUTHOR_LENGTH = 70;

//	@Before
//	public static void setCORS() {
//		Http.Header hd = new Http.Header();
//		hd.name = "Access-Control-Allow-Origin";
//		hd.values = new ArrayList<String>();
//		hd.values.add("http://localhost:9010&quot;);
//		Http.Response.current().headers.put("Access-Control-Allow-Origin",hd);
//	}
    private List<Map<String, String>> articlesList;
	
    public Application() {
      articlesList = new ArrayList<Map<String, String>>();
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      for (int i = 1; i < 100; i++) {
        Map<String, String> theMap = new HashMap<String, String>();
        theMap.put("id", Integer.toString(i));
        theMap.put("title", "Article de test numéro ".concat(Integer.toString(i)));
        theMap.put("thumbImage", "images/patgpibgray.jpg");
        // Should probably be an array:
        theMap.put("tags", "tag1, tag2");
        theMap.put("commentsCount", "10");
        theMap.put("date", dateFormat.format(new Date()));
        theMap.put("summary", "This is the summary.");
        theMap.put("author", "VeZD");
        articlesList.add(theMap);
      }
    }
	
    public Result index() {
        //return ok(index.render("Your new application is ready."));
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	return ok(Json.toJson(articlesList));
    }
    
    public Result test() {
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	List<String> test = new ArrayList<String>();
    	try {
    		long count = BlogDataAccess.getInstance().getArticleCount(false, null);
    		test.add(Long.toString(count));
    	} catch (SQLException ex) {
    		System.out.println(ex.toString()); 
    	}
    	return ok(Json.toJson(test));
    }
    
    public Result commentsStartingFrom(String articleURL, Integer start, Integer max) {
    	// See articlesStartingFrom for more details, this is more or less
    	// a copy paste of that method.
    	if (max == null) {
    		max = 30;
    	} else if (max > 50) {
    		max = 50;
    	}
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	
    	try {
    		long articleId;
    		try {
	    		// Check if we got an article ID as the URL
	    		articleId = Long.parseLong(articleURL);
        	} catch(NumberFormatException ex) {
        		articleId = BlogDataAccess.getInstance().getArticleIdFromUrl(articleURL);
        	}
    		long count = BlogDataAccess.getInstance().getCommentCount(articleId);
        	if (start >= count) {
        		return notFound();
        	} else {
        		List<Comment> list = BlogDataAccess.getInstance().getCommentsFromTo(start, max, articleId);
				List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
				for (Comment art : list) {
					listMap.add(art.toReducedMap());
				}
				return ok(Json.toJson(listMap));
        	}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		return internalServerError("Database error");
    	}
    }
    
    public Result articlesStartingFrom(Long articleId, Integer max, String tags, String order) {
    	if (max == null) {
    		max = 30;
    	} else if (max > 100) {
    		max = 30;
    	}
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		// Check if we're out of articles for this request:
        	long count = BlogDataAccess.getInstance().getArticleCount(true, tags);
        	if (articleId >= count) {
        		return notFound();
        	} else {
				List<ArticleSummary> list = BlogDataAccess.getInstance().getArticleSummariesFromTo(articleId, max, tags, order);
				List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
				for (ArticleSummary art : list) {
					listMap.add(art.toMap());
				}
				return ok(Json.toJson(listMap));
        	}
		} catch (SQLException e) {
			e.printStackTrace();
			return internalServerError("Database Error");
		}
    	//return ok(Json.toJson(articlesList.subList(start, end)));
    }
    
    public Result shortsStartingFrom(Long articleId, Integer max, String tags, String order) {
    	// This should be refactored.
    	if (max == null) {
    		max = 30;
    	} else if (max > 100) {
    		max = 30;
    	}
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		// Check if we're out of articles for this request:
        	long count = BlogDataAccess.getInstance().getShortCount(true, tags);
        	if (articleId >= count) {
        		return notFound();
        	} else {
				List<Article> list = BlogDataAccess.getInstance().getShortsFromTo(articleId, max, tags, order);
				List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
				for (Article art : list) {
					listMap.add(art.toMap());
				}
				return ok(Json.toJson(listMap));
        	}
		} catch (SQLException e) {
			e.printStackTrace();
			return internalServerError("Database Error");
		}
    }
    
    public Result article(String articleURL) {
    	// Let's use notFound() if the article doesn't exist,
    	// and internalServerError(String reason) si y a un problème.
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		Article art = null;
    		try {
        		// Check if we got an article ID as the URL
        		long articleId = Long.parseLong(articleURL);
        		if (articleId > 0l) {
        			art = BlogDataAccess.getInstance().getArticleById(articleId);
        		} else {
        			art = BlogDataAccess.getInstance().getArticleByURL(articleURL);
        		}
        	} catch(NumberFormatException ex) {
        		art = BlogDataAccess.getInstance().getArticleByURL(articleURL);
        	}
    		if (art != null) {
    			// Let's transform this into JSON.
    			// I'll make a good ol' MAP.
    			Map<String, Object> resMap = new HashMap<String, Object>();
    			resMap = art.toMap();
    			return ok(Json.toJson(resMap));
    		} else {
    			return notFound();
    		}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		return internalServerError("Database error");
    	}
    }
    
    /**
     * tags - Returns all the tags in database (no limit)
     * Wait why am I writing Javadoc now?
     * @return JSON array of all the tags made into maps; 
     * notFound if no tags are in database
     */
    public Result tags() {
        response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		List<ArticleTag> tags = BlogDataAccess.getInstance().getAllTags();
    		if (tags != null && tags.size() > 0) {
    			List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
				for (ArticleTag tag : tags) {
					listMap.add(tag.toMap());
				}
				return ok(Json.toJson(listMap));
    		} else {
    			return notFound();
    		}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		return internalServerError("Database error");
    	}
    }
    
    public Result saveComment() {
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	final Map<String, String[]> values = request().body().asFormUrlEncoded();
    	if (values.get("author") != null && values.get("comment") != null && (values.get("article_id") != null || values.get("articleurl") != null)) {
    		// We should use something to transform special chars to HTML.
    		String author = StringEscapeUtils.escapeHtml4(values.get("author")[0]);
    		String comment = StringEscapeUtils.escapeHtml4(values.get("comment")[0]);
    		/* if (values.get("escape_html") != null) {
	    		author = StringEscapeUtils.escapeHtml4(author);
	    		comment = StringEscapeUtils.escapeHtml4(comment);
    		} */
    		// I should also save the IP address of the client.
    		String clientIP = request().remoteAddress();
    		try {
    			Comment commentO = new Comment();
    			// Reduce author to some arbitrary max length:
    			if (author.length() > Application.MAX_AUTHOR_LENGTH) {
    				author = author.substring(0, Application.MAX_AUTHOR_LENGTH);
    			}
    			commentO.setAuthor(author);
    			// Reduce comment to some arbitrary max length.
    			if (comment.length() > Application.MAX_COMMENT_LENGTH) {
    				comment = comment.substring(0, Application.MAX_COMMENT_LENGTH);
    			}
    			commentO.setComment(comment);
    			long artId = -1;
    			if (values.get("article_id") != null) {
    				artId = Long.parseLong(values.get("article_id")[0]);
    			} else {
    				artId = BlogDataAccess.getInstance().getArticleIdFromUrl(values.get("articleurl")[0]);
    				if (artId < 0) throw new NumberFormatException();
    			}
    			commentO.setArticleId(artId);
    			commentO.setClientIP(clientIP);
    			BlogDataAccess.getInstance().insertComment(commentO);
    		} catch (SQLException ex) {
    			return internalServerError("Database error");
    		} catch (NumberFormatException ex) {
    			return internalServerError("Could not parse the article ID for this comment");
    		}
    		return ok("OK");
    	} else {
    		return internalServerError("Missing arguments");
    	}
    }
    
    /**
     * sitemap - Get a XML sitemap with all the articles.
     * I may not change the mime type to be XML though. 
     * @param articlesRoot the root URL (without http:// and trailing /) for your articles
     * @return
     */
    public Result sitemap(String articlesRoot) {
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		// We should have a separate call for this, as "max" is mandatory here and 
    		// I have to set it to some large value for this to work (since it's put into a 
    		// LIMIT statement).
    		List<ArticleSummary> articles = BlogDataAccess.getInstance().getArticleSummariesDescFromTo(0, Integer.MAX_VALUE, null);
    		String sitemap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	    	sitemap = sitemap.concat("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
	    	String baseRoot = "https://" + articlesRoot + "/";
    		if (articles != null && articles.size() > 0) {
		    	for (ArticleSummary sum : articles) {
		    		sitemap = sitemap.concat("\t<url>\n");
		    		sitemap = sitemap.concat("\t\t<loc>" + baseRoot.concat(sum.getArticleURL()) + "</loc>\n");
		    		sitemap = sitemap.concat("\t</url>\n");
		    	}
    		}
    		sitemap = sitemap.concat("</urlset>");
    		return ok(sitemap);
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		return internalServerError("Database error");
    	}
    }
    
    public Result lastComment() {
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		Comment lastCom = BlogDataAccess.getInstance().getLastComment();
    		if (lastCom != null) {
    			return ok(Json.toJson(lastCom.toReducedMap()));
    		} else {
    			return notFound();
    		}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		return internalServerError("Database error");
    	}
    }

}
