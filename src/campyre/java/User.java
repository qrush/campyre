package campyre.java;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	public String id, name, email;
	public Campfire campfire;
	
	public User(Campfire campfire, JSONObject json) throws JSONException {
		this.campfire = campfire;
		this.id = json.getString("id");
		this.name = json.getString("name");
		this.email = json.getString("email_address");
	}
	
	public static User find(Campfire campfire, String id) throws CampfireException {
		try {
			return new User(campfire, new CampfireRequest(campfire).getOne(Campfire.userPath(id), "user"));
		} catch(JSONException e) {
			throw new CampfireException(e, "Problem loading user details.");
		}
	}
	
	public String displayName() {
		if (name == null) return "(No name)";
		String[] names = name.split(" ");
		if (names.length > 1)
			return names[0] + " " + names[1].charAt(0) + ".";
		else
			return names[0];
	}
}
