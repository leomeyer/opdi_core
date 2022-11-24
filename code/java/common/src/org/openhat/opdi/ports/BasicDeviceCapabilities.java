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
import org.openhat.opdi.interfaces.IDeviceCapabilities;
import org.openhat.opdi.protocol.BasicProtocol;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.utils.Strings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;


/** This class describes the capabilities of an OPDI device. It contains convenience methods to access ports.
 * 
 * @author Leo
 *
 */
public class BasicDeviceCapabilities implements IDeviceCapabilities {
	
	private static final String MAGIC = "BDC";

	protected BasicProtocol protocol;
	protected List<Port> ports = new ArrayList<Port>();
	
	public static IDeviceCapabilities makeInstance(String name, String address) {
		return new BasicDeviceCapabilities();
	}
	
	private BasicDeviceCapabilities() {}

	public BasicDeviceCapabilities(BasicProtocol protocol, int channel, String serialForm) throws ProtocolException, TimeoutException, InterruptedException, DisconnectedException, DeviceException {
		
		final int PORTS_PART = 1;
		final int PART_COUNT = 2;
		
		// decode the serial representation
		String[] parts = Strings.split(serialForm, ':');
		if (parts.length != PART_COUNT) 
			throw new ProtocolException("BasicDeviceCapabilities message invalid");
		if (!parts[0].equals(MAGIC))
			throw new ProtocolException("BasicDeviceCapabilities message invalid: incorrect magic: " + parts[0]);
		this.protocol = protocol;
		// split port IDs by comma
		String[] portIDs = Strings.split(parts[PORTS_PART], ',');
		// get port info for each port and add ports
		addPorts(protocol, channel, portIDs);
	}

	protected void addPorts(BasicProtocol protocol, int channel,
			String[] portIDs) throws TimeoutException, InterruptedException,
			DisconnectedException, DeviceException, ProtocolException {
		// slow basic method: query port info one by one
		for (String portID: portIDs) {
			if (!portID.isEmpty())
				ports.add(protocol.getPortInfo(portID, channel));
		}
	}
	
	public String toString() {
		String result = "BasicDeviceCapabilities\n";
		if (ports.size() == 0)
			result += "  No ports\n";
		else
			for (Port port: ports) {
				result += "  " + port.toString() + "\n";
			}
		return result;
	}

	@Override
	public Port findPortByID(String portID) {
		for (Port port: ports) {
			if (port.getID().equals(portID)) return port;
		}
		// not found
		return null;
	}

	@Override
	public List<Port> getPorts() {
		return ports;
	}

	@Override
	public List<Port> getPorts(String groupID) {
		// filter ports by parent group ID
		List<Port> result = new ArrayList<Port>();
		for (Port port: ports) {
			if (port.isInGroup(groupID))
				result.add(port);
		}
		return result;
	}

	@Override
	public List<PortGroup> getPortGroups(String parentGroupID) {
		// TODO parentGroupID
		
		Set<PortGroup> result = new HashSet<PortGroup>();
		
		for (Port port: ports) { 
			if (port.getGroup() != null) {
				result.add(port.getGroup());
				// add all parent groups way up the hierarchy
				PortGroup parent = port.getGroup().getParentGroup();
				while (parent != null) {
					result.add(parent);
					parent = parent.getParentGroup();
				}
			}
		}
		
		return new ArrayList<PortGroup>(result);
	}
	
	@Override
	public void getPortStates() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException {
        for (Port port: getPorts()) {
			port.getPortState();
        }
	}
}
