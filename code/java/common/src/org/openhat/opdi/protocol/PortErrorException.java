//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.protocol;

@SuppressWarnings("serial")
public class PortErrorException extends Exception {

	protected String portID = "";	// avoid null
	
	public PortErrorException() {
		// TODO Auto-generated constructor stub
	}

	public PortErrorException(String portID, String message) {
		super(message);
		this.portID = portID;
	}

	public String getPortID() {
		return portID;
	}
}
