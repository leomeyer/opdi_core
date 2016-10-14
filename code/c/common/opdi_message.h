//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

    
// OPDI high level IO functions (messaging subsystem)

#ifndef __OPDI_MESSAGE_H
#define __OPDI_MESSAGE_H

#include "opdi_platformtypes.h"
#include "opdi_configspecs.h"

#ifdef __cplusplus
extern "C" {
#endif 

/** Defines the function that is used to read bytes. timeout is specified in milliseconds.
*   If can_send is true an implementation may send its own streaming messages or messages on the control channel.
*   If can_send is false an implementation MUST NOT send any messages.
*   Implementations must provide a pointer to this function when calling opdi_message_setup.
*/
typedef uint8_t (*func_receive)(void *info, uint8_t *byte, uint16_t timeout, uint8_t can_send);

/** Defines the function that is used to write bytes.
*   Implementations must provide a pointer to this function when calling opdi_message_setup.
*/
typedef uint8_t (*func_send)(void *info, uint8_t *bytes, uint16_t count);

typedef struct opdi_Message {
	channel_t channel;
	char *payload;
} opdi_Message;

/** Setup the messaging subsystem. Supply handlers for sending and receiving of bytes.
*   recv is a pointer to a function that receives bytes.
*   snd is a pointer to a function that sends bytes.
*   info is a pointer to information required by the recv and snd functions.
*/
uint8_t opdi_message_setup(func_receive recv, func_send snd, void *info);

/** Puts the next received message in message.
*   If canSend is true a receive function may send its own messages during waiting for
*   a message. This will usually be the case if no protocol is currently being executed.
*   Returns a status code != OPDI_STATUS_OK in case of an error or disconnecting.
*/
uint8_t opdi_get_message(opdi_Message *message, uint8_t canSend);

/** Sends the message to the master.
*   Returns a status code != OPDI_STATUS_OK in case of an error or disconnecting.
*/
uint8_t opdi_put_message(opdi_Message *message);

#ifndef OPDI_NO_ENCRYPTION

/** Enable encryption. See device.h for encryption functions. */
uint8_t opdi_set_encryption(uint8_t enabled);

/** Specifies the block size of the encryption. Must be specified if encryption is used.
*/
extern const uint16_t opdi_encryption_blocksize;

/** Specifies the key information for the encryption. Must match the selected encryption method.
*/
extern char opdi_encryption_key[];

#ifdef _MSC_VER
	// MS Visual C compiler can't handle non-constant-length arrays on the stack
	// implementation must provide enough space
	extern uint8_t opdi_encryption_buffer[];
	extern uint8_t opdi_encryption_buffer_2[];
#endif

#endif

/** Set the current message timeout.
*/
void opdi_set_timeout(uint16_t timeout);

/** Get the current message timeout.
*/
uint16_t opdi_get_timeout(void);

#ifdef __cplusplus
}
#endif

#endif		// __OPDI_MESSAGE_H
