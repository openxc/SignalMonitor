package com.ford.openxc.signalmonitor;

import java.util.HashMap;

import com.openxc.remote.RawMeasurement;
import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.sinks.DataSinkException;

public class VehicleSnapshotSink extends BaseVehicleDataSink {
    private HashMap<String, RawMeasurement> mMeasurements =
            new HashMap<String, RawMeasurement>();

    public synchronized boolean receive(RawMeasurement measurement)
            throws DataSinkException {
        mMeasurements.put(measurement.getName(), measurement);
        return true;
    }

    // TODO This is returning a mangy string that's not a JSON in and of itself
    // - a better way would be to generate a single JSON object, e.g.:
    //
    // {'vehicle_speed': 42,
    //   'transmission_gear_position: 'first',
    //   ...
    // }
    //
    // or a JSON object wtih an array of all of the serialized measurements:
    //
    // [{"name": "steering_wheel_angle", "value": 42},
    //     {"name": "parking_brake_status", "value": false}]
    //
    // This array is what the UploaderSink creates, so you could just copy the
    // code from there but you would have to make sure the server side was
    // looking for this format first.
    //
    public synchronized String generateSnapshot() {
        StringBuffer sb = new StringBuffer();
        for(RawMeasurement measurement : mMeasurements.values()) {
            sb.append(measurement.serialize());
        }
        return sb.toString();
    }
}
