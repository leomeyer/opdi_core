// Copyright (c) 2016, Leo Meyer, leo@leomeyer.de
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


/** OPDI example program for Arduino
 * Requires serial communication between master and slave, preferably via Bluetooth.
 * Provides a few example ports.
 */

#if defined(ARDUINO) && ARDUINO >= 100
#include "Arduino.h"
#else
#include "WProgram.h"
#endif
//#include "memory.h"

#include <opdi_constants.h>

#include "ArduinOPDI.h"

// The ArduinOPDI class defines all the Arduino specific stuff like serial ports communication.
ArduinOPDI ArduinOpdi = ArduinOPDI();
// define global OPDI instance
// important: for polymorphism to work, assign own OPDI instance
OPDI* Opdi = &ArduinOpdi;

// Port definitions

// Digital port configurable as input and output (input with pullup)
OPDI_DigitalPortPin digPort1 = OPDI_DigitalPortPin("DP1", "Pin 12", OPDI_PORTDIRCAP_BIDI, OPDI_DIGITAL_PORT_HAS_PULLUP, 12);
// Digital port connected to LED (on most Arduino boards), output only
OPDI_DigitalPortPin digPort2 = OPDI_DigitalPortPin("DP2", "Pin 13", OPDI_PORTDIRCAP_OUTPUT, 0, 13);

// Analog input port
OPDI_AnalogPortPin anaPort1 = OPDI_AnalogPortPin("AP1", "Pin A0", OPDI_PORTDIRCAP_INPUT, OPDI_ANALOG_PORT_CAN_CHANGE_REF, A0);
// Analog output port (PWM)
OPDI_AnalogPortPin anaPort2 = OPDI_AnalogPortPin("AP2", "PWM 11", OPDI_PORTDIRCAP_OUTPUT, 0, 11);

uint8_t checkerror(uint8_t result) {
	if (result != OPDI_STATUS_OK) {
		digitalWrite(13, LOW);    // set the LED off
		delay(500);

		// flash error code on LED
		for (uint8_t i = 0; i < result; i++) {
			digitalWrite(13, HIGH);   // set the LED on
			delay(200);              // wait
			digitalWrite(13, LOW);    // set the LED off
			delay(200);              // wait
		}
		digitalWrite(13, LOW);    // set the LED off
		return 0;
	}
	return 1;
}

uint8_t setupDevice() {
	// initialize the digital pin as an output.
	// Pin 13 has an LED connected on most Arduino boards
	pinMode(13, OUTPUT);

	// start serial port at 9600 baud
	Serial.begin(9600);

	// initialize the OPDI system
	uint8_t result = ArduinOpdi.setup(20000, 0, &Serial, NULL);
	if (checkerror(result) == 0)
		return 0;

	// add the ports provided by this configuration
	Opdi->addPort(&digPort1);
	Opdi->addPort(&digPort2);
	Opdi->addPort(&anaPort1);
	Opdi->addPort(&anaPort2);

	return 1;
}

int main(void)
{
	init();

	uint8_t setupOK = setupDevice();

	for (;;)
		if (setupOK)
			loop();

	return 0;
}

/* This function can be called to perform regular housekeeping.
* Passing it to the Opdi->start() function ensures that it is called regularly.
* The function can send OPDI messages to the master or even disconnect a connection.
* It should always return OPDI_STATUS_OK in case everything is normal.
* If disconnected it should return OPDI_DISCONNECTED.
* Any value that is not OPDI_STATUS_OK will terminate an existing connection.
*/
uint8_t doWork() {
	// nothing to do - just an example
	return OPDI_STATUS_OK;
}

void loop() {

	// do housekeeping
	doWork();

	// character received on serial port? may be a connection attempt
	if (Serial.available() > 0) {

		// indicate connected status
		digitalWrite(13, HIGH);   // set the LED on

		// start the OPDI protocol, passing a pointer to the housekeeping function
		// this call blocks until the slave is disconnected
		uint8_t result = Opdi->start(&doWork);

		// no regular disconnect?
		if (result != OPDI_DISCONNECTED) {
			digitalWrite(13, LOW);    // set the LED off
			delay(500);

			// flash error code on LED
			for (uint8_t i = 0; i < result; i++) {
				digitalWrite(13, HIGH);   // set the LED on
				delay(200);              // wait
				digitalWrite(13, LOW);    // set the LED off
				delay(200);              // wait
			}
		}

		digitalWrite(13, LOW);    // set the LED off
	}
}

