package me.thomasdotters.activedisplay.Services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

import me.thomasdotters.activedisplay.Views.NotificationHolder;
import me.thomasdotters.activedisplay.StaticGlobals;

/**
 * Created by Thomas Dotters on 04/09/2016.
 */
public class NotificationService extends AccessibilityService {

	private final AccessibilityServiceInfo info = new AccessibilityServiceInfo();
	private static final String TAG = "MyAccessibilityService";

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

		final int eventType = event.getEventType();

		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && StaticGlobals.isServiceRunning) {



			final String sourcePackageName = (String)event.getPackageName();
			Parcelable parcelable = event.getParcelableData();
			if(StaticGlobals.isDebug)
				Log.d("PACKAGE", sourcePackageName);

			if (parcelable instanceof Notification) {

				Notification notification = (Notification) parcelable;


				NotificationHolder holder = createHolder(notification, sourcePackageName);

				List<CharSequence> messages = event.getText();


				//StaticGlobals.onNotificationAction.run();
				StaticGlobals.isValidating = true;
				startService(new Intent(getApplicationContext(), ValidateNotificationService
						.class));
			}
		}
	}

	private NotificationHolder createHolder(Notification added, String packageName) {
		if (added.priority >= android.app.Notification.PRIORITY_LOW) {
			String title = added.extras.getString(Notification.EXTRA_TITLE) + " ";
			if (title.equals("null"))
				title = added.extras.getString(Notification.EXTRA_TITLE_BIG) + " ";
			String content = added.extras.getString(Notification.EXTRA_TEXT) + " ";
			if (content.equals("null") || content.isEmpty())
				content = added.extras.getCharSequence(Notification.EXTRA_TEXT_LINES) + " ";
			if (content.equals("null") || content.isEmpty())
				content = added.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) + " ";
			Drawable icon = getIcon(added, packageName);

			Bitmap bitmap = (Bitmap) added.extras.get(Notification.EXTRA_PICTURE);

			if(StaticGlobals.isDebug)
				Log.d("APP", icon.toString());
			ApplicationInfo notificationAppInfo = null;
			try {
				notificationAppInfo = getPackageManager().getApplicationInfo(packageName, 0);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			NotificationHolder holder = new NotificationHolder(this, title, content, icon, notificationAppInfo != null ? getPackageManager().getApplicationLabel(notificationAppInfo) : null, added.contentIntent);


			if(bitmap != null){
				holder.setContentImage(bitmap);
				if(StaticGlobals.isDebug)
					Log.d("APP", "BITMAP FOUND!");
			}

			StaticGlobals.notificationMap.put(packageName + added.toString(), holder);
			return holder;
		}
		return null;
	}



	private Drawable getIcon(Notification notification, String mPackageName) {
		if(StaticGlobals.isDebug)
			Log.d("ICON", Integer.toString(notification.icon));
		Drawable icon = null;
		try {
			Resources res = getApplicationContext().getPackageManager()
					.getResourcesForApplication(mPackageName);
			icon = res.getDrawable(notification.icon);

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}


		return icon;
	}

	@Override
	public void onInterrupt() {

	}

	@Override
	public void onServiceConnected() {
		// Set the type of events that this service wants to listen to.
		//Others won't be passed to this service.
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;

		// If you only want this service to work with specific applications, set their
		// package names here.  Otherwise, when the service is activated, it will listen
		// to events from all applications.

		// Set the type of feedback your service will provide.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
		} else {
			info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		}

		// Default services are invoked only if no package-specific ones are present
		// for the type of AccessibilityEvent generated.  This service *is*
		// application-specific, so the flag isn't necessary.  If this was a
		// general-purpose service, it would be worth considering setting the
		// DEFAULT flag.

		// info.flags = AccessibilityServiceInfo.DEFAULT;

		info.notificationTimeout = 100;

		this.setServiceInfo(info);
	}

	/**
	 * Check if Accessibility Service is enabled.
	 *
	 * @param mContext
	 * @return <code>true</code> if Accessibility Service is ON, otherwise <code>false</code>
	 */
	public static boolean isAccessibilitySettingsOn(Context mContext) {
		int accessibilityEnabled = 0;
		final String service = "me.thomasdotters.activedisplay/me.thomasdotters.activedisplay.Services.NotificationService";

		boolean accessibilityFound = false;
		try {
			accessibilityEnabled = Settings.Secure.getInt(
					mContext.getApplicationContext().getContentResolver(),
					android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
			if(StaticGlobals.isDebug)
				Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
		} catch (Settings.SettingNotFoundException e) {
			if(StaticGlobals.isDebug)
				Log.e(TAG, "Error finding setting, default accessibility to not found: "
					+ e.getMessage());
		}
		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

		if (accessibilityEnabled == 1) {
			if(StaticGlobals.isDebug)
				Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
			String settingValue = Settings.Secure.getString(
					mContext.getApplicationContext().getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null) {
				TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
				splitter.setString(settingValue);
				while (splitter.hasNext()) {
					String accessabilityService = splitter.next();

					if(StaticGlobals.isDebug)
						Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
					if (accessabilityService.equalsIgnoreCase(service)) {
						if(StaticGlobals.isDebug)
							Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
						return true;
					}
				}
			}
		} else {
			if(StaticGlobals.isDebug)

				Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
		}

		return accessibilityFound;
	}


}