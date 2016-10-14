//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.drivers;

import java.util.Date;

import org.openhat.interfaces.IDriver;

/** Implements generic functions for drivers.
 * 
 * @author Leo
 *
 */
public abstract class AbstractDriver implements IDriver {

	private long receiveTime;
	private boolean valid;
	
	/** Notifies that data has been received and whether it is valid or not.
	 * 
	 * @param valid
	 */
	public void dataReceived(boolean valid) {
		// remember receive time
		receiveTime = new Date().getTime();
		this.valid = valid;
	}
	
	public long getDataAge() {
		return new Date().getTime() - receiveTime;
	}


	@Override
	public boolean hasValidData() {
		return valid;
	}

	
}
