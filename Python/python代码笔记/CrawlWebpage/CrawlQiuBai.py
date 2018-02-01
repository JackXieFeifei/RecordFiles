#!/usr/bin/python
# -*-coding:utf-8-*-

import re
import urllib.request

def crawlQiuBai(url, page):
    """ 爬取糗事百科 """
    # 模拟成浏览器
    headers = ('User-Agent', 'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3322.4 Safari/537.36')
    opener = urllib.request.build_opener()
    opener.addheaders = [headers]

    # 将 opener 安装为全局
    urllib.request.install_opener(opener)
    file = urllib.request.urlopen(url)
    data = file.read().decode("utf-8")

    # 构建对用户提取的正则表达式
    userpat = '<a href="/users/\d{0,12}/" target="_blank" onclick="_hmt.push([\'_trackEvent\',\'web-list-author-text\',\'chick\'])">(.*?)</a>'

    # 构建段子内容提取的正则表达式
    contentpat = '<div class="content">(.*?)</div>'

    # 寻找出所有用户
    userlist = re.compile(userpat, re.S).findall(data)
    print(userlist)

    # 寻找出所有段子
    contentlist = re.compile(contentpat, re.S).findall(data)
    print(contentlist)

    x = 1
    for content in contentlist:
        content = content.replace("\n", " ")
        name = "content" + str(x)
        # 通过exec()函数实现用字符串作为变量名并赋值
        exec(name+'=content')
        x+=1

    y = 1
    for user in userlist:
        name = "content" + str(y)
        print(" 用户 " + str(page) + str(y) + "是 ：" + user)
        print(" 内容是 : ")
        exec("print("+name+")")
        print("\n")
        y+=1



for i in range(1, 5):
    url = "http://www.qiushibaike.com/8hr/page/" + str(i)
    crawlQiuBai(url, i)

# http://www.qiushibaike.com/8hr/page/3/