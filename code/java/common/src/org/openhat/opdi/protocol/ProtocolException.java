//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.protocol;

/** This exception can be thrown in case of a protocol error.
 * 
 * @author Leo
 *
 */
@SuppressWarnings("serial")
public class ProtocolException extends Exception {

	public ProtocolException() {
		super();
	}

	public ProtocolException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ProtocolException(String detailMessage) {
		super(detailMessage);
	}

	public ProtocolException(Throwable throwable) {
		super(throwable);
	}
}
