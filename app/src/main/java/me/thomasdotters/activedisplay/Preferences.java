package me.thomasdotters.activedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

/**
 * Created by Thomas Dotters on 12/09/2016.
 */
public class Preferences {

	private SharedPreferences preferences;
	private Context context;

	public boolean enabled;

	//Display
	public int clockColour;
	public int dateColour;
	public int iconColour;
	public int previewColour;
	public boolean sameIconColour;
	public int clockSize;
	public int dateSize;
	public int animationLength;

	//Notification
	public int privacyLevel;
	public boolean notificationImage;
	public boolean touchVibrate;


	public Preferences(Context context){
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
	}

	public void createPreferences(){

		enabled = preferences.getBoolean("activeEnabled", false);


		//Display Preferences.
		clockColour = preferences.getInt("clockColour", Color.WHITE);
		dateColour = preferences.getInt("dateColour", Color.WHITE);
		iconColour = preferences.getInt("iconColour", Color.WHITE);
		previewColour = preferences.getInt("previewColour", Color.WHITE);
		sameIconColour = preferences.getBoolean("iconColourSame", true);
		clockSize = Integer.parseInt(preferences.getString("clockSize", "48"));
		dateSize = Integer.parseInt(preferences.getString("dateSize", "12"));
		animationLength = preferences.getInt("animationLength", 100);

		//Notification Preferences.
		privacyLevel =  Integer.parseInt(preferences.getString("notificationPrivacy", "1"));
		notificationImage = preferences.getBoolean("notificationImageShow", true);
		touchVibrate = preferences.getBoolean("notificationVibrateTouch", true);


	}

	public SharedPreferences getSharedPreferences(){
		return preferences;
	}

}
