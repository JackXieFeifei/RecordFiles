#include "stdafx.h"
#include "json.h"

/*
static CJsonExpand* gJsonExpand = NULL;

CJsonExpand* CJsonExpand::sharedCJsonExpand()
{
    if(NULL == gJsonExpand)
    {
        gJsonExpand = new CJsonExpand();
    }
    
    return gJsonExpand; 
}


CJsonExpand::CJsonExpand()
{
}

CJsonExpand::~CJsonExpand()
{
}
*/

std::string 
JsonWriter(const Json::Value& v)
{
	Json::FastWriter w;

	std::string result;

	try
	{
		result = w.write(v);
	}
	catch (...)
	{
		//TRACE0("JsonWriter ...error..");
	}


	return result;
}

//
bool 
JsonReader(std::string& strData, Json::Value& v)
{
	Json::Reader r;
	
	bool result = false;
	try
	{

		result = r.parse(strData, v);
	}
	catch (...)
	{
		//TRACE0("JsonWriter ...error..");
	}

	return result;
}


Json::Value 
ReadJsonFile(const std::string& name)
{
	std::string file = name + ".json";
	Json::Value v;

	FILE* fp = fopen(file.c_str(), "rt");

	if(fp != NULL) 
	{
		fseek(fp, 0, SEEK_END);
		int len = ftell(fp);
		fseek(fp, 0, SEEK_SET);

		char tmp[1024*10] = {0};

		fread(tmp, 1, len, fp);
		fclose(fp);

		std::string data = tmp;
		JsonReader(data, v);
	}

	return v;
}

void 
WriteJsonFile(const std::string& name, Json::Value& v)
{
	std::string file = name + ".json";

	FILE* fp = fopen(file.c_str(), "wt");
	if(fp != NULL)
	{	
		std::string data = JsonWriter(v);
		fwrite(data.c_str(), 1, data.size(), fp);
		fclose(fp);
	}
}