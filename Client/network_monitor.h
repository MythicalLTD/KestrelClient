#pragma once
#include <string>
#include <vector>
#include <cstdint>

struct ConnectionInfo {
	std::string remoteAddress; // IPv4 dotted string
	uint16_t remotePort; // Minecraft server port
	uint32_t pingMs; // last measured RTT in ms
	bool pingSuccess;
	bool isActive; // true if connection is active
};

void NetworkMonitor_Initialize();
void NetworkMonitor_Shutdown();
void NetworkMonitor_GetSnapshot(std::vector<ConnectionInfo>& outConnections);
void NetworkMonitor_StartPacketCapture();
void NetworkMonitor_StopPacketCapture();

