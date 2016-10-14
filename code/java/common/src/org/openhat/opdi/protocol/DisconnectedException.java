//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.protocol;

/** This exception is thrown when a connected device disconnects via the protocol
 * while a message is expected.
 * 
 * @author Leo
 *
 */
@SuppressWarnings("serial")
public class DisconnectedException extends Exception {

	public DisconnectedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DisconnectedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public DisconnectedException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public DisconnectedException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
