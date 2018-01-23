#!/usr/bin/python
# -*-coding:utf-8-*-

import urllib.request

url = "http://blog.csdn.net/weiwei_pig/article/details/51178226"
headers = ("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.4 Safari/537.36")
opener =urllib.request.build_opener()
opener.addheaders = [headers]
data = opener.open(url).read()
fhandle = open("2.html", "wb")
fhandle.write(data)
fhandle.close()













