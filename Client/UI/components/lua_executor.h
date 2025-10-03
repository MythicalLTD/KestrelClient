#pragma once
#include <string>
#include <vector>

// Executor variables
extern bool showExecutor;
extern char executorCode[8192];
extern char executorOutput[4096];
extern bool executorExecuting;
extern std::vector<std::string> executorHistory;

// Function declarations
void RenderExecutor();
void ExecuteCode();
void LoadExampleScript();
