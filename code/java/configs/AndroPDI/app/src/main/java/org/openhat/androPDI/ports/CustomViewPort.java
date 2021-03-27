package org.openhat.androPDI.ports;

import org.openhat.opdi.ports.CustomPort;

public abstract class CustomViewPort extends CustomPort {

    public CustomViewPort(String id, String name) {
        super(id, name);
    }

    public abstract IPortViewAdapter getViewAdapter(ShowDevicePorts showDevicePorts);
}
