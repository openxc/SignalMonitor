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
     * Threshold is considered met when trigger(threshold) value < current val for '<' etc. 
     */
    public boolean test(double val) {
    	//Log.i(TAG, "testing value= " + value + "val = " + val);
        if (this.testCriterion.equals("<>")) {
        	//Log.i(TAG, "testing for value < val" + "or " + value + "<" + val);
            return (value < val);
        } else if (this.testCriterion.equals(">")) {
        	//Log.i(TAG, "testing for value > val" + "or " + value + "<" + val);
            return (value > val);
        } else {
        	//Log.i(TAG, "testCriterion = " + testCriterion);
            return false;
        }
    }
}
