#ifndef JSON_JSON_H_INCLUDED
#define JSON_JSON_H_INCLUDED

# include "autolink.h"
# include "value.h"
# include "reader.h"
# include "writer.h"
# include "features.h"
#include <string>

//class CJsonExpand  : public cocos2d::CCObject
//{
//public:
//    static CJsonExpand* sharedCJsonExpand();
    //
//    virtual ~CJsonExpand();
    //
    std::string JsonWriter(const Json::Value& v);
    //
    bool JsonReader(std::string& strData, Json::Value& v);
    //
    Json::Value ReadJsonFile(const std::string& name);
    //
    void WriteJsonFile(const std::string& name, Json::Value& v);
//protected:
//    CJsonExpand();
//};
#endif // JSON_JSON_H_INCLUDED
