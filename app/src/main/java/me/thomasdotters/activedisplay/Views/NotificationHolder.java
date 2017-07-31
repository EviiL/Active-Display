package me.thomasdotters.activedisplay.Views;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Thomas Dotters on 03/09/2016.
 */
public class NotificationHolder {

	private String appName;
	private Drawable icon;
	private String title, message;
	private Context context;
	private PendingIntent intent;
	private Bitmap contentImage = null;

	public NotificationHolder(Context context, String title, String message, Drawable icon, CharSequence appName, PendingIntent intent) {
		this.context = context;
		this.title = title;
		this.message = message;
		this.icon = icon;
		this.appName = (String) appName;
		if (this.message.equals("null"))
			this.message = "";
		if (this.title.equals("null"))
			this.title = "";
		this.intent = intent;
	}

	public Drawable getIcon() {
//		if (icon != null)
//			icon.mutate().setColorFilter(ContextCompat.getColor(context, android.R.color.primary_text_dark), PorterDuff.Mode.MULTIPLY);
		return icon;
	}

	public void setContentImage(Bitmap bitmap){
		contentImage = bitmap;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getAppName() {
		return appName;
	}

	public PendingIntent getIntent() {
		return intent;
	}


	public Bitmap getContentImage() {
		return contentImage;
	}
}

