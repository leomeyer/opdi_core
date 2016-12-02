#!/bin/bash

# test build script
# build all projects that can be built on Linux; abort on errors

setenv -e

pushd code/c/configs/Arduino/DoorControl/DoorControl
./copy_library_files.sh
popd

make -C code/c/configs/Arduino/DoorControl/DoorControl clean
make -C code/c/configs/Arduino/DoorControl/DoorControl

make -C code/c/configs/Chromashade clean
make -C code/c/configs/Chromashade

make -C code/c/configs/Poladisc clean
make -C code/c/configs/Poladisc

make -C code/c/configs/LinOPDI clean
make -C code/c/configs/LinOPDI CC=$CC
