// Copyright (c) 2016, Leo Meyer, leo@leomeyer.de
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


// Implements device specific attributes and functions for the Linux OPDI slave implementation on a Raspberry Pi ("RaspOPDI").

// Define USE_GERTBOARD to test the Gertboard over OPDI.
// Program must be run as root. Gertboard configuration
// should be the one for the LED test program (see Gertboard manual).
// This demo uses only two of the Gertboard LEDs.
#define USE_GERTBOARD	1

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include <sys/param.h>
#include <sys/socket.h>
#include <errno.h>

#include "opdi_platformtypes.h"
#include "opdi_configspecs.h"
#include "opdi_constants.h"
#include "opdi_port.h"
#include "opdi_message.h"
#include "opdi_slave_protocol.h"
#include "slave.h"

#ifdef USE_GERTBOARD
#include "rpi/gertboard/gb_common.h"
#define GB_L1		(1<<25)
#define GB_L2		(1<<24)
#define GB_ALL_LEDS	(L1|L2)
#endif

#define OPDI_SLAVE_NAME_LENGTH 32
const char *opdi_config_name;

char opdi_master_name[OPDI_MASTER_NAME_LENGTH];
const char *opdi_encoding = "";
const char *opdi_encryption_method = "AES";
const char *opdi_supported_protocols = "BP";
const char *opdi_username = "admin";
const char *opdi_password = "admin";

uint16_t opdi_device_flags = OPDI_FLAG_AUTHENTICATION_REQUIRED;

uint16_t opdi_encryption_blocksize = OPDI_ENCRYPTION_BLOCKSIZE;
uint8_t opdi_encryption_buffer[OPDI_ENCRYPTION_BLOCKSIZE];
uint8_t opdi_encryption_buffer_2[OPDI_ENCRYPTION_BLOCKSIZE];
const char *opdi_encryption_key = "0123456789012345";

/// Ports

static struct opdi_Port digPort = { "DP1", "Digital Port" };
static struct opdi_Port anaPort = { "AP1", "Analog Port" };
static struct opdi_Port selectPort = { "SL1", "Switch" };
static struct opdi_Port dialPort = { "DL1", "Audio Volume" };
static struct opdi_DialPortInfo dialPortInfo = { 0, 100, 5 };
static struct opdi_Port streamPort1 = { "SP1", "Temp/Pressure" };
static struct opdi_StreamingPortInfo sp1Info = { "BMP085", OPDI_STREAMING_PORT_NORMAL };

#ifdef USE_GERTBOARD
static struct opdi_Port gertboardLED1 = { "GBLED1", "Gertboard LED 1" };
static struct opdi_Port gertboardLED2 = { "GBLED2", "Gertboard LED 2" };
#endif

static char digmode[] = "0";		// floating input
static char digline[] = "0";

static char anamode[] = "1";		// output
static char anares[] = "1";		// 9 bit
static char anaref[] = "0";
static int anavalue = 0;

static const char *selectLabels[] = {"Position A", "Position B", "Position C", NULL};
static uint16_t selectPos = 0;

static int dialvalue = 0;

static double temperature = 20.0;
static double pressure = 1000.0;

// global connection mode (TCP or COM)
#define MODE_TCP 1
#define MODE_SERIAL 2

static int connection_mode = 0;
static char first_com_byte = 0;

/** Helper function: Returns current time in milliseconds
*/
static unsigned long GetTickCount()
{
  struct timeval tv;
  if( gettimeofday(&tv, NULL) != 0 )
    return 0;

  return (tv.tv_sec * 1000) + (tv.tv_usec / 1000);
}

