#!/usr/bin/python
# -*-coding:utf-8-*-

import re
import urllib.request

def getlink(url):
    # 模拟成浏览器
    headers = ('User-Agent', 'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.4 Safari/537.36')
    opener = urllib.request.build_opener()
    opener.addheaders = [headers]

    # 将 opener 安装为全局
    urllib.request.install_opener(opener)
    file = urllib.request.urlopen(url)
    data = str(file.read())

    # 根据需求构建好链接表达式
    pat = '(https?://[^\s)";]+\.(\w|/)*)'
    link = re.compile(pat).findall(data)

    # 去除重复元素
    link = list(set(link))
    return link

url = "http://blog.csdn.net/"
linklist = getlink(url)
for link in linklist:
    print(link[0])


