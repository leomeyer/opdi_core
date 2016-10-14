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

/** This class receives text messages from a device.
 * 
 * @author leo.meyer
 *
 */
public class Text_Driver extends AbstractDriver implements IStreamingPortListener {
	
	public static final String MAGIC = "Text";
	
	protected String text;

	public synchronized String getText() {
		return text;
	}

	@Override
	public synchronized void attach(StreamingPort port) {
		// register this driver as a listener
		port.setStreamingPortListener(this);
	}

	@Override
	public synchronized void dataReceived(StreamingPort port, String data) {
		text = data;
		
		super.dataReceived(true);
	}

	@Override
	public void portUnbound(StreamingPort port) {
	}
}
