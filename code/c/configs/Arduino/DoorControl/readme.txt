Arduino door control using OPDI and a keypad and an RTC clock

Copyright (c) 2016 Leo Meyer, leo@leomeyer.de

The door control can be connected via a Bluetooth module.
The OPDI login credentials as well as the master code for the key pad
are set in the file DoorControl_secrets.h.
The time and the "normal" code can be set via OPDI. This code will be
active in a specified time window only (e. g. from 8:00 to 18:00).
Last activation time is stored and can be read using OPDI.
When the normal code or the master code is entered a relay will be
activated for three seconds (door opener).

To build this example, do the following:

1. On Windows:

In the DoorControl folder, execute the script copy_library_files.bat.
This will copy the necessary OPDI files to the folder.
Unzip the libraries.zip file to your Arduino user library folder.
For most users this will be My Documents\Arduino\libraries.
Open DoorControl.ino with the Arduino IDE.

2. On Linux:

In the DoorControl folder, execute the script copy_library_files.sh.
This will copy the necessary OPDI files to the folder.
Unzip the libraries.zip file to your Arduino user library folder:
> unzip -x libraries.zip
This will create a local folder "libraries".
Build the sketch using
> make
See the Makefile for board options.
