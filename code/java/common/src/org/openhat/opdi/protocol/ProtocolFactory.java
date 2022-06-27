//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.protocol;

import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.interfaces.IDevice;
import org.openhat.opdi.interfaces.IProtocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/** This class is a protocol factory that selects the proper protocol for a device.
 * It supports registering protocols that are not implemented in the library itself.
 * A protocol class must provide a constructor with parameters (IDevice device, IConnectionListener listener).
 * 
 * @author Leo
 *
 */
public final class ProtocolFactory {
	
	private static HashMap<String, Class<? extends IProtocol>> registeredProtocols = new HashMap<String, Class<? extends IProtocol>>();
	
	static {
		// register basic protocol
		registerProtocol(BasicProtocol.getMAGIC(), BasicProtocol.class);
		// register extended protocol
		registerProtocol(ExtendedProtocol.getMAGIC(), ExtendedProtocol.class);
	}
	
	public static void registerProtocol(String magic, Class<? extends IProtocol> clazz) {
		// check that class is valid
		try {
			Constructor<? extends IProtocol> cons = clazz.getConstructor(IDevice.class);
			if (cons == null)
				throw new IllegalArgumentException("Constructor with parameter IDevice not found");
		} catch (Exception e) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " can't be used as protocol implementation", e);
		}		
		// the class is ok
		synchronized(registeredProtocols) {
			registeredProtocols.put(magic, clazz);
		}
	}
	
	public static IProtocol getProtocol(IDevice device, String magic, IBasicProtocol.CustomPortResolver customPortResolver) {
		Class<? extends IProtocol> clazz;
		synchronized(registeredProtocols) {
			clazz = registeredProtocols.get(magic);
		}
		if (clazz != null) {
			try {
				// matching class found; get constructor
				Constructor<? extends IProtocol> cons = null;

				// custom port resolver provided?
				if (customPortResolver != null) {
					try {
						cons = clazz.getConstructor(IDevice.class, IBasicProtocol.CustomPortResolver.class);
						return cons.newInstance(device, customPortResolver);
					} catch (Exception e) {}
				}

				// no custom port resolver or not supported
				if (cons == null) {
					try {
						cons = clazz.getConstructor(IDevice.class);
						return cons.newInstance(device);
					} catch (Exception e) {}
				}

				throw new Exception("No suitable constructor found");
			} catch (Exception ex) {
				throw new IllegalArgumentException("Class " + clazz.getName() + " can't be used as protocol implementation: " + ex.getMessage(), ex);
			}
		}
		return null;
	}
	
	// This class can't be instantiated
	private ProtocolFactory() {}
}
