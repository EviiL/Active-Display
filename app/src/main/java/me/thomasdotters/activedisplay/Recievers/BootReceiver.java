package me.thomasdotters.activedisplay.Recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.thomasdotters.activedisplay.Services.InitService;
import me.thomasdotters.activedisplay.StaticGlobals;
import me.thomasdotters.activedisplay.Utils;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		if(StaticGlobals.isDebug)
			Utils.LogD("Booted UP");

		context.startService(new Intent(context, InitService.class));
	}
}
