// TcpClient.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

/*
int _tmain(int argc, _TCHAR* argv[])
{
	return 0;
}
*/

#include "InitSock.h"   
#include <stdio.h>   
#include <iostream> 
#include <thread>

using namespace std; 
CInitSock initSock;     // ��ʼ��W insock��   

void recMsg(SOCKET s)
{
	while (true)
	{
		char buff[256];
		//�ӷ������˽�������   
		int nRecv = ::recv(s, buff, 256, 0);
		if (nRecv > 0)
		{
			buff[nRecv] = '\0';
			printf("���յ����ݣ�%s\n", buff);
		}
	}
}

void sendMsg(SOCKET s)
{
	char szText[256];
	while (true)
	{
		// ��������˷�������   
		gets(szText);
		szText[255] = '\0';
		::send(s, szText, strlen(szText), 0);
	}
}

int client()
{
	// �����׽���   
	SOCKET s = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (s == INVALID_SOCKET)
	{
		printf(" Failed socket() \n");
		return 0;
	}

	// Ҳ�������������bind������һ�����ص�ַ   
	// ����ϵͳ�����Զ�����   

	// ��дԶ�̵�ַ��Ϣ   
	sockaddr_in servAddr;
	servAddr.sin_family = AF_INET;
	servAddr.sin_port = htons(4567);
	// ע�⣬����Ҫ��д����������TCPServer�������ڻ�����IP��ַ   
	// �����ļ����û��������ֱ��ʹ��127.0.0.1����   
	servAddr.sin_addr.S_un.S_addr = inet_addr("127.0.0.1");

	if (::connect(s, (sockaddr*)&servAddr, sizeof(servAddr)) == -1)
	{
		return 0;
	}

	while (true)
	{
		thread tr(recMsg, s);
		tr.detach();

		sendMsg(s);
	}

	// �ر��׽���   
	::closesocket(s);
	return 0;
}

int main()
{
	client();

	return 0;
}