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
	private String value;
	public String testCriterion; // what before I called 'threshold_type'
	private String TAG = "SignalMonitor";


	Trigger(String name, String value, String tCrit) {
		this.name = name;
		this.value = value;
		this.testCriterion = tCrit;
	}

	/**
	 * I think I can make this private, since it will only be in
	 * uploadConditionally()
	 *
	 * Status: works
	 */
	public boolean test(String val) {
		if (this.testCriterion == "<") {
			return (Double.parseDouble(this.value) < Double.parseDouble(val));
		} else if (this.testCriterion == ">") {
			return (Double.parseDouble(this.value) < Double.parseDouble(val));
		} else if (this.testCriterion == "boolean") {
			return (this.value.equals(true));
		} else if (this.testCriterion == "state") { // e.g. test(gear_pos, "first")
			return (this.value.equals(val));
		} else
			return false;
	}
	public void uploadConditionally(Trigger aTrigger, String value) {
	    if (aTrigger.test(value)) {
	        Log.i(TAG, "Speed threshold met");
	         SignalMonitorMainActivity.makeSnapshot();
	         SignalMonitorMainActivity.uploadSnapshot();
	    }
	}

}
