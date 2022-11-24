//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openhat.opdi.interfaces.IDevice;
import org.openhat.opdi.interfaces.IDevice.DeviceStatus;

import java.util.List;

/** Adapts a list of devices for display in a ListView.
	 * 
	 * @author Leo
	 *
	 */
	class DeviceListAdapter extends ArrayAdapter<AndroPDIDevice> {

		private Context context;
	    private List<AndroPDIDevice> items;

	    public DeviceListAdapter(Context context, int textViewResourceId, List<AndroPDIDevice> items) {
	        super(context, textViewResourceId, items);
	        this.context = context;
	        this.items = items;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.device_row, null);
            }
            v.setBackgroundColor(Color.TRANSPARENT);
            IDevice device = items.get(position);
            
            // The null device is treated specially. It serves as a menu item for grouped device functions
            // such as deleting all, adding another device etc.
            
            if (device == null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                View lock = v.findViewById(R.id.image_lock);
                if (lock != null)
                	lock.setVisibility(View.GONE);
                if (tt != null) {
                	tt.setText(R.string.manage_devices_);
                }
                if (bt != null){
                	bt.setText(R.string.click_here_to_add_a_new_device_);
//	                	bt.setTextColor(Color.WHITE);
                }
                ImageView icon = (ImageView)v.findViewById(R.id.icon);
                icon.setImageDrawable(null);
            } else {
            	// "normal" device entry. Display in the list
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                View lock = v.findViewById(R.id.image_lock);
            	
            	if (device.isConnected()) {
            		// connected devices get the name on top, their label and address below
                    if (tt != null) {
                    	tt.setText(device.getDeviceName());
                    	tt.setTextColor(Color.BLUE);
                    }
                    if (bt != null){
                    	bt.setText(device.getDisplayAddress());
                    }
                    if (lock != null)
                    	lock.setVisibility((device.usesEncryption() ? View.VISIBLE : View.GONE));
            	} else {
            		// disconnected devices have their label on top and the status below
	                if (tt != null) {
	                	tt.setText(device.getLabel());
	                	// error? device label in red
	                	if (device.getStatus() == DeviceStatus.ERROR)
	                		tt.setTextColor(Color.RED);
	                	else
	                		tt.setTextColor(Color.BLACK);
	                }
	                if (bt != null){
	                	bt.setText(device.getStatusText());
	                }
	                if (lock != null)
	                	lock.setVisibility(View.GONE);
            	}
                ImageView icon = (ImageView)v.findViewById(R.id.icon);
                Drawable drawable = context.getResources().getDrawable(device.getImageResource());
                icon.setImageDrawable(drawable);
            }
            return v;
	    }
	}