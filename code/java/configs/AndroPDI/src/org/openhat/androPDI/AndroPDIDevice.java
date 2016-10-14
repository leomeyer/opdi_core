//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI;

import java.lang.ref.WeakReference;

import org.openhat.androPDI.DeviceManager.IDeviceStatusListener;
import org.openhat.devices.DeviceInfo;
import org.openhat.devices.IODevice;

public abstract class AndroPDIDevice extends IODevice {

	protected static int idCounter = 1; 
	protected String id;		// temporary identifier

	// initially empty device info object (filled if the device supports the Extended Protocol)
	DeviceInfo deviceInfo = new DeviceInfo("");
	
	// a device can have only one active listener
	// which is set when the device is being connected
	WeakReference<IDeviceStatusListener> deviceListener;
	
	public AndroPDIDevice() {
		
		// create temporary ID
		id = "dev" + (idCounter++);
	}

	public String getId() {
		return id;
	}
	
	public IDeviceStatusListener getListener() {
		if (deviceListener == null)
			return null;
		
		if (deviceListener.get() == null)
			deviceListener = null;
		
		return deviceListener.get();
	}

	public void setListener(IDeviceStatusListener listener) {
		deviceListener = new WeakReference<IDeviceStatusListener>(listener);
	}
	
	/** Is called before connect() to give the device the chance to prepare IO, e. g. enable Bluetooth.
	 * Returns true if the connect can proceed.
	 */
	public abstract boolean prepare();

	@Override
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
	
	@Override
	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
}
