//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


// Common string functions

#ifndef __OPDI_STRINGS_H
#define __OPDI_STRINGS_H

#include "opdi_platformtypes.h"

#ifdef __cplusplus
extern "C" {
#endif 

/** Splits a null-terminated string at the given separator into substrings.
*   Substrings may contain the escaped separator (twice in a row) which is substituted
*   in-place. Returns the number of parts found in part_count if it is != NULL. 
*   This routine also trims the parts of whitespace if trim is != 0. 
*   In case of an error it returns a value != 0.
*/
uint8_t strings_split(const char *str, char separator, const char **parts, uint8_t max_parts, uint8_t trim, uint8_t *part_count);

/** Joins the null-terminated strings in the parts array by the separator until one entry in parts is NULL.
*   The output is written to dest. If the separator is contained in any of the strings it is escaped.
*   Empty parts are encoded as a single blank.
*   If max_length is exceeded a value != 0 is returned.
*/
uint8_t strings_join(const char **parts, char separator, char *dest, uint16_t max_length);

#ifdef __cplusplus
}
#endif

#endif		// __OPDI_STRINGS_H