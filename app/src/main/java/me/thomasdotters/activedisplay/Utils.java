package me.thomasdotters.activedisplay;

import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Thomas Dotters on 03/09/2016.
 */
public class Utils {


	public static String getUniqueKey(StatusBarNotification notification) {
		return notification.getPackageName().concat(":").concat(String.valueOf(notification.getId()));
	}


	public static void LogD(String message){
		Log.d("ACTIVE DISPLAY", message);
	}
}
