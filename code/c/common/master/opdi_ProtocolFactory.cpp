//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "opdi_IBasicProtocol.h"
#include "opdi_ProtocolFactory.h"
#include "opdi_BasicProtocol.h"

IBasicProtocol* ProtocolFactory::getProtocol(IDevice* device, std::string magic) {
	if (magic == "BP") {
		return new BasicProtocol(device);
	}

	return NULL;
}
