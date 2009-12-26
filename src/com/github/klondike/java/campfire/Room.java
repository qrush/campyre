package com.github.klondike.java.campfire;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class Room {
	public String id, name;
	public boolean full = false;
	private Campfire campfire;
	
	// For those times when you don't need a whole Room's details,
	// You just have the ID and need a Room function (e.g. uploading a file)
	public Room(Campfire campfire, String id) {
		this.campfire = campfire;
		this.id = id;
	}
	
	protected Room(Campfire campfire, JSONObject json) throws JSONException {
		this.campfire = campfire;
		this.id = json.getString("id");
		this.name = json.getString("name");
		if (json.has("full"))
			this.full = json.getBoolean("full");
	}
	
	public static Room find(Campfire campfire, String id) throws CampfireException {
		try {
			return new Room(campfire, new CampfireRequest(campfire).getOne(Campfire.roomPath(id), "room"));
		} catch(JSONException e) {
			throw new CampfireException(e, "Problem loading Room from the API.");
		}
	}
	
	public boolean join() throws CampfireException {
		return new CampfireRequest(campfire).post(Campfire.joinPath(id)).getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}
	
	/* Main methods */
	
	public ArrayList<RoomEvent> listen() throws CampfireException {
		ArrayList<RoomEvent> events = new ArrayList<RoomEvent>();		
		return events;
	}
	
	public RoomEvent speak(String message) throws CampfireException {
		return null;
	}
	
	public List<CampfireFile> getRoomFiles() {
		List<CampfireFile> retFiles = new ArrayList<CampfireFile>();
		return retFiles;
	}
	
	//TODO: Get this to work for more than just JPGs
	public boolean uploadFile(FileInputStream stream) throws CampfireException {
		String filename = "from_phone.jpg";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL connectURL = new URL(Campfire.uploadPath(id));
            HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("User-Agent", CampfireRequest.USER_AGENT);
            conn.setRequestProperty("Connection","Keep-Alive");
            conn.setRequestProperty("Content-Type","multipart/form-data, boundary="+boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            
            // submit header
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"submit\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // insert submit
            dos.writeBytes("Upload");
            // submit closer
            dos.writeBytes(lineEnd);
            
            // file header
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            // OH MY GOD the space between the semicolon and "filename=" is ABSOLUTELY NECESSARY
            dos.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + filename + "\"" + lineEnd);
            dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            dos.writeBytes("Content-Type: image/jpg" + lineEnd);
            dos.writeBytes(lineEnd);

            // insert file
            int bytesAvailable = stream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = stream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = stream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = stream.read(buffer, 0, bufferSize);
            }
            
            // file closer
            dos.writeBytes(lineEnd);
            
            // send multipart form data necesssary after file data...            
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            stream.close();
            dos.flush();
            dos.close();

            InputStream is = conn.getInputStream();
            int ch;

            StringBuffer b = new StringBuffer();
            while((ch = is.read()) != -1) {
            	b.append((char) ch);
            }

            String s = b.toString();
            return (s.contains("waitForMessage"));
        } catch (IOException e) {
        	throw new CampfireException(e.getClass().getCanonicalName() + "\n" + e.getMessage());
        } 
		
	}

	public String toString() {
		return name;
	}
}