//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;

abstract class PortAction {

	protected IPortViewAdapter adapter;
	protected Port port;
	
	public PortAction(IPortViewAdapter adapter) {
		this.adapter = adapter;
	}

	public PortAction(Port port) {
		this.port = port;
	}

	protected PortAction() {}

	/** Defines the operations to perform asynchronously.
	 * 
	 * @throws TimeoutException
	 * @throws InterruptedException
	 * @throws DisconnectedException
	 * @throws DeviceException
	 * @throws ProtocolException
	 * @throws PortAccessDeniedException 
	 */
	abstract void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException;

	/** Defines the operations to be done on the UI thread after perform() has been run.
	 * By default calls the updateState() method of the adapter. This causes a UI refresh.
	 */
	public void runOnUIThread() {
		if (adapter != null)
			adapter.updateState();
	}
	
	public String getName() {
		return toString();
	}

	public void startPerformAction() {
		if (adapter != null)
			adapter.startPerformAction();
	}

	public void setError(Throwable t) {
		if (adapter != null)
			adapter.setError(t);
	}
}