//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.model;

/** A Function describes one device function (actor or sensor).
 *
 * @author Leo
 *
 */
public class Function {

	/** The ID of the function that is used to identify it on the device.
	 * 
	 */
	public String Id;
	
	/** The class that implements 
	 * 
	 */
	public String Class;
}
