package com.ford.openxc.signalmonitor;

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
    private final static String TAG = "PostalService";
    public final static String INTENT_EXTRA_DATA_FLAG = "SNAPSHOT";

    public PostalService() {
        super(TAG);
    }

    private String stubData =
"[{\"timestamp\": \"1351181673.5829988\", \"name\": \"accelerator_pedal_position\", \"value\": \"0.0\"} ];";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent Start");

        String responseContents = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://shatechcrunchhana.sapvcm.com:8000/Ford/services/fordstatus.xsodata/FordStatus");
            //HttpPost httpPost = new HttpPost("http://shacricketwin.sapvcm.com:8080/FordData_v2/fordxctest_vs.jsp");
            StringEntity stringEntity = new StringEntity(
                    intent.getExtras().getString(INTENT_EXTRA_DATA_FLAG),
                    "UTF-8");
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
