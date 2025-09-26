#pragma once
#include <map>
#include <string>
#include "../../SDK/LocalPlayer.hpp"
#include "../../SDK/Launchwrapper.hpp"
#include "../Settings.hpp"
#include "Module.hpp"
class Fly : Module
{
private:

public:
	Fly();
	static void OnTick();
};