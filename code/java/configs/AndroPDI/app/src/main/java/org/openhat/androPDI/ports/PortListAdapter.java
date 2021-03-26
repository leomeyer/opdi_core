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
class PortListAdapter extends ArrayAdapter<Port> {

	private final ShowDevicePorts showDevicePorts;
	private Context context;
    private List<Port> items;

    public PortListAdapter(ShowDevicePorts showDevicePorts, Context context, int textViewResourceId, List<Port> items) {
        super(context, textViewResourceId, items);
		this.showDevicePorts = showDevicePorts;
        this.context = context;
        this.items = items;
    }
    
    // ListView optimization support
    // Note: Dealing with proper view types (i. e. one view type per port type) yields strange problems.
    // Dial ports won't update their labels on converted views even though the updateState method should run.
    // This method disables the ListView optimization and uses one view per port. However, this should
    // not be a problem because we're not dealing with hundreds or thousands of ports, anyway.
    
    @Override
    public int getViewTypeCount() {
        // return PortType.values().length;
    	return items.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        // return items.get(position).getType().ordinal();
        return position;
    }

    // end of ListView optimization support
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View result = null;		// do not reuse convert views because their types probably don't match!
    	
    	IPortViewAdapter adapter = null;
    	if (result == null) { 		
	    	Port port = items.get(position);
	    	
	    	// does the port have a view adapter assigned?
	    	if (port.getViewAdapter() != null) {
	    		
	    		// re-use existing view adapter for performance
	    		adapter = (IPortViewAdapter)port.getViewAdapter();
	    		// assume it is already configured
	    		return adapter.getView(convertView);
	    	} else {
	    		
	    		// to display the port correctly, set its default unit
	    		// this will also influence the layout to use for the port
	    		port.setUnitFormat(Units.getDefaultFormat(context, port.getUnit()));
	    	}

	    	if (port instanceof DigitalPort) {
            	adapter = new DigitalPortViewAdapter(showDevicePorts);
            }
            else if (port instanceof AnalogPort) {
            	adapter = new AnalogPortViewAdapter(showDevicePorts);
            } 
            else if (port instanceof SelectPort) {
            	adapter = new SelectPortViewAdapter(showDevicePorts);
            } 
            else if (port instanceof DialPort) {
            	adapter = new DialPortViewAdapter(showDevicePorts);
            } 
            else if (port instanceof StreamingPort) {
            	adapter = new StreamingPortViewAdapter(showDevicePorts);
            }
            else if (port instanceof CustomViewPort) {
            	adapter = ((CustomViewPort)port).getViewAdapter(showDevicePorts);
			}
			else if (port instanceof CustomPort) {
				adapter = new CustomPortViewAdapter(showDevicePorts);
			}

            // register adapter with the port
            port.setViewAdapter(adapter);
            
            if (adapter != null) {
            	// configure the port adapter
	            adapter.configure(port, context);
	            
	            // get the adapter view
	            if (result == null) {
	                result = adapter.getView(convertView);
	            }
            }	            
            else {
            	// no matching adapter found - create a simple default view
	            if (result == null) {
	                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                result = vi.inflate(R.layout.default_port_row, null);
	            }
	            
	            TextView tt = result.findViewById(R.id.toptext);
	            TextView bt = result.findViewById(R.id.bottomtext);
	            if (tt != null) {
	            	tt.setText(port.getName());
	            }
	            if (bt != null){
	            	bt.setText(port.getType() + " " + port.getDirCaps());
	            }
            }
    	}
    	
        result.setBackgroundColor(Color.TRANSPARENT);            
        return result;
    }
    
    /** Clears all cached view adapters from the items list.
     * Must be done before the list is released.
     */
    public void invalidateViews() {
    	for (Port port: items) {
	    	port.setViewAdapter(null);
    	}
    }
}