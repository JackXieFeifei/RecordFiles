#!/usr/bin/python
# -*-coding:utf-8-*-

# 电话010-63901166  <a target='_blank' href='//my.ctrip.com/uxp/Community/CommunityAdvice.aspx?producttype=3&categoryid=65'>纠错</a>

import re



str = "电话010-67190666 传真010-67190388  <a target='_blank' href='//my.ctrip.com/uxp/Community/CommunityAdvice.aspx?producttype=3&categoryid=65'>纠错</a>"
links = re.findall("(电话\d{3}-\d{8}|\d{4}-\d{7,8})|(传真\d{3}-\d{8}|\d{4}-\d{7,8})", str)
print (links)
print (len(links))

for index in links:
    for i in index:
        if i != '':
            print (i)










