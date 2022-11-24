//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.ports;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.protocol.BasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.ExtendedProtocol;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;

/** DeviceCapabilities for a device that supports the Extended Protocol.
 * 
 * @author leo.meyer
 *
 */
public class ExtendedDeviceCapabilities extends BasicDeviceCapabilities {

	private ExtendedProtocol protocol;

	public ExtendedDeviceCapabilities(ExtendedProtocol protocol, int channel, String serialForm)
			throws ProtocolException, TimeoutException, InterruptedException,
			DisconnectedException, DeviceException {
		
		super((BasicProtocol)protocol, channel, serialForm);
		this.protocol = protocol;
	}
	
	@Override
	protected void addPorts(BasicProtocol protocol, int channel,
			String[] portIDs) throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException {
		
		this.addPorts((ExtendedProtocol)protocol, channel, portIDs);
	}

	protected void addPorts(ExtendedProtocol protocol, int channel,
			String[] portIDs) throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException {

		this.ports.addAll(protocol.getAllPortInfos(channel, portIDs));
	}
	
	@Override
	public void getPortStates() throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException,
			PortAccessDeniedException {
		
		protocol.getAllPortStates(this.ports);
	}
}
