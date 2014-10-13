package com.bewareofraj.magic8ball;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	// the connection to the hardware
	private SensorManager mySensorManager;

	// Current acceleration values
	private float xAccel, yAccel, zAccel;

	// Previous acceleration values
	private float xPreviousAccel, yPreviousAccel, zPreviousAccel;

	// Used to suppress the first shaking
	private boolean firstUpdate = true;

	// What acceleration difference would we assume as rapid movement?
	private final float shakeThreshold = 1.5f;

	// Has shaking motion been started (one direction)
	private boolean shakeInitiated = false;

	// Screen orientation constants
	private final String ORIENTATION_UP = "up";
	private final String ORIENTATION_DOWN = "down";

	// Current orientation of device (facing up/down)
	private String currentOrientation = ORIENTATION_UP;

	// Previous orientation of device
	private String prevOrientation = ORIENTATION_DOWN;

	// The SensorEventListener lets us wire up to the real hardware events
	private final SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent se) {
			// check if the device was shaken, not flipped
			updateAccelParameters(se.values[0], se.values[1], se.values[2]);
			if ((!shakeInitiated) && isAccelerationChanged()) {
				shakeInitiated = true;
			} else if ((shakeInitiated) && isAccelerationChanged()) {
				giveAnswer();
			} else if ((shakeInitiated) && (!isAccelerationChanged())) {
				shakeInitiated = false;
			}

			// check if the device was flipped
			float z = se.values[2];
			if (z > 9 && z < 10) {
				updateOrientation(ORIENTATION_UP);
			} else if (z > -10 && z < -9) {
				updateOrientation(ORIENTATION_DOWN);
			}
			if (prevOrientation.equals(ORIENTATION_DOWN)
					&& currentOrientation.equals(ORIENTATION_UP)) {
				giveAnswer();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private void updateOrientation(String newOrientation) {
		prevOrientation = currentOrientation;
		currentOrientation = newOrientation;
	}

	private void updateAccelParameters(float xNewAccel, float yNewAccel,
			float zNewAccel) {
		// we have to supress the first change of acceleration, it results from
		// first values being initialized with 0
		if (firstUpdate) {
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
		} else {
			xPreviousAccel = xAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}

	private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - yAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeThreshold && deltaY > shakeThreshold)
				|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
				|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
	}

	private void giveAnswer() {
		// TODO: display an answer and make the device say it out loud
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySensorManager.registerListener(mySensorEventListener,
				mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		initializeScreen();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void initializeScreen() {
		// hide action bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.hide();
		}

		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and
		// higher, but as
		// a general rule, you should design your app to hide the status bar
		// whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);

	}
}
