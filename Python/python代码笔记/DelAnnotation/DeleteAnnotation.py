#!/usr/bin/python
# -*-coding:utf-8-*-

import re
import os

class OperateFile():
    """ 对文件进行操作 """

    def __init__(self, fileName):
        self.fileName = fileName
        self.delAnnotation()

    def delAnnotation(self):
        """ 删除注释并写入临时文件 """
        print("delete annotation start !!!")
        fr = open(self.fileName, 'r', encoding='utf-8')
        fw = open('temp.json', 'w+', encoding='utf-8')
        pattern = '/\*(.*?)\*/'
        for line in fr.readlines():
            string = re.sub(pattern, '', line)
            fw.write(string)
        fr.close()
        fw.close()
        self.rewriteToOriginalFile('temp.json', self.fileName)

    def rewriteToOriginalFile(self, fileFrom, fileTo):
        """ 将临时文件内容复制到源文件中，并删除临时文件 """
        data = ""
        with open(fileFrom, 'r', encoding='utf-8') as fr:
            data = fr.read()
        with open(fileTo, 'w', encoding='utf-8') as fw:
            fw.write(data)
        os.remove(fileFrom)     # 删除临时文件
        print("Operate successful !!!")

if __name__ == '__main__':
    OperateFile("manifest.json")