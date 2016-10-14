//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

  
#ifndef __OPDI_PROTOCOL_H
#define __OPDI_PROTOCOL_H

#include "opdi_platformtypes.h"
#include "opdi_configspecs.h"

// buffer sizes for numeric to string conversions
#define BUFSIZE_8BIT	5
#define BUFSIZE_16BIT	7
#define BUFSIZE_32BIT	11
#define BUFSIZE_64BIT	23

// Common functions of the OPDI protocol.

extern const char *opdi_msg_parts[OPDI_MAX_MESSAGE_PARTS];
// for assembling a payload
extern char opdi_msg_payload[OPDI_MESSAGE_PAYLOAD_LENGTH];

// expects a control message on channel 0
uint8_t expect_control_message(const char **parts, uint8_t *partCount);

// sends an error with optional additional information
uint8_t send_error(uint8_t code, const char *part1, const char *part2);

// sends a disagreement message with optional additional information
uint8_t send_disagreement(channel_t channel, uint8_t code, const char *part1, const char *part2);

// sends a port error message with optional additional information
uint8_t send_port_error(channel_t channel, const char *portID, const char *part1, const char *part2);

// sends an agreement message
uint8_t send_agreement(channel_t channel);

// sends the contents of the opdi_msg_parts array on the specified channel
uint8_t send_payload(channel_t channel);

// sends the contents of the opdi_msg_parts array on the specified channel
uint8_t send_parts(channel_t channel);

#endif		// __OPDI_PROTOCOL_H
