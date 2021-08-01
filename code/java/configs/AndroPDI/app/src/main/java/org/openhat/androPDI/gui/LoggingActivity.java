//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openhat.androPDI.DeviceManager;
import org.openhat.androPDI.R;

/** An activity that implements basic logging functionality.
 * Expects that onCreate methods load a view that contains a ScrollView svLog and a TextView svLog.
 * 
 * @author leo.meyer
 *
 */
public abstract class LoggingActivity extends Activity {
	
	protected ScrollView svLog;
	protected TextView tvLog;
    
	protected DeviceManager deviceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        
		deviceManager = DeviceManager.getInstance();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
        tvLog = (TextView)findViewById(R.id.tvLog);
        if (tvLog != null)
        	tvLog.setText(deviceManager.getLogText());
        svLog = (ScrollView)findViewById(R.id.svLog);
        if (svLog != null)
        	svLog.fullScroll(View.FOCUS_DOWN);
	}
	
	protected void addLogMessage(final String message) {
        if (tvLog != null) {
			tvLog.append(message);
			tvLog.append("\n");
        }
        if (svLog != null)
        	svLog.fullScroll(View.FOCUS_DOWN);
		
		deviceManager.addLogText(message + "\n");
	}
}
