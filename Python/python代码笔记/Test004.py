#!/usr/bin/python
# -*-coding:utf-8-*-

import re

patten = "\w+([.+-]\w+)*@\w+([.-]\w+)*\.\w+([.-]\w+)*"
string = "<a href='316948714@qq.com.cn'>百度首页</a>"
result = re.search(patten, string)
print(result)


str = "电话010-63022282 传真010-63022282-63021751  <a target='_blank' href='//my.ctrip.com/uxp/Community/CommunityAdvice.aspx?producttype=3&categoryid=65'>纠错</a>"
pat = "<a(.+?)</a>"
res = re.search(pat, str)
print(res) 






