package me.thomasdotters.activedisplay.Views;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Map;

import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Utils;

public class IconView extends LinearLayout {
	Context mContext;
	private ImageView backImageView;
	private NotificationDisplay notificationDisplay;
	private Preferences preferences;

	public IconView(Context context) {
		super(context);
		mContext = context;
		init(null, 0);
	}

	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		init(attrs, 0);
	}

	public IconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;

		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Load attributes

		preferences = new Preferences(mContext);
		preferences.createPreferences();
	}

	public void setBackImageView(ImageView imageView){
		backImageView = imageView;
	}

	public void update(int textColor, final Runnable action) {
		removeAllViews();

		for (final Map.Entry<String, NotificationHolder> entry : StaticGlobals.notificationMap.entrySet()) {
			Drawable icon = entry.getValue().getIcon();

			if (icon != null) {
				final ImageView iconView = new ImageView(getContext());
				iconView.setImageDrawable(icon);

				final LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams
						(72, 72, Gravity.CENTER);

				iconView.setPadding(12, 0, 12, 0);
				iconView.setLayoutParams(iconLayoutParams);
				iconView.setColorFilter(new
						PorterDuffColorFilter(preferences.iconColour, PorterDuff.Mode
						.MULTIPLY));

				final int[] temp = {72};

				iconView.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							//Animate on press.
							ResizeAnimation anim = new ResizeAnimation(iconView, 96, 96);
							anim.setDuration(preferences.animationLength);
							iconView.startAnimation(anim);

							if(preferences.notificationImage) {

								if (entry.getValue().getContentImage() != null) {

									backImageView.setVisibility(View.VISIBLE);
									backImageView.setImageBitmap(entry.getValue().getContentImage());

									AlphaAnimation animation = new AlphaAnimation(0, 0.3f);
									animation.setDuration(preferences.animationLength);
									animation.setFillAfter(true);
									backImageView.startAnimation(animation);

								} else {
									backImageView.setImageBitmap(null);
								}

							}
							if(preferences.touchVibrate) {
								Vibrator vb = (Vibrator) mContext.getSystemService(Context
										.VIBRATOR_SERVICE);
								vb.vibrate(100);
							}


							notificationDisplay.display(entry.getValue(), preferences
									.previewColour, preferences.sameIconColour ? preferences
									.previewColour : preferences.iconColour);

							if(StaticGlobals.isDebug)
								Utils.LogD("Showing now");


						}
						if ( event.getAction() == MotionEvent.ACTION_MOVE) {

							temp[0] +=10;
							Utils.LogD(Integer.toString(temp[0]));
							if (temp[0] > 300) {
								if (entry.getValue().getIntent() != null) {
									try {
										KeyguardManager km = (KeyguardManager) mContext
												.getSystemService(Context.KEYGUARD_SERVICE);
										entry.getValue().getIntent().send();
										action.run();
									} catch (PendingIntent.CanceledException e) {
										e.printStackTrace();
									}
								} else {
									removeView(iconView);
								}
							}
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							//Animate on release.

//							iconLayoutParams.width = 72;
//							iconLayoutParams.height = 72;
//							iconView.setLayoutParams(iconLayoutParams);

							if(backImageView.getAlpha() > 0.0f){

								AlphaAnimation animation = new AlphaAnimation(0.3f, 0.0f);
								animation.setDuration(preferences.animationLength);
								animation.setFillAfter(true);
								backImageView.startAnimation(animation);
							}

							ResizeAnimation anim = new ResizeAnimation(iconView, 72, 72);
							anim.setDuration(preferences.animationLength);
							iconView.startAnimation(anim);

							notificationDisplay.hide();
							if(StaticGlobals.isDebug)
								Utils.LogD("Going Away Now");
							backImageView.setImageBitmap(null);

						}
						return true;
					}
				});

				iconView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
//						iconView.setColorFilter(new
//								PorterDuffColorFilter(preferences.iconColour, PorterDuff.Mode
//								.MULTIPLY));
					}
				});
				addView(iconView);
			}
		}


	}

	public NotificationDisplay getNotificationDisplay() {
		return notificationDisplay;
	}

	public void setNotificationDisplay(NotificationDisplay notificationDisplay) {
		this.notificationDisplay = notificationDisplay;
	}
}
