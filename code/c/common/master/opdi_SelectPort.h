//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef __SELECTPORT_H
#define __SELECTPORT_H

#include "opdi_OPDIPort.h"

/** Represents a select port on a device.
 * 
 * @author Leo
 *
 */
class SelectPort : public OPDIPort {
	
protected:
	uint16_t labelCount;
	int flags;

	std::vector<std::string> positionLabels;

	// State section
	int16_t position;
	
public:

	/** Constructor for deserializing from wire form
	 * 
	 * @param protocol
	 * @param parts
	 * @throws ProtocolException
	 */
	SelectPort(IBasicProtocol& protocol, std::vector<std::string> parts);
	
	std::string serialize(void) override;

	void loadLabels(void);
	
	void checkPosition(uint16_t position);

	// Retrieve all port state from the device
	void getPortState(void) override;
	
	void setPortPosition(IBasicProtocol& protocol, uint16_t position);

	uint16_t getMaxPosition(void);
	
	/** Sets the port to the given position. Throws an IllegalArgumentException if the position
	 * is not supported.
	 * @param portState
	 */
	void setPosition(uint16_t position);
	
	uint16_t getPosition(void);

	std::string getPositionText(void);
	
	std::string toString(void) override;

	void refresh(void) override;
};

#endif
