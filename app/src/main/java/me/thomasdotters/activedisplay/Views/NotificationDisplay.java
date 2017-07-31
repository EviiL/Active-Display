package me.thomasdotters.activedisplay.Views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.R;

/**
 * TODO: document your custom view class.
 */
public class NotificationDisplay extends LinearLayout {

	private Context context;
	private RelativeLayout notificationBox;
	private Preferences preferences;
	private boolean isShown;
	private int textColour;



	public NotificationDisplay(Context context) {
		super(context);
		this.context = context;
		init(null, 0);
	}

	public NotificationDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs, 0);
	}

	public NotificationDisplay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {

		preferences = new Preferences(context);
		preferences.createPreferences();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.notification_display, null));
		notificationBox = (RelativeLayout) findViewById(R.id.notification_display);
	}


	public void display(NotificationHolder notification, int colour, int iconColour){
		textColour = colour;

		if(notification != null){

			if(notification.getTitle() != null){
				isShown = true;

				Animation fadeIn = new AlphaAnimation(0, 1);
				fadeIn.setInterpolator(new DecelerateInterpolator());
				fadeIn.setDuration(preferences.animationLength);
				fadeIn.setFillAfter(true);
				//Fade out animation

				if(preferences.privacyLevel != 3) {
					//Set the notification text and icon
					((TextView) notificationBox.findViewById(R.id.notification_message_title)).setText(notification
							.getTitle());
					if(preferences.privacyLevel == 1) {
						((TextView) notificationBox.findViewById(R.id.notification_message)).setText(notification
								.getMessage());
						((TextView) notificationBox.findViewById(R.id.notification_message)).setTextColor(colour);

					}
					((TextView) notificationBox.findViewById(R.id.notification_name)).setText(notification
							.getAppName());

					((TextView) notificationBox.findViewById(R.id.notification_message_title))
							.setTextColor(colour);
					((TextView) notificationBox.findViewById(R.id.notification_name)).setTextColor(colour);

					((ImageView) notificationBox.findViewById(R.id.notification_icon)).setImageDrawable
							(notification.getIcon().getConstantState().newDrawable());

					((ImageView) notificationBox.findViewById(R.id.notification_icon)).setColorFilter(new
							PorterDuffColorFilter(iconColour, PorterDuff.Mode.MULTIPLY));
				}



				//Run animations
//				AnimationSet animation = new AnimationSet(false);
//				animation.addAnimation(fadeIn);
				notificationBox.startAnimation(fadeIn);
			}
		}
	}

	public void hide() {
		if (isShown) {
//			((ImageView) notificationBox.findViewById(R.id.notification_icon)).setColorFilter(new
//					PorterDuffColorFilter(textColour, PorterDuff.Mode.MULTIPLY));
			Animation fadeOut = new AlphaAnimation(1, 0);
			fadeOut.setInterpolator(new AccelerateInterpolator());
			fadeOut.setDuration(preferences.animationLength);

			AnimationSet animation = new AnimationSet(false);
			animation.addAnimation(fadeOut);
			fadeOut.setFillAfter(true);
			notificationBox.startAnimation(fadeOut);

		}
	}



}
