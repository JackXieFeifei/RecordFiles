#!/usr/bin/python
# -*-coding:utf-8-*-

import re
import requests
from bs4 import BeautifulSoup

from WriteData2Excel import WriteData2Excel as wd2e

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
        telephones = re.findall("电话\d{3}-\d{8}|电话\d{4}-\d{7,8}", hotel_tel)
        faxes = re.findall("传真\d{3}-\d{8}-\d{8}|传真\d{3}-\d{8}|传真\d{4}-\d{7,8}", hotel_tel)
        tel = ''
        fax = ''

        if len(telephones) == 1:
            tel = telephones[0]
        else:
            for index in range(len(telephones)):
                if index == len(telephones)-1:
                    tel += telephones[index]
                else:
                    tel += telephones[index] + '/'
        hotel_list[index]["tel"] = tel
        # print (hotel_list[index]["tel"])

        if len(faxes) == 1:
            fax = faxes[0]
        else:
            for index in range(len(faxes)):
                if index == len(faxes)-1:
                    fax += faxes[index]
                else:
                    fax += faxes[index] + '/'
        hotel_list[index]["fax"] = fax
        # print (hotel_list[index]["fax"])


for index in range(len(hotel_list)):
    """ 输出酒店信息 """
    print (hotel_list[index]["name"], hotel_list[index]["url"], hotel_list[index]["tel"], hotel_list[index]["fax"])

hotel_write_info = ['name', 'url', 'tel', 'fax']
wd2e(hotel_list, 'hotel.xls', 'hotels', hotel_write_info)

"电话010-63022282 传真010-63022282-63021751  <a target='_blank' href='//my.ctrip.com/uxp/Community/CommunityAdvice.aspx?producttype=3&categoryid=65'>纠错</a>"