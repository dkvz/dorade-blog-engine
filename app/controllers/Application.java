package controllers;

import play.*;
import play.libs.Json;
import play.mvc.*;
import java.util.*;

import views.html.*;

public class Application extends Controller {

//	@Before
//	public static void setCORS() {
//		Http.Header hd = new Http.Header();
//		hd.name = "Access-Control-Allow-Origin";
//		hd.values = new ArrayList<String>();
//		hd.values.add("http://localhost:9010&quot;);
//		Http.Response.current().headers.put("Access-Control-Allow-Origin",hd);
//	}
	
    public Result index() {
        //return ok(index.render("Your new application is ready."));
    	List<Map<String, String>> articles = new ArrayList<Map<String, String>>();
    	Map<String, String> theMap = new HashMap<String, String>();
    	theMap.put("id", "210");
    	theMap.put("title", "Some Article");
    	theMap.put("thumbImage", "images/art.png");
    	// Should probably be an array:
    	theMap.put("tags", "tag1, tag2");
    	theMap.put("commentsCount", "12");
    	theMap.put("date", new Date().toString());
    	theMap.put("summary", "This is the summary.");
    	theMap.put("author", "VeZD");
    	articles.add(theMap);
    	theMap = new HashMap<String, String>();
    	theMap.put("id", "350");
    	theMap.put("title", "Another Article");
    	theMap.put("thumbImage", "images/art.png");
    	// Should probably be an array:
    	theMap.put("tags", "Technology, Science");
    	theMap.put("commentsCount", "6");
    	theMap.put("date", new Date().toString());
    	theMap.put("summary", "I'm writing a slightly longer summary. Sort of.<br />A line feed too.");
    	theMap.put("author", "VeZD");
    	articles.add(theMap);
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	return ok(Json.toJson(articles));
    }

}
