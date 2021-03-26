//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


// Implements the slave protocol part of the OPDI specification.
//
// Port support may be selectively disabled by using the defines OPDI_NO_ANALOG_PORTS,
// OPDI_NO_DIGITAL_PORTS, OPDI_NO_SELECT_PORTS, OPDI_NO_DIAL_PORTS, and 
// OPDI_STREAMING_PORTS to conserve memory.
//
// Supported protocols are: basic protocol, extended protocol.
// For the extended protocol, define OPDI_EXTENDED_PROTOCOL in your configspecs.h.
// By default, only the basic protocol is supported.

// disable string function deprecation warnings
#define _CRT_SECURE_NO_WARNINGS	1

#include <stdlib.h>
#include <string.h>

#include "opdi_constants.h"
#include "opdi_strings.h"
#include "opdi_message.h"
#include "opdi_protocol.h"
#include "opdi_slave_protocol.h"
#include "opdi_protocol_constants.h"
#include "opdi_config.h"
#include "opdi_platformfuncs.h"
#include "opdi_platformtypes.h"
#include "opdi_configspecs.h"

static uint8_t connected;

// send a comma-separated list of port IDs
static uint8_t send_device_caps(channel_t channel) {
	opdi_Message message;
	uint16_t portCount = 0;
	const char *opdi_msg_parts[OPDI_MAX_DEVICE_PORTS];
	char portCSV[OPDI_MESSAGE_PAYLOAD_LENGTH];
	opdi_Port *port;
	uint8_t result;

	// prepare list of device ports
	opdi_msg_parts[0] = NULL;
	port = opdi_get_ports();
	while (port != NULL) {
		if (portCount >= OPDI_MAX_DEVICE_PORTS)
			return OPDI_TOO_MANY_PORTS;
		opdi_msg_parts[portCount++] = port->id;
		port = port->next;
	}
	opdi_msg_parts[portCount] = NULL;

	// join port IDs as comma separated list
	result = strings_join(opdi_msg_parts, ',', portCSV, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	// join payload
	opdi_msg_parts[0] = "BDC";
	opdi_msg_parts[1] = portCSV;
	opdi_msg_parts[2] = NULL;
	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	// send on the same channel
	message.channel = channel;
	message.payload = opdi_msg_payload;

	result = opdi_put_message(&message);
	if (result != OPDI_STATUS_OK)
		return result;
	
	return OPDI_STATUS_OK;
}

#ifndef OPDI_NO_DIGITAL_PORTS
static uint8_t send_digital_port_info(channel_t channel, opdi_Port *port) {
	char flagStr[BUFSIZE_32BIT];

	opdi_int32_to_str(port->flags, flagStr);

	// join payload
	opdi_msg_parts[0] = OPDI_digitalPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
	opdi_msg_parts[3] = port->caps;
	opdi_msg_parts[4] = flagStr;
	opdi_msg_parts[5] = NULL;

	return send_parts(channel);
}
#endif

#ifndef OPDI_NO_ANALOG_PORTS
static uint8_t send_analog_port_info(channel_t channel, opdi_Port *port) {
	char flagStr[BUFSIZE_32BIT];

	opdi_int32_to_str(port->flags, flagStr);

	// join payload
	opdi_msg_parts[0] = OPDI_analogPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
	opdi_msg_parts[3] = port->caps;
	opdi_msg_parts[4] = flagStr;
	opdi_msg_parts[5] = NULL;

	return send_parts(channel);
}
#endif

#ifndef OPDI_NO_SELECT_PORTS
static uint8_t send_select_port_info(channel_t channel, opdi_Port *port) {
	char **labels;
	uint16_t positions = 0;
	char buf[BUFSIZE_16BIT];
	char flagStr[BUFSIZE_32BIT];

	// port info is an array of char*
	labels = (char**)port->info.ptr;

	// count positions
	while (labels[positions])
		positions++;

	// convert positions to str
	opdi_uint16_to_str(positions, (char *)&buf);
	opdi_int32_to_str(port->flags, flagStr);

	// join payload
	opdi_msg_parts[0] = OPDI_selectPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
	opdi_msg_parts[3] = buf;
	opdi_msg_parts[4] = flagStr;
	opdi_msg_parts[5] = NULL;

	return send_parts(channel);
}
#endif

#ifndef OPDI_NO_DIAL_PORTS
static uint8_t send_dial_port_info(channel_t channel, opdi_Port *port) {
	char minbuf[BUFSIZE_64BIT];
	char maxbuf[BUFSIZE_64BIT];
	char stepbuf[BUFSIZE_64BIT];
	char flagStr[BUFSIZE_32BIT];

	opdi_DialPortInfo *dpi = (opdi_DialPortInfo *)port->info.ptr;
	// convert values to strings
	opdi_int64_to_str(dpi->min, (char *)&minbuf);
	opdi_int64_to_str(dpi->max, (char *)&maxbuf);
	opdi_int64_to_str(dpi->step, (char *)&stepbuf);
	opdi_int32_to_str(port->flags, flagStr);

	// join payload
	opdi_msg_parts[0] = OPDI_dialPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
	opdi_msg_parts[3] = minbuf;
	opdi_msg_parts[4] = maxbuf;
	opdi_msg_parts[5] = stepbuf;
	opdi_msg_parts[6] = flagStr;
	opdi_msg_parts[7] = NULL;

	return send_parts(channel);
}
#endif

#ifdef OPDI_USE_CUSTOM_PORTS
static uint8_t send_custom_port_info(channel_t channel, opdi_Port *port) {
	char flagStr[BUFSIZE_32BIT];

//	opdi_CustomPortInfo *cpi = (opdi_CustomPortInfo *)port->info.ptr;
	// convert values to strings
	opdi_int32_to_str(port->flags, flagStr);

	// join payload
	opdi_msg_parts[0] = OPDI_customPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
//	opdi_msg_parts[3] = cpi->custom;
	opdi_msg_parts[3] = flagStr;
	opdi_msg_parts[4] = NULL;

	return send_parts(channel);
}
#endif

#if (OPDI_STREAMING_PORTS > 0)
static uint8_t send_streaming_port_info(channel_t channel, opdi_Port *port) {
	char buf[BUFSIZE_16BIT];

	opdi_StreamingPortInfo *spi = (opdi_StreamingPortInfo *)port->info.ptr;
	// convert flags to str
	opdi_int32_to_str(port->flags, (char *)&buf);

	// join payload
	opdi_msg_parts[0] = OPDI_streamingPort;	// port magic
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = port->name;
	opdi_msg_parts[3] = spi->driverID;
	opdi_msg_parts[4] = buf;
	opdi_msg_parts[5] = NULL;

	return send_parts(channel);
}
#endif

static uint8_t send_port_info(channel_t channel, opdi_Port *port) {

#ifndef OPDI_NO_DIGITAL_PORTS
	if (0 == strcmp(port->type, OPDI_PORTTYPE_DIGITAL)) {
		return send_digital_port_info(channel, port);
#else
	// "better keep it gramat."
	if (0) {
#endif
#ifndef OPDI_NO_ANALOG_PORTS
	} else if (0 == strcmp(port->type, OPDI_PORTTYPE_ANALOG)) {
		return send_analog_port_info(channel, port);
#endif
#ifndef OPDI_NO_SELECT_PORTS
	} else if (0 == strcmp(port->type, OPDI_PORTTYPE_SELECT)) {
		return send_select_port_info(channel, port);
#endif
#ifndef OPDI_NO_DIAL_PORTS
	} else if (0 == strcmp(port->type, OPDI_PORTTYPE_DIAL)) {
		return send_dial_port_info(channel, port);
#endif
#if (OPDI_STREAMING_PORTS > 0)
	} else if (0 == strcmp(port->type, OPDI_PORTTYPE_STREAMING)) {
		return send_streaming_port_info(channel, port);
#endif
	} else
		return OPDI_PORTTYPE_UNKNOWN;
}

/// analog port functions

#ifndef OPDI_NO_ANALOG_PORTS
static uint8_t get_analog_port_state(opdi_Port *port) {
	uint8_t result;
	char mode[] = " ";
	char res[] = " ";
	char ref[] = " ";
	int32_t value = 0;
	char valStr[BUFSIZE_32BIT];

	if (0 != strcmp(port->type, OPDI_PORTTYPE_ANALOG)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_get_analog_port_state(port, mode, res, ref, &value);
	if (result != OPDI_STATUS_OK)
		return result;

	opdi_int32_to_str(value, valStr);

	// join payload
	opdi_msg_parts[0] = OPDI_analogPortState;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = mode;
	opdi_msg_parts[3] = ref;
	opdi_msg_parts[4] = res;
	opdi_msg_parts[5] = valStr;
	opdi_msg_parts[6] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

static uint8_t send_analog_port_state(channel_t channel, opdi_Port *port) {
	uint8_t result = get_analog_port_state(port);
	if (result == OPDI_PORT_ERROR) {
		send_port_error(channel, port->id, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	else
	if (result == OPDI_PORT_ACCESS_DENIED) {
		send_disagreement(channel, OPDI_PORT_ACCESS_DENIED, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	if (result != OPDI_STATUS_OK)
		return result;

	return send_payload(channel);
}

static uint8_t set_analog_port_value(channel_t channel, opdi_Port *port, const char *value) {
	int32_t val;
	uint8_t result;

	result = opdi_str_to_int32(value, &val);
	if (result != OPDI_STATUS_OK)
		return result;

	result = opdi_set_analog_port_value(port, val);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_analog_port_state(channel, port);
}

static uint8_t set_analog_port_mode(channel_t channel, opdi_Port *port, const char *mode) {
	uint8_t result;

	result = opdi_set_analog_port_mode(port, mode);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_analog_port_state(channel, port);
}

static uint8_t set_analog_port_resolution(channel_t channel, opdi_Port *port, const char *res) {
	uint8_t result;

	result = opdi_set_analog_port_resolution(port, res);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_analog_port_state(channel, port);
}

static uint8_t set_analog_port_reference(channel_t channel, opdi_Port *port, const char *ref) {
	uint8_t result;

	result = opdi_set_analog_port_reference(port, ref);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_analog_port_state(channel, port);
}
#endif

/// digital port functions

#ifndef OPDI_NO_DIGITAL_PORTS
// writes the digital port state to the opdi_msg_parts array
static uint8_t get_digital_port_state(opdi_Port *port) {
	uint8_t result;
	char mode[] = " ";
	char line[] = " ";

	if (strcmp(port->type, OPDI_PORTTYPE_DIGITAL)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_get_digital_port_state(port, mode, line);
	if (result != OPDI_STATUS_OK)
		return result;

	// join payload
	opdi_msg_parts[0] = OPDI_digitalPortState;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = mode;
	opdi_msg_parts[3] = line;
	opdi_msg_parts[4] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

static uint8_t send_digital_port_state(channel_t channel, opdi_Port *port) {
	uint8_t result = get_digital_port_state(port);
	if (result == OPDI_PORT_ERROR) {
		send_port_error(channel, port->id, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	else
	if (result == OPDI_PORT_ACCESS_DENIED) {
		send_disagreement(channel, OPDI_PORT_ACCESS_DENIED, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	if (result != OPDI_STATUS_OK)
		return result;

	return send_payload(channel);
}

static uint8_t set_digital_port_line(channel_t channel, opdi_Port *port, const char *line) {
	uint8_t result;

	result = opdi_set_digital_port_line(port, line);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_digital_port_state(channel, port);
}

static uint8_t set_digital_port_mode(channel_t channel, opdi_Port *port, const char *mode) {
	uint8_t result;

	result = opdi_set_digital_port_mode(port, mode);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_digital_port_state(channel, port);
}
#endif

/// select port functions

#ifndef OPDI_NO_SELECT_PORTS

static uint8_t send_select_port_label(channel_t channel, opdi_Port *port, const char *position) {
	uint8_t result;
	uint16_t pos;
	uint16_t i;
	char **labels;

	if (strcmp(port->type, OPDI_PORTTYPE_SELECT)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	// port info is an array of char*
	labels = (char**)port->info.ptr;

	result = opdi_str_to_uint16(position, &pos);
	if (result != OPDI_STATUS_OK)
		return result;

	// count labels up to specified position
	i = 0;
	while (i < pos && labels[i])
		i++;

	// invalid value?
	if (labels[i] == NULL)
		return OPDI_POSITION_INVALID;

	// join payload
	opdi_msg_parts[0] = OPDI_selectPortLabel;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = position;
	opdi_msg_parts[3] = labels[i];
	opdi_msg_parts[4] = NULL;

	return send_parts(channel);
}

static uint8_t get_select_port_state(opdi_Port *port) {
	uint8_t result;
	uint16_t pos;
	char position[BUFSIZE_32BIT];

	if (strcmp(port->type, OPDI_PORTTYPE_SELECT)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_get_select_port_state(port, &pos);
	if (result != OPDI_STATUS_OK)
		return result;

	opdi_uint16_to_str(pos, position);

	// join payload
	opdi_msg_parts[0] = OPDI_selectPortState;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = position;
	opdi_msg_parts[3] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

static uint8_t send_select_port_state(channel_t channel, opdi_Port *port) {
	uint8_t result = get_select_port_state(port);
	if (result == OPDI_PORT_ERROR) {
		send_port_error(channel, port->id, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	else
	if (result == OPDI_PORT_ACCESS_DENIED) {
		send_disagreement(channel, OPDI_PORT_ACCESS_DENIED, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	if (result != OPDI_STATUS_OK)
		return result;

	return send_payload(channel);
}

static uint8_t set_select_port_position(channel_t channel, opdi_Port *port, const char *position) {
	uint8_t result;
	uint16_t pos;
	uint16_t i;
	char **labels;

	if (strcmp(port->type, OPDI_PORTTYPE_SELECT)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	// port info is an array of char*
	labels = (char**)port->info.ptr;

	result = opdi_str_to_uint16(position, &pos);
	if (result != OPDI_STATUS_OK)
		return result;

	// count labels up to pos
	i = 0;
	while (i < pos && labels[i])
		i++;

	// invalid value?
	if (labels[i] == NULL)
		return OPDI_POSITION_INVALID;

	result = opdi_set_select_port_position(port, i);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_select_port_state(channel, port);
}
#endif

/// dial port functions

#ifndef OPDI_NO_DIAL_PORTS
static uint8_t get_dial_port_state(opdi_Port *port) {
	uint8_t result;
	int64_t pos;
	char position[BUFSIZE_64BIT];

	if (strcmp(port->type, OPDI_PORTTYPE_DIAL)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_get_dial_port_state(port, &pos);
	if (result != OPDI_STATUS_OK)
		return result;

	opdi_int64_to_str(pos, position);

	// join payload
	opdi_msg_parts[0] = OPDI_dialPortState;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = position;
	opdi_msg_parts[3] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

static uint8_t send_dial_port_state(channel_t channel, opdi_Port *port) {
	uint8_t result = get_dial_port_state(port);
	if (result == OPDI_PORT_ERROR) {
		send_port_error(channel, port->id, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	else
	if (result == OPDI_PORT_ACCESS_DENIED) {
		send_disagreement(channel, OPDI_PORT_ACCESS_DENIED, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	if (result != OPDI_STATUS_OK)
		return result;

	return send_payload(channel);
}

static uint8_t set_dial_port_position(channel_t channel, opdi_Port *port, const char *position) {
	uint8_t result;
	int64_t pos;
	opdi_DialPortInfo *dpi;
	int64_t i;

	if (strcmp(port->type, OPDI_PORTTYPE_DIAL)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_str_to_int64(position, &pos);
	if (result != OPDI_STATUS_OK)
		return result;

	// validate position
	dpi = (opdi_DialPortInfo *)port->info.ptr;

	// invalid value?
	i = pos - dpi->min;
	if ((pos < dpi->min) || (pos > dpi->max) || (i % dpi->step != 0))
		return OPDI_POSITION_INVALID;

	result = opdi_set_dial_port_position(port, pos);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_dial_port_state(channel, port);
}
#endif

#ifdef OPDI_USE_CUSTOM_PORTS
static uint8_t get_custom_port_value(opdi_Port *port) {
	uint8_t result;
	char value[256];    // TODO

	if (strcmp(port->type, OPDI_PORTTYPE_CUSTOM)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_get_custom_port_value(port, value);
	if (result != OPDI_STATUS_OK)
		return result;

	// join payload
	opdi_msg_parts[0] = OPDI_customPortState;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = value;
	opdi_msg_parts[3] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

static uint8_t send_custom_port_state(channel_t channel, opdi_Port *port) {
	uint8_t result = get_custom_port_value(port);
	if (result == OPDI_PORT_ERROR) {
		send_port_error(channel, port->id, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	else
	if (result == OPDI_PORT_ACCESS_DENIED) {
		send_disagreement(channel, OPDI_PORT_ACCESS_DENIED, opdi_get_port_message(), NULL);
		return OPDI_STATUS_OK;
	}
	if (result != OPDI_STATUS_OK)
		return result;

	return send_payload(channel);
}

static uint8_t set_custom_port_value(channel_t channel, opdi_Port *port, const char *value) {
	uint8_t result;

	if (strcmp(port->type, OPDI_PORTTYPE_CUSTOM)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	result = opdi_set_custom_port_value(port, value);
	if (result != OPDI_STATUS_OK)
		return result;

	return send_custom_port_state(channel, port);
}
#endif

/// streaming port functions
#if (OPDI_STREAMING_PORTS > 0)
static uint8_t bind_streaming_port(channel_t channel, opdi_Port *port, const char *bChan) {
	uint8_t result;
	channel_t bChannel;

	// convert channel string to number
#if (channel_bits == 8)
	result = opdi_str_to_uint8(bChan, &bChannel);
#elif (channel_bits == 16)
	result = opdi_str_to_uint16(bChan, &bChannel);
#else
#error "Not implemented; unable to convert channel string to numeric value"
#endif

	if (result != OPDI_STATUS_OK)
		return result;

	// channel sanity check
	if (bChannel <= 0)
		return OPDI_CHANNEL_INVALID;

	// bind port
	result = opdi_bind_port(port, bChannel);

	// problem
	if (result == OPDI_TOO_MANY_BINDINGS) {
		return send_disagreement(channel, result, NULL, NULL);
	}

	// error
	if (result != OPDI_STATUS_OK)
		return result;

	// ok
	return send_agreement(channel);
}

static uint8_t unbind_streaming_port(channel_t channel, opdi_Port *port) {
	uint8_t result;

	// unbind port
	result = opdi_unbind_port(port);

	// error
	if (result != OPDI_STATUS_OK)
		return result;

	// ok
	return send_agreement(channel);
}
#endif

#ifdef OPDI_EXTENDED_PROTOCOL

static uint8_t send_extended_port_info(channel_t channel, const char *portID, char *portInfo) {
	// join payload
	opdi_msg_parts[0] = OPDI_extendedPortInfo;
	opdi_msg_parts[1] = portID;
	opdi_msg_parts[2] = portInfo;
	opdi_msg_parts[3] = NULL;

	return send_parts(channel);
}

static uint8_t send_extended_port_state(channel_t channel, const char *portID, char *portState) {
	// join payload
	opdi_msg_parts[0] = OPDI_extendedPortState;
	opdi_msg_parts[1] = portID;
	opdi_msg_parts[2] = portState;
	opdi_msg_parts[3] = NULL;

	return send_parts(channel);
}

static uint8_t send_all_port_infos(channel_t channel) {
	opdi_Port *port;
	uint8_t result;
	char buffer[OPDI_EXTENDED_INFO_LENGTH];

	port = opdi_get_ports();
	// go through list of device ports
	while (port != NULL) {

#ifndef OPDI_NO_DIGITAL_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_DIGITAL) == 0) {
			result = send_digital_port_info(channel, port);
			if (result != OPDI_STATUS_OK)
				return result;
		}
		else
#endif
#ifndef OPDI_NO_ANALOG_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_ANALOG) == 0) {
			result = send_analog_port_info(channel, port);
			if (result != OPDI_STATUS_OK)
				return result;
		}
		else
#endif
#ifndef OPDI_NO_SELECT_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_SELECT) == 0) {
			result = send_select_port_info(channel, port);
			if (result != OPDI_STATUS_OK)
				return result;
		}
		else
#endif
#ifndef OPDI_NO_DIAL_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_DIAL) == 0) {
			result = send_dial_port_info(channel, port);
			if (result != OPDI_STATUS_OK)
				return result;
		}
#endif
#ifdef OPDI_USE_CUSTOM_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_CUSTOM) == 0) {
			result = send_custom_port_info(channel, port);
			if (result != OPDI_STATUS_OK)
				return result;
		}
#endif
		// send extended port info
		// copy port ID to the buffer
		strncpy(buffer, opdi_msg_parts[1], OPDI_EXTENDED_INFO_LENGTH);
		result = opdi_slave_callback(OPDI_FUNCTION_GET_EXTENDED_PORTINFO, buffer, OPDI_EXTENDED_INFO_LENGTH);
		if (result != OPDI_STATUS_OK)
			return result;
		result = send_extended_port_info(channel, opdi_msg_parts[1], buffer);
		if (result != OPDI_STATUS_OK)
			return result;

		port = port->next;
	}
	return OPDI_STATUS_OK;
}

static uint8_t send_all_port_states(channel_t channel) {
	uint8_t result;
	opdi_Port *port;
	char buffer[OPDI_EXTENDED_INFO_LENGTH];

	// go through list of device ports
	port = opdi_get_ports();
	while (port != NULL) {
		result = OPDI_PORTTYPE_UNKNOWN;

#ifndef OPDI_NO_DIGITAL_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_DIGITAL) == 0) {
			result = send_digital_port_state(channel, port);
		}
		else
#endif
#ifndef OPDI_NO_ANALOG_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_ANALOG) == 0) {
			result = send_analog_port_state(channel, port);
		}
		else
#endif
#ifndef OPDI_NO_SELECT_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_SELECT) == 0) {
			result = send_select_port_state(channel, port);
		}
		else
#endif
#ifndef OPDI_NO_DIAL_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_DIAL) == 0) {
			result = send_dial_port_state(channel, port);
		}
                else
#endif
#ifdef OPDI_USE_CUSTOM_PORTS
		if (strcmp(port->type, OPDI_PORTTYPE_CUSTOM) == 0) {
			result = send_custom_port_state(channel, port);
		}
#endif
		if (result == OPDI_STATUS_OK) {
			// state sent ok; send extended info
			// copy port ID to the buffer
			strncpy(buffer, opdi_msg_parts[1], OPDI_EXTENDED_INFO_LENGTH);
			result = opdi_slave_callback(OPDI_FUNCTION_GET_EXTENDED_PORTSTATE, buffer, OPDI_EXTENDED_INFO_LENGTH);
			if (result != OPDI_STATUS_OK)
				return result;
			result = send_extended_port_state(channel, opdi_msg_parts[1], buffer);
			if (result != OPDI_STATUS_OK)
				return result;
		}

		port = port->next;
	}

	return OPDI_STATUS_OK;
}

static uint8_t send_group_info(channel_t channel, opdi_PortGroup *group) {
	char buf[BUFSIZE_32BIT];

	// convert flags
	opdi_int32_to_str(group->flags, buf);

	// join payload
	opdi_msg_parts[0] = OPDI_groupInfo;
	opdi_msg_parts[1] = group->id;
	opdi_msg_parts[2] = group->label;
	opdi_msg_parts[3] = group->parent;
	opdi_msg_parts[4] = buf;	// flags
	opdi_msg_parts[5] = NULL;

	return send_parts(channel);
}

static uint8_t send_extended_group_info(channel_t channel, opdi_PortGroup *group) {
	// join payload
	opdi_msg_parts[0] = OPDI_extendedGroupInfo;
	opdi_msg_parts[1] = group->id;
	opdi_msg_parts[2] = group->extendedInfo;
	opdi_msg_parts[3] = NULL;

	return send_parts(channel);
}

static uint8_t send_extended_device_info(channel_t channel, char *deviceInfo) {
	// join payload
	opdi_msg_parts[0] = OPDI_extendedDeviceInfo;
	opdi_msg_parts[1] = deviceInfo;
	opdi_msg_parts[2] = NULL;

	return send_parts(channel);
}

static uint8_t send_all_select_port_labels(channel_t channel, opdi_Port *port) {
	uint8_t result;
	uint16_t pos = 0;
	char position[BUFSIZE_16BIT];
	char **labels;

	if (strcmp(port->type, OPDI_PORTTYPE_SELECT)) {
		return OPDI_WRONG_PORT_TYPE;
	}

	// port info is an array of char*
	labels = (char**)port->info.ptr;

	// join payload
	opdi_msg_parts[0] = OPDI_selectPortLabel;
	opdi_msg_parts[1] = port->id;
	opdi_msg_parts[2] = position;
	opdi_msg_parts[4] = NULL;
	while (labels[pos]) {
		opdi_uint16_to_str(pos, position);
		opdi_msg_parts[3] = labels[pos];

		result = send_parts(channel);
		if (result != OPDI_STATUS_OK)
			return result;
		pos++;
	}
	return OPDI_STATUS_OK;
}

#endif		// OPDI_EXTENDED_PROTOCOL

/** Implements the basic protocol message handler.
*/
static uint8_t basic_protocol_message(channel_t channel) {
	uint8_t result;
	opdi_Port *port;

	// we can be sure to have no control channel messages here
	// so we don't have to handle Disconnect etc.

	if (0 == strcmp(opdi_msg_parts[0], OPDI_getDeviceCaps)) {
		// get device capabilities
		return send_device_caps(channel);
	}
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getPortInfo)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		result = send_port_info(channel, port);
		return result;
	} 
#ifndef OPDI_NO_ANALOG_PORTS
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getAnalogPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		result = send_analog_port_state(channel, port);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setAnalogPortValue)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// set the value
		result = set_analog_port_value(channel, port, opdi_msg_parts[2]);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setAnalogPortMode)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// set the value
		result = set_analog_port_mode(channel, port, opdi_msg_parts[2]);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setAnalogPortResolution)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// set the value
		result = set_analog_port_resolution(channel, port, opdi_msg_parts[2]);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setAnalogPortReference)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// set the value
		result = set_analog_port_reference(channel, port, opdi_msg_parts[2]);
		return result;
	} 
#endif
#ifndef OPDI_NO_DIGITAL_PORTS
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getDigitalPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		result = send_digital_port_state(channel, port);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setDigitalPortLine)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		result = set_digital_port_line(channel, port, opdi_msg_parts[2]);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setDigitalPortMode)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;

		result = set_digital_port_mode(channel, port, opdi_msg_parts[2]);
		return result;
	}
#endif
#ifndef OPDI_NO_SELECT_PORTS
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getSelectPortLabel)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		result = send_select_port_label(channel, port, opdi_msg_parts[2]);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getSelectPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		result = send_select_port_state(channel, port);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setSelectPortPosition)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		result = set_select_port_position(channel, port, opdi_msg_parts[2]);
		return result;
	} 
#endif
#ifndef OPDI_NO_DIAL_PORTS
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getDialPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		result = send_dial_port_state(channel, port);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setDialPortPosition)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		result = set_dial_port_position(channel, port, opdi_msg_parts[2]);
		return result;
	} 
#endif
#ifdef OPDI_USE_CUSTOM_PORTS
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getCustomPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		result = send_custom_port_state(channel, port);
		return result;
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_setCustomPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		result = set_custom_port_value(channel, port, opdi_msg_parts[2]);
		return result;
	} 
#endif
#if (OPDI_STREAMING_PORTS > 0)
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_bindStreamingPort)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		if (opdi_msg_parts[2] == NULL)
			return OPDI_PROTOCOL_ERROR;
		result = bind_streaming_port(channel, port, opdi_msg_parts[2]);
		return result;
	}
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_unbindStreamingPort)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		result = unbind_streaming_port(channel, port);
		return result;
	}
#endif
	else {
		// unknown message received
		return OPDI_MESSAGE_UNKNOWN;
	}
}

#ifdef OPDI_EXTENDED_PROTOCOL
/** Implements the extended protocol message handler.
*/
static uint8_t extended_protocol_message(channel_t channel) {
	uint8_t result;
	opdi_Port *port;
	opdi_PortGroup *group;
	char buffer[OPDI_EXTENDED_INFO_LENGTH];
	// only handle messages of the extended protocol here
	if (0 == strcmp(opdi_msg_parts[0], OPDI_getAllPortInfos)) {
		return send_all_port_infos(channel);
	} 
	else 
	if (0 == strcmp(opdi_msg_parts[0], OPDI_getAllPortStates)) {
		return send_all_port_states(channel);
	} 
	else 
	if (0 == strcmp(opdi_msg_parts[0], OPDI_getExtendedPortInfo)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// copy port ID to the buffer
		strncpy(buffer, opdi_msg_parts[1], OPDI_EXTENDED_INFO_LENGTH);
		result = opdi_slave_callback(OPDI_FUNCTION_GET_EXTENDED_PORTINFO, buffer, OPDI_EXTENDED_INFO_LENGTH);
		if (result != OPDI_STATUS_OK)
			return result;
		return send_extended_port_info(channel, opdi_msg_parts[1], buffer);
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getExtendedPortState)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// copy port ID to the buffer
		strncpy(buffer, opdi_msg_parts[1], OPDI_EXTENDED_INFO_LENGTH);
		result = opdi_slave_callback(OPDI_FUNCTION_GET_EXTENDED_PORTSTATE, buffer, OPDI_EXTENDED_INFO_LENGTH);
		if (result != OPDI_STATUS_OK)
			return result;
		return send_extended_port_state(channel, opdi_msg_parts[1], buffer);
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getGroupInfo)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find group
		group = opdi_find_portgroup_by_id(opdi_msg_parts[1]);
		if (group == NULL)
			return OPDI_GROUP_UNKNOWN;
		return send_group_info(channel, group);
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getExtendedGroupInfo)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find group
		group = opdi_find_portgroup_by_id(opdi_msg_parts[1]);
		if (group == NULL)
			return OPDI_GROUP_UNKNOWN;
		return send_extended_group_info(channel, group);
	} 
	else if (0 == strcmp(opdi_msg_parts[0], OPDI_getExtendedDeviceInfo)) {
		result = opdi_slave_callback(OPDI_FUNCTION_GET_EXTENDED_DEVICEINFO, buffer, OPDI_EXTENDED_INFO_LENGTH);
		if (result != OPDI_STATUS_OK)
			return result;
		return send_extended_device_info(channel, buffer);
	} 
	else
	// only handle messages of the extended protocol here
	if (0 == strcmp(opdi_msg_parts[0], OPDI_getAllSelectPortLabels)) {
		if (opdi_msg_parts[1] == NULL)
			return OPDI_PROTOCOL_ERROR;
		// find port
		port = opdi_find_port_by_id(opdi_msg_parts[1]);
		if (port == NULL)
			return OPDI_PORT_UNKNOWN;
		return send_all_select_port_labels(channel, port);
	} 
	else 
		// for all other messages, fall back to the basic protocol
		return basic_protocol_message(channel);
}
#endif

/** The message handler for the basic protocol.
*/
/*
uint8_t opdi_handle_basic_message(opdi_Message *m) {
	uint8_t result;

#if (OPDI_STREAMING_PORTS > 0)
	// try to dispatch message to streaming ports
	result = opdi_try_dispatch_stream(m);
	if (result != OPDI_NO_BINDING)
		return result;

	// there is no binding for the message's channel
#endif

	result = strings_split(m->payload, OPDI_PARTS_SEPARATOR, opdi_msg_parts, OPDI_MAX_MESSAGE_PARTS, 1, NULL);
	if (result != OPDI_STATUS_OK)
		return result;

	// let the protocol handle the message
	result = basic_protocol_message(m->channel);
	if (result != OPDI_STATUS_OK) {
		// special case: slave function wants to disconnect
		if (result == OPDI_DISCONNECTED)
			return opdi_disconnect();
		// error case
		return send_error(result, NULL, NULL);
	}

	return OPDI_STATUS_OK;
} 
*/

static uint8_t handle_message_result(opdi_Message *m, uint8_t result) {
	if (result != OPDI_STATUS_OK) {
		// special case: message unknown
		if (result == OPDI_MESSAGE_UNKNOWN) {
			opdi_debug_msg("Unknown message", OPDI_DIR_DEBUG);
			result = OPDI_STATUS_OK;
		} else
		// intentional disconnects are not an error
		if (result != OPDI_DISCONNECTED)
			// an error occurred during message handling; send error to device and exit message processing
			return send_error(result, NULL, NULL);
	}
	return result;
}

/** The protocol message loop.
*/
static uint8_t message_loop(opdi_ProtocolHandler protocolHandler) {
	opdi_Message m;
	uint8_t result;

	// enter processing loop
	while (1) {
		// because this call is blocking, it will result
		// in a timeout error if no ping message is coming in any more
		// causing the termination of the connection
		result = opdi_get_message(&m, OPDI_CAN_SEND);
		if (result != OPDI_STATUS_OK)
			return result;

		result = strings_split(m.payload, OPDI_PARTS_SEPARATOR, opdi_msg_parts, OPDI_MAX_MESSAGE_PARTS, 1, NULL);
		if (result != OPDI_STATUS_OK)
			return result;

		// message on control channel?
		if (m.channel == 0) {
			// disconnect message?
			if (0 == strcmp(opdi_msg_parts[0], OPDI_Disconnect))
				return OPDI_DISCONNECTED;

			// error message?
			if (0 == strcmp(opdi_msg_parts[0], OPDI_Error))
				return OPDI_DEVICE_ERROR;

			// debug message?
			if (0 == strcmp(opdi_msg_parts[0], OPDI_Debug)) {
				result = opdi_debug_msg(opdi_msg_parts[1], OPDI_DIR_DEBUG);
				if (result != OPDI_STATUS_OK)
					return result;
			}
		} else {
			// clear port info message
			opdi_set_port_message("");

			// message other than control message received
			// let the protocol handle the message
			result = protocolHandler(m.channel);
			result = handle_message_result(&m, result);
			if (result != OPDI_STATUS_OK)
				return result;
		}

#ifdef OPDI_HAS_MESSAGE_HANDLED
		// notify the device that a message has been handled
		result = opdi_message_handled(m.channel, opdi_msg_parts);
		if (result != OPDI_STATUS_OK) {
			// intentional disconnects are not an error
			if (result != OPDI_DISCONNECTED)
				// an error occurred during message handling; send error to device and exit
				return send_error(result, NULL, NULL);
			return result;
		}
#endif
	}	// while
}

/** Prepares the OPDI protocol implementation for a new connection.
*   Most importantly, this will disable encryption as it is not used at the beginning
*   of the handshake.
*/
uint8_t opdi_slave_init() {
#ifndef OPDI_NO_ENCRYPTION
	// handshake starts unencrypted
	opdi_set_encryption(OPDI_DONT_USE_ENCRYPTION);
#endif

	return OPDI_STATUS_OK;
}

/* Performs the handshake and runs the message processing loop if successful.
*  Errors during the handshake are not sent to the connected device.
*/
uint8_t opdi_slave_start(opdi_Message *message, opdi_GetProtocol get_protocol, opdi_ProtocolCallback protocol_callback) {
#define MAX_ENCRYPTIONS		3
	opdi_Message m;
	uint8_t result;
	uint8_t partCount;
	int32_t flags;
	char buf[BUFSIZE_32BIT];
#ifndef OPDI_FUNCTION_BUFFERSIZE
#define OPDI_FUNCTION_BUFFERSIZE	32
#endif
	char funcBuf1[OPDI_FUNCTION_BUFFERSIZE];
	char funcBuf2[OPDI_FUNCTION_BUFFERSIZE];
	opdi_ProtocolHandler protocol_handler = &basic_protocol_message;

#ifndef OPDI_NO_ENCRYPTION
	const char *encryptions[MAX_ENCRYPTIONS];
	uint8_t i;
	const char *encryption = "";
	uint8_t use_encryption = 0;
#endif
#ifndef OPDI_NO_AUTHENTICATION
	uint32_t savedTimeout;
#endif

#if (OPDI_STREAMING_PORTS > 0)
	// initiate a new connection: clear port bindings
	opdi_reset_bindings();
#endif

	connected = 0;

	if (protocol_callback != NULL)
		protocol_callback(OPDI_PROTOCOL_START_HANDSHAKE);

	////////////////////////////////////////////////////////////
	///// Received: Handshake
	////////////////////////////////////////////////////////////

	// expect a message	on the control channel
	if (message->channel != 0)
		return OPDI_PROTOCOL_ERROR;

	result = strings_split(message->payload, OPDI_PARTS_SEPARATOR, opdi_msg_parts, OPDI_MAX_MESSAGE_PARTS, 1, &partCount);
	if (result != OPDI_STATUS_OK)
		return result;

	if (partCount != 4)
		return OPDI_PROTOCOL_ERROR;

	// handshake tag must match
	if (strcmp(opdi_msg_parts[0], OPDI_Handshake))
		return OPDI_PROTOCOL_ERROR;

	// protocol version must match
	if (strcmp(opdi_msg_parts[1], OPDI_Handshake_version))
		return OPDI_PROTOCOL_ERROR;

	// convert flags
	result = opdi_str_to_int32(opdi_msg_parts[2], &flags);
	if (result != OPDI_STATUS_OK)
		return result;

#ifdef OPDI_NO_ENCRYPTION
	// is encryption required by the master?
	if (flags & OPDI_FLAG_ENCRYPTION_REQUIRED) {
		// this device does not support encryption
		send_disagreement(OPDI_ENCRYPTION_NOT_SUPPORTED, 0, NULL, NULL);
		return OPDI_ENCRYPTION_NOT_SUPPORTED;
	}
#else
	// encryption is supported
	// split supported encryptions
	result = strings_split(opdi_msg_parts[3], ',', encryptions, MAX_ENCRYPTIONS, 1, &partCount);
	if (result != OPDI_STATUS_OK)
		return result;

	// is encryption required by the master?
	if ((flags & OPDI_FLAG_ENCRYPTION_REQUIRED) == OPDI_FLAG_ENCRYPTION_REQUIRED) {
		// does the device not allow or support encryption?
		if (((opdi_device_flags & OPDI_FLAG_ENCRYPTION_NOT_ALLOWED) == OPDI_FLAG_ENCRYPTION_NOT_ALLOWED) || (opdi_encryption_method[0] == '\0')) {
			send_disagreement(0, OPDI_ENCRYPTION_NOT_SUPPORTED, "Encryption not supported: ", "by device");
			return OPDI_ENCRYPTION_NOT_SUPPORTED;
		}

		// device's encryption must be supported
		result = OPDI_ENCRYPTION_NOT_SUPPORTED;
		for (i = 0; i < partCount; i++) {
			if (0 == strcmp(encryptions[i], opdi_encryption_method)) {
				result = OPDI_STATUS_OK;
				break;
			}
		}
		if (result != OPDI_STATUS_OK) {
			send_disagreement(0, result, "Encryption not supported: ", opdi_encryption_method);
			return result;
		}
		// choose encryption
		encryption = opdi_encryption_method;
		use_encryption = 1;
	} 
	// does the device require encryption?
	else if ((opdi_device_flags & OPDI_FLAG_ENCRYPTION_REQUIRED) == OPDI_FLAG_ENCRYPTION_REQUIRED) {
		// does the master not allow encryption?
		if (flags & OPDI_FLAG_ENCRYPTION_NOT_ALLOWED) {
			send_disagreement(0, OPDI_ENCRYPTION_REQUIRED, "Encryption required: ", "by device");
			return OPDI_ENCRYPTION_REQUIRED;
		}

		// device's encryption must be supported
		result = OPDI_ENCRYPTION_REQUIRED;
		for (i = 0; i < partCount; i++) {
			if (0 == strcmp(encryptions[i], opdi_encryption_method)) {
				result = OPDI_STATUS_OK;
				break;
			}
		}
		if (result != OPDI_STATUS_OK) {
			send_disagreement(0, result, "Encryption required: ", opdi_encryption_method);
			return result;
		}
		// choose encryption
		encryption = opdi_encryption_method;
		use_encryption = 1;
	}
	else {
		// encryption is optional

		// not forbidden by device flags?
		if ((opdi_device_flags & OPDI_FLAG_ENCRYPTION_NOT_ALLOWED) != OPDI_FLAG_ENCRYPTION_NOT_ALLOWED) {
			// device's encryption may be supported
			for (i = 0; i < partCount; i++) {
				if (0 == strcmp(encryptions[i], opdi_encryption_method)) {
					// choose encryption
					encryption = opdi_encryption_method;
					use_encryption = 1;
					break;
				}
			}
		}
	}
#endif	// OPDI_NO_ENCRYPTION

	////////////////////////////////////////////////////////////
	///// Send: Handshake reply
	////////////////////////////////////////////////////////////

	// get encoding
	result = opdi_slave_callback(OPDI_FUNCTION_GET_ENCODING, funcBuf1, OPDI_FUNCTION_BUFFERSIZE);
	if (result != OPDI_STATUS_OK)
		return result;
	// get supported protocols
	result = opdi_slave_callback(OPDI_FUNCTION_GET_SUPPORTED_PROTOCOLS, funcBuf2, OPDI_FUNCTION_BUFFERSIZE);
	if (result != OPDI_STATUS_OK)
		return result;

	// prepare handshake reply message
	m.channel = 0;
	m.payload = opdi_msg_payload;
	opdi_msg_parts[0] = OPDI_Handshake;
	opdi_msg_parts[1] = OPDI_Handshake_version;
	opdi_msg_parts[2] = funcBuf1;
#ifndef OPDI_NO_ENCRYPTION
	opdi_msg_parts[3] = encryption;
#else
	opdi_msg_parts[3] = "";
#endif
	// convert flags to string
	opdi_int32_to_str(opdi_device_flags, buf);
	opdi_msg_parts[4] = buf;
	opdi_msg_parts[5] = funcBuf2;
	opdi_msg_parts[6] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	result = opdi_put_message(&m);
	if (result != OPDI_STATUS_OK)
		return result;

#ifndef OPDI_NO_ENCRYPTION
	// if encryption is used, switch it on
	if (use_encryption) {
		opdi_set_encryption(OPDI_USE_ENCRYPTION);
	}
#endif

	////////////////////////////////////////////////////////////
	///// Receive: Protocol Select
	////////////////////////////////////////////////////////////

	result = expect_control_message(opdi_msg_parts, &partCount);
	if (result != OPDI_STATUS_OK)
		return result;

	if (partCount != 3)
		return OPDI_PROTOCOL_ERROR;
		
#ifdef OPDI_EXTENDED_PROTOCOL
	// check extended protocol implementation
	if (0 == strcmp(opdi_msg_parts[0], OPDI_Extended_protocol_magic)) {
		protocol_handler = &extended_protocol_message;
	}
	else
#endif
	// check chosen protocol implementation
	if (0 != strcmp(opdi_msg_parts[0], OPDI_Basic_protocol_magic)) {
		// not the basic protocol, use device supplied function to determine protocol handler
		if (get_protocol == NULL)
			return OPDI_PROTOCOL_NOT_SUPPORTED;
		protocol_handler = get_protocol(opdi_msg_parts[0]);
		// protocol not registered
		if (protocol_handler == NULL)
			// fallback to basic
			protocol_handler = &basic_protocol_message;
	}

	// set master's name
	result = opdi_slave_callback(OPDI_FUNCTION_SET_MASTER_NAME, (char*)opdi_msg_parts[2], 0);
	if (result != OPDI_STATUS_OK)
		return result;

	// pass preferred languages, see opdi_device.h
	result = opdi_slave_callback(OPDI_FUNCTION_SET_LANGUAGES, (char*)opdi_msg_parts[1], 0);
	if (result != OPDI_STATUS_OK)
		return result;

	////////////////////////////////////////////////////////////
	///// Send: Slave Name
	////////////////////////////////////////////////////////////
		
	// get slave name
	result = opdi_slave_callback(OPDI_FUNCTION_GET_CONFIG_NAME, funcBuf1, OPDI_FUNCTION_BUFFERSIZE);
	if (result != OPDI_STATUS_OK)
		return result;

	m.channel = 0;
	m.payload = opdi_msg_payload;
	opdi_msg_parts[0] = OPDI_Agreement;
	opdi_msg_parts[1] = funcBuf1;
	opdi_msg_parts[2] = NULL;

	result = strings_join(opdi_msg_parts, OPDI_PARTS_SEPARATOR, opdi_msg_payload, OPDI_MESSAGE_PAYLOAD_LENGTH);
	if (result != OPDI_STATUS_OK)
		return result;

	result = opdi_put_message(&m);
	if (result != OPDI_STATUS_OK)
		return result;

#ifdef OPDI_NO_AUTHENTICATION
	// is authentication required by the master?
	if (flags & OPDI_FLAG_AUTHENTICATION_REQUIRED) {
		// this device does not support authentication
		send_disagreement(OPDI_AUTH_NOT_SUPPORTED, 0, NULL, NULL);
		return OPDI_AUTH_NOT_SUPPORTED;
	}
#else
	// does this device require authentication?
	if (opdi_device_flags & OPDI_FLAG_AUTHENTICATION_REQUIRED) {

		////////////////////////////////////////////////////////////
		///// Receive: Authentication
		////////////////////////////////////////////////////////////

		// increase the timeout for this procedure (the user may have to enter credentials first)
		savedTimeout = opdi_get_timeout();
		opdi_set_timeout(OPDI_AUTHENTICATION_TIMEOUT);
		result = opdi_get_message(&m, OPDI_CANNOT_SEND);
		// set the saved timeout back
		opdi_set_timeout(savedTimeout);
		if (result != OPDI_STATUS_OK)
			return result;

		result = strings_split(m.payload, OPDI_PARTS_SEPARATOR, opdi_msg_parts, OPDI_MAX_MESSAGE_PARTS, 0, NULL);	// no trim!
		if (result != OPDI_STATUS_OK)
			return result;

		if (0 != strcmp(opdi_msg_parts[0], OPDI_Auth)) {
			send_disagreement(0, OPDI_AUTHENTICATION_EXPECTED, NULL, NULL);
			return OPDI_AUTHENTICATION_EXPECTED;
		}

		// set user name
		result = opdi_slave_callback(OPDI_FUNCTION_SET_USERNAME, (char *)opdi_msg_parts[1], 0);
		if (result == OPDI_STATUS_OK)
			// set password
			result = opdi_slave_callback(OPDI_FUNCTION_SET_PASSWORD, (char *)opdi_msg_parts[2], 0);
		if (result != OPDI_STATUS_OK) {
			send_disagreement(0, OPDI_AUTHENTICATION_FAILED, "Authentication failed", NULL);
			return OPDI_AUTHENTICATION_FAILED;
		}

		result = send_agreement(0);
		if (result != OPDI_STATUS_OK)
			return result;
	}
#endif	// OPDI_NO_AUTHENTICATION

	connected = 1;

	if (protocol_callback != NULL)
		protocol_callback(OPDI_PROTOCOL_CONNECTED);

	// start the protocol
	result = message_loop(protocol_handler);

	if (protocol_callback != NULL)
		protocol_callback(OPDI_PROTOCOL_DISCONNECTED);

	connected = 0;

	return result;
}

uint8_t opdi_slave_connected(void) {
	return connected;
}

/** Sends a debug message to the master.
*/
uint8_t opdi_send_debug(const char *debugmsg) {
	opdi_msg_parts[0] = OPDI_Debug;
	opdi_msg_parts[1] = debugmsg;
	opdi_msg_parts[2] = NULL;

	// send the opdi_msg_parts on the control channel
	return send_parts(0);
}

/** Causes the Reconfigure message to be sent which prompts the master to re-read the device capabilities.
*/
uint8_t opdi_reconfigure(void) {
	// send a reconfigure message on the control channel
	opdi_Message message;
	uint8_t result;

	// send on the control channel
	message.channel = 0;
	message.payload = (char *)OPDI_Reconfigure;

	result = opdi_put_message(&message);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_STATUS_OK;
}

/** Causes the Refresh message to be sent for the specified ports. The last element must be NULL.
*   If the first element is NULL, sends the empty refresh message causing all ports to be
*   refreshed.
*/
uint8_t opdi_refresh(opdi_Port **ports) {
	opdi_Port *port = ports[0];
	uint8_t i = 1;

	// prepare the opdi_msg_parts
	opdi_msg_parts[0] = OPDI_Refresh;
	// iterate over all specified ports
	while (port != NULL) {
		opdi_msg_parts[i] = port->id;
		port = ports[i++];
		if (i >= OPDI_MAX_MESSAGE_PARTS)
			return OPDI_ERROR_PARTS_OVERFLOW;
	}
	opdi_msg_parts[i] = NULL;

	// send the opdi_msg_parts
	return send_parts(0);
}

/** Causes the Disconnect message to be sent to the master.
*   Returns OPDI_DISCONNECTED. After this, no more messages may be sent to the master.
*/
uint8_t opdi_disconnect(void) {
	// send a disconnect message on the control channel
	opdi_Message message;
	uint8_t result;

	// send on the control channel
	message.channel = 0;
	message.payload = (char *)OPDI_Disconnect;

	result = opdi_put_message(&message);
	if (result != OPDI_STATUS_OK)
		return result;

	return OPDI_DISCONNECTED;
}
