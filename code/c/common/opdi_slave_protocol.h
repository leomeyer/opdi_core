//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

  
// OPDI slave protocol level functions

#ifndef __OPDI_SLAVE_PROTOCOL_H
#define __OPDI_SLAVE_PROTOCOL_H

#include "opdi_platformtypes.h"
#include "opdi_port.h"

#ifdef __cplusplus
extern "C" {
#endif 

/** Callback function that is called when the protocol status changes.
* Used by devices to update UI etc.
*/
typedef void (*opdi_ProtocolCallback)(uint8_t protocol_state);

/** Defines a protocol handler that has been identified by a protocol identifier.
*   This function processes messages
*/
typedef uint8_t (*opdi_ProtocolHandler)(channel_t channel);

/** Callback function to determine a protocol handler.
*   If this is NULL, supports only the basic protocol.
*   The protocol identifier from the master is passed into this function.
*   If this function returns NULL, the basic protocol handler is used.
*/
typedef opdi_ProtocolHandler (*opdi_GetProtocol)(const char *protocolID);

/** The message handler for the basic protocol.
*   This handler is available as a fallback mechanism for extended protocols.
*   It returns OPDI_STATUS_OK even if the message could not be processed (ignore unknown messages).
*/
//uint8_t opdi_handle_basic_message(opdi_Message *m);

/** Prepares the OPDI protocol implementation for a new connection.
*   Most importantly, this will disable encryption as it is not used at the beginning
*   of the handshake.
*/
uint8_t opdi_slave_init(void);

/** Starts the OPDI protocol by performing the handshake using the given message.
*   get_protocol is used to determine extended protocols. It is passed the master's chosen
*   protocol identifier and may return a protocol handler. It may also be NULL.
*   If it is NULL, only the basic protocol can be used.
*/
uint8_t opdi_slave_start(opdi_Message *m, opdi_GetProtocol get_protocol, opdi_ProtocolCallback protocol_callback);

/** Returns 1 if the slave is currently connected, i. e. a protocol handler is running; 0 otherwise.
*/
uint8_t opdi_slave_connected(void);

/** Sends a debug message to the master.
*/
uint8_t opdi_send_debug(const char *debugmsg);
	
/** Causes the Reconfigure message to be sent which prompts the master to re-read the device capabilities.
*/
uint8_t opdi_reconfigure(void);

/** Causes the Refresh message to be sent for the specified ports. The last element must be NULL.
*   If the first element is NULL, sends the empty refresh message causing all ports to be
*   refreshed.
*/
uint8_t opdi_refresh(opdi_Port **ports);

/** Causes the Disconnect message to be sent to the master.
*   Returns OPDI_DISCONNECTED. After this, no more messages may be sent to the master.
*/
uint8_t opdi_disconnect(void);

#ifdef __cplusplus
}
#endif

#endif		// __OPDI_SLAVE_PROTOCOL_H
