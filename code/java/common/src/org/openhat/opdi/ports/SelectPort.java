//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.ports;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.PortErrorException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.utils.Strings;


/** Represents a select port on a device.
 * 
 * @author Leo
 *
 */
public class SelectPort extends Port {
	
	static final String MAGIC = "SLP";

	int posCount;
	int position = -1;
	List<String> labels = new ArrayList<String>();
	
	protected SelectPort(String id, String name, PortDirCaps portDirection, int flags) {
		super(null, id, name, PortType.SELECT, portDirection);
	}
	
	/** Constructor for deserializing from wire form
	 * 
	 * @param protocol
	 * @param parts
	 * @throws ProtocolException
	 * @throws DeviceException 
	 * @throws DisconnectedException 
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws PortAccessDeniedException 
	 * @throws PortErrorException 
	 */
	public SelectPort(IBasicProtocol protocol, String[] parts) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException {
		super(protocol, null, null, PortType.SELECT, PortDirCaps.OUTPUT);
		
		final int ID_PART = 1;
		final int NAME_PART = 2;
		final int POS_PART = 3;
		final int FLAGS_PART = 4;
		final int PART_COUNT = 5;
		
		checkSerialForm(parts, PART_COUNT, MAGIC);

		setID(parts[ID_PART]);
		setName(parts[NAME_PART]);
		posCount = Strings.parseInt(parts[POS_PART], "position count", 0, Integer.MAX_VALUE);
		flags = Strings.parseInt(parts[FLAGS_PART], "flags", 0, Integer.MAX_VALUE);
		
		labels = protocol.getSelectPortLabels(this);
	}
	
	public String serialize() {
		return Strings.join(':', MAGIC, getID(), getName(), posCount);
	}

	public int getPosCount() {
		return posCount;
	}

	public String getLabelAt(int pos) {
		if ((pos < 0) || (pos >= labels.size()))
			throw new IllegalArgumentException("The given position is not valid or the label has not yet been added");
		return labels.get(pos);
	}
	
	public void setPortPosition(IBasicProtocol protocol, int position) {
		if (protocol != getProtocol())
			throw new IllegalAccessError("Setting the port state is only allowed from its protocol implementation");
		clearError();
		this.position = position;
	}
	
	// Retrieve the position from the device
	public int getPosition() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
		if (position < 0)
			getPortState();
		return position;
	}
	
	public boolean setPosition(int pos) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException, PortAccessDeniedException {
		clearError();
		try {
			getProtocol().setPosition(this, pos);
			return true;
		} catch (PortErrorException e) {
			handlePortError(e);
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "SelectPort id=" + getID() + "; name='" + getName() + "'; type=" + getType() + "; dir_caps=" + getDirCaps() + "; posCount=" + posCount;
	}

	@Override
	public void refresh() {
		position = -1;
	}
	
	@Override
	public void getPortState() throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
		clearError();
		// Request port state
		try {
			getProtocol().getPosition(this);
		} catch (PortErrorException e) {
			handlePortError(e);
		}
	}

	@Override	
	public boolean isReadonly() {
		return (flags & PORTFLAG_READONLY) == PORTFLAG_READONLY;
	}
}
