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


    
#ifndef _TIMER0_H___
#define _TIMER0_H___

/************************************************************************/
/*                                                                      */
/*                  Timer0 helper routines                   			*/
/*                  Supports a clock reference and interrupt chaining.	*/
/*                  This library is the basis for several other 		*/
/*					components such as controls, encoder, segdisp etc.	*/
/************************************************************************/
// Leo Meyer, 02/2010

#include <stdint.h>

// Specifies the maximum number of hooks that can be added.
// Keep the number low to conserve RAM. Each hook uses 6 bytes.
// The following modules may use one hook each:
// - rotary encoder
// - control buttons
// - segment display
// If you are using a module that requires a hook but there is no hook free,
// nothing will probably happen except that the module will not work.
#define MAX_HOOKS	3

// Timer and interrupt settings
// Add the settings of your controller here.
// Additional CPU specific settings must be inserted in the .c file.

#if defined (atmega8)
	#define TIMER0_VECTOR		TIMER0_OVF_vect
#elif defined (atmega168)
	#define TIMER0_VECTOR		TIMER0_COMPA_vect
#else
	#error "CPU type not recognized, use CDEFS=-D$(MCU) in the makefile"
#endif

// Interrupt hook function type
typedef void (*TIMER0_HOOK)(long ticks);

// Initializes the timer.
// Interrupts are not automatically enabled in this routine.
void timer0_init(void);

// Adds the given hook for the specified number of ticks
// (hook is called every <ticks> ms).
// The hook is a normal function of type TIMER0_HOOK,
// not an interrupt service routine or something.
// A value of NULL is invalid.
// If the timer0 hasn't been initialized yet, this is being done.
void timer0_add_hook(TIMER0_HOOK hook, uint16_t ticks);

// Returns the number of ticks since the start of the timer.
// This represents a fairly accurate 1ms clock.
long timer0_ticks(void);

// Busy waits until the ms have elapsed.
// If the timer has not been initialized before, this routine hangs
// indefinitely.
void timer0_delay(uint32_t ms);

#endif
