//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.protocol;

@SuppressWarnings("serial")
public class MessageException extends Exception {

	public MessageException() {
		super();
	}

	public MessageException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public MessageException(String detailMessage) {
		super(detailMessage);
	}

	public MessageException(Throwable throwable) {
		super(throwable);
	}
	
}
