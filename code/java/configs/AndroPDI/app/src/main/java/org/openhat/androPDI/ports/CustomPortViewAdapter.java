//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.openhat.androPDI.R;
import org.openhat.androPDI.ports.editors.DialPortDateTimeEditor;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.ports.DialPort;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.units.DisplayHint;

import java.util.concurrent.TimeoutException;

/** A port view adapter for dial ports.
 * 
 * @author Leo
 *
 */
class CustomPortViewAdapter implements IPortViewAdapter {

	protected final ShowDevicePorts showDevicePorts;

	Context context;

	private TextView tvToptext;
	private TextView tvBottomtext;
	private ImageView ivPortIcon;

	CustomPort cPort;

	// cache values
	boolean valueValid;
	boolean stateError;
	String value;

	protected CustomPortViewAdapter(ShowDevicePorts showDevicePorts) {
		super();
		this.showDevicePorts = showDevicePorts;
	}

	public View getView(View convertView) {
		View cachedView;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			cachedView = vi.inflate(R.layout.default_port_row, null);
		} else
			cachedView = convertView;
				
        tvToptext = (TextView) cachedView.findViewById(R.id.toptext);
        tvBottomtext = (TextView) cachedView.findViewById(R.id.bottomtext);
        ivPortIcon = (ImageView) cachedView.findViewById(R.id.port_icon);

        if (tvToptext != null) tvToptext.setText(cPort.getName());
        if (tvBottomtext != null) tvBottomtext.setText("");
        
        showDevicePorts.addPortAction(new PortAction(CustomPortViewAdapter.this) {
			@Override
			void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
				queryState();
				showDevicePorts.mHandler.post(new Runnable() {
					@Override
					public void run() {
						updateState();
					}
				});
			}
		});

        return cachedView;
	}
	
	public void openMenu() {
		this.showDevicePorts.portAndAdapter = this.showDevicePorts.new PortAndAdapter(cPort, this);
		this.showDevicePorts.mHandler.post(new Runnable() {
			@Override
			public void run() {
/*
			    CustomPortViewAdapter.this.showDevicePorts.registerForContextMenu(CustomPortViewAdapter.this.showDevicePorts.ports_listview);
			    CustomPortViewAdapter.this.showDevicePorts.openContextMenu(CustomPortViewAdapter.this.showDevicePorts.ports_listview);
			    CustomPortViewAdapter.this.showDevicePorts.unregisterForContextMenu(CustomPortViewAdapter.this.showDevicePorts.ports_listview);
*/
			}
		});
	}
	
	@Override
	public void handleClick() {
		openMenu();
	}

	protected void queryState() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
		valueValid = false;
		stateError = cPort.hasError();
		if (stateError)
			return;

		try {
			value = cPort.getValue();
			valueValid = true;
		} catch (PortAccessDeniedException e) {
		}
        stateError = cPort.hasError();
//        inaccurate = cPort.getExtendedState("inaccurate", "false").equals("true");
    }
	
	@Override
	public void startPerformAction() {
		// reset error state
		stateError = false;
	}
	
	@Override
	public void setError(Throwable t) {
		stateError = true;
	}

	@Override
	public void configure(Port port, Context context) {
		this.cPort = (CustomPort)port;
		this.context = context;
	}
	
	@Override
	public void refresh() {
		showDevicePorts.addPortAction(new PortAction(this) {
			@Override
			void perform()
					throws TimeoutException,
					InterruptedException,
					DisconnectedException,
					DeviceException,
					ProtocolException {
				// reload the port state
				cPort.refresh();
				queryState();
			}
		});
	}

	// must be called on the UI thread!
	public void updateState() {
		if (stateError) {
			// indicate an error
	        Drawable drawable = context.getResources().getDrawable(R.drawable.default_port_error);
			if (ivPortIcon != null) ivPortIcon.setImageDrawable(drawable);
			if (ivPortIcon != null) ivPortIcon.setOnClickListener(null);		// no context menu

			if (tvBottomtext != null) tvBottomtext.setText(cPort.getErrorMessage());
			return;
		}
		
		// set the proper icon
		if (ivPortIcon != null) {
			// default icon
			Drawable drawable = context.getResources().getDrawable(R.drawable.default_port);
			String iconName = cPort.getUnitFormat().getProperty("icon", cPort.getExtendedInfo("icon", ""));
			if (!iconName.equals("")) {
				// get icon identifier
				int iconID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
				if (iconID == 0)
					throw new IllegalArgumentException("Drawable resource not found: " + iconName);							
					
				drawable = context.getResources().getDrawable(iconID);
			}
			
			ivPortIcon.setImageDrawable(drawable);

			// context menu when clicking
			ivPortIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					handleClick();
				}
			});
		}
		
			
		if (tvBottomtext != null)
			tvBottomtext.setText(value);
	}
	
	@Override
	public void createContextMenu(ContextMenu menu,
			ContextMenuInfo menuInfo) {
	}

	@Override
	public void showMessage(String message) {
		showDevicePorts.receivedPortMessage(this.cPort, message);
	}

}