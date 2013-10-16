package com.example.signalmonitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.VehicleServiceException;

import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract.Contacts.Data;
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
		private String signal_threshold = "<"; // the default.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signal_monitor_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signal_monitor_main, menu);
		return true;
	}
	
	// services
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established                                                                                                                      
        public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i("openxc", "Bound to VehicleManager");
                mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();
                Log.i(TAG, "mVehicleManager = " + mVehicleManager.toString());
                // He forgot to say this 'try' would be necessary                                                                                                                          
                try {
                        mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener); // TODO:modify to read choice of signal from Watcher.txt and do condition test                    
                } catch (VehicleServiceException e) {
                        Log.e(TAG, "Vehicle Service Exception " + e.toString());
                } catch (UnrecognizedMeasurementTypeException e) {
                        Log.e(TAG, "Unrecognized Measurment type: " + e.toString());
                }
        }

        // Called when the connection with the service disconnects unexpectedly                                                                                                            
        public void onServiceDisconnected(ComponentName className) {
                Log.w(TAG, "VehicleService disconnected unexpectedly");
                mVehicleManager = null;
                mBound = false;
        }
};
protected void onStart() {
    super.onStart();

    Intent intent = new Intent(this, VehicleManager.class);
    if (!mBound)
            mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

private void triggerFound() {
	Log.i(TAG, "triggerFound called");
}
// creation time:                                                                                                                                                                          
VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
                final VehicleSpeed speed = (VehicleSpeed) measurement;
                SignalMonitorMainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        }
                });
                // "do stuff with the measurement"                                                                                                                                         
                // what I do is test against a criterion.                                                                                                                                  
                if (signal_threshold.equals(">")) { // decide which test to do -- for now, just this one.                                                                                  
                        Log.i(TAG, "Testing for speed > " + Double.parseDouble(signal_threshold));
                        if (speed.getValue().doubleValue() < Double.parseDouble(signal_threshold)) {
                                triggerFound();
                        }
                }
        }
};


}
