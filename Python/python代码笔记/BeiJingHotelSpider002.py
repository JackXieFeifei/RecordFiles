#!/usr/bin/python
# -*-coding:utf-8-*-

import requests
import re
from bs4 import BeautifulSoup

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
    """"输出酒店信息"""
    print hotel_list[index]["name"], hotel_list[index]["url"], hotel_list[index]["tel"], '\n'
