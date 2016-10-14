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


// Arduino configuration specifications

#ifndef __OPDI_CONFIGSPECS_H
#define __OPDI_CONFIGSPECS_H

#define OPDI_IS_SLAVE	1

#define OPDI_ENCODING_DEFAULT	OPDI_ENCODING_UTF8

// Defines the maximum message length this slave can receive.
// Consumes this amount of bytes in data and the same amount on the stack.
#define OPDI_MESSAGE_BUFFER_SIZE		48

// Defines the maximum message string length this slave can receive.
// Consumes this amount of bytes times sizeof(char) in data and the same amount on the stack.
// As the channel identifier and the checksum of a message typically consume a
// maximum amount of nine bytes, this value may be OPDI_MESSAGE_BUFFER_SIZE - 9
// on systems with only single-byte character sets.
#define OPDI_MESSAGE_PAYLOAD_LENGTH	(OPDI_MESSAGE_BUFFER_SIZE - 9)

// maximum permitted message parts
#define OPDI_MAX_MESSAGE_PARTS	12

// maximum length of master's name this device will accept
#define OPDI_MASTER_NAME_LENGTH	32

// maximum possible ports on this device
#define OPDI_MAX_DEVICE_PORTS	8

// define to conserve RAM and ROM
//#define OPDI_NO_DIGITAL_PORTS

// define to conserve RAM and ROM
//#define OPDI_NO_ANALOG_PORTS

// define to conserve RAM and ROM
#define OPDI_NO_SELECT_PORTS

// define to conserve RAM and ROM
#define OPDI_NO_DIAL_PORTS

// maximum number of streaming ports
// set to 0 to conserve both RAM and ROM
#define OPDI_STREAMING_PORTS		0

// Define to conserve memory
#define OPDI_NO_ENCRYPTION

// Define to conserve memory
#define OPDI_NO_AUTHENTICATION

#define OPDI_HAS_MESSAGE_HANDLED

// Defines for the OPDI implementation on Arduino

// keep these numbers as low as possible to conserve memory
#define MAX_PORTIDLENGTH		5
#define MAX_PORTNAMELENGTH		20
#define MAX_SLAVENAMELENGTH		16
#define MAX_ENCODINGNAMELENGTH	10

#define OPDI_MAX_PORT_INFO_MESSAGE	0

#endif		// __OPDI_CONFIGSPECS_H
