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
    private String mName;
    private double mValue;
    public String mTestCriterion; // what before I called 'threshold_type'
    private String TAG = "SignalMonitor";


    Trigger(String name, String value, String tCrit) {
        this.mName = name;
        this.mValue = Double.parseDouble(value);
        this.mTestCriterion = tCrit;
    }
    
    /**
     * Tests whether a trigger condition is met.  Currently we only trigger on doubles.
     * Example: if we are testing the current value "val" against a trigger "> 20",
     * test will return true if val > 20.
     * @param val
     * @return
     */
    public boolean test(double val) {
    	//Log.i(TAG, "testing value= " + value + "val = " + val);
        if (this.mTestCriterion.equals("<")) {
        	//Log.i(TAG, "testing for value < val" + "or " + value + "<" + val);
            return (val < mValue);
        } else if (this.mTestCriterion.equals(">")) {
        	//Log.i(TAG, "testing for value > val" + "or " + value + "<" + val);
            return (val > mValue);
        } else {
        	//Log.i(TAG, "testCriterion = " + testCriterion);
            return false;
        }
    }
}
