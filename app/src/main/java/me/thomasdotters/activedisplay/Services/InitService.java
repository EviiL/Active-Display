package me.thomasdotters.activedisplay.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.Recievers.ScreenReceiver;
import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Utils;

public class InitService extends Service {

	private Preferences preferences;
	private BroadcastReceiver mReceiver;
	private boolean isRegistered = false;

	@Override
	public int onStartCommand(Intent origIntent, int flags, int startId) {

		if(preferences != null) {
			if (preferences.enabled) {
				registerScreenReceiver();
			}
		}
		return super.onStartCommand(origIntent, flags, startId);

	}

	public InitService() {




	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();


		preferences = new Preferences(this);
		preferences.createPreferences();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterScreenReceiver();
	}


	private void registerScreenReceiver() {
		unregisterScreenReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);
		isRegistered = true;

		if(StaticGlobals.isDebug)
			Utils.LogD("Receiver Registered");
	}

	private void unregisterScreenReceiver() {
		if (!isRegistered) {
			return;
		}
		try {
			unregisterReceiver(mReceiver);
			if (mReceiver.isOrderedBroadcast())
				mReceiver.abortBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isRegistered = false;
		}
	}


}
