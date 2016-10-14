//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef __OPDI_OPDIPort_H
#define __OPDI_OPDIPort_H

#include <string>
#include <vector>
#include <sstream>

/** Defines different types of ports. */
enum PortType {
	/** A digital port with two states (low and high). */
	PORTTYPE_DIGITAL,
	/** An analog port. */
	PORTTYPE_ANALOG,
	/** A select port. */
	PORTTYPE_SELECT,
	/** A USART. */
	PORTTYPE_STREAMING,
	/** Another port type. */
	PORTTYPE_OTHER
};

/** Defines the possible directions (capabilities) of ports. */
enum PortDirCaps {
	/** undefined capabilities */
	PORTDIRCAP_UNKNOWN = -1,
	/** A port that can only be used for input. */
	PORTDIRCAP_INPUT,		
	/** A port that can only be used for output. */
	PORTDIRCAP_OUTPUT,		
	/** A port that can be configured for input or output. */
	PORTDIRCAP_BIDIRECTIONAL
};

class IBasicProtocol;

/** Defines the abstract properties and functions of a port.
 * 
 * @author Leo
 *
 */
class OPDIPort {
	
protected:
	IBasicProtocol& protocol;
	std::string id;
	std::string name;
	PortType type;
	PortDirCaps dirCaps;
	void *viewAdapter;
	
	/** Only to be used by subclasses
	 * 
	 * @param protocol
	 * @param id
	 * @param name
	 * @param type
	 */
	OPDIPort(IBasicProtocol& protocol, std::string id, std::string name, PortType type, PortDirCaps dirCaps);

	template <class T> inline std::string to_string(const T& t) {
		std::stringstream ss;
		ss << t;
		return ss.str();
	}

public:
	/** Returns the protocol.
	 * 
	 * @return
	 */
	IBasicProtocol& getProtocol();

	/** Returns the unique ID of this port.
	 * 
	 * @return
	 */
	std::string getID();

	/** Returns the name of this port.
	 * 
	 * @return
	 */
	std::string getName();
		
	/** Returns the type of this port.
	 * 
	 * @return
	 */
	PortType getType();

	/** Returns the possible direction of this port.
	 * 
	 * @return
	 */
	PortDirCaps getDirCaps();

	std::string getDirCapsText();

	void setProtocol(IBasicProtocol& protocol);

	void setID(std::string id);

	void setName(std::string name);

	void setType(PortType type);

	void setDirCaps(PortDirCaps dir);
	
	/** A convenience method for subclasses. Throws a ProtocolException if the part count doesn't match
	 * or if the first part is not equal to the magic.
	 * @param parts
	 * @param count
	 * @param magic
	 * @throws ProtocolException
	 */
	virtual void checkSerialForm(std::vector<std::string> parts, unsigned int count, std::string magic);

	/** Returns a serialized description of this port.
	 * 
	 * @return
	 */
	virtual std::string serialize() = 0;
	
	/** Clear cached state values.
	 * 
	 */
	virtual void refresh() = 0;

	virtual std::string toString() = 0;

	/** Stores an arbitrary view object.
	 * 
	 * @param viewAdapter
	 */
	inline void setViewAdapter(void *viewAdapter);

	/** Returns a stored view object.
	 * 
	 * @return
	 */
	inline void *getViewAdapter();

	/** Fetches the state of the port from the device.
	 * 
	 */
	virtual void getPortState() = 0;
};


#endif
