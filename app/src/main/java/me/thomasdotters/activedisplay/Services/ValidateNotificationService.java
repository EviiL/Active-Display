package me.thomasdotters.activedisplay.Services;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Views.NotificationHolder;

/**
 * Created by Thomas Dotters on 04/09/2016.
 */
public class ValidateNotificationService extends NotificationListenerService {

	@Override
	public void onCreate() {
		super.onCreate();
		StaticGlobals.isValidatorAlive = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(StaticGlobals.isValidating) {
			Map<String, NotificationHolder> temp = new ConcurrentHashMap<>();

			for (final Map.Entry<String, NotificationHolder> entry : StaticGlobals.notificationMap.entrySet()) {

				for (StatusBarNotification sbn : getActiveNotifications()) {
					if (sbn.getNotification().contentIntent != null) {
						if (sbn.getNotification().contentIntent.equals(entry.getValue().getIntent())) {
							temp.put(entry.getKey(), entry.getValue());

						}
					}
				}

			}

			StaticGlobals.notificationMap.clear();
			StaticGlobals.notificationMap = temp;

			StaticGlobals.isValidating = false;
		}
		if(StaticGlobals.onNotificationAction != null)
			StaticGlobals.onNotificationAction.run();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onNotificationPosted(StatusBarNotification added) {

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification removed) {

	}

	@Override
	public void onListenerConnected(){
		stopSelf();
	}
}
