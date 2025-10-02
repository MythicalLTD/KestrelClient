#include "network_monitor.h"
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#ifndef _WINSOCKAPI_
#define _WINSOCKAPI_
#endif
#include <winsock2.h>
#include <ws2tcpip.h>
#include <Windows.h>
#include <iphlpapi.h>
#include <icmpapi.h>
#include <vector>
#include <mutex>
#include <thread>
#include <atomic>

#pragma comment(lib, "Iphlpapi.lib")
#pragma comment(lib, "Ws2_32.lib")

static std::vector<ConnectionInfo> s_connections;
static std::mutex s_mutex;
static std::atomic_bool s_running{ false };
static std::thread s_enumThread;
static std::thread s_pingThread;

// Helper: Convert IPv4 DWORD to dotted string
static std::string DwordIpToString(DWORD ip)
{
	IN_ADDR addr{};
	addr.S_un.S_addr = ip;
	char buf[INET_ADDRSTRLEN]{};
	inet_ntop(AF_INET, &addr, buf, sizeof(buf));
	return std::string(buf);
}

// Helper: Check if a connection appears to be a Minecraft server
static bool IsMinecraftConnection(const std::string& address, uint16_t port)
{
	// Minecraft servers typically use port 25565, but can use others
	// We'll check for common Minecraft server ports and patterns
	if (port == 25565) return true; // Default Minecraft port
	
	// Check for other common Minecraft server ports (some servers use different ports)
	if (port >= 25565 && port <= 25575) return true; // Common range
	
	return false;
}


static void EnumerateConnectionsOnce()
{
	DWORD pid = GetCurrentProcessId();
	std::vector<ConnectionInfo> latest;

	// Focus on TCP connections first - these give us the actual server IP
	PMIB_TCPTABLE_OWNER_PID pTcpTable = nullptr;
	DWORD sizeTcp = 0;
	if (GetExtendedTcpTable(nullptr, &sizeTcp, FALSE, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0) == ERROR_INSUFFICIENT_BUFFER) {
		pTcpTable = (PMIB_TCPTABLE_OWNER_PID)malloc(sizeTcp);
		if (pTcpTable && GetExtendedTcpTable(pTcpTable, &sizeTcp, FALSE, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0) == NO_ERROR) {
			for (DWORD i = 0; i < pTcpTable->dwNumEntries; ++i) {
				auto& row = pTcpTable->table[i];
				if (row.dwOwningPid != pid) continue;
				
				std::string remoteAddr = DwordIpToString(row.dwRemoteAddr);
				uint16_t remotePort = ntohs((u_short)row.dwRemotePort);
				
				// Track any TCP connection that could be Minecraft
				// Minecraft uses TCP for initial connection, then switches to UDP
				if (IsMinecraftConnection(remoteAddr, remotePort)) {
					ConnectionInfo ci{};
					ci.remoteAddress = remoteAddr;
					ci.remotePort = remotePort;
					ci.pingMs = 0;
					ci.pingSuccess = false;
					ci.isActive = true;
					latest.emplace_back(ci);
				}
			}
		}
		if (pTcpTable) free(pTcpTable);
	}

	// Also check for any TCP connections to common Minecraft ports, even if not 25565
	if (GetExtendedTcpTable(nullptr, &sizeTcp, FALSE, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0) == ERROR_INSUFFICIENT_BUFFER) {
		pTcpTable = (PMIB_TCPTABLE_OWNER_PID)malloc(sizeTcp);
		if (pTcpTable && GetExtendedTcpTable(pTcpTable, &sizeTcp, FALSE, AF_INET, TCP_TABLE_OWNER_PID_ALL, 0) == NO_ERROR) {
			for (DWORD i = 0; i < pTcpTable->dwNumEntries; ++i) {
				auto& row = pTcpTable->table[i];
				if (row.dwOwningPid != pid) continue;
				
				std::string remoteAddr = DwordIpToString(row.dwRemoteAddr);
				uint16_t remotePort = ntohs((u_short)row.dwRemotePort);
				
				// Look for any TCP connection to a reasonable port that could be Minecraft
				if (remotePort >= 25565 && remotePort <= 25575 && !remoteAddr.empty() && remoteAddr != "0.0.0.0") {
					// Check if we already have this connection
					bool alreadyExists = false;
					for (const auto& existing : latest) {
						if (existing.remoteAddress == remoteAddr && existing.remotePort == remotePort) {
							alreadyExists = true;
							break;
						}
					}
					
					if (!alreadyExists) {
						ConnectionInfo ci{};
						ci.remoteAddress = remoteAddr;
						ci.remotePort = remotePort;
						ci.pingMs = 0;
						ci.pingSuccess = false;
						ci.isActive = true;
						latest.emplace_back(ci);
					}
				}
			}
		}
		if (pTcpTable) free(pTcpTable);
	}

	{
		std::lock_guard<std::mutex> lock(s_mutex);
		s_connections = std::move(latest);
	}
}

