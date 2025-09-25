#include <imgui.h>

bool showMenu = false;

void RenderImGui() {
	if (!showMenu) return;

	ImGui::Begin("Example Menu");
	ImGui::End();
	
}