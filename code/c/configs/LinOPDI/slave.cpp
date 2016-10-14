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


#include <fcntl.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "opdi_constants.h"
#include "slave.h"

// Listen to incoming TCP requests. Supply the port you want the server to listen on
int listen_tcp(int host_port) {
	// adapted from: http://www.linuxhowtos.org/C_C++/socket.htm

	int err = 0;

	int sockfd, newsockfd;
	socklen_t clilen;
	struct sockaddr_in serv_addr, cli_addr;

	// create socket
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		printf("ERROR opening socket\n");
		err = OPDI_DEVICE_ERROR;
		goto FINISH;
	}

	// prepare address
	bzero((char*) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(host_port);

	// bind to specified port
	if (bind(sockfd, (struct sockaddr*) &serv_addr, sizeof(serv_addr)) < 0) {
		printf("ERROR on binding\n");
		err = OPDI_DEVICE_ERROR;
		goto FINISH;
	}

	// listen for incoming connections
	// set maximum queue size to 5
	listen(sockfd, 5);

	while (true) {
        	printf("listening for a connection on port %d\n", host_port);
		clilen = sizeof(cli_addr);
		newsockfd = accept(sockfd, (struct sockaddr*)&cli_addr, &clilen);
		if (newsockfd < 0) {
			printf("ERROR on accept\n");
			err = OPDI_DEVICE_ERROR;
			goto FINISH;
		}

		printf("Connection attempt from %s\n", inet_ntoa(cli_addr.sin_addr));

		err = HandleTCPConnection(newsockfd);

		close(newsockfd);
		fprintf(stderr, "Result: %d\n", err);
        }

FINISH:
	if (sockfd > 0)
		close(sockfd);
	return err;
}


int listen_com(char* portName, int baudRate, int stopBits, int parity, int byteSize, int timeout) {

	// adapted from: http://stackoverflow.com/questions/6947413/how-to-open-read-and-write-from-serial-port-in-c

	struct termios tty;
	int err = 0;

	int fd = open(portName, O_RDWR | O_NOCTTY | O_SYNC);
	if (fd < 0)
	{
		printf("error %d opening %s: %s", errno, portName, strerror (errno));
		err = OPDI_DEVICE_ERROR;
		goto FINISH;
	}

        memset (&tty, 0, sizeof tty);
        if (tcgetattr(fd, &tty) != 0)
        {
                printf("error %d from tcgetattr", errno);
		err = OPDI_DEVICE_ERROR;
		goto FINISH;
        }

	if (baudRate != -1)
	{
		// TODO calculate call constant from baud rate
		cfsetospeed(&tty, B9600);
		cfsetispeed(&tty, B9600);
	}

        tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;     // 8-bit chars
        // disable IGNBRK for mismatched speed tests; otherwise receive break
        // as \000 chars
        tty.c_iflag &= ~IGNBRK;         // ignore break signal
        tty.c_lflag = 0;                // no signaling chars, no echo,
                                        // no canonical processing
        tty.c_oflag = 0;                // no remapping, no delays
        tty.c_cc[VMIN]  = (timeout < 1 ? 1 : 0);	// block if no timeout specified
        tty.c_cc[VTIME] = (timeout < 1 ? 0 : (timeout / 100));	// read timeout

        tty.c_iflag &= ~(IXON | IXOFF | IXANY); // shut off xon/xoff ctrl

        tty.c_cflag |= (CLOCAL | CREAD);// ignore modem controls,
                                        // enable reading
	if (parity == 0)
	        tty.c_cflag &= ~(PARENB | PARODD);      // shut off parity
	else
	if (parity == 1) {
	        tty.c_cflag |= PARENB | PARODD;		// parity odd
	} else
	if (parity == 2) {
	        tty.c_cflag &= ~(PARODD);		// parity even
		tty.c_cflag |= PARENB;
	}

	if (stopBits == 2) {
        	tty.c_cflag |= CSTOPB;
	} else 
	if (stopBits == 1) {
        	tty.c_cflag &= ~CSTOPB;
	}
	
	if (byteSize == 5) {
		tty.c_cflag &= ~CSIZE;
		tty.c_cflag |= CS5;
	} else
	if (byteSize == 6) {
		tty.c_cflag &= ~CSIZE;
		tty.c_cflag |= CS6;
	} else
	if (byteSize == 7) {
		tty.c_cflag &= ~CSIZE;
		tty.c_cflag |= CS7;
	} else
	if (byteSize == 8) {
		tty.c_cflag &= ~CSIZE;
		tty.c_cflag |= CS8;
	}

        tty.c_cflag &= ~CRTSCTS;

        if (tcsetattr (fd, TCSANOW, &tty) != 0)
        {
                printf("error %d from tcsetattr", errno);
		err = OPDI_DEVICE_ERROR;
		goto FINISH;
        }

	// wait for connections

	char inputData;
	int bytesRead;

	while (true) {
		printf("listening for a connection on serial port %s\n", portName);
        
		while (true) {
			// try to read a byte
			if ((bytesRead = read(fd, &inputData, 1)) >= 0) {
				if (bytesRead > 0) {
					printf("Connection attempt on serial port\n");

					err = HandleSerialConnection(inputData, fd);
			
					fprintf(stderr, "Result: %d\n", err);
				}
			}
			else {
				// timeouts are expected here
				// TODO
			}
		}
	}

FINISH:

	// cleanup
	if (fd > 0) {
		close(fd);
	}

	return err;
}
