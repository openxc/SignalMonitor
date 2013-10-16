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

/**
 * 
 * @author mjohn706
 * For now, ignore the signal_name just read from JSON and always set up Listener for VehicleSpeed
 */
public class SignalMonitorMainActivity extends Activity {

	// per the tutorial, at object creation time:
	private VehicleManager mVehicleManager;
	private boolean mBound = false;
	private String TAG = "SignalMonitor";
	private String signal_threshold = "<"; // the default.

	// It is a small file, it should not take long, so we read it in here:
	// Read in list of signals to monitor and threshold conditions/values
	File sdcard = Environment.getExternalStorageDirectory();
    File watchersFile = new File(sdcard, "Watchers.txt");

	String watchersString = null;
	StringBuffer watchersStringBuff = new StringBuffer();
	String watchersLine;
	JSONObject watchersObject;
	String signal_name;
	String threshold_type;
    BufferedReader br = null;
	{
		try {
		    br = new BufferedReader(new FileReader(watchersFile));
			String line=null;
			Log.i(TAG, "br = " + br.toString());
			// originally buffered up line by line. But what I want is do do all
			// the processing based on line read in.
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) { // works, but should not be needed
					continue;
				}
				Log.i(TAG, "line length: " + line.length() + " line: " + line);
				watchersStringBuff.append(line); // may keep for debugging, but no longer used.
				try {
					watchersObject = new JSONObject(line);
					Log.i(TAG, "created JSONObject " + watchersObject);
					// read the easy ones: threshold_type, team_id
					threshold_type = watchersObject.get("threshold_type").toString();
					Log.i(TAG, 	"threshold_type: "	+ threshold_type);
					Log.i(TAG, "team_id: " + watchersObject.get("team_id"));
					
				} catch (JSONException e) {
					Log.e(TAG, "Exception reading JSONobject itself from one line of Watchers.txt: " + e.getMessage());
				} finally {
					Log.e(TAG, "Read JSON on one line");
				}
					try {
						@SuppressWarnings("unchecked")
						Iterator<String> ourNamesIt = watchersObject.keys();
						String candidateKey;
						while (ourNamesIt.hasNext()) {
							candidateKey = ourNamesIt.next();
							if (candidateKey.equals("team_id")	|| candidateKey.equals("threshold_type")) {
								ourNamesIt.remove();
							} // at this point, watchersObject has only one name left, the signal name.
							String won = watchersObject.names().toString();
							Log.i(TAG, "watchersObject signal name: " + won); // alas, Android's JSON incomplete, so:
							String wsb = new String(won.substring(2, won.length()-2));
							Log.i(TAG, "watchersObject signal name as string: " + wsb); // alas, Android's JSON incomplete, so:
							signal_name = wsb;

						}
					} catch (IllegalStateException e1) {
						Log.e(TAG, "IllegalStateException reading from newly created JSONObject" + e1.getMessage());
					} catch (UnsupportedOperationException boohoo) {
						Log.e(TAG, "UnsupportedOperationException reading from newly created JSONObject" + boohoo.getMessage()); // we should never get here
					} finally {
						Log.e(TAG, "Parsed JSON object for team_id, threshold_type and signal name");
						//setListeners (threshold_type, signal_name); 
					}
			}
				} catch (IOException e) { Log.e(TAG, "Exception reading Watchers file" + e.getMessage());
				} finally {
					try {br.close();} catch(IOException ee) {Log.e(TAG, "Exception closing buffered reader"); } //TODO: obviate ts
		Log.i(TAG, "Got it all");
		// triggerFound();
		Class postalClass = PostalService.class;
		Log.i(TAG, "PoastalService.class = " + postalClass.toString());
		Intent newIntent = new Intent(this, postalClass);
		startService(newIntent);
		Log.i(TAG, "started PostalService");
		}
	}

	/**
	 * 
	 * @param threshold
	 * @param sig
	 * This will create a Listener for the given signal and this listener in turn will do the test indicated
	 * by threshold, calling triggerFound() to start sending all the data up to SAP server.
	 */
	private void setListeners( String threshold, String sig) {
		Log.i(TAG, "Stub for setting Listener for signal " + sig + " and threshold " + threshold + ".");
		signal_threshold = threshold; // not actually using these yet
		try {
			mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
		} catch (VehicleServiceException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());

		} catch (UnrecognizedMeasurementTypeException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		} // TODO:modify to read choice of signal from Watcher.txt and do condition test

	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i("openxc", "Bound to VehicleManager");
			mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();
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

	// Judging from when he says to register it, this too must be object
	// creation time:
	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
		public void receive(Measurement measurement) {
			final VehicleSpeed speed = (VehicleSpeed) measurement;
			SignalMonitorMainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					mVehicleSpeedView.setText("Vehicle speed (km/h): "
							+ speed.getValue().doubleValue());
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

	/**
	 * When the trigger is found, we are to send up the data via our "Postal Service"
	 */
	private void triggerFound() {
		Log.i(TAG, "stub for triggerFound called");
try {
		Intent intent;
		intent = new Intent(this, PostalService.class);
		Log.i(TAG, "new intent: " + intent.toString());

		this.startService(intent); // "set and forget" via IntentService
} catch (Exception e) { 
	Log.e(TAG, e.getMessage());
	
}
	}
	
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
	
	public void onTrigger() {
		Intent intent = new Intent(this, PostalService.class);
		startService(intent);
		
	}

}
