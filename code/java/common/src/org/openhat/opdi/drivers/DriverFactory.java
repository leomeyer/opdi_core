//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.drivers;

import org.openhat.interfaces.IDriver;

/** A factory that creates driver instances from driver IDs.
 * 
 * @author Leo
 *
 */
public class DriverFactory {

	/** Returns a driver instance if it could be created, otherwise null.
	 * 
	 * @param driverID
	 * @return
	 */
	public static IDriver getDriverInstance(String driverID) {
		if (driverID.equals(Text_Driver.MAGIC))
			return new Text_Driver();
		else
		if (driverID.equals(BMP085_Driver.MAGIC))
			return new BMP085_Driver();
		else
		if (driverID.equals(NMEAGen_Driver.MAGIC))
			return new NMEAGen_Driver();
		else
		if (driverID.equals(CHR6dm_Driver.MAGIC))
			return new CHR6dm_Driver();

		return null;
	}
}
