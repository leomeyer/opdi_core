//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import java.util.List;

import org.openhat.androPDI.R;
import org.openhat.androPDI.units.Units;
import org.openhat.opdi.ports.AnalogPort;
import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.ports.DialPort;
import org.openhat.opdi.ports.DigitalPort;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.ports.SelectPort;
import org.openhat.opdi.ports.StreamingPort;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** Adapts a list of devices for display in a ListView.
 * 
 * @author Leo
 *
 */
