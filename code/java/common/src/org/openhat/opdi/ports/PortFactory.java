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
import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;


/** This class serves as a factory for basic port objects.
 * 
 * @author Leo
 *
 */
public class PortFactory {
	
	public static Port createPort(IBasicProtocol protocol, String[] parts) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException {
		// the "magic" in the first part decides about the port type		
		if (parts[0].equals(DigitalPort.MAGIC))
			return new DigitalPort(protocol, parts);
		else if (parts[0].equals(AnalogPort.MAGIC))
			return new AnalogPort(protocol, parts);
		else if (parts[0].equals(SelectPort.MAGIC))
			return new SelectPort(protocol, parts);
		else if (parts[0].equals(DialPort.MAGIC))
			return new DialPort(protocol, parts);
		else if (parts[0].equals(CustomPort.MAGIC))
			return new CustomPort(protocol, parts);
		else if (parts[0].equals(StreamingPort.MAGIC))
			return new StreamingPort(protocol, parts);
		else
			throw new IllegalArgumentException("Programmer error: Unknown port magic: '" + parts[0] + "'. Protocol must check supported ports");
	}
}
