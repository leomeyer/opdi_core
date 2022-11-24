//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.devices;

import org.openhat.opdi.utils.Strings;

import java.util.HashMap;

public class DeviceInfo {
	
	String deviceInfo;
	HashMap<String, String> properties = new HashMap<String, String>();
	
	public DeviceInfo(String info) {
		this.deviceInfo = info;
		
		this.properties = Strings.getProperties(this.deviceInfo);
	}

	public String getStartGroup() {
		if (!properties.containsKey("startGroup"))
			return null;

		return properties.get("startGroup");
	}

}
