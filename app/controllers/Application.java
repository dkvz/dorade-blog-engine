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
    	Map<String, String> theMap = new HashMap<String, String>();
    	theMap.put("SomeKey", "SomeValue");
    	response().setHeader("Access-Control-Allow-Origin", "*");
    	return ok(Json.toJson(theMap));
    }

}
