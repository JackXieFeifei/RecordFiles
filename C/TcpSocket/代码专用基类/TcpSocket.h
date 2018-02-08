#ifndef __TCP_SOCKET__
#define __TCP_SOCKET__

#include <string>

#define INVALID_SOCKET	-1
#define SOCKET_ERROR		-1
typedef int SOCKET;


enum TLocalIpType
{
	ELocalIpType_None = 0,
	ELocalIpType_IPv4 = 1,
	ELocalIpType_IPv6 = 2,
	ELocalIpType_Dual = 3,
};

class TcpSocket
{
public:
	TcpSocket();
	~TcpSocket();

public:
	bool connectDomain(std::string& strIP, int port);

	bool connectIpv4(std::string& strIP, int port);

	bool connectIpv6(std::string& strIP, int port);

	bool disconnect();

	virtual bool send(const char* data, int length);

	virtual int receive(unsigned char* pBuffer, int buffLen);

	virtual bool isConnected() const;

	virtual bool isConnecting() const;

	SOCKET getSocket() const;

	const char* getIP() const;

	int getPort() const;

	void onError();

private:
	/*
	* @ brief	获取IP地址的类型
	*/
	TLocalIpType getIpType(const char* ipAddr);

	/*
	* @ brief	获取IPv6的地址
	*/
	std::string getIPv6Addr(const char* ipv4);

private:
	SOCKET			m_socket;// 
	std::string		m_ip;	// 服务器地址
	int				m_port;	// 服务器端口号
	bool				m_isConnected;	// socket 是否已经连接
	bool				m_isConnecting;	// socket 正在连接
};

#endif //__TCP_SOCKET__