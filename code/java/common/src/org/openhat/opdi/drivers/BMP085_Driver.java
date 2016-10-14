//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.drivers;

import org.openhat.ports.StreamingPort;
import org.openhat.ports.StreamingPort.IStreamingPortListener;
import org.openhat.utils.Strings;

/** Driver for Bosch Sensortec pressure sensor BMP085
 * 
 * @author Leo
 *
 */
public class BMP085_Driver extends AbstractDriver implements IStreamingPortListener {
	
	protected static final char SEPARATOR = ':';
	public static final String MAGIC = "BMP085";
	
	protected float temperature;
	protected float pressure;
	
	@Override
	public synchronized void attach(StreamingPort port) {
		// register this driver as a listener
		port.setStreamingPortListener(this);
	}

	@Override
	public synchronized void dataReceived(StreamingPort port, String data) {
		
		// extract pressure and temperature from the stream
		
		String[] parts = Strings.split(data, SEPARATOR);
		
		final int ID = 0;
		final int TEMP = 1;
		final int PRES = 2;
		final int PART_COUNT = 3;

		if (parts.length != PART_COUNT || !parts[ID].equals(MAGIC))
			super.dataReceived(false);
		else {
			try {
				temperature = Float.parseFloat(parts[TEMP]);
				pressure = Float.parseFloat(parts[PRES]);
			} catch (NumberFormatException e) {
				super.dataReceived(false);
			}			
		}
		super.dataReceived(true);
	}

	public synchronized float getTemperature() {
		return temperature;
	}

	public synchronized float getPressure() {
		return pressure;
	}

	@Override
	public void portUnbound(StreamingPort port) {
	}
}
