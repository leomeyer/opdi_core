
export POCO_TARGET_OSARCH="i386"
export ARCHFLAGS="-arch i386"

# configure
./configure --config=Linux_i386 --no-samples --no-tests --minimal

# build
make -s
