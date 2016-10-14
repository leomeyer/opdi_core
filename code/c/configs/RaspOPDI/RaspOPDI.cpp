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


// RaspOPDI.cpp : Defines the entry point for the console application
// 

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "server.h"

int main(int argc, char* argv[])
{
	int code = 0;
	int tcp_port = 13110;
	char* comPort = NULL;

	printf("RaspOPDI server. Arguments: [-tcp <port>] [-serial <device>]\n");

	for (int i = 1; i < argc; i++) {
            if (i < argc - 1) {
                if (strcmp(argv[i], "-tcp") == 0) {
					// parse tcp port number
					tcp_port = atoi(argv[++i]);
					if ((tcp_port < 1) || (tcp_port > 65535)) {
						printf("Invalid TCP port number: %d\n", tcp_port);
						exit(1);
					}
                } else if (strcmp(argv[i], "-serial") == 0) {
			comPort = argv[++i];
                } else {
					printf("Unrecognized argument: %s\n", argv[i]);
					exit(1);
				}
            }
			else {
				printf("Invalid syntax: missing argument\n");
				exit(1);
			}
        }

	if (comPort != NULL) {
		code = listen_com(comPort, -1, -1, -1, -1, 1000);
	}
	else {
		code = listen_tcp(tcp_port);
	}

	printf("Exit code: %d\n", code);
	exit(code);
}

