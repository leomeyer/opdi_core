package org.openhat.androPDI.ports;

import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.protocol.PortAccessDeniedException;

public abstract class ColoredLampPort extends CustomViewPort {

    public ColoredLampPort(CustomPort port) {
        super(port);
    }
}
