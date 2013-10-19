package com.ford.openxc.signalmonitor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.openxc.remote.RawMeasurement;
import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.sinks.DataSinkException;

public class VehicleSnapshotSink extends BaseVehicleDataSink {
    
    public static final String TAG = "VehicleSnapshotSink";
    
    private HashMap<String, RawMeasurement> mMeasurements =
            new HashMap<String, RawMeasurement>();

    public synchronized boolean receive(RawMeasurement measurement)
            throws DataSinkException {
        mMeasurements.put(measurement.getName(), measurement);
        return true;
    }
    /**
     * Given an iterator over a collection of serialized JSON objects, 
     * returns a serialized JSON array.
     * @param recordIterator An iterator over a collection of strings representing JSON objects
     * @return A serialized JSON array
     * @throws IOException
     */
    private String objectsToArray(Iterator<RawMeasurement> recordIterator)
        throws IOException {
        StringWriter buffer = new StringWriter(512);
        JsonFactory jsonFactory = new JsonFactory();

        JsonGenerator gen = jsonFactory.createJsonGenerator(buffer);

        gen.writeStartObject();
        gen.writeArrayFieldStart("records");
        while(recordIterator.hasNext()) {
            gen.writeRaw(recordIterator.next().serialize());
            if(recordIterator.hasNext()) {
                gen.writeRaw(",");
            }
        }
        gen.writeEndArray();
        gen.writeEndObject();

        gen.close();
        return buffer.toString();
    }
    
    public synchronized String generateSnapshot() {
        try {
            return objectsToArray(mMeasurements.values().iterator());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
