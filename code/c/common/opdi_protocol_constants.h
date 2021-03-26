//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

  
// Protocol constant definitions

// Not for inclusion by configs!

#define OPDI_PARTS_SEPARATOR			':'
#define OPDI_MULTIMESSAGE_SEPARATOR		'\r'

#define OPDI_Handshake 					"OPDI"
#define OPDI_Handshake_version 			"0.1"

#define OPDI_Basic_protocol_magic 		"BP"
#define OPDI_Extended_protocol_magic	"EP"

// control channel message identifiers
#define OPDI_Error 						"Err"
#define OPDI_Disconnect 				"Dis"
#define OPDI_Refresh 					"Ref"
#define OPDI_Reconfigure 				"Reconf"
#define OPDI_Debug 						"Debug"

#define OPDI_Agreement 					"OK"
#define OPDI_Disagreement 				"NOK"

#define OPDI_Auth 						"Auth"

// protocol message identifiers
#define OPDI_getDeviceCaps  			"gDC"
#define OPDI_getPortInfo  			"gPI"

#define OPDI_analogPort  			"AP"
#define OPDI_analogPortState  			"AS"
#define OPDI_getAnalogPortState  		"gAS"
#define OPDI_setAnalogPortValue  		"sAV"
#define OPDI_setAnalogPortMode  		"sAM"
#define OPDI_setAnalogPortResolution            "sAR"
#define OPDI_setAnalogPortReference		"sARF"

#define OPDI_digitalPort  			"DP"
#define OPDI_digitalPortState  			"DS"
#define OPDI_getDigitalPortState  		"gDS"
#define OPDI_setDigitalPortLine  		"sDL"
#define OPDI_setDigitalPortMode  		"sDM"

#define OPDI_selectPort  			"SLP"
#define OPDI_getSelectPortLabel  		"gSL"
#define OPDI_selectPortLabel  			"SL"
#define OPDI_selectPortState  			"SS"
#define OPDI_getSelectPortState  		"gSS"
#define OPDI_setSelectPortPosition		"sSP"

#define OPDI_dialPort  				"DL"
#define OPDI_dialPortState  			"DLS"
#define OPDI_getDialPortState  			"gDLS"
#define OPDI_setDialPortPosition  		"sDLP"

#define OPDI_customPort  			"CP"
#define OPDI_customPortState  			"CPS"
#define OPDI_getCustomPortState  		"gCPS"
#define OPDI_setCustomPortState  		"sCPS"

#define OPDI_streamingPort  			"SP"
#define OPDI_bindStreamingPort  		"bSP"
#define OPDI_unbindStreamingPort  		"uSP"

// extended protocol
#define OPDI_getAllPortInfos			"gAPI"

#define OPDI_getExtendedPortInfo		"gEPI"
#define OPDI_extendedPortInfo			"EPI"

#define OPDI_getExtendedPortState		"gEPS"
#define OPDI_extendedPortState			"EPS"

#define OPDI_getAllPortStates			"gAPS"

#define OPDI_getExtendedDeviceInfo		"gEDI"
#define OPDI_extendedDeviceInfo			"EDI"

#define OPDI_getGroupInfo				"gGI"
#define OPDI_groupInfo					"GI"
#define OPDI_getExtendedGroupInfo		"gEGI"
#define OPDI_extendedGroupInfo			"EGI"

#define OPDI_getAllSelectPortLabels		"gASL"

