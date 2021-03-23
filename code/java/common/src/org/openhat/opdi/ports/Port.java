//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.ports;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.PortErrorException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.units.UnitFormat;
import org.openhat.opdi.utils.Strings;


/** Defines the abstract properties and functions of a port.
 * 
 * @author Leo
 *
 */
public abstract class Port {
	
	/** Defines different types of ports. */
	public enum PortType {
		/** A digital port with two states (low and high). */
		DIGITAL,
		/** An analog port. */
		ANALOG,
		/** A select port. */
		SELECT,
		/** A USART. */
		STREAMING,
		/** Another port type. */
		OTHER
	}	
	
	/** Defines the possible directions (capabilities) of ports. */
	public enum PortDirCaps {
		/** A port that can only be used for input. */
		INPUT,		
		/** A port that can only be used for output. */
		OUTPUT,		
		/** A port that can be configured for input or output. */
		BIDIRECTIONAL
	}
	
	public static final int PORTFLAG_READONLY = 0x4000;

	protected IBasicProtocol protocol;
	protected String id;
	protected String name;
	protected PortType type;
	protected PortDirCaps dirCaps;
	protected int flags;
	protected Object viewAdapter;
	protected boolean hasError;
	protected String errorMessage;
	
	// extended properties
	protected String unit;
	protected UnitFormat unitFormat = UnitFormat.DEFAULT;
	protected String colorScheme;
	
	protected PortGroup group;
	
	protected Map<String, String> extendedInfoProperties = new HashMap<String, String>(); 
	protected Map<String, String> extendedStateProperties = new HashMap<String, String>();

	protected boolean refreshing; 

	/** Only to be used by subclasses
	 * 
	 * @param protocol
	 * @param id
	 * @param name
	 * @param type
	 */
	protected Port(IBasicProtocol protocol, String id, String name, PortType type, PortDirCaps dirCaps) {
		super();
		this.protocol = protocol;
		this.id = id;
		this.name = name;
		this.type = type;
		this.dirCaps = dirCaps;
	}	

	public synchronized boolean hasError() {
		return hasError;
	}

	public synchronized String getErrorMessage() {
		if (errorMessage == null)
			return "";
		return errorMessage;
	}
	
	/** Returns the protocol.
	 * 
	 * @return
	 */
	protected IBasicProtocol getProtocol() {
		return protocol;
	}

	/** Returns the unique ID of this port.
	 * 
	 * @return
	 */
	public String getID() {
		return id;
	}

	/** Returns the name of this port.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
		
	/** Returns the type of this port.
	 * 
	 * @return
	 */
	public PortType getType() {
		return type;
	}

	/** Returns the possible direction of this port.
	 * 
	 * @return
	 */
	public PortDirCaps getDirCaps() {
		return dirCaps;
	}

	protected void setProtocol(IBasicProtocol protocol) {
		this.protocol = protocol;
	}

	protected void setID(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setType(PortType type) {
		this.type = type;
	}

	protected void setDirCaps(PortDirCaps dir) {
		dirCaps = dir;
	}
	
	/** A convenience method for subclasses. Throws a ProtocolException if the part count doesn't match
	 * or if the first part is not equal to the magic.
	 * @param parts
	 * @param count
	 * @param magic
	 * @throws ProtocolException
	 */
	protected void checkSerialForm(String[] parts, int count, String magic) throws ProtocolException {
		if (parts.length != count) 
			throw new ProtocolException("Serial form invalid");
		if (!parts[0].equals(magic))
			throw new ProtocolException("Serial form invalid: wrong magic");		
	}

	/** Returns a serialized description of this port.
	 * 
	 * @return
	 */
	public abstract String serialize();
	
	/** Called when the port should be refreshed.
	 * Clears cached state values and sets the refreshing flag to indicate
	 * that the next state query should use the refresh channel instead of an
	 * ordinary synchronous channel.
	 */
	public void refresh() {
		clearError();
		// flag: the next state query is in response to a refresh command
		refreshing = true;
	}

	/** Returns whether the next port state query is in response to a refresh. Calling this method
	 * resets the refreshing flag. 
	 * @return
	 */
	public boolean isRefreshing() {
		boolean result = refreshing;
		refreshing = false;
		return result;
	}
	
	@Override
	public String toString() {
		return "Port id=" + getID() + "; name='" + getName() + "'; type=" + getType() + "; dir_caps=" + getDirCaps();
	}

	/** Stores an arbitrary view object.
	 * 
	 * @param viewAdapter
	 */
	public void setViewAdapter(Object viewAdapter) {
		this.viewAdapter = viewAdapter;
	}

	/** Returns a stored view object.
	 * 
	 * @return
	 */
	public Object getViewAdapter() {
		return viewAdapter;
	}

	/** Fetches the state of the port from the device.
	 * 
	 */
	public abstract void getPortState() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException;
	
	/** Should be called internally before port handling methods are called.
	 * 
	 */
	protected void clearError() {
		hasError = false;
		errorMessage = null;
	}
	
	public void handlePortError(PortErrorException pee) throws ProtocolException {
		// check port ID
		if (!pee.getPortID().equals(id))
			throw new ProtocolException("Port " + id + ": Received Port Error for port " + pee.getPortID());

		hasError = true;
		errorMessage = pee.getMessage();
	}

	/** Returns true if the port cannot be written.
	 * 
	 * @return
	 */
	public abstract boolean isReadonly();

	public String getUnit() {
		return unit;
	}

	public UnitFormat getUnitFormat() {
		return unitFormat;
	}

	public void setUnitFormat(UnitFormat unitFormat) {
		this.unitFormat = unitFormat;
	}
	
	public void setExtendedPortInfo(String info) {
		// extract detailed information from extended info
		extendedInfoProperties = Strings.getProperties(info);
		
		if (extendedInfoProperties.containsKey("unit")) {
			unit = extendedInfoProperties.get("unit");
		}
	}
	
	public void setExtendedPortState(String info) {
		// extract detailed information from extended info
		extendedStateProperties = Strings.getProperties(info);
	}
	
	public String getExtendedInfo(String property, String defaultValue) {
		if (extendedInfoProperties.containsKey(property)) {
			return extendedInfoProperties.get(property);
		}
		return defaultValue;
	}

	public String getExtendedState(String property, String defaultValue) {
		if (extendedStateProperties.containsKey(property)) {
			return extendedStateProperties.get(property);
		}
		return defaultValue;
	}

	public synchronized PortGroup getGroup() {
		return group;
	}

	public synchronized void setGroup(PortGroup group) {
		this.group = group;
	}

	public String getGroupID() {
		if (extendedInfoProperties.containsKey("group"))
			return extendedInfoProperties.get("group");
		return null;
	}

	public boolean isInGroup(String groupID) {
		// no group means all ports
		if ((groupID == null) || (groupID.equals("")))
			return true;
		if (group == null)
			return false;
		// A port is considered "in a group" if its group or one of its parent groups match the groupID
		if (groupID.equals(group.getID())) {
			return true;
		}
		return group.hasParentGroup(groupID);
	}
}
