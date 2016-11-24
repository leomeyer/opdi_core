# download
wget https://pocoproject.org/releases/poco-1.7.6/poco-1.7.6.tar.gz

# extract
tar xzf poco-1.7.6.tar.gz --strip-components=1

# configure
./configure --no-samples --no-tests --omit=CppUnit,CppUnit/WinTestRunner,Crypto,NetSSL_OpenSSL,Data,Data/SQLite,Data/ODBCData/MySQL,MongoDB,PageCompiler,PageCompiler/File2Page

# build
make -s

# install
sudo make -s install
