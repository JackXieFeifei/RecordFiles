#!/usr/bin/python
# -*-coding:utf-8-*-

import urllib.request

file = urllib.request.urlopen('http://www.baidu.com')

data = file.read()
dataline = file.readline()

# print(dataline)
# print(data)

fhandle = open('test001.html', 'wb')
fhandle.write(data)
fhandle.close()

print(file.info())
print(file.getcode())
print(file.geturl())

url_quote = urllib.request.quote("http://www.sina.com.cn")
print(url_quote)
url_unquote = urllib.request.unquote(url_quote)
print(url_unquote)



# filename = urllib.request.urlretrieve("http://edu.51cto.com", "test002.html")
# urllib.request.urlcleanup()

