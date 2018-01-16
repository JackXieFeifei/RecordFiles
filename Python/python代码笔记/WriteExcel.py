#!/usr/bin/python
# -*-coding:utf-8-*-

from xlwt import *

file = Workbook(encoding = 'utf-8')
table = file.add_sheet('sheet1')

data = {
    "1":["张三", 150, 120, 100], \
    "3":["李四", 90, "ha哈", 98], \
    "2":["赵六", 23, 66, 90] \
}

ldata = []

keyList = [key for key in data]
keyList.sort()

for index in keyList:
    t = [int(index)]
    for a in data[index]:
        t.append(a)
    ldata.append(t)

# [[1, '张三', 150, 120, 100], [2, '赵六', 23, 66, 90], [3, '李四', 90, 99, 98]]

for i, p in enumerate(ldata):
    for j, q in enumerate(p):
        print (i, j, q)
        table.write(i, j, q)
file.save('sheet1.xls')