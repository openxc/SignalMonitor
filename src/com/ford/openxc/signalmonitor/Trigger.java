package com.ford.openxc.signalmonitor;

import android.util.Log;

/**
* Trigger exists mainly to support test() which
* allows testing for whether or not threshold (trigger) is met
* for all types: '<', '>', true/false, state
* TODO: make sure I know where to set all these. Provide accessors
* in case I need thread safe?
 */
public class Trigger {
    private String name;
    private double value;
    public String testCriterion; // what before I called 'threshold_type'
    private String TAG = "SignalMonitor";


    Trigger(String name, String value, String tCrit) {
        this.name = name;
        this.value = Double.parseDouble(value);
        this.testCriterion = tCrit;
    }

    /**
     *
     * Status: under test
     * val is ultimately from a Measurement, .value a threshold value from Watchers.txt
     */
    public boolean test(double val) {
        if (this.testCriterion == "<") {
            return (value < val);
        } else if (this.testCriterion == ">") {
            return (value > val);
        } else
            return false;
    }

}