/** For TCP connections, receives a byte from the socket specified in info and places the result in byte.
*   For serial connections, reads a byte from the file handle specified in info and places the result in byte.
*   Blocks until data is available or the timeout expires. 
*   If an error occurs returns an error code != 0. 
*   If the connection has been gracefully closed, returns STATUS_DISCONNECTED.
*/
static uint8_t io_receive(void *info, uint8_t *byte, uint16_t timeout, uint8_t canSend) {
	char c;
	int result;
	opdi_Message m;
	long ticks = GetTickCount();
	long sendTicks = ticks;

	while (1) {
		// send a message every few ms if canSend
		// independent of connection mode
		if (GetTickCount() - sendTicks >= 830) {
			if (canSend) {
				sendTicks = GetTickCount();
				// channel assigned for streaming port?
				if (sp1Info.channel > 0) {

				}
			}
		}

		if (connection_mode == MODE_TCP) {

			int newsockfd = (long)info;

			// try to read data
			result = read(newsockfd, &c, 1);
			if (result < 0) {
				// timed out?
				if (errno == EAGAIN || errno == EWOULDBLOCK) {
					// possible timeout
					// "real" timeout condition
					if (GetTickCount() - ticks >= timeout)
						return OPDI_TIMEOUT;
				}
				else {
					// other error condition
					perror("ERROR reading from socket");
					return OPDI_NETWORK_ERROR;
				}
			}
			else
			// connection closed?
			if (result == 0)
				// dirty disconnect
				return OPDI_NETWORK_ERROR;
			else
				// a byte has been received
//				printf("%i", c);
				break;
		}
		else
		if (connection_mode == MODE_SERIAL) {
			int fd = (long)info;
			char inputData;
			int bytesRead;
			int err;

			// first byte of connection remembered?
			if (first_com_byte != 0) {
				c = first_com_byte;
				first_com_byte = 0;
				break;
			}

			if ((bytesRead = read(fd, &inputData, 1)) >= 0) {
				if (bytesRead == 1) {
					// a byte has been received
					c = inputData;
					break;
				}
				else {
					// ran into timeout
					// "real" timeout condition
					if (GetTickCount() - ticks >= timeout)
						return OPDI_TIMEOUT;
				}
			}
			else {
				// device error
				return OPDI_DEVICE_ERROR;
			}
		}
	}

	*byte = (uint8_t)c;

	return OPDI_STATUS_OK;
}

/** For TCP connections, sends count bytes to the socket specified in info.
*   For serial connections, writes count bytes to the file handle specified in info.
*   If an error occurs returns an error code != 0. */
static uint8_t io_send(void *info, uint8_t *bytes, uint16_t count) {
	char *c = (char *)bytes;

	if (connection_mode == MODE_TCP) {

		int newsockfd = (long)info;

		if (write(newsockfd, c, count) < 0) {
			printf("ERROR writing to socket");
			return OPDI_DEVICE_ERROR;
		}
	}
	else
	if (connection_mode == MODE_SERIAL) {
		int fd = (long)info;

		if (write(fd, c, count) != count) {
			return OPDI_DEVICE_ERROR;
		}
	}

	return OPDI_STATUS_OK;
}


