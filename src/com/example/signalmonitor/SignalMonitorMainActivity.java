package com.example.signalmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;

import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class SignalMonitorMainActivity extends Activity {

	// per the tutorial, at object creation time:
	private VehicleManager mVehicleManager;
	private boolean mBound = false;
	private String TAG = "SignalMonitor";

	// It is a small file, it should not take long, so we read it in here:
	// Read in list of signals to monitor and threshold conditions/values
	File sdcard = Environment.getExternalStorageDirectory();
	File watcherFile = new File(sdcard, "Watchers.txt");
	String watcherString = null;
	String watcherLine;

	FileInputStream fis = null;
	{
		try {
			fis = new FileInputStream(watcherFile);
			byte[] reader = new byte[fis.available()]; // weird, isn't it?
			while (fis.read(reader) != -1) {
			} // yes, he really did that
			watcherString = new String(reader);
		} catch (IOException e) {
			Log.e(TAG, "oops");
		}
		Log.i(TAG, "Read watchers file: " + watcherString);

	}
    
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        // He forgot to say this 'try' would be necessary
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener); // TODO: modify to read choice of signal from Watchers.txt and do condition test
			} catch (VehicleServiceException e) {
				Log.e(TAG, "Vehicle Service Exception " + e.toString());
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				Log.e(TAG, "Unrecognized Measurment type: " + e.toString());
				e.printStackTrace();
			}
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.w(TAG, "VehicleService disconnected unexpectedly");
	        mVehicleManager = null;
	        mBound = false;
	    }
	};
	


	
	// Judging from when he says to register it, this too must be object creation time:
	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
	    public void receive(Measurement measurement) {
	        final VehicleSpeed speed = (VehicleSpeed) measurement;
	        SignalMonitorMainActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	                mVehicleSpeedView.setText(
	                    "Vehicle speed (km/h): " + speed.getValue().doubleValue());
	            }
	        });
	        // do stuff with the measurement
	    }
	};

	
	protected void onStart() {
	    super.onStart();

	    Intent intent = new Intent(this, VehicleManager.class);
	    if (!mBound)
	         mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private TextView mVehicleSpeedView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signal_monitor_main);
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
	}

	public void onPause() {
	    super.onPause();
	    Log.i(TAG, "Unbinding from vehicle service");
	    unbindService(mConnection);
	    mBound = false;
	}
	
	public void onResume() {
	    super.onResume();
	    if (!mBound) {
	        Intent intent = new Intent(this, VehicleManager.class);
	        mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	        if (mBound)
	            Log.i(TAG, "Binding to Vehicle Manager");
	        else
	            Log.e(TAG, "Failed to bind to Vehicle Manager");
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signal_monitor_main, menu);
		return true;
	}

}
