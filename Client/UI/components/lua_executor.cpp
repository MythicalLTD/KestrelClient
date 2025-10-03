#include "../../pch.h"
#include "lua_executor.h"
#include "../../include/imgui.h"
#include <string>
#include <vector>
#include <cstring>
#include "../../script_engine.h"

// Executor variables
bool showExecutor = false;
char executorCode[8192] = "";
char executorOutput[4096] = "";
bool executorExecuting = false;
std::vector<std::string> executorHistory;

void ExecuteCode() {
	executorExecuting = true;
	
	// Add to history
	std::string codeStr(executorCode);
	if (!codeStr.empty()) {
		executorHistory.push_back(codeStr);
		if (executorHistory.size() > 50) { // Limit history size
			executorHistory.erase(executorHistory.begin());
		}
	}
	
	// Execute Lua code
	std::string output = "[LUA] Executing Lua script...\n";
	
	// Execute real Lua script
	if (g_ScriptEngine) {
		ScriptResult result = g_ScriptEngine->ExecuteScript(codeStr, "lua");
		
		if (result.success) {
			output += "✓ Script executed successfully!\n";
			output += "Execution time: " + std::to_string(result.executionTime) + "ms\n";
			if (!result.output.empty()) {
				output += "\nScript output:\n" + result.output + "\n";
			}
		} else {
			output += "✗ Script execution failed!\n";
			output += "Error: " + result.error + "\n";
		}
	} else {
		output += "✗ Script engine not initialized!\n";
		output += "Make sure the script engine is properly loaded.\n";
	}
	
	// Add code preview to output
	output += "\nCode executed:\n";
	output += "```lua\n";
	if (codeStr.length() > 200) {
		output += codeStr.substr(0, 200) + "...\n";
	} else {
		output += codeStr + "\n";
	}
	output += "```\n";
	
	// Copy to output buffer
	strncpy_s(executorOutput, output.c_str(), sizeof(executorOutput) - 1);
	executorOutput[sizeof(executorOutput) - 1] = '\0';
	
	executorExecuting = false;
}

void LoadExampleScript() {
	strcpy_s(executorCode, "-- KestrelClient Lua Script\nlog('Hello from Lua!')\nlocal fps = getFPS()\nlocal ping = getPing()\nlog('Current FPS: ' .. fps)\nlog('Current Ping: ' .. ping .. 'ms')\n\n-- Memory example\nlocal addr = findPattern('\\x48\\x89\\x5C\\x24', 'xxxx')\nif addr > 0 then\n    log('Found pattern at: 0x' .. string.format('%X', addr))\nelse\n    log('Pattern not found')\nend\n\n-- Key detection example\nif keyPressed('VK_SPACE') then\n    log('SPACE key is pressed!')\nend");
}

void RenderExecutor() {
	if (!showExecutor) return;
	
	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(viewport->GetCenter(), ImGuiCond_FirstUseEver, ImVec2(0.5f, 0.5f));
	ImGui::SetNextWindowSize(ImVec2(800, 600), ImGuiCond_FirstUseEver);
	
	ImGui::Begin("Lua Script Executor", &showExecutor, ImGuiWindowFlags_NoCollapse);
	
	// Header
	ImGui::Text("KestrelClient Lua Script Executor");
	ImGui::Separator();
	
	ImGui::Columns(2, "ExecutorColumns", false);
	
	// Left column - Code input
	ImGui::BeginChild("CodeInput", ImVec2(0, 0), true);
	ImGui::Text("Lua Script Input:");
	ImGui::Separator();
	
	// Large text input area
	ImGui::InputTextMultiline("##CodeInput", executorCode, sizeof(executorCode), ImVec2(-1, 300), 
		ImGuiInputTextFlags_AllowTabInput | ImGuiInputTextFlags_NoHorizontalScroll);
	
	// Execute button
	ImGui::Spacing();
	ImGui::Columns(2, "ButtonColumns", false);
	
	if (ImGui::Button("Execute", ImVec2(-1, 0)) && !executorExecuting) {
		ExecuteCode();
	}
	
	ImGui::NextColumn();
	if (ImGui::Button("Clear", ImVec2(-1, 0))) {
		memset(executorCode, 0, sizeof(executorCode));
		memset(executorOutput, 0, sizeof(executorOutput));
	}
	
	ImGui::Columns(1);
	ImGui::EndChild();
	
	ImGui::NextColumn();
	
	// Right column - Output
	ImGui::BeginChild("CodeOutput", ImVec2(0, 0), true);
	ImGui::Text("Output:");
	ImGui::Separator();
	
	// Output text area
	ImGui::InputTextMultiline("##Output", executorOutput, sizeof(executorOutput), ImVec2(-1, 300), 
		ImGuiInputTextFlags_ReadOnly);
	
	// Execution status
	ImGui::Spacing();
	if (executorExecuting) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("Executing...");
		ImGui::PopStyleColor();
	} else {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("Ready");
		ImGui::PopStyleColor();
	}
	
	// History section
	if (!executorHistory.empty()) {
		ImGui::Spacing();
		ImGui::Separator();
		ImGui::Text("Recent Executions:");
		
		ImGui::BeginChild("History", ImVec2(0, 100), true);
		for (int i = static_cast<int>(executorHistory.size()) - 1; i >= 0 && i >= static_cast<int>(executorHistory.size()) - 10; --i) {
			std::string preview = executorHistory[i];
			if (preview.length() > 50) {
				preview = preview.substr(0, 50) + "...";
			}
			
			if (ImGui::Selectable(("[" + std::to_string(i + 1) + "] " + preview).c_str())) {
				strncpy_s(executorCode, executorHistory[i].c_str(), sizeof(executorCode) - 1);
			}
		}
		ImGui::EndChild();
	}
	
	ImGui::EndChild();
	
	ImGui::Columns(1);
	
	// Footer
	ImGui::Separator();
	ImGui::Text("Toggle via main menu | Lua Script Engine with Game API Access");
	
	// Help section
	if (ImGui::CollapsingHeader("Lua API Reference")) {
		ImGui::Text("Available Lua functions:");
		ImGui::BulletText("log('message') - Print to console");
		ImGui::BulletText("getFPS() - Get current FPS");
		ImGui::BulletText("getPing() - Get current ping");
		ImGui::BulletText("writeMemory(address, value) - Write to memory");
		ImGui::BulletText("readMemory(address) - Read from memory");
		ImGui::BulletText("findPattern(pattern, mask) - Find memory pattern");
		ImGui::BulletText("keyPressed('VK_SPACE') - Check if key is pressed");
		
		if (ImGui::Button("Load Example Script")) {
			LoadExampleScript();
		}
	}
	
	ImGui::End();
}
