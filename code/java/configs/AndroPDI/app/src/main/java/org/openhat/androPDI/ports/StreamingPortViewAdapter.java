//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.openhat.androPDI.R;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.drivers.Text_Driver;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.ports.StreamingPort;
import org.openhat.opdi.ports.StreamingPort.IStreamingPortListener;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;

/** A port view adapter for select ports.
 * 
 * @author Leo
 *
 */
class StreamingPortViewAdapter implements IPortViewAdapter, IStreamingPortListener {
	
	protected final ShowDevicePorts showDevicePorts;
	
	Context context;
	
	protected TextView tvToptext;
	protected TextView tvBottomtext;
	protected ImageView ivPortIcon;
	
	protected StreamingPort sPort;

	// view state values
	protected boolean stateError;
	protected int position = -1;
	
	protected StreamingPortViewAdapter(ShowDevicePorts showDevicePorts) {
		super();
		this.showDevicePorts = showDevicePorts;
	}

	public View getView(View convertView) {
		View cachedView;
		if (convertView == null) {
			
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// get layout property from UnitFormat definition
			String layoutName = sPort.getUnitFormat().getProperty("layout", "default_port_row");
			// get layout identifier
			int layoutID = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
			// inflate the identified layout
			cachedView = vi.inflate(layoutID, null);
		} else
			cachedView = convertView;
		
        tvToptext = (TextView) cachedView.findViewById(R.id.toptext);
        tvBottomtext = (TextView) cachedView.findViewById(R.id.bottomtext);
        ivPortIcon = (ImageView) cachedView.findViewById(R.id.icon);

    	tvToptext.setText(sPort.getName());

    	tvBottomtext.setText("");

    	// autobind?
    	if (sPort.isAutobind() && !sPort.isBound()) {
    		// try to autobind
			showDevicePorts.addPortAction(new PortAction(StreamingPortViewAdapter.this) {
				@Override
				void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
					try {
						sPort.bind();
					} catch (PortAccessDeniedException e) {
					}
			        stateError = sPort.hasError();
			    	// listen for port events
			    	sPort.setStreamingPortListener(StreamingPortViewAdapter.this);
				}
			});
    	}
    	
		return cachedView;
	}

	public void openMenu() {
		this.showDevicePorts.portAndAdapter = this.showDevicePorts.new PortAndAdapter(sPort, this);
		this.showDevicePorts.mHandler.post(new Runnable() {
			@Override
			public void run() {
			    StreamingPortViewAdapter.this.showDevicePorts.registerForContextMenu(StreamingPortViewAdapter.this.showDevicePorts.ports_listview);
			    StreamingPortViewAdapter.this.showDevicePorts.openContextMenu(StreamingPortViewAdapter.this.showDevicePorts.ports_listview);
			    StreamingPortViewAdapter.this.showDevicePorts.unregisterForContextMenu(StreamingPortViewAdapter.this.showDevicePorts.ports_listview);
			}
		});
	}
	
	@Override
	public void handleClick() {
		openMenu();
	}
	
	protected void queryState() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
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
		this.sPort = (StreamingPort)port;
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
				sPort.refresh();
				queryState();
			}
		});
	}
	
	// must be called on the UI thread!
	public void updateState() {
		if (stateError) {
			// indicate an error
			if (ivPortIcon != null) {
				ivPortIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.default_port_error));
				ivPortIcon.setOnClickListener(null);
			}
			return;
		}
		
		// set the proper icon
		Drawable portIcon = context.getResources().getDrawable(R.drawable.default_port);
		ivPortIcon.setImageDrawable(portIcon);
		// context menu when clicking
		ivPortIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClick();
			}
		});
	}		
	
	@Override
	public void createContextMenu(ContextMenu menu,
			ContextMenuInfo menuInfo) {
		// MenuInflater inflater = this.showDevicePorts.getMenuInflater();
		
		try {
			// inflate the context menu 
//			inflater.inflate(R.menu.streaming_port_menu, menu);

			// add menu items dynamically
			//MenuItem item;
			
		} catch (Exception e) {
			this.showDevicePorts.showError("Can't show the context menu");
			menu.close();
		}
	}
	
	@Override
	public void dataReceived(StreamingPort port, final String data) {
		if (sPort == null)
			return;
		// default behavior for text drivers
		if (Text_Driver.MAGIC.equals(sPort.getDriverID())) {
			// default behavior: display data on UI thread
			this.showDevicePorts.mHandler.post(new Runnable() {
				@Override
				public void run() {
					tvBottomtext.setText(data);
				}
			});
		}
	}
	
	@Override
	public void portUnbound(StreamingPort port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMessage(String message) {
		showDevicePorts.receivedPortMessage(this.sPort, message);
	}

}