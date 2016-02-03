package controllers;

import play.*;
import play.libs.Json;
import play.mvc.*;
import java.util.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
        //return ok(index.render("Your new application is ready."));
    	Map<String, String> theMap = new HashMap<String, String>();
    	theMap.put("SomeKey", "SomeValue");
    	return ok(Json.toJson(theMap));
    }

}
