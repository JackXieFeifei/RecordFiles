#!/usr/bin/python
# -*-coding:utf-8-*-

import urllib.request
import urllib.parse

"""
# GET 请求
url = "http://www.baidu.com/s?wd="
key = "凤凰传奇"
key_code = urllib.request.quote(key)
url_all = url + key_code
req = urllib.request.Request(url_all)
data = urllib.request.urlopen(req).read()
fh = open('test003.html', 'wb')
fh.write(data)
fh.close()
"""


# POST 请求
url = "http://www.iqianyue.com/mypost/"
postdata = urllib.parse.urlencode({
    "name:":"123",
    "pass":"456"
}).encode('utf-8')
req = urllib.request.Request(url, postdata)
req.add_header('User-Agent', 'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.4 Safari/537.36')
try:
    data=urllib.request.urlopen(req).read()
    fh=open('testPost.html', 'wb')
    fh.write(data)
    fh.close()
except urllib.error.URLError:
    print("URL Error")


def use_proxy(proxy_addr, url):
    proxy = urllib.request.ProxyHandler({'http':proxy_addr})
    opener = urllib.request.build_opener(proxy, urllib.request.HTTPHandler)
    urllib.request.install_opener(opener)
    data = urllib.request.urlopen(url).read()
    return data
proxy_addr = "118.180.40.195:8118"
data = use_proxy(proxy_addr, "http://www.baidu.com")
print(len(data))  

