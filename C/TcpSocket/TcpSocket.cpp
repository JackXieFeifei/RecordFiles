#include "stdafx.h"
#include "TcpSocket.h"

#include <stdio.h>
#include <stdlib.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")

TcpSocket::TcpSocket()
{
	m_socket = INVALID_SOCKET;
	m_isConnected = false;
	m_isConnecting = false;
}


TcpSocket::~TcpSocket()
{
	disconnect();
}

bool TcpSocket::connectDomain(std::string& strIP, int port)
{
	TLocalIpType ipvType = getIpType(strIP.c_str());
	if (ipvType == ELocalIpType_IPv4)
	{
		return connectIpv4(strIP, port);
	}
	else if (ipvType == ELocalIpType_IPv6)
	{
		return connectIpv6(strIP, port);
	}
	return false;
}

bool TcpSocket::connectIpv4(std::string& strIP, int port)
{
	struct hostent *hptr = NULL;
	if ((hptr = gethostbyname(strIP.c_str())) == NULL)
	{
		return false;
	}

	char ** pptr = hptr->h_addr_list;
	char str[32] = {0};
	const char* ip = inet_ntop(hptr->h_addrtype, *pptr, str, sizeof(str));
	// log

	m_ip = ip;
	m_port = port;
	m_socket = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

	if (m_socket == INVALID_SOCKET)
		return false;

	struct sockaddr_in sa;
	memset(&sa, 0, sizeof(sa));
	sa.sin_family = AF_INET;
	sa.sin_addr.s_addr = inet_addr(m_ip.c_str());
	sa.sin_port = htons(m_port);

	int result = ::connect(m_socket, reinterpret_cast<sockaddr*>(&sa), sizeof(sa));
	if (result == SOCKET_ERROR)
		return false;

	m_isConnected = true;
	return m_isConnected;
}

bool TcpSocket::connectIpv6(std::string& strIP, int port)
{
	std::string ip = this->getIPv6Addr(strIP.c_str());
	struct in6_addr ipv6_addr = {0};
	inet_pton(AF_INET6, ip.c_str(), &ipv6_addr);
	// log

	struct sockaddr_in6 v6_addr = { 0 };
	v6_addr.sin6_family = AF_INET6;
	v6_addr.sin6_port = htons(m_port);
	v6_addr.sin6_addr = ipv6_addr;

	m_socket = ::socket(AF_INET6, SOCK_STREAM, IPPROTO_TCP);
	if (m_socket == INVALID_SOCKET)
		return false;

	std::string v6_error;
	int result = ::connect(m_socket, (sockaddr*)&v6_addr, sizeof(v6_addr));
	if (result == SOCKET_ERROR)
	{
		//log
		return false;
	}

	m_isConnected = true;
	return m_isConnected;
}

bool TcpSocket::disconnect()
{
	if (m_socket != INVALID_SOCKET)
	{
		int result = ::closesocket(m_socket);
		m_socket = INVALID_SOCKET;
		return (result != SOCKET_ERROR);
	}

	m_isConnected = false;
	m_isConnecting = false;
	
	return true;
}

bool TcpSocket::send(const char* data, int length)
{
	if (isConnected())
	{
		int reslut = 0;
		try
		{
			reslut = ::send(m_socket, data, length, 0);
		}
		catch (...)
		{
			disconnect();
			onError();
			return false;
		}

		if (reslut == SOCKET_ERROR)
		{
			return SOCKET_ERROR;
		}
		else if (reslut == 0)
		{
			disconnect();
			onError();
			return false;
		}
	}

	return false;
}

int TcpSocket::receive(unsigned char* pBuffer, int buffLen)
{
	if (!isConnected())
	{
		return -1;
	}
	int result = ::recv(m_socket, (char*)pBuffer, buffLen, 0);

	if (result == SOCKET_ERROR)
	{
		if (errno == EINTR || errno == EWOULDBLOCK || errno == EAGAIN)
		{
			return -1;
		}
		else
		{
			disconnect();
			onError();
			return -1;
		}
	}
	else if (result == 0)
	{
		disconnect();
		onError();
		return -1;
	}

	return result;
}

bool TcpSocket::isConnected() const
{
	return (m_socket != INVALID_SOCKET) && m_isConnected;
}

bool TcpSocket::isConnecting() const
{
	return (m_socket != INVALID_SOCKET) && m_isConnecting;
}

SOCKET TcpSocket::getSocket() const
{
	return m_socket;
}

const char* TcpSocket::getIP() const
{
	return m_ip.c_str();
}

int TcpSocket::getPort() const
{
	return m_port;
}


void TcpSocket::onError()
{
	// ·¢ËÍÍ¨Öª
}

TLocalIpType TcpSocket::getIpType(const char* ipAddr)
{
	TLocalIpType type;

	struct addrinfo *answer, hint, *curr;
	memset(&hint, 0, sizeof(hint));
	hint.ai_family = AF_UNSPEC;
	hint.ai_socktype = SOCK_STREAM;

	int ret = getaddrinfo(ipAddr, NULL, &hint, &answer);
	if (ret != 0)
	{
		type = ELocalIpType_None;
	}
	else
	{
		for (curr = answer; curr != NULL; curr = curr->ai_next)
		{
			switch (curr->ai_family)
			{
			case AF_UNSPEC:
				break;
			case AF_INET:
				type = ELocalIpType_IPv4;
				break;
			case AF_INET6:
				type = ELocalIpType_IPv6;
				break;
			default:
				break;
			}
			if (type != ELocalIpType_None)
				break;
		}
	}
	freeaddrinfo(answer);

	return type;
}

std::string TcpSocket::getIPv6Addr(const char* ipv4)
{
	char nIp[128];
	struct addrinfo hints, *res, *res0;
	int error;
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = PF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;  // XCode : AI_DEFAULT
	error = getaddrinfo(ipv4, "http", &hints, &res0);
	for (res = res0; res; res = res->ai_next)
	{
		inet_ntop(AF_INET6,
			&(((struct sockaddr_in6*)(res->ai_addr))->sin6_addr),
			nIp, 128);
		break;
	}
	return nIp;
}




