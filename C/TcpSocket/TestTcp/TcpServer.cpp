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

using namespace std; 
CInitSock initSock;     // 初始化Winsock库


int main()
{
	// 创建套节字   
	SOCKET sListen = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	//用来指定套接字使用的地址格式，通常使用AF_INET  
	//指定套接字的类型，若是SOCK_DGRAM，则用的是udp不可靠传输  
	//配合type参数使用，指定使用的协议类型（当指定套接字类型后，可以设置为0，因为默认为UDP或TCP）  
	if (sListen == INVALID_SOCKET)
	{
		printf("Failed socket() \n");
		return 0;
	}

	// 填充sockaddr_in结构 ,是个结构体  
	/* struct sockaddr_in {

	short sin_family;  //地址族（指定地址格式） ，设为AF_INET
	u_short sin_port; //端口号
	struct in_addr sin_addr; //IP地址
	char sin_zero[8]; //空子节，设为空
	} */

	sockaddr_in sin;
	sin.sin_family = AF_INET;
	sin.sin_port = htons(4567);  //1024 ~ 49151：普通用户注册的端口号  
	sin.sin_addr.S_un.S_addr = INADDR_ANY;

	// 绑定这个套节字到一个本地地址   
	if (::bind(sListen, (LPSOCKADDR)&sin, sizeof(sin)) == SOCKET_ERROR)
	{
		printf("Failed bind() \n");
		return 0;
	}

	// 进入监听模式   
	//2指的是，监听队列中允许保持的尚未处理的最大连接数  

	if (::listen(sListen, 2) == SOCKET_ERROR)
	{
		printf("Failed listen() \n");
		return 0;
	}

	// 循环接受客户的连接请求   
	sockaddr_in remoteAddr;
	int nAddrLen = sizeof(remoteAddr);
	SOCKET sClient = 0;
	char szText[] = " TCP Server Demo! \r\n";
	while (sClient == 0)
	{
		// 接受一个新连接   
		//（(SOCKADDR*)&remoteAddr）一个指向sockaddr_in结构的指针，用于获取对方地址  
		sClient = ::accept(sListen, (SOCKADDR*)&remoteAddr, &nAddrLen);
		if (sClient == INVALID_SOCKET)
		{
			printf("Failed accept()");
		}


		printf("接受到一个连接：%s \r\n", inet_ntoa(remoteAddr.sin_addr));
		continue;
	}

	while (TRUE)
	{
		// 向客户端发送数据   
		::gets(szText);
		::send(sClient, szText, strlen(szText), 0);

		// 从客户端接收数据   
		char buff[256];
		int nRecv = ::recv(sClient, buff, 256, 0);
		if (nRecv > 0)
		{
			buff[nRecv] = '\0';
			printf(" 接收到数据：%s\n", buff);
		}

	}

	// 关闭同客户端的连接   
	::closesocket(sClient);

	// 关闭监听套节字   
	::closesocket(sListen);

	return 0;
}