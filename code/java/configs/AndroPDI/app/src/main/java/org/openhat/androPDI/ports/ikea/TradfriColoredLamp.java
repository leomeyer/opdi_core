package org.openhat.androPDI.ports.ikea;

import android.graphics.Color;

import org.openhat.androPDI.ports.ColoredLampPort;
import org.openhat.androPDI.ports.ColoredLampViewAdapter;
import org.openhat.androPDI.ports.IPortViewAdapter;
import org.openhat.androPDI.ports.ShowDevicePorts;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;

public class TradfriColoredLamp extends ColoredLampPort  {

    class MyViewAdapter extends ColoredLampViewAdapter<TradfriColoredLamp> {

        MyViewAdapter(ShowDevicePorts showDevicePorts) {
            super(TradfriColoredLamp.this, showDevicePorts);
        }

        @Override
        protected void parseValue(String value) {
            if (value.contains("\"state\":\"ON\""))
                this.state = LampState.ON;
            else
            if (value.contains("\"state\":\"OFF\""))
                this.state = LampState.OFF;
            else
                this.state = LampState.UNKNOWN;
            if (color == -1)
                color = Color.DKGRAY;
        }

        @Override
        protected void switchOn() throws PortAccessDeniedException {
            try {
                port.setValue("{\"state\":\"ON\"}");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (DisconnectedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void switchOff() throws PortAccessDeniedException {
            try {
                port.setValue("{\"state\":\"OFF\"}");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (DisconnectedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void setColor(int newColor) {
            try {
                port.setValue("{\"color\": {\"r\":" + Color.red(newColor) + ", \"g\":" + Color.green(newColor) + ", \"b\":" + Color.blue(newColor) + "}}");
                color = newColor;
                updateState();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (DisconnectedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            } catch (PortAccessDeniedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void toggle() {
            try {
                port.setValue("{\"state\":\"TOGGLE\"}");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (DisconnectedException e) {
                e.printStackTrace();
            } catch (DeviceException e) {
                e.printStackTrace();
            } catch (PortAccessDeniedException e) {
                e.printStackTrace();
            }
        }
    }

    public TradfriColoredLamp(CustomPort port) {
        super(port);
    }

    public IPortViewAdapter getViewAdapter(ShowDevicePorts showDevicePorts) {
        return new MyViewAdapter(showDevicePorts);
    }
}
