//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.units;

import android.content.Context;

import org.openhat.androPDI.utils.ResourceFactory;
import org.openhat.opdi.units.UnitFormat;
import org.openhat.opdi.utils.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Units {
	
	String name;
	
	static Map<String, List<UnitFormat>> unitFormatCache = new HashMap<String, List<UnitFormat>>(); 

	public static List<UnitFormat> loadUnits(Context context, String name) {
		// get string identifier
		int stringID = context.getResources().getIdentifier(name, "string", context.getPackageName());
		
		String unitDef = ResourceFactory.getInstance().getString(stringID);
		if (unitDef == name)
			throw new IllegalArgumentException("Units not or incorrectly defined for: " + name);

		Map<String, String> config = Strings.getProperties(unitDef);
		// build sorted list of specified formats
		TreeMap<String, String> sortedFormats = new TreeMap<String, String>();
		for (String key: config.keySet()) {
			sortedFormats.put(config.get(key), key);
		}
		
		// go through formats and build the list
		List<UnitFormat> formatList = new ArrayList<UnitFormat>();
		for (String key: sortedFormats.keySet()) {
			int formatID = context.getResources().getIdentifier(sortedFormats.get(key), "string", context.getPackageName());
			String formatDef = ResourceFactory.getInstance().getString(formatID);
			if (formatDef == key)
				throw new IllegalArgumentException("Unit format not or incorrectly defined: " + name);

			formatList.add(new UnitFormat(name, formatDef));
		}
		unitFormatCache.put(name, formatList);
		return formatList;
	}

	public static UnitFormat getDefaultFormat(Context context, String unit) {
		if (unit == null)
			return UnitFormat.DEFAULT;
		List<UnitFormat> formatList = null;
		if (unitFormatCache.containsKey(unit)) {
			formatList = unitFormatCache.get(unit);
		} else {
			// initialize format list from resources
			formatList = loadUnits(context, unit);
		}
		if (formatList.size() == 0)
			return UnitFormat.DEFAULT;
		return formatList.get(0);
	}
}
