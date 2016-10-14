//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef __OPDI_PROTOCOLFACTORY_H
#define __OPDI_PROTOCOLFACTORY_H

#include "opdi_IBasicProtocol.h"
#include "opdi_IDevice.h"

/** This class is a protocol factory that selects the proper protocol for a device.
 * A protocol class must provide a constructor with parameters (IDevice device, IConnectionListener listener).
 * 
 * @author Leo
 *
 */
class ProtocolFactory {

public:
	static IBasicProtocol* getProtocol(IDevice* device, std::string magic);

};

#endif
