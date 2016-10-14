//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// Windows type definitions

#ifndef __OPDI_PLATFORMTYPES_H
#define __OPDI_PLATFORMTYPES_H

// basic types on Windows, conditionally dependent on compiler version
#if _MSC_VER >= 1900

#include <stdint.h>

#else

#define uint8_t		unsigned char
#define int8_t		signed char
#define uint16_t	unsigned __int16
#define int16_t		signed __int16
#define uint32_t	unsigned __int32
#define int32_t		signed __int32
#define uint64_t	unsigned __int64
#define int64_t		__int64

#endif

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


#endif		// __OPDI_PLATFORMTYPES_H