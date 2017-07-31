package me.thomasdotters.activedisplay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.thomasdotters.activedisplay.Views.NotificationHolder;

/**
 * Created by Thomas Dotters on 03/09/2016.
 */
public class StaticGlobals {
	public static boolean isDebug = true;

	public static Map<String, NotificationHolder> notificationMap = new ConcurrentHashMap<>();

	public static Runnable onNotificationAction;

	public static boolean isServiceRunning = false;
	public static boolean isValidating = false;
	public static boolean isValidatorAlive = false;

	public static boolean isDisplayOn = false;

	public static boolean isScreenOn = false;

}
