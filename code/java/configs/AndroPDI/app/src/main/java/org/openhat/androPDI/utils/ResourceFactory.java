//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.utils;

import android.content.Context;


public class ResourceFactory extends org.openhat.opdi.utils.ResourceFactory {

	Context context;
	private static ResourceFactory instance;
	
	public ResourceFactory(Context context) {
		this.context = context;
		
		instance = this;
	}
	
	public static ResourceFactory getInstance() {
		return instance;
	}
	
	@Override
	public String getString(String id) {
		
		// get android resource string
		int intId = (id == null ? 0 : context.getResources().getIdentifier(id, "string", context.getPackageName()));
	    String value = intId == 0 ? "" : (String)context.getResources().getText(intId);

	    if (value == null || value.equals(""))
	    		return super.getString(id);
	    else
	    	return value;
	}

	public String getString(int id) {
		
	    String value = id == 0 ? "" : (String)context.getResources().getText(id);

	    return value;
	}
}
