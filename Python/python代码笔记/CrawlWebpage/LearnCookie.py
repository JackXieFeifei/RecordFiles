#!/usr/bin/python
# -*-coding:utf-8-*-

import urllib.request
import urllib.parse
import http.cookiejar

url = "http://bbs.chinaunix.net/member.php?mod=logging&action=login&loginsubmit=yes&loginhash=LtGp6"
postdata = urllib.parse.urlencode({
    "username":"weisuen",
    "password":"aA123456"
}).encode('utf-8')
req = urllib.request.Request(url, postdata)
req.add_header('User-Agent', 'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.4 Safari/537.36')
# 使用 http.cookiejar.CookieJar() 创建 CookieJar 对象
cjar = http.cookiejar.CookieJar()
# 使用 HTTPCookieProcessor 创建 Cookie 处理器，并以其为参数构建 opener 对象
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cjar))
# 将 opener 安装为全局
urllib.request.install_opener(opener)
file = opener.open(req)
data = file.read()
file = open("cookie3.html", "wb")
file.write(data)
file.close()

url2 = "http://bbs.chinaunix.net/"
data2 = urllib.request.urlopen(url2).read()
fhandle = open("cookie4.html", "wb")
fhandle.write(data2)
fhandle.close()

