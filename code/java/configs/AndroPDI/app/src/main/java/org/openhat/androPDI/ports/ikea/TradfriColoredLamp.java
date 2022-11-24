package org.openhat.androPDI.ports.ikea;

import static java.lang.Math.pow;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradfriColoredLamp extends ColoredLampPort  {

    class MyViewAdapter extends ColoredLampViewAdapter {

        Pattern brightnessPattern = Pattern.compile("\"brightness\":([0-9]+)");
        Pattern colorPattern = Pattern.compile("\"color\":\\{\"x\":([0-9\\.]+),\"y\":([0-9\\.]+)\\}");

        MyViewAdapter(ShowDevicePorts showDevicePorts) {
            super(TradfriColoredLamp.this, showDevicePorts);
        }

        protected int getColorFromXY(float brightness, float valX, float valY) {
            float x = valX; // the given x value
            float y = valY; // the given y value
            float z = 1.0f - x - y;
            float Y = 0.5f; //(brightness / 254); // The given brightness value
            float X = (Y / y) * x;
            float Z = (Y / y) * z;
            float red = X * 1.656492f - Y * 0.354851f - Z * 0.255038f;
            float green = -X * 0.707196f + Y * 1.655397f + Z * 0.036152f;
            float blue = X * 0.051713f - Y * 0.121364f + Z * 1.011530f;

            //If red, green or blue is larger than 1.0 set it back to the maximum of 1.0
            if (red > blue && red > green && red > 1.0) {

                green = green / red;
                blue = blue / red;
                red = 1.0f;
            }
            else if (green > blue && green > red && green > 1.0) {

                red = red / green;
                blue = blue / green;
                green = 1.0f;
            }
            else if (blue > red && blue > green && blue > 1.0) {

                red = red / blue;
                green = green / blue;
                blue = 1.0f;
            }

            // Apply reverse gamma correction
            red = red <= 0.0031308f ? 12.92f * red : (1.0f + 0.055f) * (float)pow(red, (1.0f / 2.4f)) - 0.055f;
            green = green <= 0.0031308f ? 12.92f * green : (1.0f + 0.055f) * (float)pow(green, (1.0f / 2.4f)) - 0.055f;
            blue = blue <= 0.0031308f ? 12.92f * blue : (1.0f + 0.055f) * (float)pow(blue, (1.0f / 2.4f)) - 0.055f;

            int r = (int)(red * 255);
            int g = (int)(green * 255);
            int b = (int)(blue * 255);
            return 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);
        }

        @Override
        protected void parseValue(String value) {
            // parse state
            if (value.contains("\"state\":\"ON\""))
                this.state = LampState.ON;
            else
            if (value.contains("\"state\":\"OFF\""))
                this.state = LampState.OFF;
            else
                this.state = LampState.UNKNOWN;
            // parse brightness
            Matcher matcher = brightnessPattern.matcher(value);
            if (matcher.find()) {
                try {
                    brightness = Integer.parseInt(matcher.group(1));
                } catch (Exception e) {
                    brightness = -1;
                }
            } else
                brightness = -1;

            matcher = colorPattern.matcher(value);
            if (brightness >= 0 && matcher.find()) {
                try {
                    float x = Float.parseFloat(matcher.group(1));
                    float y = Float.parseFloat(matcher.group(2));
                    color = getColorFromXY(brightness, x, y);
                } catch (Exception e) {
                    color = -1;
                }
            } else
                color = -1;

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
        protected void setBrightness(int brightness) throws PortAccessDeniedException {
            try {
                port.setValue("{\"brightness\":\"" + brightness + "\"}");
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
