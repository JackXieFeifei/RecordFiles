// TestDes.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "Des.h"
#include "XXOR.h"
#include <iostream>
#include "json/json.h"
using namespace std;


int _tmain(int argc, _TCHAR* argv[])
{
	cout << "This project for testing DES £¡£¡£¡" << endl;

	Des des;
	char pJiaMi[1024] = { 0 };
	char pIn[1024] = "Hello World";
	const std::string g_DesKey = "lizheng1202casiongame20140422_7094705";
	int count = 1024;
	des.Des_Go(pJiaMi, pIn, count, g_DesKey.c_str(), (int)g_DesKey.size(), ENCRYPT);
	cout << pJiaMi << endl;

	char pJieMi[1024] = { 0 };
	des.Des_Go(pJieMi, pJiaMi, count, g_DesKey.c_str(), (int)g_DesKey.size(), DECRYPT);
	cout << pJieMi << endl;
	
	XXOR xxor;
	string from = "{\"account\":18921,\"idfa\":\"1138DAE7-EF1C-42C6-8E5F-FCC9296DBE93\",\"openudid\":\"35226151ecb9695f2f5c1670729906d6a1d54dd7\",\"pwd\":\"133\"}\n";
	xxor.Compress(from, g_DesKey);
	//cout << from << endl;

	//string data = "[23,75,27,11,6,1,18,95,70,18,8,82,89,74,91,94,66,69,8,9,3,83,18,11,22,1,5,1,10,27,118,117,14,25,114,118,4,47,68,78,90,38,88,74,9,119,5,116,78,39,48,42,86,92,94,87,41,39,119,9,2,22,28,22,93,66,58,89,69,93,93,83,18,15,78,90,79,90,87,88,86,4,3,85,81,1,88,69,80,90,8,85,7,88,6,3,6,6,4,7,6,11,11,111,1,84,15,85,6,84,0,88,13,30,95,71,66,69,65,69,84,16,89,67,66,90,92,76,26,107]\n";
	string data = "[23,75,27,11,6,1,18,95,70,18,8,82,89,74,90,89,66,69,8,9,3,83,18,11,22,118,118,112,113,105,0,113,124,25,116,118,12,47,68,78,45,81,91,74,115,7,115,7,78,37,53,90,91,90,83,32,40,86,6,5,0,22,28,22,93,66,58,89,69,93,93,83,18,15,78,92,25,88,81,92,81,5,84,3,86,6,86,16,15,11,13,4,89,94,84,4,84,5,2,83,87,11,81,60,5,86,93,1,7,8,1,85,95,72,93,71,66,69,65,69,84,16,89,67,64,93,93,76,26,107]";
	Json::Value obj;
	if (JsonReader(data, obj))
	{
		int size = (int)obj.size();
		BYTE* cc = new BYTE[size];
		for (int i = 0; i < size; i++)
		{
			cc[i] = (int)obj[i].asInt();
		}
		data.clear();
		data.append((char*)cc, size);
		delete cc;
		data = xxor.Decompress((BYTE*)data.c_str(), data.size(), g_DesKey);
		cout << data << endl;
	}
	

	return 0;
}

