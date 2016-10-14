//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.interfaces;

import java.util.concurrent.TimeoutException;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.Message;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.PortErrorException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.protocol.AbstractProtocol.IAbortable;

public interface IProtocol {

	enum ExpectationMode {
		NORMAL,
		IGNORE_REFRESHES
	};
	
	/** Returns the port with the given ID. null if the port is not there.
	 * 
	 * @param portID
	 * @return
	 * @throws DisconnectedException 
	 * @throws InterruptedException 
	 * @throws DeviceException 
	 * @throws ProtocolException 
	 * @throws TimeoutException 
	 * @throws PortErrorException 
	 * @throws PortAccessDeniedException 
	 */
	public Port findPortByID(String portID) throws TimeoutException, ProtocolException, DeviceException, InterruptedException, DisconnectedException, PortAccessDeniedException, PortErrorException;
	
	/** Returns the information about the port with the given ID.
	 * Requires the channel from the initiating protocol.
	 * 
	 * @return
	 * @throws PortErrorException 
	 * @throws PortAccessDeniedException 
	 */
	public Port getPortInfo(String id, int channel) throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException;
	

	public Message expect(long channel, int timeout, IAbortable abortable, ExpectationMode mode) 
			throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, PortAccessDeniedException, PortErrorException;

	public IDeviceCapabilities getDeviceCapabilities() throws TimeoutException, ProtocolException, DeviceException, InterruptedException, DisconnectedException;

	public boolean dispatch(Message msg);

	public void initiate();

	public void disconnect();

	public String getMagic();
}
