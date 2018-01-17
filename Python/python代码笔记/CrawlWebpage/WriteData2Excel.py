import re
from xlwt import *

class WriteData2Excel:
    """ 将数据吸入Excel文件中 """

    def __init__(self, data_list, excel_name, sheet_name, write_info_list):
        self.dic_data = {}
        self.list_list_data = []
        self.write_info_list = write_info_list
        self.excel_name = excel_name    # 文件名
        self.sheet_name = sheet_name    # Excel 中表单名

        self.decorate_dic(data_list)
        self.write_data_to_excel()

    def decorate_dic(self, data_list):
        for index in range(len(data_list)):
            self.dic_data[index] = data_list[index]
        print (self.dic_data)

    def write_data_to_excel(self):
        self.file_excel = Workbook(encoding='utf-8')
        self.table_excel = self.file_excel.add_sheet(self.sheet_name)
        self.sort_key()
        self.parse_data()
        self.save_data()

    def sort_key(self):
        self.keyList = [key for key in self.dic_data]
        self.keyList.sort()

    def parse_data(self):
        length = len(self.write_info_list)
        for index in self.keyList:
            t = [int(index)]
            for i in range(len(self.write_info_list)):
                t.append(self.dic_data[index][self.write_info_list[i]])
            self.list_list_data.append(t)

    def save_data(self):
        for i, p in enumerate(self.list_list_data):
            for j, q in enumerate(p):
                print(i, j, q)
                self.table_excel.write(i, j, q)
        self.file_excel.save(self.excel_name)