#ifdef __cplusplus
extern "C" {
#endif

// is called by the protocol
uint8_t opdi_choose_language(const char *languages) {
	if (!strcmp(languages, "de_DE")) {
		digPort.name = "Digitaler Port";
		anaPort.name = "Analoger Port";
		selectPort.name = "Wahlschalter";
	}

	return OPDI_STATUS_OK;
}

uint8_t opdi_get_analog_port_state(opdi_Port *port, char mode[], char res[], char ref[], int32_t *value) {
	if (!strcmp(port->id, anaPort.id)) {
		mode[0] = anamode[0];
		res[0] = anares[0];
		ref[0] = anaref[0];
		*value = anavalue;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_analog_port_value(opdi_Port *port, int32_t value) {
	if (!strcmp(port->id, anaPort.id)) {
		anavalue = value;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_get_digital_port_state(opdi_Port *port, char mode[], char line[]) {
	unsigned int b;
	if (!strcmp(port->id, digPort.id)) {
		mode[0] = digmode[0];
		line[0] = digline[0];
#ifdef USE_GERTBOARD
	} else
	if (!strcmp(port->id, gertboardLED1.id)) {
		b = GPIO_IN0;
		mode[0] = OPDI_DIGITAL_MODE_OUTPUT[0];
		// LED bit set?
		line[0] = (b & GB_L1) == GB_L1 ? '1' : '0';
	} else
	if (!strcmp(port->id, gertboardLED2.id)) {
		b = GPIO_IN0;
		mode[0] = OPDI_DIGITAL_MODE_OUTPUT[0];
		// LED bit set?
		line[0] = (b & GB_L2) == GB_L2 ? '1' : '0';
#endif	
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_analog_port_mode(opdi_Port *port, const char mode[]) {
	if (!strcmp(port->id, anaPort.id)) {
		anamode[0] = mode[0];
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_analog_port_resolution(opdi_Port *port, const char res[]) {
	if (!strcmp(port->id, anaPort.id)) {
		anares[0] = res[0];
		// TODO check value in range
		anavalue = 0;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_analog_port_reference(opdi_Port *port, const char ref[]) {

	if (!strcmp(port->id, anaPort.id)) {
		anaref[0] = ref[0];
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_digital_port_line(opdi_Port *port, const char line[]) {
	if (!strcmp(port->id, digPort.id)) {
		digline[0] = line[0];
#ifdef USE_GERTBOARD
	} else
	if (!strcmp(port->id, gertboardLED1.id)) {
		if (line[0] == '1') {
			GPIO_SET0 = GB_L1;
		} else {
			GPIO_CLR0 = GB_L1;
		}
	} else
	if (!strcmp(port->id, gertboardLED2.id)) {
		if (line[0] == '1') {
			GPIO_SET0 = GB_L2;
		} else {
			GPIO_CLR0 = GB_L2;
		}
#endif	
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_digital_port_mode(opdi_Port *port, const char mode[]) {
	if (!strcmp(port->id, digPort.id)) {
		digmode[0] = mode[0];
#ifdef USE_GERTBOARD
	} else
	if (!strcmp(port->id, gertboardLED1.id)) {
		// mode change not possible
	} else
	if (!strcmp(port->id, gertboardLED2.id)) {
		// mode change not possible
#endif	
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_get_select_port_state(opdi_Port *port, uint16_t *position) {
	if (!strcmp(port->id, selectPort.id)) {
		*position = selectPos;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_select_port_position(opdi_Port *port, uint16_t position) {
	if (!strcmp(port->id, selectPort.id)) {
		selectPos = position;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_get_dial_port_state(opdi_Port *port, int32_t *position) {
	if (!strcmp(port->id, dialPort.id)) {
		*position = dialvalue;
	} else
		// unknown port
		return OPDI_PORT_UNKNOWN;
		
	return OPDI_STATUS_OK;
}

uint8_t opdi_set_dial_port_position(opdi_Port *port, int32_t position) {
	return OPDI_STATUS_OK;
}

static void my_protocol_callback(uint8_t state) {
	if (state == OPDI_PROTOCOL_START_HANDSHAKE) {
		printf("Handshake started\n");
	} else
	if (state == OPDI_PROTOCOL_CONNECTED) {
		printf("Connected to: %s\n", opdi_master_name);
	} else
	if (state == OPDI_PROTOCOL_DISCONNECTED) {
		printf("Disconnected\n");
	}
}

void init_device() {
	// set slave name
	char nameBuf[1024];
	size_t compNameLen = 1024;

	if (opdi_config_name == NULL) {
		opdi_config_name = (const char*)malloc(OPDI_SLAVE_NAME_LENGTH);
	}

	// requires #undef UNICODE to work properly
	if (0 != gethostname(nameBuf, compNameLen)) {
		sprintf((char *)opdi_config_name, "RaspOPDI (%s)", nameBuf);
	}
	else {
		opdi_config_name = "RaspOPDI";
	}

#ifdef USE_GERTBOARD
	// prepare Gertboard IO
	setup_io();
	
	INP_GPIO(24);  OUT_GPIO(24);
	INP_GPIO(25);  OUT_GPIO(25);
#endif

	// configure ports
	if (opdi_get_ports() == NULL) {
		digPort.type = OPDI_PORTTYPE_DIGITAL;
		digPort.caps = OPDI_PORTDIRCAP_BIDI;
		digPort.info.i = (OPDI_DIGITAL_PORT_HAS_PULLUP | OPDI_DIGITAL_PORT_HAS_PULLDN);
		opdi_add_port(&digPort);

		anaPort.type = OPDI_PORTTYPE_ANALOG;
		anaPort.caps = OPDI_PORTDIRCAP_OUTPUT;
		anaPort.info.i = (OPDI_ANALOG_PORT_CAN_CHANGE_RES	
							   | OPDI_ANALOG_PORT_RESOLUTION_8	
							   | OPDI_ANALOG_PORT_RESOLUTION_9	
							   | OPDI_ANALOG_PORT_RESOLUTION_10	
							   | OPDI_ANALOG_PORT_RESOLUTION_11	
							   | OPDI_ANALOG_PORT_RESOLUTION_12	
							   | OPDI_ANALOG_PORT_CAN_CHANGE_REF	
							   | OPDI_ANALOG_PORT_REFERENCE_INT	
							   | OPDI_ANALOG_PORT_REFERENCE_EXT);
		opdi_add_port(&anaPort);

		selectPort.type = OPDI_PORTTYPE_SELECT;
		selectPort.caps = OPDI_PORTDIRCAP_OUTPUT;
		selectPort.info.ptr = (void*)selectLabels;	// position labels
		opdi_add_port(&selectPort);

		dialPort.type = OPDI_PORTTYPE_DIAL;
		dialPort.caps = OPDI_PORTDIRCAP_OUTPUT;
		dialPort.info.ptr = (void*)&dialPortInfo;
		opdi_add_port(&dialPort);

		streamPort1.type = OPDI_PORTTYPE_STREAMING;
		streamPort1.caps = OPDI_PORTDIRCAP_BIDI;
		streamPort1.info.ptr = (void*)&sp1Info;
//		opdi_add_port(&streamPort1);
		
#ifdef USE_GERTBOARD
		gertboardLED1.type = OPDI_PORTTYPE_DIGITAL;
		gertboardLED1.caps = OPDI_PORTDIRCAP_OUTPUT;
		gertboardLED1.info.i = 0;
		opdi_add_port(&gertboardLED1);
		
		gertboardLED2.type = OPDI_PORTTYPE_DIGITAL;
		gertboardLED2.caps = OPDI_PORTDIRCAP_OUTPUT;
		gertboardLED2.info.i = 0;
		opdi_add_port(&gertboardLED2);
#endif	
	}
}

/** This method handles an incoming TCP connection. It blocks until the connection is closed.
*/
int HandleTCPConnection(int csock) {
	opdi_Message message;
	uint8_t result;

	connection_mode = MODE_TCP;
	init_device();

	struct timeval aTimeout;
	aTimeout.tv_sec = 0;
	aTimeout.tv_usec = 1000;		// one ms timeout

	// set timeouts on socket
	if (setsockopt (csock, SOL_SOCKET, SO_RCVTIMEO, (char *)&aTimeout, sizeof(aTimeout)) < 0) {
		printf("setsockopt failed\n");
		return OPDI_DEVICE_ERROR;
	}
	if (setsockopt (csock, SOL_SOCKET, SO_SNDTIMEO, (char *)&aTimeout, sizeof(aTimeout)) < 0) {
		printf("setsockopt failed\n");
		return OPDI_DEVICE_ERROR;
	}

	// info value is the socket handle
	opdi_message_setup(&io_receive, &io_send, (void *)(long)csock);

	result = opdi_get_message(&message, OPDI_CANNOT_SEND);
	if (result != 0) 
		return result;

	// initiate handshake
	result = opdi_slave_start(&message, NULL, &my_protocol_callback);

	// release the socket
	return result;
}

/** This method handles an incoming serial connection. It blocks until the connection is closed.
*/
int HandleSerialConnection(char firstByte, int fd) {
	opdi_Message message;
	uint8_t result;

	connection_mode = MODE_SERIAL;
	first_com_byte = firstByte;
	init_device();

	// info value is the serial port handle
	opdi_message_setup(&io_receive, &io_send, (void *)(long)fd);

	result = opdi_get_message(&message, OPDI_CANNOT_SEND);
	if (result != 0) 
		return result;

	// initiate handshake
	result = opdi_slave_start(&message, NULL, &my_protocol_callback);

	return result;
}

uint8_t opdi_debug_msg(const uint8_t *str, uint8_t direction) {
	if (direction == OPDI_DIR_INCOMING)
		printf(">");
	else
		printf("<");
	printf("%s\n", str);
	return OPDI_STATUS_OK;
}

#ifdef __cplusplus
}
#endif 
