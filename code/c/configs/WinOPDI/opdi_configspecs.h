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


// WinOPDI config specifications

#ifndef __OPDI_CONFIGSPECS_H
#define __OPDI_CONFIGSPECS_H

#include "opdi_platformtypes.h"

#ifdef __cplusplus
extern "C" {
#endif

// This config is a slave
#define OPDI_IS_SLAVE	1

// Sets the default encoding that is used by this config.
#define OPDI_ENCODING_DEFAULT		OPDI_ENCODING_UTF8

// Defines the maximum message length this config can receive.
// Consumes this amount of bytes in RAM.
#define OPDI_MESSAGE_BUFFER_SIZE		256

// Defines the maximum message string length this config can receive.
// Consumes this amount of bytes times sizeof(char) in RAM.
// As the channel identifier and the checksum of a message typically consume a
// maximum amount of nine bytes, this value may be OPDI_MESSAGE_BUFFER_SIZE - 9
// on systems with only single-byte character sets.
#define OPDI_MESSAGE_PAYLOAD_LENGTH	(OPDI_MESSAGE_BUFFER_SIZE - 9)

// maximum length of master's name this device will accept
#define OPDI_MASTER_NAME_LENGTH	32

// maximum permitted message parts
#define OPDI_MAX_MESSAGE_PARTS	16

// maximum possible ports on this device
#define OPDI_MAX_DEVICE_PORTS	32

// define to conserve RAM and ROM
//#define OPDI_NO_DIGITAL_PORTS

// define to conserve RAM and ROM
//#define OPDI_NO_ANALOG_PORTS

// define to conserve RAM and ROM
//#define OPDI_NO_SELECT_PORTS

// define to conserve RAM and ROM
//#define OPDI_NO_DIAL_PORTS

// Defines the number of possible streaming ports on this device.
// May be set to 0 to conserve memory.
#define OPDI_STREAMING_PORTS		2

// define to conserve memory
//#define OPDI_NO_ENCRYPTION

// define to conserve memory
//#define OPDI_NO_AUTHENTICATION

// this device supports the extended protocol
#define EXTENDED_PROTOCOL			1

/** Defines the block size of data for encryption. Depends on the encryption implementation.
*   Data is always sent to the encrypt_block function in blocks of this size. If necessary, it is
*   padded with random bytes. When receiving data, the receiving function waits until a full block has 
*   been received before passing it to the decrypt_block function.
*   A maximum of 2^^16 - 1 is supported.
*/
#define OPDI_ENCRYPTION_BLOCKSIZE	16

#define OPDI_HAS_MESSAGE_HANDLED

#define OPDI_MAX_PORT_INFO_MESSAGE	256

#ifdef __cplusplus
}
#endif


#endif		// __CONFIGSPECS_H