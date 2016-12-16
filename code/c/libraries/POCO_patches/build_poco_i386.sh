
export POCO_TARGET_OSARCH="i386"
export ARCHFLAGS="-arch i386"

# configure
./configure --config=Linux_i386 --no-samples --no-tests --omit=CppUnit,CppUnit/WinTestRunner,Crypto,NetSSL_OpenSSL,Data,Data/SQLite,Data/ODBCData/MySQL,MongoDB,PageCompiler,PageCompiler/File2Page

# build
make -s
