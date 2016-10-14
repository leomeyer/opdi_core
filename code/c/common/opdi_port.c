//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

    
// Implementation of common port functions

// disable string function deprecation warnings
#define _CRT_SECURE_NO_WARNINGS	1

#include <stdlib.h>
#include <string.h>

#include "opdi_constants.h"
#include "opdi_config.h"

static uint16_t portCount = 0;
static opdi_Port *portHead = NULL;
static opdi_Port *portTail = NULL;

#ifdef OPDI_EXTENDED_PROTOCOL
static opdi_PortGroup *portGroupHead = NULL;
static opdi_PortGroup *portGroupTail = NULL;
#endif

static char port_info_message[OPDI_MAX_PORT_INFO_MESSAGE];

#if (OPDI_STREAMING_PORTS > 0)

// streaming port bindings
static opdi_StreamingPortBinding sPortBinds[OPDI_STREAMING_PORTS];
static uint16_t sPortBindCount = 0;

#endif

uint8_t opdi_clear_ports(void) {
	// remove all ports from the list
	portCount = 0;
	portHead = NULL;
	portTail = NULL;
#if (OPDI_STREAMING_PORTS > 0)

// reset streaming port bindings
	opdi_reset_bindings();
#endif

	return OPDI_STATUS_OK;
}

opdi_Port *opdi_get_ports(void) {
	return portHead;
}

opdi_Port *opdi_get_last_port(void) {
	return portTail;
}

uint8_t opdi_add_port(opdi_Port *port) {
	portCount++;
	if (portCount > OPDI_MAX_DEVICE_PORTS)
		return OPDI_TOO_MANY_PORTS;
	if (portHead == NULL)
		portHead = port;
	if (portTail != NULL)
		portTail->next = port;
	portTail = port;
	port->next = NULL;
	return OPDI_STATUS_OK;
}

opdi_Port *opdi_find_port_by_id(const char *id) {
	opdi_Port *port = portHead;
	while (port != NULL) {
		if (!strcmp(port->id, id))
			return port;
		port = port->next;
	}
	return NULL;
}

#ifdef OPDI_EXTENDED_PROTOCOL

uint8_t opdi_add_portgroup(opdi_PortGroup *group) {
	if (portGroupHead == NULL)
		portGroupHead = group;
	if (portGroupTail != NULL)
		portGroupTail->next = group;
	portGroupTail = group;
	group->next = NULL;
	return OPDI_STATUS_OK;
}

opdi_PortGroup *opdi_find_portgroup_by_id(const char *id) {
	opdi_PortGroup *group = portGroupHead;
	while (group != NULL) {
		if (!strcmp(group->id, id))
			return group;
		group = group->next;
	}
	return NULL;
}

#endif

#if (OPDI_STREAMING_PORTS > 0)

uint8_t opdi_bind_port(opdi_Port *port, channel_t channel) {
	uint8_t i;
	opdi_StreamingPortInfo *spi;

	if (strcmp(port->type, OPDI_PORTTYPE_STREAMING))
		return OPDI_WRONG_PORT_TYPE;

	// channel already bound?
	// determine existing binding
	for (i = 0; i < sPortBindCount; i++) {
		if (sPortBinds[i].channel == channel) {
			// different port bound?
			if (sPortBinds[i].port != port)
				return OPDI_CHANNEL_INVALID;
			else
				// port already bound to this channel
				return OPDI_STATUS_OK;
		}
	}

	// new binding possible?
	if (sPortBindCount >= OPDI_STREAMING_PORTS)
		return OPDI_TOO_MANY_BINDINGS;

	sPortBinds[sPortBindCount].channel = channel;
	sPortBinds[sPortBindCount].port = port;

	// remember channel
	spi = (opdi_StreamingPortInfo *)port->info.ptr;
	spi->channel = channel;

	sPortBindCount++;

	return OPDI_STATUS_OK;
}

uint8_t opdi_unbind_port(opdi_Port *port) {
	uint8_t i;
	opdi_StreamingPortInfo *spi;
	int8_t portPos = -1;

	if (strcmp(port->type, OPDI_PORTTYPE_STREAMING))
		return OPDI_WRONG_PORT_TYPE;

	// determine port binding location
	for (i = 0; i < sPortBindCount; i++) {
		if (sPortBinds[i].port == port) {
			portPos = i;
			break;
		}
	}

	// not found?
	if (portPos < 0)
		// the port is not bound
		return OPDI_STATUS_OK;

	// clear channel
	spi = (opdi_StreamingPortInfo *)port->info.ptr;
	spi->channel = 0;

	// shift port bindings left
	for (i = portPos + 1; i < sPortBindCount; i++)
		sPortBinds[i - 1] = sPortBinds[i];

	sPortBindCount--;
	
	return OPDI_STATUS_OK;
}

uint8_t opdi_try_dispatch_stream(opdi_Message *m) {
	uint8_t i;
	// try to find a binding
	for (i = 0; i < sPortBindCount; i++) {
		if (sPortBinds[i].channel == m->channel) {
			// binding found
			opdi_StreamingPortInfo *spi = (opdi_StreamingPortInfo *)sPortBinds[i].port->info.ptr;
			// dataReceivce function specified?
			if (spi->dataReceived)
				return spi->dataReceived(sPortBinds[i].port, m->payload);
			else
				// handler not provided; this port doesn't accept data
				return OPDI_STATUS_OK;
		}
	}
	// no binding for this channel
	return OPDI_NO_BINDING;
}

uint8_t opdi_reset_bindings() {
	uint8_t i;

	// clear all bound channels
	for (i = 0; i < sPortBindCount; i++) {
		opdi_StreamingPortInfo *spi = (opdi_StreamingPortInfo *)sPortBinds[i].port->info.ptr;
		spi->channel = 0;
	}

	sPortBindCount = 0;

	return OPDI_STATUS_OK;
}

uint16_t opdi_get_port_bind_count() {
	return sPortBindCount;
}

#endif

void opdi_set_port_message(const char *message) {
	strncpy(port_info_message, message, OPDI_MAX_PORT_INFO_MESSAGE);
}

const char *opdi_get_port_message() {
	return port_info_message;
}
