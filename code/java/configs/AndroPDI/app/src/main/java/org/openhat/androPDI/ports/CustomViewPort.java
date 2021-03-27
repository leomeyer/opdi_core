package org.openhat.androPDI.ports;

import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.ports.Port;

public abstract class CustomViewPort extends CustomPort {

    protected CustomViewPort(Port other) {
        super(other);
    }

    public abstract IPortViewAdapter getViewAdapter(ShowDevicePorts showDevicePorts);
}
