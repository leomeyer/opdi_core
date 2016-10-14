//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.model;

/** A Device describes a remote device.
 * 
 * @author Leo
 *
 */
public class Device {
	
	private String clazz;
	private String info;
	
	protected Device() {}

	public Device(String clazz, String info) {
		this.clazz = clazz;
		this.info = info;
	}

	/** The class that is used to represent this device. */
	public String getClazz() {
		return clazz;
	}

	/** The serialized information required to restore the device object. */
	public String getInfo() {
		return info;
	}
}
