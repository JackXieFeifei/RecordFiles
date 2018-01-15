#pragma once
#include <string>
using namespace std;

typedef unsigned char BYTE;

class XXOR
{
public:
	XXOR();
	~XXOR();

	int Compress(string& strContent, const string& key);
	int Compress(const BYTE* p, int nSize, const string& key);
	string Decompress(const BYTE* p, int nSize, const string& key);

	BYTE* GetBuffer();
	int GetBufferSize();
	int GetDataSize();

private:
	BYTE* mBuffer;
	int mBufferSize;
	int mDataSize;
};


