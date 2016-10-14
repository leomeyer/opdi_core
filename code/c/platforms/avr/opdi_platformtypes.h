//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// AVR 8 bit type definitions

#ifndef __PLATFORMTYPES_H
#define __PLATFORMTYPES_H

// use types from standard lib
#include <stdint.h>

// channel number data type bit length
// It's important to define it this way because the C preprocessor can't compare strings.
// Way down in the protocol implementation this information is needed again.
#define channel_bits	16

#if (channel_bits == 8)
	#define channel_t	uint8_t
#elif (channel_bits == 16)
	#define channel_t	uint16_t
#else
#error "Not implemented; please define channel number data type"
#endif


#endif		// __PLATFORMTYPES_H