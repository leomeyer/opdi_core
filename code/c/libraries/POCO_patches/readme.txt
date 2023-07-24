The POCO libraries are required to build the OPDI test configs WinOPDI, LinOPDI and RaspOPDI.
OpenHAT also uses this library within the opdi_core submodule.

The POCO libraries will automatically be cloned as a Git submodule into the POCO folder.
Alternatively, download the POCO C++ libraries from pocoproject.org and extract them into this folder, omitting the versioned top folder.

Copy the contents of this folder over to the POCO folder.

Clone the POCO crypto support into the POCO folder:
> git clone https://github.com/pocoproject/openssl

Build POCO to create the libraries as described below.

See the README file for advanced instructions.

------------------------------------------------------------------------------------------------------

On Windows:

To build from the command line, start a regular command prompt (not the "x86 Native Tools Command Prompt for Visual Studio 2019") and cd to the POCO directory.

Execute the following command (for Visual Studio 2019):
> buildwin 160 build static_mt debug Win32 nosamples notests

This command compiles for debug mode only (Windows is normally used for testing, not deployment).
Note that building shared libraries does not currently work due to an unresolved problem with libcryptod.lib.

The Windows configs should link the POCO libraries statically. This avoids the need to deploy the DLLs as well.
If you want to use the DLLs remove the POCO_STATIC compiler directive from the project settings and copy the DLLs to the resulting exe's folder.

In case you receive the following error:
"The windows SDK version 10.0.19041.0 was not found"
run the following command before calling buildwin:
"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvarsall.bat" x86 10.0.19041.0

------------------------------------------------------------------------------------------------------

On Linux, do the following:

Invoke
> ./build_poco.sh
to make and install the dynamic libraries.

Making and installing manually:
> ./configure --no-tests --no-samples --minimal
Then, compile POCO:
> make -s
For multiprocessor machines:
> make -s -j4
Install the libraries:
> sudo make -s install
Reload LD cache:
> sudo ldconfig

The Linux configs should link the POCO libraries dynamically. This avoids problems ("Using 'gethostbyname' in statically linked applications requires at runtime the shared libraries from the glibc version used for linking").

------------------------------------------------------------------------------------------------------

If you want to cross-compile for the Raspberry Pi, follow these steps:

Install a cross-compiler toolchain on your compile system and make sure that it works. For example: https://github.com/raspberrypi/tools
Note: Currently it is not recommended to use the popular crosstools-ng for this because of problems downloading library dependencies (eglibc.org SVN server is unreliable).

Invoke
> ./build_poco_rpi.sh
to create the static libraries for Raspberry Pi cross-compiling.

Cross-compiled Raspberry Pi binaries should link statically against POCO. This avoids the need to compile and install the POCO library binaries on the Raspberry.

Manual process:
 Create a build configuration for POCO (or use the provided config file):
> cd build/config
> cp ARM-Linux RaspberryPi
Edit the file RaspberryPi:
In general settings, set the LINKMODE variable to STATIC (to create .a files for static linking):
LINKMODE		?= STATIC
In general settings, remove the lines starting with "STLPORT" and "OPENSSL"
In general settings, change the tool (Note: this assumes that you are using the RaspberryPi cross compiler from Github mentioned above):
TOOL = arm-linux-gnueabihf 
In System Specific Flags, remove the following entries:
-I$(STLPORT_INCLUDE) -I$(OPENSSL_INCLUDE)
In System Specific Libraries, remove the following entries:
-L$(STLPORT_LIB) -L$(OPENSSL_LIB) -lstlport_arm-linux-gcc
Save and return to POCO root:
> cd ../..

3. Configure POCO to use the new build configuration:
> ./configure --config=RaspberryPi --no-samples --no-tests --minimal

4. Build:
> make -s

If you need to build POCO on Raspberry Pi, make sure that the libssl-dev library is installed:
> sudo apt-get install libssl-dev
> ./configure --no-tests --no-samples  --minimal
Then, compile POCO:
> make -s
Compiling can take quite a long time. Check for errors; install missing libraries if necessary.
Install the libraries:
> sudo make -s install
Reload LD cache:
> sudo ldconfig


