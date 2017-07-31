package me.thomasdotters.activedisplay.Activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import me.thomasdotters.activedisplay.Preferences;
import me.thomasdotters.activedisplay.R;
import me.thomasdotters.activedisplay.Services.DisplayService;
import me.thomasdotters.activedisplay.Services.InitService;
import me.thomasdotters.activedisplay.Services.NotificationService;
import me.thomasdotters.activedisplay.Services.ValidateNotificationService;
import me.thomasdotters.activedisplay.StaticGlobals;

public class MainActivity extends AppCompatActivity {

	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);





		if(StaticGlobals.isValidatorAlive){
			StaticGlobals.isValidatorAlive = false;
			stopService(new Intent(getApplicationContext(), ValidateNotificationService.class));
		}

		Preferences prefs = new Preferences(this);
		prefs.createPreferences();

		Switch enableSwitch = (Switch) findViewById(R.id.enableSwitch);
		assert enableSwitch != null;
		enableSwitch.setChecked(prefs.enabled);

		enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = prefs.getSharedPreferences().edit();
				editor.putBoolean("activeEnabled", isChecked);
				editor.apply();

				Intent InitIntent = new Intent(getApplicationContext(), InitService.class);
				stopService(InitIntent);
				startService(InitIntent);
			}
		});

		Intent InitIntent = new Intent(getApplicationContext(), InitService.class);


		stopService(InitIntent);
		startService(InitIntent);


		Button startbutton = (Button) findViewById(R.id.start_service_button);


		assert startbutton != null;
		startbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent demoService = new Intent(getApplicationContext(), DisplayService.class);
				//demoService.putExtra("demo", true);
				startService(demoService);
			}
		});

		Button settingsButton = (Button)findViewById(R.id.settings_button);

		assert settingsButton != null;

		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent activity = new Intent(MainActivity.this, PreferencesActivity.class);

				startActivity(activity);

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean isEnabledNLS = isEnabled();
		if (!isEnabledNLS) {
			showConfirmDialog();
		}

		if(NotificationService.isAccessibilitySettingsOn(this) == false){
			showAccessibilityDialog();
		}
	}
	

	private boolean isEnabled() {
		String pkgName = getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void openNotificationAccess() {
		startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
	}

	private void showConfirmDialog() {
		new AlertDialog.Builder(this)
				.setMessage("Please enable Notification access")
				.setTitle("Notification Access")
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								openNotificationAccess();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}
						})
				.create().show();
	}

	private void showAccessibilityDialog(){
		new AlertDialog.Builder(this)
				.setMessage("Please enable Accessibility Access")
				.setTitle("Accessibility Service Access")
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}
						})
				.create().show();
	}
}
