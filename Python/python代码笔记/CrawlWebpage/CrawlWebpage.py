#!/usr/bin/python
# -*-coding:utf-8-*-

import re
import requests
from bs4 import BeautifulSoup

from WriteData2Excel import WriteData2Excel as wd2e

class CrawlWebpage:
    """ 爬取网页内容 """

    def __init__(self, url):
        self.url = url
        self.hotel_list = []

        self.get_hotel_url()

    def request_url(self, url):
        res = requests.get(url)
        res.encoding = 'utf-8'
        soup = BeautifulSoup(res.text, 'html.parser')
        return soup

    def get_hotel_url(self):
        """ 获取酒店名称和网址 """
        soup = self.request_url(self.url)
        for hotel in soup.select('.hotel_item_name'):
            hotel_name = hotel.select('a')[0]['title']
            hotel_url = "http://hotels.ctrip.com" + hotel.select('a')[0]['href']
            self.get_hotel_detail_info(hotel_name, hotel_url)

        hotel_write_info_list = ['name', 'url', 'tel', 'fax']
        self.write_info_2_excel('hotel.xls', 'hotels', hotel_write_info_list)

    def parse_string(self, patten_str, str):
        """ 解析字符串 """
        str_list = re.findall(patten_str, str)
        str = ''
        if len(str_list) == 1:
            str = str_list[0]
        else:
            for index in range(len(str_list)):
                if index == len(str_list) - 1:
                    str += str_list[index]
                else:
                    str += str_list[index] + '/'
        return str

    def get_hotel_detail_info(self, name, url):
        """ 获取酒店详细信息 """
        soup = self.request_url(url)
        for content in soup.select('.htl_room_txt'):
            hotel_tel = content.select('span')[0]['data-real']
            tel = self.parse_string("电话\d{3}-\d{8}|电话\d{4}-\d{7,8}", hotel_tel)
            fax = self.parse_string("传真\d{3}-\d{8}-\d{8}|传真\d{3}-\d{8}|传真\d{4}-\d{7,8}", hotel_tel)
            self.hotel_list.append({'name': name, 'url': url, 'tel':tel, 'fax':fax})

    def write_info_2_excel(self, excel_name, excel_sheet_name, write_info_list):
        """ 将数据写入Excel表格 """
        wd2e(self.hotel_list, excel_name, excel_sheet_name, write_info_list)

if __name__ == '__main__':
    url = "http://hotels.ctrip.com/hotel/beijing1#ctm_ref=hod_hp_sb_lst"
    cw = CrawlWebpage(url)


'''
"电话010-63022282 传真010-63022282-63021751  <a target='_blank' href='//my.ctrip.com/uxp/Community/CommunityAdvice.aspx?producttype=3&categoryid=65'>纠错</a>"
'''


