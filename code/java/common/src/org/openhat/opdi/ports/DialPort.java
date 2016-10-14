//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.ports;

import java.util.concurrent.TimeoutException;

import org.openhat.devices.DeviceException;
import org.openhat.interfaces.IBasicProtocol;
import org.openhat.protocol.BasicProtocol;
import org.openhat.protocol.DisconnectedException;
import org.openhat.protocol.PortAccessDeniedException;
import org.openhat.protocol.PortErrorException;
import org.openhat.protocol.ProtocolException;
import org.openhat.utils.Strings;

/** This class represents an analog dial for things like volume controls.
 * It may be represented by knobs or sliders.
 * A dial has a minimum, a maximum and a step width (increment).
 * It is always an output only port.
 * 
 * @author Leo
 *
 */
public class DialPort extends Port {

	static final String MAGIC = "DL";
	
	protected boolean posUnknown = true;
	protected long position = 0;
	protected long minimum;
	protected long maximum;
	protected long step;
	
	protected DialPort(String id, String name, int minimum, int maximum, int step) {
		super(null, id, name, PortType.OTHER, PortDirCaps.OUTPUT);
		this.minimum = minimum;
		this.maximum = maximum;
		this.step = step;
	}
	
	/** Constructor for deserializing from wire form
	 * 
	 * @param protocol
	 * @param parts
	 * @throws ProtocolException
	 */
	public DialPort(IBasicProtocol protocol, String[] parts) throws ProtocolException {
		super(protocol, null, null, PortType.OTHER, null);
		
		final int ID_PART = 1;
		final int NAME_PART = 2;
		final int MIN_PART = 3;
		final int MAX_PART = 4;
		final int STEP_PART = 5;
		final int FLAGS_PART = 6;
		final int PART_COUNT = 7;
		
		checkSerialForm(parts, PART_COUNT, MAGIC);

		setID(parts[ID_PART]);
		setName(parts[NAME_PART]);
		setMinimum(Strings.parseLong(parts[MIN_PART], "Minimum", Long.MIN_VALUE, Long.MAX_VALUE));
		setMaximum(Strings.parseLong(parts[MAX_PART], "Maximum", Long.MIN_VALUE, Long.MAX_VALUE));
		setStep(Strings.parseLong(parts[STEP_PART], "Step", Long.MIN_VALUE, Long.MAX_VALUE));
		flags = Strings.parseInt(parts[FLAGS_PART], "flags", 0, Integer.MAX_VALUE);
	}
	
	public String serialize() {
		return Strings.join(':', MAGIC, getID(), getName(), minimum, maximum, step);
	}
	
	public long getMinimum() {
		return minimum;
	}
	
	public long getMaximum() {
		return maximum;
	}

	public long getStep() {
		return step;
	}
	
	protected void setMinimum(long minimum) {
		this.minimum = minimum;
	}

	protected void setMaximum(long maximum) {
		this.maximum = maximum;
	}

	protected void setStep(long step) {
		this.step = step;
	}

	@Override
	protected BasicProtocol getProtocol() {
		// return protocol as extended type
		return (BasicProtocol)super.getProtocol();
	}
	
	@Override
	public void refresh() {
		super.refresh();
		posUnknown = true;
	}

	public void setPortPosition(IBasicProtocol protocol, long position) {
		if (protocol != getProtocol())
			throw new IllegalAccessError("Setting the port state is only allowed from its protocol implementation");
		clearError();
		this.position = position;
		this.posUnknown = false;
	}
	
	// Retrieve the position from the device
	public long getPosition() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
		if (posUnknown)
			getPortState();
		return position;
	}
		
	public void setPosition(long pos) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException, PortAccessDeniedException {
		// limit value range
		long val = pos;
		if (val < minimum) val = minimum;
		// adjust to nearest step
		long modulo = val % step;
		// round up or down, depending on what's nearest
		val = (modulo <= step / 2 ? val - modulo : val + step - modulo);
		if (val > maximum) val = maximum;		
		clearError();
		try {
			getProtocol().setPosition(this, val);
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
