//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.devices;

/** This class is used to indicate an error on the device while executing a protocol request or otherwise.
 * 
 * @author Leo
 *
 */
@SuppressWarnings("serial")
public class DeviceException extends Exception {

	public DeviceException() {
		// TODO Auto-generated constructor stub
	}

	public DeviceException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public DeviceException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public DeviceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
