//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


#ifndef __OPDI_BASICDEVICECAPABILITIES_H
#define __OPDI_BASICDEVICECAPABILITIES_H

#include "opdi_OPDIPort.h"

#define OPDI_BASIC_DEVICE_CAPABILITIES_MAGIC "BDC"

/** This class describes the capabilities of an OPDI device. It contains convenience methods to access ports.
 * 
 * @author Leo
 *
 */
class BasicDeviceCapabilities {

protected:
	std::vector<OPDIPort*> ports;
	
public:
	BasicDeviceCapabilities(IBasicProtocol* protocol, int channel, std::string serialForm);
	
	OPDIPort* findPortByID(std::string portID);

	std::vector<OPDIPort*> & getPorts();
};

#endif
