//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.bluetooth;

import java.util.Locale;

import org.openhat.androPDI.R;
import org.openhat.androPDI.AndroPDI;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/** This class implements an activity to edit a Bluetooth device's settings. 
 * */
public class EditBluetoothDevice extends Activity {

    private BluetoothAdapter mBluetoothAdapter;

	private EditText etAddress;
	private EditText etName;
	private EditText etPSK;
	private EditText etPIN;
	private CheckBox cbSecure;    
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bluetooth_device);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        Bundle extras = getIntent().getExtras();
		if (extras == null) {
			// no data? return
			finish();
			return;
		}

		final String originalDeviceSerialization = extras.getString(AndroPDI.DEVICE_SERIALIZATION);
		if (originalDeviceSerialization == null) {
			finish();
			return;
		}
		
		// deserialize device from data
		BluetoothDevice device = null;
		
		try {
			device = new BluetoothDevice(originalDeviceSerialization);
		}
		catch (Exception e) {
			// can't deserialize - invalid data
			finish();
			return;
		}

		etAddress = (EditText)findViewById(R.id.etDeviceAddress);
		etName = (EditText)findViewById(R.id.etDeviceName);
		etPSK = (EditText)findViewById(R.id.etPSK);
		etPIN = (EditText)findViewById(R.id.etDevicePIN);
		cbSecure = (CheckBox)findViewById(R.id.cbSecure);
		
		etAddress.setText(device.getBluetoothAddress());
		etName.setText(device.getName());
		etPSK.setText(device.getEncryptionKey());
		etPIN.setText(device.getPIN());
		cbSecure.setChecked(device.isSecure());
        
        // register button click handlers
        Button btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				// validate
				String address = etAddress.getText().toString().trim().toUpperCase(Locale.getDefault());
				// check bluetooth address
				if (!BluetoothAdapter.checkBluetoothAddress(address)) {
					// invalid address
					Toast.makeText(EditBluetoothDevice.this, R.string.bluetooth_invalid_address, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// create the device
				BluetoothDevice device = new BluetoothDevice(mBluetoothAdapter, etName.getText().toString(), address, etPSK.getText().toString().trim(), etPIN.getText().toString(), cbSecure.isChecked());
	            // Set result and finish this Activity
        		Intent result = new Intent();
        		result.putExtra(AndroPDI.DEVICE_SERIALIZATION, device.serialize());
        		result.putExtra(AndroPDI.DEVICE_PREVIOUS_SERIALIZATION, originalDeviceSerialization);
	            setResult(AndroPDI.EDIT_BLUETOOTH_DEVICE, result);
	            
	            finish();
			}
		});
        
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {	
        	@Override
        	public void onClick(View v) {
        		setResult(Activity.RESULT_CANCELED);
        		finish();
        	}
        });
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        }
    }
}
