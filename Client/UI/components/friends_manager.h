#pragma once
#include <string>
#include <vector>

// Friends management variables
extern std::vector<std::string> friendsList;
extern char newFriendName[128];
extern std::string friendsStatus;
extern int apiPort;

// Function declarations
void RenderFriendsManager();
void AddFriend(const std::string& playerName);
void RemoveFriend(const std::string& playerName);
void LoadFriends();
