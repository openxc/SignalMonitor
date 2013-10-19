package com.ford.openxc.signalmonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 *
 * @author mjohn706
 * The ultimate purpose is to do an HTTP Post, not a Get. But I want to get this running w/ the Get first
 */
public class PostalService extends IntentService {
    private final static String TAG = "PostalService";
    public final static String INTENT_EXTRA_DATA_FLAG = "SNAPSHOT";

    public PostalService() {
        super(TAG);
    }

    private String stubData =
"[{\"timestamp\": \"1351181673.5829988\", \"name\": \"accelerator_pedal_position\", \"value\": \"0.0\"} ];";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent Start");

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost("http://192.168.2.160:5000/posty");
        //HttpPost httpPost = new HttpPost("http://shatechcrunchhana.sapvcm.com:8000/Ford/services/fordstatus.xsodata/FordStatus");
        // Uncomment above for Ford's server, though this is the wrong one.
            StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(intent.getExtras().getString(INTENT_EXTRA_DATA_FLAG), "UTF-8");
        } catch(UnsupportedEncodingException e) {
            Log.d(TAG, "Unable to encode snapshot data into UTF-8" + e.getMessage());
            return;

        } catch(Exception anyOtherException) {
            Log.d(TAG, "Unexpected exception" + anyOtherException.getMessage());
            return;
        }

        httpPost.addHeader("Content-type", "application/json; charset=utf-8");
        httpPost.setEntity(stringEntity);
        HttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch(IOException e) {
            Log.d(TAG, "Unable to make HTTP request" + e.getMessage());
            return;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        Log.i(TAG, "POST ret. code: " + statusCode);
    }
}
