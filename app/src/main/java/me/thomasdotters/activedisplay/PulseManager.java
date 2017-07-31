package me.thomasdotters.activedisplay;

import android.content.Context;
import android.os.Handler;

/**
 * Created by Thomas Dotters on 27/09/2016.
 */
public class PulseManager {

	private Context mContext;
	private Handler mHandler;

	public enum STATES{
		EXIT_PREVIEW, HIDDEN, SHOWN, IN_PREVIEW
	}


	public PulseManager(Context context){
		mContext = context;

		mHandler = new Handler();

	}


	public void update(){

	}
}
