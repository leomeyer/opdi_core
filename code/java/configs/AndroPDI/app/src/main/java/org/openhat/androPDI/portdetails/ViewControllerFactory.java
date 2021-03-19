//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.portdetails;

import org.openhat.opdi.drivers.BMP085_Driver;
import org.openhat.opdi.drivers.CHR6dm_Driver;
import org.openhat.opdi.drivers.NMEAGen_Driver;

/** A factory that creates view controllers for streaming port drivers.
 * 
 * @author Leo
 *
 */
public class ViewControllerFactory {

	public static IViewController getViewController(String driverID) {
		if (driverID.equals(BMP085_Driver.MAGIC))
			return new BMP085_ViewController();
		else
		if (driverID.equals(NMEAGen_Driver.MAGIC))
			return new NMEAGen_ViewController();
		else
		if (driverID.equals(CHR6dm_Driver.MAGIC))
			return new CHR6dm_ViewController();
		return null;
	}
}
