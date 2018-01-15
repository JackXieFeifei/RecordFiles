#include "stdafx.h"
#include "XXOR.h"

XXOR::XXOR()
{
	mDataSize = 0;
	mBufferSize = 1024;
	mBuffer = new BYTE[mBufferSize];
	memset(mBuffer, 0, mBufferSize);
}

XXOR::~XXOR()
{
	delete mBuffer;
}

int XXOR::Compress(string& strContent, const string& key)
{
	if (strContent.size() > mBufferSize)
	{
		mBufferSize = (int)strContent.size();
		delete mBuffer;
		mBuffer = new BYTE[mBufferSize];
		memset(mBuffer, 0, mBufferSize);
	}

	const BYTE* pKey = (BYTE*)key.c_str();
	int len = (int)key.size();

	const BYTE* pIn = (BYTE*)strContent.c_str();
	for (int i = 0; i < strContent.size(); i++)
	{
		mBuffer[i] = pIn[i] ^ pKey[i%len];
	}
	mDataSize = (int)strContent.size();
	return mDataSize;
}

int XXOR::Compress(const BYTE* p, int nSize, const string& key)
{
	if (nSize > mBufferSize)
	{
		delete mBuffer;
		mBufferSize = nSize;
		mBuffer = new BYTE[mBufferSize];
		memset(mBuffer, 0, mBufferSize);
	}
	const BYTE* pKey = (BYTE*)key.c_str();
	int len = (int)key.size();
	for (int i = 0; i < nSize; i++)
	{
		mBuffer[i] = p[i] ^ pKey[i%len];
	}
	mDataSize = nSize;
	return mDataSize;
}

string XXOR::Decompress(const BYTE* p, int nSize, const string& key)
{
	if (nSize > mBufferSize)
	{
		delete mBuffer;
		mBufferSize = nSize;
		mBuffer = new BYTE[mBufferSize];
		memset(mBuffer, 0, mBufferSize);
	}
	string outStr;
	const BYTE* pKey = (BYTE*)key.c_str();
	int len = (int)key.size();
	for (int i = 0; i < nSize; i++)
	{
		mBuffer[i] = p[i] ^ pKey[i%len];
	}
	outStr.append((char*)mBuffer, nSize);
	mDataSize = nSize;
	return outStr;
}

BYTE* XXOR::GetBuffer()
{
	if (mBuffer)
		return mBuffer;
	return NULL;
}

int XXOR::GetBufferSize()
{
	return mBufferSize;
}

int XXOR::GetDataSize()
{
	return mDataSize;
}