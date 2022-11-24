//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.interfaces;

import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.ports.PortGroup;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface IDeviceCapabilities {

	public abstract List<PortGroup> getPortGroups(String parentGroupID);

	public abstract List<Port> getPorts(String groupID);

	public abstract List<Port> getPorts();

	public abstract Port findPortByID(String portID);

	public abstract void getPortStates() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException, PortAccessDeniedException;

}
