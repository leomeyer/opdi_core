//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

    
// Implements encryption functions using AES.

#include "opdi_constants.h"
#include "opdi_config.h"

#include "opdi_rijndael.h"

static CRijndael *rijndael = nullptr;

static CRijndael *get_rijndael() {
	if (rijndael != nullptr) {
		return rijndael;
	}
	if (strlen(opdi_encryption_key) != OPDI_ENCRYPTION_BLOCKSIZE)
		throw runtime_error("AES encryption key length does not match the block size");

	rijndael = new CRijndael();
	rijndael->MakeKey(opdi_encryption_key, "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0", OPDI_ENCRYPTION_BLOCKSIZE, OPDI_ENCRYPTION_BLOCKSIZE);
	return rijndael;
}


uint8_t opdi_encrypt_block(uint8_t* dest, const uint8_t* src) {
	try
	{
		CRijndael *oRijndael = get_rijndael();
		oRijndael->EncryptBlock((const char*)src, (char*)dest);
	} catch (exception&) {
		return OPDI_ENCRYPTION_ERROR;
	}

	return OPDI_STATUS_OK;
}


uint8_t opdi_decrypt_block(uint8_t* dest, const uint8_t* src) {
	try
	{
		CRijndael *oRijndael = get_rijndael();
		oRijndael->DecryptBlock((const char*)src, (char*)dest);
	} catch (exception&) {
		return OPDI_ENCRYPTION_ERROR;
	}

	return OPDI_STATUS_OK;
}