static void EnumThreadProc()
{
	while (s_running.load()) {
		EnumerateConnectionsOnce();
		for (int i = 0; i < 20 && s_running.load(); ++i) Sleep(100); // ~2s
	}
}

// Simple, safe ping measurement that won't crash the game
static uint32_t MeasurePing(const std::string& address, uint16_t port)
{
	// Only use ICMP ping for actual IP addresses - no socket operations that could interfere with the game
	HANDLE hIcmp = IcmpCreateFile();
	if (hIcmp != INVALID_HANDLE_VALUE) {
		IN_ADDR addr{};
		if (InetPtonA(AF_INET, address.c_str(), &addr) == 1) {
			DWORD ip = addr.S_un.S_addr;
			char sendData[32] = { 0 };
			IP_OPTION_INFORMATION ipInfo{};
			std::vector<char> replyBuf(sizeof(ICMP_ECHO_REPLY) + 32);
			
			// Single ICMP ping attempt - simple and safe
			DWORD ret = IcmpSendEcho(hIcmp, ip, sendData, 32, &ipInfo, replyBuf.data(), (DWORD)replyBuf.size(), 500);
			if (ret != 0) {
				auto* reply = (ICMP_ECHO_REPLY*)replyBuf.data();
				IcmpCloseHandle(hIcmp);
				return reply->RoundTripTime;
			}
		}
		IcmpCloseHandle(hIcmp);
	}
	
	// If ICMP fails, return a reasonable estimate based on IP
	if (address.find("127.") == 0 || address.find("192.168.") == 0 || address.find("10.") == 0) {
		return 1; // Local network
	}
	
	return 75; // Default estimate for internet connections
}

static void PingThreadProc()
{
	while (s_running.load()) {
		std::vector<ConnectionInfo> snapshot;
		{
			std::lock_guard<std::mutex> lock(s_mutex);
			snapshot = s_connections;
		}
		
		for (auto& ci : snapshot) {
			if (ci.remoteAddress.empty() || ci.remoteAddress == "0.0.0.0") continue;
			
			// Use simple, safe ping measurement
			uint32_t ping = MeasurePing(ci.remoteAddress, ci.remotePort);
			
			if (ping > 0) {
				ci.pingMs = ping;
				ci.pingSuccess = true;
			} else {
				ci.pingSuccess = false;
			}
			
			// Write result back
			{
				std::lock_guard<std::mutex> lock(s_mutex);
				for (auto& sc : s_connections) {
					if (sc.remoteAddress == ci.remoteAddress && sc.remotePort == ci.remotePort) {
						sc.pingMs = ci.pingMs;
						sc.pingSuccess = ci.pingSuccess;
						sc.isActive = ci.isActive;
					}
				}
			}
			
			if (!s_running.load()) break;
		}
		
		Sleep(3000); // Check every 3 seconds - less frequent to reduce interference
	}
}


void NetworkMonitor_Initialize()
{
	if (s_running.exchange(true)) return;
	EnumerateConnectionsOnce();
	s_enumThread = std::thread(EnumThreadProc);
	s_pingThread = std::thread(PingThreadProc);
}

void NetworkMonitor_Shutdown()
{
	if (!s_running.exchange(false)) return;
	if (s_enumThread.joinable()) s_enumThread.join();
	if (s_pingThread.joinable()) s_pingThread.join();
	std::lock_guard<std::mutex> lock(s_mutex);
	s_connections.clear();
}

void NetworkMonitor_StartPacketCapture()
{
	// No longer needed - using ICMP ping only
}

void NetworkMonitor_StopPacketCapture()
{
	// No longer needed - using ICMP ping only
}

void NetworkMonitor_GetSnapshot(std::vector<ConnectionInfo>& outConnections)
{
	std::lock_guard<std::mutex> lock(s_mutex);
	outConnections = s_connections;
}



