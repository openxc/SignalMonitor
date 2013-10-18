package com.ford.openxc.signalmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.example.signalmonitor.R;
import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.remote.VehicleServiceException;

/**
 *
 * @author mjohn706
 *
 */
public class SignalMonitorMainActivity extends Activity {

	// per the tutorial, at object creation time:
	private static VehicleManager mVehicleManager;
	private boolean mBound = false;
	private static String TAG = "SignalMonitor";
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
	String threshold_value;
	String team_id;
    BufferedReader br = null;

    static String snapshot = null;

    Trigger[]  triggers = new Trigger[20]; // surely never more than this. // Obsolete already?
    HashMap<String, Trigger> NamesToTriggers = new HashMap<String, Trigger>();


    int lineNo = 0;
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
					team_id =  watchersObject.get("team_id").toString();
					Log.i(TAG, "team_id: " + team_id);

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
						}
						}
						// at this point, watchersObject has only one name
							// left, the signal name.
						String won = watchersObject.names().toString();
						Log.i(TAG, "watchersObject signal name: " + won); // alas, Android's JSON incomplete, so:
						String wsb = new String(won.substring(2, won.length() - 2));
						Log.i(TAG, "watchersObject signal name as string: "	+ wsb); // alas, Android's JSON incomplete, so:
						signal_name = wsb;
						// at this point we have read in the whole line and set
						// only wsb=signal_name and threshold_type. But we can
						// use the signal_name to get the value, so:
						// and now the all important value:
						threshold_value = watchersObject.getString(signal_name);

					} catch (IllegalStateException e1) {
						Log.e(TAG, "IllegalStateException reading from newly created JSONObject" + e1.getMessage());
					} catch (UnsupportedOperationException boohoo) {
						Log.e(TAG, "UnsupportedOperationException reading from newly created JSONObject" + boohoo.getMessage()); // we should never get here
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					} finally {
						Log.e(TAG, "Parsed JSON object for team_id, threshold_type and signal name");
						//setListeners (threshold_type, signal_name);
					}
					Log.i(TAG, "lineNo = " + lineNo);
					triggers[lineNo] = new Trigger(signal_name, threshold_value, threshold_type);
			        NamesToTriggers.put(signal_name, triggers[lineNo]);
					lineNo++;
			}


				} catch (IOException e) { Log.e(TAG, "Exception reading Watchers file" + e.getMessage());
				} finally {
					try {br.close();} catch(IOException ee) {Log.e(TAG, "Exception closing buffered reader"); } //TODO: obviate ts
		Log.i(TAG, "Got it all");
		Log.i(TAG, "Should be two triggers now:");
		Log.i(TAG, "triggers[0] = " + triggers[0]);
		Log.i(TAG, "triggers[1] = " + triggers[1]);

		// triggerFound();
		/*Intent newIntent = new Intent(this, PostalService.class);
		startService(newIntent);
		Log.i(TAG, "started PostalService"); */
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
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
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

			// "do stuff with the measurement"
			// what I do is test against a criterion, using my new Trigger class
		    Trigger ourTrigger = NamesToTriggers.get("vehicle_speed");
			Log.i(TAG, "Testing for speed " + ourTrigger.testCriterion + " speed");
		    if (ourTrigger.test(speed.toString())) {
                Log.i(TAG, "vehicle speed test passed");
		    }
		}
	};

	EngineSpeed.Listener mEngineSpeed = new EngineSpeed.Listener() {
		public void receive(Measurement measurement) {
			final EngineSpeed speed = (EngineSpeed) measurement;

			// "do stuff with the measurement"
			// what I do is test against a criterion, using my new Trigger class
		    Trigger ourTrigger = NamesToTriggers.get("vehicle_speed");
			Log.i(TAG, "Testing for engine speed " + ourTrigger.testCriterion + " speed");
		    if (ourTrigger.test(speed.toString())) {
                Log.i(TAG, "engine speed test passed");
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

	public void registerListener(String signalName, String threshold,
			String thresholdValue) {
		try {
			if (signalName.equals("vehicle_speed"))
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
			else if (signalName.equals("engine_speed"))
				mVehicleManager.addListener(EngineSpeed.class, mEngineSpeed);
			else
				Log.e(TAG, "We should never get here");
		} catch (Exception e) {
			Log.e(TAG, "Exception in registerListener: " + e.getMessage());
		}
	}

	static public void makeSnapshot() {
		StringBuffer sb = new StringBuffer();
		try {
			VehicleSpeed rawSpeed = (VehicleSpeed) mVehicleManager.get(VehicleSpeed.class);
			sb.append(rawSpeed.serialize()); // repeat fr ea msrt
			TorqueAtTransmission rawTorque = (TorqueAtTransmission) mVehicleManager.get(TorqueAtTransmission.class);
			AcceleratorPedalPosition rawPedal = (AcceleratorPedalPosition) mVehicleManager.get(AcceleratorPedalPosition.class);
			sb.append(rawPedal.serialize());
			BrakePedalStatus rawBrake = (BrakePedalStatus) mVehicleManager.get(BrakePedalStatus.class);
			sb.append(rawBrake.serialize());
			EngineSpeed rawRPM = (EngineSpeed) mVehicleManager.get(EngineSpeed.class);
			sb.append(rawRPM.serialize());
			FuelConsumed used = (FuelConsumed) mVehicleManager.get(FuelConsumed.class);
			sb.append(used.serialize());
			FuelLevel left = (FuelLevel) mVehicleManager.get(FuelLevel.class);
			sb.append(left.serialize());
			HeadlampStatus lights = (HeadlampStatus) mVehicleManager.get(HeadlampStatus.class);
			sb.append(lights.serialize());
			HighBeamStatus brights = (HighBeamStatus) mVehicleManager.get(HighBeamStatus.class);
			sb.append(brights.serialize());
			IgnitionStatus key = (IgnitionStatus) mVehicleManager.get(IgnitionStatus.class);
			sb.append(key.serialize());
			Latitude lat = (Latitude) mVehicleManager.get(Latitude.class);
			sb.append(lat.serialize());
			Longitude lon = (Longitude) mVehicleManager.get(Longitude.class);
			sb.append(lon.serialize());
			Odometer odo = (Odometer) mVehicleManager.get(Odometer.class);
			sb.append(odo.serialize());
			ParkingBrakeStatus pbs = (ParkingBrakeStatus) mVehicleManager.get(ParkingBrakeStatus.class);
			sb.append(pbs.serialize());
			SteeringWheelAngle swa = (SteeringWheelAngle) mVehicleManager.get(SteeringWheelAngle.class);
			sb.append(swa.serialize());
			TransmissionGearPosition tgp = (TransmissionGearPosition) mVehicleManager.get(TransmissionGearPosition.class);
			sb.append(tgp.serialize());
			TurnSignalStatus tss = (TurnSignalStatus) mVehicleManager.get(TurnSignalStatus.class);
			sb.append(tss.serialize());
			VehicleButtonEvent vbe = (VehicleButtonEvent) mVehicleManager.get(VehicleButtonEvent.class);
			sb.append(vbe.serialize());
			VehicleDoorStatus vds = (VehicleDoorStatus) mVehicleManager.get(VehicleDoorStatus.class);
			sb.append(vds.serialize());
			WindshieldWiperStatus wws = (WindshieldWiperStatus) mVehicleManager.get(WindshieldWiperStatus.class);
			sb.append(wws.serialize());
            snapshot = sb.toString();
		} catch (NoValueException e) {
			Log.w(TAG, "The vehicle may not have made the measurement yet");
		} catch (UnrecognizedMeasurementTypeException e) {
			Log.w(TAG, "The measurement type was not recognized");
		}
	}

	static public void uploadSnapshot() {
	} // void for now
}
