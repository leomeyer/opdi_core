//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.ports;

import org.openhat.androPDI.ports.ColoredLampPort;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.protocol.BasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.PortErrorException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.utils.Strings;

import java.util.concurrent.TimeoutException;

/** This class represents an analog dial for things like volume controls.
 * It may be represented by knobs or sliders.
 * A dial has a minimum, a maximum and a step width (increment).
 * It is always an output only port.
 * 
 * @author Leo
 *
 */
public class CustomPort extends Port {

	static final String MAGIC = "CP";

	protected String value;
	protected boolean valueUnknown = true;

	protected CustomPort(Port other) {
		super(other);
	}

	protected CustomPort(String id, String name) {
		super(null, id, name, PortType.CUSTOM, PortDirCaps.BIDIRECTIONAL);
	}

	/** Constructor for deserializing from wire form
	 *
	 * @param protocol
	 * @param parts
	 * @throws ProtocolException
	 */
	public CustomPort(IBasicProtocol protocol, String[] parts) throws ProtocolException {
		super(protocol, null, null, PortType.CUSTOM, null);
		
		final int ID_PART = 1;
		final int NAME_PART = 2;
		final int FLAGS_PART = 3;
		final int PART_COUNT = 4;
		
		checkSerialForm(parts, PART_COUNT, MAGIC);

		setID(parts[ID_PART]);
		setName(parts[NAME_PART]);
		flags = Strings.parseInt(parts[FLAGS_PART], "flags", 0, Integer.MAX_VALUE);
	}
	
	public String serialize() {
		return Strings.join(':', MAGIC, getID(), getName());
	}

	@Override
	protected BasicProtocol getProtocol() {
		// return protocol as extended type
		return (BasicProtocol)super.getProtocol();
	}
	
	@Override
	public void refresh() {
		super.refresh();
		valueUnknown = true;
	}

	public void setPortValue(IBasicProtocol protocol, String value) {
		if (protocol != getProtocol())
			throw new IllegalAccessError("Setting the port state is only allowed from its protocol implementation");
		clearError();
		this.value = value;
		this.valueUnknown = false;
	}
	
	// Retrieve the position from the device
	public String getValue() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
		if (valueUnknown)
			getPortState();
		return value;
	}
		
	public void setValue(String value) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException, PortAccessDeniedException {
		clearError();
		try {
			getProtocol().setValue(this, value);
		} catch (PortErrorException e) {
			handlePortError(e);
		}
	}
	
	@Override
	public void getPortState() throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
		clearError();
		// Request port state
		try {
			getProtocol().getValue(this);
		} catch (PortErrorException e) {
			handlePortError(e);
		}
	}

	@Override	
	public boolean isReadonly() {
		return (flags & PORTFLAG_READONLY) == PORTFLAG_READONLY;
	}

	@Override
	public void transferPropertiesFrom(Port other) {
		if (!(other instanceof CustomPort))
			throw new IllegalArgumentException("Can only transfer properties from another CustomPort, other was: " + other.getClass().toString());
		super.transferPropertiesFrom(other);
		this.value = ((CustomPort)other).value;
	}
}
