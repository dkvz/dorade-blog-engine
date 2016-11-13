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
    		long count = BlogDataAccess.getInstance().getArticleCount();
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
    		long count = BlogDataAccess.getInstance().getCommentCount();
        	if (start >= count) {
        		return notFound();
        	} else {
        		List<Comment> list = BlogDataAccess.getInstance().getCommentsFromTo(start, max, articleURL);
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
    
    public Result articlesStartingFrom(Long articleId, Integer max) {
    	if (max == null) {
    		max = 30;
    	} else if (max > 100) {
    		max = 30;
    	}
    	//int start = articleId.intValue();
    	//int end = start + max.intValue();
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	
    	try {
    		// Check if we're out of articles for this request:
        	long count = BlogDataAccess.getInstance().getArticleCount();
        	if (articleId >= count) {
        		return notFound();
        	} else {
				List<ArticleSummary> list = BlogDataAccess.getInstance().getArticleSummariesDescFromTo(articleId, max);
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
    
    public Result article(String articleURL) {
    	// Let's use notFound() if the article doesn't exist,
    	// and internalServerError(String reason) si y a un problème.
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	try {
    		Article art = BlogDataAccess.getInstance().getArticleByURL(articleURL);
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
    
    public Result saveComment() {
    	final Map<String, String[]> values = request().body().asFormUrlEncoded();
    	if (values.get("author") != null && values.get("comment") != null && values.get("article_id") != null) {
    		// We should use something to transform special chars to HTML.
    		String author = values.get("author")[0];
    		String comment = values.get("comment")[0];
    		author = StringEscapeUtils.escapeHtml4(author);
    		comment = StringEscapeUtils.escapeHtml4(comment);
    		// I should also save the IP address of the client.
    		String clientIP = request().remoteAddress();
    		try {
    			Comment commentO = new Comment();
    			commentO.setAuthor(author);
    			// Reduce comment to some arbitrary max length.
    			if (comment.length() > Application.MAX_COMMENT_LENGTH) {
    				comment = comment.substring(0, Application.MAX_COMMENT_LENGTH);
    			}
    			commentO.setComment(comment);
    			long artId = Long.parseLong(values.get("article_id")[0]);
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

}
