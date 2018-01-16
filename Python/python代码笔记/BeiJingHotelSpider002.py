#!/usr/bin/python
# -*-coding:utf-8-*-

import requests
import re
from bs4 import BeautifulSoup
from xlwt import *

res = requests.get('http://hotels.ctrip.com/hotel/beijing1#ctm_ref=hod_hp_sb_lst')
res.encoding = 'utf-8'
# print(res.text)
soup = BeautifulSoup(res.text, 'html.parser')

hotel_list = []
for hotel in soup.select('.hotel_item_name'):
    """获取第一个网页显示酒店信息"""
    hotel_name = hotel.select('a')[0]['title']
    hotel_url = "http://hotels.ctrip.com" + hotel.select('a')[0]['href']
    hotel_list.append({'name':hotel_name, 'url':hotel_url})

for index in range(len(hotel_list)):
    """获取酒店的网址并添加到列表中"""
    res = requests.get(hotel_list[index]["url"])
    res.encoding = 'utf-8'
    soup = BeautifulSoup(res.text, 'html.parser')
    for content in soup.select('.htl_room_txt'):
        hotel_tel = content.select('span')[0]['data-real']
        # re.compile(r'^{\<a} {\<\/a\>}$', hotel_tel)
        hotel_list[index]["tel"] = hotel_tel
        # print hotel_tel

for index in range(len(hotel_list)):
    """ 输出酒店信息 """
    # print (hotel_list[index]["name"], hotel_list[index]["url"], hotel_list[index]["tel"])

dic_data = {}
ldata = []
""" 将列表装扮成字典 """
for index in range(len(hotel_list)):
    dic_data[index] = hotel_list[index]

print (dic_data)

""" 应该将爬到的数据进行输出 输出到Excel """
file_excel = Workbook(encoding = 'utf-8')
table_excel = file_excel.add_sheet('sheet02')

keyList = [key for key in dic_data]
keyList.sort()

for index in keyList:
    t = [int(index)]
    t.append(dic_data[index]['name'])
    t.append(dic_data[index]['url'])
    t.append(dic_data[index]['tel'])
    ldata.append(t)

print (ldata)
# [[1, '张三', 150, 120, 100], [2, '赵六', 23, 66, 90], [3, '李四', 90, 99, 98]]

for i, p in enumerate(ldata):
    for j, q in enumerate(p):
        print (i, j, q)
        table_excel.write(i, j, q)
file_excel.save('sheet02.xls')