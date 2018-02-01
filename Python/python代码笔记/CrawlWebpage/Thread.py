#!/usr/bin/python
# -*-coding:utf-8-*-

import threading

class A(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)

    def run(self):
        for i in range(1000):
            print("This is thread A")


class B(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)

    def run(self):
        for i in range(1000):
            print("This is thread B")

t1 = A()
t1.start()

t2 = B()
t2.start()
