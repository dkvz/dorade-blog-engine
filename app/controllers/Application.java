package controllers;

import play.*;
import play.libs.Json;
import play.mvc.*;

import java.text.*;
import java.util.*;

import views.html.*;
import models.*;
import java.sql.SQLException;

public class Application extends Controller {

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
    		User usr = BlogDataAccess.getInstance().getUser(1l);
    		if (usr != null) {
    			test.add(usr.getName());
    		} else {
    			test.add("Not Found");
    		}
    	} catch (SQLException ex) {
    		System.out.println(ex.toString()); 
    	}
    	return ok(Json.toJson(test));
    }
    
    public Result articlesStartingFrom(Long articleId, Integer max) {
    	if (max == null) {
    		max = 30;
    	}
    	if (articleId > articlesList.size()) {
    		return badRequest();
    	}
    	int start = articleId.intValue();
    	int end = start + max.intValue();
    	if (end > articlesList.size()) end = articlesList.size();
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	return ok(Json.toJson(articlesList.subList(start, end)));
    }

}
