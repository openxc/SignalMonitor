package com.example.signalmonitor;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author mjohn706
 * The ultimate purpose is to do an HTTP Post, not a Get. But I want to get this running w/ the Get first
 */
public class PostalService extends IntentService {

	public PostalService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private String TAG = "SignalMonitor:PostalService: ";

	private String stubData =
"[{\"timestamp\": \"1351181673.5829988\", \"name\": \"accelerator_pedal_position\", \"value\": \"0.0\"} ];";

/*"{\"timestamp\": 1351181673.5879989, \"name\": \"engine_speed\", \"value\": 0.0}," +
"{\"timestamp\": 1351181673.592999, \"name\": \"vehicle_speed\", \"value\": 0.0}," +
"{\"timestamp\": 1351181673.597999, \"name\": \"transmission_gear_position\", \"value\": \"neutral\"}," +
"{\"timestamp\": 1351181673.6029992, \"name\": \"fine_odometer_since_restart\", \"value\": 1.011181}," +
"{\"timestamp\": 1351181673.6079993, \"name\": \"steering_wheel_angle\", \"value\": -81.26355}," +
"{\"timestamp\": 1351181673.6129994, \"name\": \"parking_brake_status\", \"value\": false}," +
"{\"timestamp\": 1351181673.6179996, \"name\": \"headlamp_status\", \"value\": false}," +
"{\"timestamp\": 1351181673.6480002, \"name\": \"powertrain_torque\", \"value\": 60.0}," +
"{\"timestamp\": 1351181673.6930013, \"name\": \"brake_pedal_status\", \"value\": false}," +
"{\"timestamp\": 1351181673.728002, \"name\": \"windshield_wiper_status\", \"value\": false}," +
"{\"timestamp\": 1351181673.7480025, \"name\": \"torque_at_transmission\"\", \"value\": 60.0}," +
"{\"timestamp\": 1351181673.773003, \"name\": \"gear_level_position\", \"value\": \"second\"}," +
"{\"timestamp\": 1351181673.773003, \"name\": \"odometer\", \"value\": 132017}," +
"{\"timestamp\": 1351181673.773003, \"name\": \"fuel_consumed_since_restart\", \"value\": 0.204}," +
"{\"timestamp\": 1351181673.773003, \"name\": \"ignition_status\", \"value\": \"accessory\"}," +
"{\"timestamp\": 1351181673.773003, \"name\": \"fuel_level\", \"value\": 35}," +
"{\"timestamp\": 1351181673.773003, \"name\":  \"high_beam_status\", \"value\": true}," +
"{\"timestamp\": 1351181673.773003, \"name\":  \"windshield_wiper_status\", \"value\": false}," +
"{\"timestamp\": 1351181673.773003, \"name\":  \"latitude\", \"value\": 37.28}," +
"{\"timestamp\": 1351181673.773003, \"name\":  \"longitude\", \"value\": 32.36}]";
*/


	public PostalService() {
		super("PostalService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("IntentService", "onHandleIntent Start");


		String responseContents = null;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://shatechcrunchhana.sapvcm.com:8000/Ford/services/fordstatus.xsodata/FordStatus");
			//HttpPost httpPost = new HttpPost("http://shacricketwin.sapvcm.com:8080/FordData_v2/fordxctest_vs.jsp");
			// This is where we will set up the post data itself, then
			// call post.setEntity(<data here>); // BEFORE httpClient.execute // for now I will use my array from the email
			StringEntity stringEntity = new StringEntity (stubData, "UTF-8");
			httpPost.addHeader("Content-type", "application/json; charset=utf-8");
			httpPost.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			Log.i(TAG, "POST ret. code: " + statusCode);
			HttpEntity httpEntity = httpResponse.getEntity();
			responseContents = EntityUtils.toString(httpEntity); // which I barely care about now, since I change to POST.

		} catch (Exception e) {
			Log.e(TAG, "Uh, oh");
			e.printStackTrace();
		}
		if (responseContents.length() < 1000) {
			Log.e(TAG, "retrieved: " + responseContents);
		} else {
			Log.e(TAG, "retrieved " + responseContents.length() + " chars");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 500; i++) {
				sb.append(responseContents.charAt(i));
			}
			Log.i(TAG, "first 500: " + sb);
		}
	}

}
