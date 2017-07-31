package me.thomasdotters.activedisplay.Recievers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.Services.DisplayService;
import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Utils;

import static android.content.Context.POWER_SERVICE;


public class ScreenReceiver extends BroadcastReceiver {

	private boolean screenOff;
	private static final String WAKE_LOCK_TAG = "ScreenOnWakeLock";
	private Preferences preferences;


	public ScreenReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// an Intent broadcast.

		preferences = new Preferences(context);
		preferences.createPreferences();

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			if(StaticGlobals.isDebug)
				Utils.LogD("Screen OFF");

			StaticGlobals.isScreenOn = false;

			if(StaticGlobals.isDisplayOn && !StaticGlobals.isScreenOn){
				turnScreenOn(context, true);
				if(StaticGlobals.isDebug)
					Utils.LogD("Turning on");

			}else {

				if (preferences.enabled) {
					final KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
					if (myKM.inKeyguardRestrictedInputMode()) {
						//Screen is locked, start the service
						context.startService(new Intent(context, DisplayService.class));
						StaticGlobals.isDisplayOn = true;

						if(StaticGlobals.isDebug)
							Utils.LogD("Displaying");

					}else{
						context.startService(new Intent(context, DisplayService.class));
						StaticGlobals.isDisplayOn = true;

						if(StaticGlobals.isDebug)
							Utils.LogD("Displaying No Lock");
					}
				}

			}


		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			if(StaticGlobals.isDebug)
				Utils.LogD("Screen ON");

			StaticGlobals.isScreenOn = true;


		}

	}

	public static void turnScreenOn(Context c, boolean stopService) {
		try {
			if (stopService) {
				c.stopService(new Intent(c, DisplayService.class));
				StaticGlobals.isDisplayOn = false;
			}
			@SuppressWarnings("deprecation")
			PowerManager.WakeLock wl = ((PowerManager) c.getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKE_LOCK_TAG);
			wl.acquire();
			wl.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
