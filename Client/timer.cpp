#include "timer.h"
#include <jni.h>
#include <Windows.h>
#include <thread>
#include <iostream>
#include <atomic>

static JavaVM* g_jvm = nullptr;
static jobject g_minecraftObj = nullptr;
static jclass g_minecraftClass = nullptr;

static std::atomic<bool> g_running{ false };
static std::thread g_timerThread;
static float g_timerSpeed = 1.0f;

void FindJVM() {
	if (g_jvm) return; // Already found

	JavaVM* vms[1];
	jsize nVMs = 0;
	if (JNI_GetCreatedJavaVMs(vms, 1, &nVMs) == JNI_OK && nVMs > 0) {
		g_jvm = vms[0];
		printf("JVM found successfully!\n");
	}
	else {
		printf("Failed to get created Java VMs.\n");
	}
}

void InitMinecraft(JNIEnv* env) {
	if (g_minecraftClass && g_minecraftObj) {
		printf("Minecraft already initialized.\n");
		return; // Already initialized
	}

	jclass mcClass = env->FindClass("ave");
	if (!mcClass) {
		printf("Failed to find class 'ave'\n");
		printf("Make sure the Minecraft version is correct.\n");
		return;
	}

	jmethodID getMcMethod = env->GetStaticMethodID(mcClass, "A", "()Lave;");
	if (!getMcMethod) {
		printf("Failed to find method 'A'\n");
		return;
	}

	jobject mcInstance = env->CallStaticObjectMethod(mcClass, getMcMethod);
	if (!mcInstance) {
		printf("Failed to get Minecraft instance\n");
		return;
	}

	g_minecraftClass = (jclass)env->NewGlobalRef(mcClass);
	g_minecraftObj = env->NewGlobalRef(mcInstance);

	printf("Minecraft initialized successfully.\n");
}

void UpdateTimerSpeed(JNIEnv* env, float speed) {
	if (!g_minecraftObj || !g_minecraftClass) return; // Not initialized


	jfieldID timerField = env->GetFieldID(g_minecraftClass, "Y", "Lavl;");
	if (!timerField) {
		printf("Failed to find field 'A'\n");
		return;
	}

	jobject timerObj = env->GetObjectField(g_minecraftObj, timerField);
	if (!timerObj) {
		printf("Failed to get timer object\n");
		return;
	}

	jclass timerClass = env->FindClass("avl");
	if (!timerClass) {
		printf("Failed to find class 'avl'\n");
		return;
	}

	jfieldID speedField = env->GetFieldID(timerClass, "d", "F");
	if (!speedField) {
		printf("Failed to find field 'd'\n");
		return;
	}

	env->SetFloatField(timerObj, speedField, speed);

	env->DeleteLocalRef(timerObj);
	env->DeleteLocalRef(timerClass);
}

void TimerThread() {
	while (g_running.load()) {
		if (!g_jvm) {
			printf("JVM not initialized\n");
			Sleep(100);
			continue;
		}

		JNIEnv* env = nullptr;
		if (g_jvm->AttachCurrentThread((void**)&env, nullptr) != JNI_OK || !env) {
			printf("Failed to attach thread to JVM\n");
			Sleep(100);
			continue;
		}

		InitMinecraft(env);
		UpdateTimerSpeed(env, g_timerSpeed);

		g_jvm->DetachCurrentThread();
		Sleep(100);
	}
}

void InitTimerHack() {
	FindJVM();
}

void StartTimerHackThread() {
	if (g_running.load()) return; // Already running
	g_running.store(true);
	g_timerThread = std::thread(TimerThread);
}

void StopTimerHackThread() {
	g_running.store(false);
	if (g_timerThread.joinable()) {
		g_timerThread.join();
	}

	if (g_jvm) {
		JNIEnv* env = nullptr;
		if (g_jvm->AttachCurrentThread((void**)&env, nullptr) == JNI_OK) {
			if (g_minecraftObj) {
				env->DeleteGlobalRef(g_minecraftObj);
				g_minecraftObj = nullptr;
			}
			if (g_minecraftClass) {
				env->DeleteGlobalRef(g_minecraftClass);
				g_minecraftClass = nullptr;
			}

			g_jvm->DetachCurrentThread();
		}
	}

}

void SetTimerSpeed(float speed) {
	g_timerSpeed = speed;
}