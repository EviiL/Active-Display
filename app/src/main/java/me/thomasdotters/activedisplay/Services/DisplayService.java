package me.thomasdotters.activedisplay.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import me.thomasdotters.activedisplay.Activities.MainActivity;
import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.R;
import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Utils;
import me.thomasdotters.activedisplay.Views.IconView;
import me.thomasdotters.activedisplay.Views.NotificationDisplay;

import static android.hardware.SensorManager.SENSOR_DELAY_UI;

/**
 * Created by Thomas Dotters on 29/08/2016.
 */
public class DisplayService extends Service implements SensorEventListener {

	private WindowManager windowManager;
	private FrameLayout frameLayout;
	private RelativeLayout mainView;
	private ImageView backImageView;
	private WindowManager.LayoutParams windowParams;
	private boolean demo;
	private Handler UIhandler;
	private IconView iconView;
	private Preferences preferences;
	private PowerManager.WakeLock wakeLock;
	private SensorManager sensorManager;
	private Handler pulseHandler;
	private int pulseState;
	private Runnable pulseRunnable;
	private boolean hasView = true;
	private FrameLayout.LayoutParams mainLayoutParams;


	public static final int ACTION_SWIPE_UP = 1;
	public static final int ACTION_SWIPE_DOWN = 2;
	public static final int ACTION_DOUBLE_TAP = 3;


	@Override
	public int onStartCommand(Intent origIntent, int flags, int startId) {

		if(StaticGlobals.isDebug)
			Utils.LogD("Start Command");

		if (windowParams == null) {
			windowParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
					| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
					| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
					| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
					| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, -2);

			if (origIntent != null) {
				demo = origIntent.getBooleanExtra("demo", false);
				windowParams.type = origIntent.getBooleanExtra("demo", false) ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
			} else
				windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
				if (!Settings.canDrawOverlays(this)) {
					Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					return super.onStartCommand(origIntent, flags, startId);
				}

			windowManager.addView(frameLayout, windowParams);
		}
		return super.onStartCommand(origIntent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		preferences = new Preferences(this);
		preferences.createPreferences();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		frameLayout = new FrameLayout(this){
			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {

				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					windowManager.removeView(frameLayout);
					stopSelf();
				}
				return super.dispatchKeyEvent(event);
			}
		};

		wakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE))
				.newWakeLock(PowerManager.FULL_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP |
						PowerManager.ON_AFTER_RELEASE, "WAKELOCK");
		//wakeLock.setReferenceCounted(false);


		backImageView = new ImageView(this);
		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		imageParams.gravity = Gravity.CENTER;
		backImageView.setLayoutParams(imageParams);

		backImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		//backImageView.setAlpha(0.0f);
		backImageView.setVisibility(View.INVISIBLE);


		frameLayout.setOnTouchListener(new OnDismissListener(this));

		frameLayout.setBackgroundColor(Color.BLACK);
		frameLayout.setForegroundGravity(Gravity.CENTER);

		frameLayout.addView(backImageView);
		mainView = (RelativeLayout) (layoutInflater.inflate(R.layout.display_widget, frameLayout)
				.findViewById
				(R.id.display_wrapper));

		hasView = true;

		mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

		mainView.setLayoutParams(mainLayoutParams);

		Calendar c = Calendar.getInstance();

		SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
		String formattedDate = df.format(c.getTime());

		TextView t = (TextView)mainView.findViewById(R.id.display_text);
		t.setText(formattedDate);
		t.setTextColor(preferences.dateColour);
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.dateSize);

		TextClock textClock = (TextClock) mainView.findViewById(R.id.display_clock);
		textClock.setTextColor(preferences.clockColour);
		textClock.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.clockSize);

		NotificationDisplay notificationDisplay = (NotificationDisplay) mainView.findViewById(R
				.id.notification_box);

		iconView = (IconView) mainView.findViewById(R.id.display_icons);
		iconView.setBackImageView(backImageView);
		iconView.setNotificationDisplay(notificationDisplay);


		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor != null) {
			sensorManager.registerListener(this, proximitySensor, SENSOR_DELAY_UI, 1000000);
			if(StaticGlobals.isDebug)
				Utils.LogD("Registered Proximity Sensor.");
		}

		UIhandler = new Handler();
		pulseHandler = new Handler();

		//Notification setup
		StaticGlobals.onNotificationAction = () -> {
			//if (prefs.notificationsAlerts)
			UIhandler.post(() -> iconView.update(1, this::stopSelf));
		};


		StaticGlobals.isServiceRunning = true;
		StaticGlobals.onNotificationAction.run();

		handlePulse();
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}




	@Override
	public void onDestroy() {
		super.onDestroy();

		display(false, false);
		windowManager.removeViewImmediate(frameLayout);

		pulseHandler = null;

		if(wakeLock.isHeld())
			wakeLock.release();

		if(sensorManager != null){
			sensorManager.unregisterListener(this);
		}

		//Empty the Map upon Death.
		StaticGlobals.notificationMap.clear();

		//Inform the App that the service is no longer running.
		StaticGlobals.isServiceRunning = false;
		StaticGlobals.onNotificationAction = null;
		StaticGlobals.isDisplayOn = false;

		if(StaticGlobals.isValidatorAlive){
			StaticGlobals.isValidatorAlive = false;
			stopService(new Intent(getApplicationContext(), ValidateNotificationService.class));
		}


	}

	private boolean gestureAction(int gesture) {
		if (gesture == ACTION_SWIPE_UP) {
			//stopSelf();
			showNotification();
			return true;
		}else if(gesture == ACTION_DOUBLE_TAP){
			Intent i = new Intent(this, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(i);
			stopSelf();
			return true;
		}else if(gesture == 4) {
			return true;
		}
		return false;
	}


	private void handlePulse(){
		pulseRunnable = new Runnable() {
			@Override
			public void run() {
				display(true, true);
				pulseHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Utils.LogD("Released");
						display(false, false);
				}
				}, 5000);
			}
		};

		pulseHandler.postDelayed(pulseRunnable, 5000);

	}


	public void showNotification() {
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		Resources r = getResources();
		Notification notification = new NotificationCompat.Builder(this)
				.setTicker("Test String")
				.setSmallIcon(android.R.drawable.ic_menu_report_image)
				.setContentTitle("Test String")
				.setContentText("This is a test Notification")
				.setContentIntent(pi)
				.setAutoCancel(true)
				.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

	private void display(boolean state, boolean first){
		if(StaticGlobals.isDebug)
			Utils.LogD("Lights");

		if(first && state){

			if(StaticGlobals.isDebug)
				Utils.LogD("State");

			if(!StaticGlobals.isScreenOn){

				if(StaticGlobals.isDebug)
					Utils.LogD("Yay");

				new Handler().postDelayed(() -> {
					if(!hasView) {
						frameLayout.addView(mainView);
					}


					//Turn on the display
					if (!wakeLock.isHeld()) wakeLock.acquire();
					AlphaAnimation animation = new AlphaAnimation(0, 1f);
					animation.setDuration(preferences.animationLength);
					animation.setFillAfter(true);
					mainView.startAnimation(animation);
				}, 10);
			}
		}
		if(!state){
			new Handler().postDelayed(() -> {
				//Turn on the display

				if(hasView) frameLayout.removeView(mainView);
				hasView = false;

				if(wakeLock.isHeld())
					wakeLock.release();
			}, 10);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

//		switch (event.sensor.getType()) {
//			case Sensor.TYPE_PROXIMITY:
//
//				if(StaticGlobals.isDebug)
//					Utils.LogD("Proximity");
//				if (event.values[0] < 1) {
//					// Sensor distance smaller than 1cm
//					wakeLock.release();
//
//					if (StaticGlobals.isScreenOn) {
////						showBlackScreen(true);
//						new Thread(() -> {
//							//Screen off using device admin
//							((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
//						}).start();
//					}
//
//					if(StaticGlobals.isDebug)
//						Utils.LogD("Less than 1 Proximity");
//
//				}else{
//
//					if(StaticGlobals.isDebug)
//						Utils.LogD("More than 1 Proximity");
//					if(!StaticGlobals.isScreenOn)
//						ScreenReceiver.turnScreenOn(this, false);
//
//					new Handler().postDelayed(() -> {
////						if (!refreshing)
////							refresh();
//						wakeLock.acquire();
//
//						if(StaticGlobals.isDebug)
//							Utils.LogD("Lock Acquired");
//					}, 500);
//				}
//		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}


	private class OnDismissListener implements View.OnTouchListener {
		private final GestureDetector gestureDetector;

		OnDismissListener(Context ctx) {
			gestureDetector = new GestureDetector(ctx, new GestureListener());
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}

		private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

			private static final int SWIPE_THRESHOLD = 150;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				try {
					if (!isInCenter(e1)) {
						return false;
					}
					if (e2 != null) {
						float diffY = e2.getY() - e1.getY();
						float diffX = e2.getX() - e1.getX();
						if (Math.abs(diffX) > Math.abs(diffY)) {
							if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

							}
						} else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffY > 0) {
								return gestureAction(ACTION_SWIPE_DOWN);

							} else {

								return gestureAction(ACTION_SWIPE_UP);
							}
						}
					}
				} catch (IllegalArgumentException ignored) {
				}
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return gestureAction(ACTION_DOUBLE_TAP);
			}

			private boolean isInCenter(MotionEvent e) {
				if (e != null) {
					int width = getResources().getDisplayMetrics().widthPixels;
					int height = getResources().getDisplayMetrics().heightPixels;
					return e.getX() > width / 4 && e.getX() < width * 3 / 4 && e.getY() > height / 2.5 && e.getY() < height * 4 / 5;
				}
				return false;
			}
		}
	}
}
