// TcpServer.cpp : Defines the entry point for the console application.
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
CInitSock initSock;     // 初始化Winsock库


void recMsg(SOCKET s)
{
	while (true)
	{  
		char buff[256];
		int nRecv = ::recv(s, buff, 256, 0);
		if (nRecv > 0)
		{
			buff[nRecv] = '\0';
			printf(" 客户端：%s\n", buff);
		}
	}
}

void sendMsg(SOCKET s)
{
	char szText[] = " TCP Server Demo! \r\n";
	while (TRUE)
	{
		// 向客户端发送数据   
		::gets(szText);
		::send(s, szText, strlen(szText), 0);
	}
}

int server()
{
	SOCKET sListen = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sListen == INVALID_SOCKET)
	{
		printf("Failed socket() \n");
		return 0;
	}

	sockaddr_in sin;
	sin.sin_family = AF_INET;
	sin.sin_port = htons(4567);  //1024 ~ 49151：普通用户注册的端口号  
	sin.sin_addr.S_un.S_addr = INADDR_ANY;

	if (::bind(sListen, (LPSOCKADDR)&sin, sizeof(sin)) == SOCKET_ERROR)
	{
		printf("Failed bind() \n");
		return 0;
	}

	if (::listen(sListen, 10) == SOCKET_ERROR)
	{
		printf("Failed listen() \n");
		return 0;
	}

	sockaddr_in remoteAddr;
	int nAddrLen = sizeof(remoteAddr);

	// 监听客户连接
	while (true)
	{
		SOCKET sClient = ::accept(sListen, (SOCKADDR*)&remoteAddr, &nAddrLen);
		if (sClient == INVALID_SOCKET)
		{
			printf("Failed accept()");
		}
		printf("接受到一个连接：%s \r\n", inet_ntoa(remoteAddr.sin_addr));
		
		// 接受消息的线程
		thread tr(recMsg, sClient);
		tr.detach();

		thread ts(sendMsg, sClient);
		ts.detach();

		continue;
	}

	// 关闭监听套节字   
	::closesocket(sListen);
}

int main()
{  
	server();

	return 0;
}