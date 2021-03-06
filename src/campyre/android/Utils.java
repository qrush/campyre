package campyre.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import campyre.java.Campfire;
import campyre.java.CampfireException;

public class Utils {
	public static final int ABOUT = 0;
	
	// change this to false for the donate version
	public static final boolean ASK_DONATE = true;
	
	// change this to icon_donate for the donate version
	public static final int SHORTCUT_ICON = R.drawable.icon; 
	
	public static void alert(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
    
    public static void alert(Context context, CampfireException exception) {
    	String message = exception == null ? "Unhandled error." : exception.getMessage();
    	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    public static Dialog aboutDialog(Context context) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	LayoutInflater inflater = LayoutInflater.from(context);
    	
    	ScrollView aboutView = (ScrollView) inflater.inflate(R.layout.about, null);
    	
    	TextView about3 = (TextView) aboutView.findViewById(R.id.about_links);
    	about3.setText(R.string.about_links);
    	Linkify.addLinks(about3, Linkify.WEB_URLS);
    	
    	String versionString = context.getResources().getString(R.string.version_string);
    	((TextView) aboutView.findViewById(R.id.about_version)).setText("Version " + versionString);
    	
    	builder.setView(aboutView);
    	builder.setPositiveButton(R.string.about_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});
        return builder.create();
    }
    
    public static Intent feedbackIntent(Context context) {
    	return new Intent(Intent.ACTION_SENDTO, 
    			Uri.fromParts("mailto", context.getResources().getString(R.string.contact_email), null))
    		.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.contact_subject));
    }
    
    public static Intent donateIntent(Context context) {
    	return new Intent(Intent.ACTION_VIEW,
    			Uri.parse("market://search?q=pname:" + context.getResources().getString(R.string.package_name_donate)));
    }
    
    public static Campfire getCampfire(Context context) {
    	SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
    	String user_id = prefs.getString("user_id", null);
        
        if (user_id != null) {
        	String subdomain = prefs.getString("subdomain", null);
            String token = prefs.getString("token", null);
            boolean ssl = prefs.getBoolean("ssl", false);
        	return new Campfire(subdomain, token, ssl, user_id);
        } else
        	return null;
	}
    
    public static String getCampfireValue(Context context, String key) {
    	return context.getSharedPreferences("campfire", 0).getString(key, null);
    }
	
	public static void saveCampfire(Context context, Campfire campfire) {
		SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
		Editor editor = prefs.edit();
	
		editor.putString("subdomain", campfire.subdomain);
		editor.putString("token", campfire.token);
		editor.putBoolean("ssl", campfire.ssl);
		editor.putString("user_id", campfire.user_id);
		
		editor.commit();
	}
	
	public static void logoutCampfire(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("campfire", 0);
		Editor editor = prefs.edit();
	
		editor.putBoolean("ssl", false);
		editor.putString("user_id", null);
		
		editor.commit();
	}
}