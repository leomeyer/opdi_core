//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.interfaces;

import org.openhat.opdi.ports.StreamingPort;

/** Specifies the functions of a device driver based on a streaming port.
 * 
 * @author Leo
 *
 */
public interface IDriver {

	/** Attaches this driver to the specified port.
	 * May perform initialization functions.
	 * @param port
	 */
	public void attach(StreamingPort port);
	
	/** Returns true if the data is valid.
	 * 
	 * @return
	 */
	public boolean hasValidData();
	
	/** Returns the age of the data in milliseconds, i. e. the difference between
	 * the last receive time and now.
	 * @return
	 */
	public long getDataAge();
}
