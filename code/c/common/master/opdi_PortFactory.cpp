//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include <string>
#include <vector>

#include "opdi_IBasicProtocol.h"
#include "opdi_PortFactory.h"


OPDIPort* PortFactory::createPort(IBasicProtocol& protocol, std::vector<std::string> parts)
{
	// the "magic" in the first part decides about the port type		
	if (parts[0] == OPDI_digitalPort)
		return new DigitalPort(protocol, parts);
	else if (parts[0] == OPDI_selectPort)
		return new SelectPort(protocol, parts);
/*
	else if (parts[0].equals(AnalogPort.MAGIC))
		return new AnalogPort(protocol, parts);
	else if (parts[0].equals(DialPort.MAGIC))
		return new DialPort(protocol, parts);
	else if (parts[0].equals(StreamingPort.MAGIC))
		return new StreamingPort(protocol, parts);
*/
	else
		return NULL;

//	else
	//	throw Poco::InvalidArgumentException("Programmer error: Unknown port magic: '" + parts[0] + "'. Protocol must check supported ports");
}
