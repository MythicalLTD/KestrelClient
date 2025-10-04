using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.IO;

namespace KestrelClientInjector.utils
{
    public enum DllInjectionResult
    {
        DllNotFound,
        GameProcessNotFound,
        InjectionFailed,
        EjectionFailed,
        Success,
        AlreadyInjected,
        NotInjected,
        AccessDenied
    }

    public class InjectedDll
    {
        public uint ProcessId { get; set; }
        public string ProcessName { get; set; } = string.Empty;
        public string DllPath { get; set; } = string.Empty;
        public IntPtr ModuleHandle { get; set; }
        public DateTime InjectionTime { get; set; }
    }

    public sealed class DllInjector
    {
        private static readonly IntPtr INTPTR_ZERO = IntPtr.Zero;
        private static readonly List<InjectedDll> _injectedDlls = new List<InjectedDll>();

        // Process access rights
        private const uint PROCESS_ALL_ACCESS = 0x1F0FFF;

        // Memory allocation flags
        private const uint MEM_COMMIT = 0x1000;
        private const uint MEM_RESERVE = 0x2000;
        private const uint PAGE_READWRITE = 0x04;

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr OpenProcess(uint dwDesiredAccess, bool bInheritHandle, uint dwProcessId);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool CloseHandle(IntPtr hObject);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr GetProcAddress(IntPtr hModule, string lpProcName);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr GetModuleHandle(string lpModuleName);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr VirtualAllocEx(IntPtr hProcess, IntPtr lpAddress, IntPtr dwSize, uint flAllocationType, uint flProtect);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool WriteProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] buffer, uint size, out UIntPtr lpNumberOfBytesWritten);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr CreateRemoteThread(IntPtr hProcess, IntPtr lpThreadAttributes, IntPtr dwStackSize, IntPtr lpStartAddress,
            IntPtr lpParameter, uint dwCreationFlags, IntPtr lpThreadId);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern uint WaitForSingleObject(IntPtr hHandle, uint dwMilliseconds);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool VirtualFreeEx(IntPtr hProcess, IntPtr lpAddress, uint dwSize, uint dwFreeType);

        [DllImport("psapi.dll", SetLastError = true)]
        private static extern bool EnumProcessModules(IntPtr hProcess, [Out] IntPtr[] lphModule, uint cb, out uint lpcbNeeded);

        [DllImport("psapi.dll", SetLastError = true)]
        private static extern uint GetModuleFileNameEx(IntPtr hProcess, IntPtr hModule, StringBuilder lpFilename, uint nSize);

        // Privilege escalation
        [DllImport("advapi32.dll", SetLastError = true)]
        private static extern bool OpenProcessToken(IntPtr ProcessHandle, uint DesiredAccess, out IntPtr TokenHandle);

        [DllImport("advapi32.dll", SetLastError = true)]
        private static extern bool AdjustTokenPrivileges(IntPtr TokenHandle, bool DisableAllPrivileges, ref TOKEN_PRIVILEGES NewState, uint BufferLength, IntPtr PreviousState, IntPtr ReturnLength);

        [DllImport("advapi32.dll", SetLastError = true)]
        private static extern bool LookupPrivilegeValue(string lpSystemName, string lpName, out LUID lpLuid);

        [StructLayout(LayoutKind.Sequential)]
        private struct LUID
        {
            public uint LowPart;
            public int HighPart;
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct TOKEN_PRIVILEGES
        {
            public uint PrivilegeCount;
            public LUID Luid;
            public uint Attributes;
        }

        private static DllInjector? _instance;

        public static DllInjector Instance
        {
            get
            {
                if (_instance == null)
                {
                    _instance = new DllInjector();
                }
                return _instance;
            }
        }

        public static List<InjectedDll> InjectedDlls => _injectedDlls.ToList();

        private DllInjector()
        {
            EnableDebugPrivilege();
        }

        private void EnableDebugPrivilege()
        {
            Logger.Info("=== ENABLING DEBUG PRIVILEGES ===");
            try
            {
                IntPtr hToken;
                IntPtr currentProcess = Process.GetCurrentProcess().Handle;
                Logger.Info($"Current process handle: {currentProcess}");
                
                if (OpenProcessToken(currentProcess, 0x0020 | 0x0008, out hToken))
                {
                    Logger.Info("Successfully opened process token");
                    
                    TOKEN_PRIVILEGES tp = new TOKEN_PRIVILEGES();
                    tp.PrivilegeCount = 1;
                    tp.Attributes = 0x0002; // SE_PRIVILEGE_ENABLED

                    if (LookupPrivilegeValue(null!, "SeDebugPrivilege", out tp.Luid))
                    {
                        Logger.Info("Successfully looked up SeDebugPrivilege");
                        
                        if (AdjustTokenPrivileges(hToken, false, ref tp, 0, IntPtr.Zero, IntPtr.Zero))
                        {
                            Logger.Info("Successfully enabled SeDebugPrivilege");
                        }
                        else
                        {
                            int errorCode = Marshal.GetLastWin32Error();
                            Logger.Warn($"Failed to adjust token privileges. Error code: {errorCode}");
                        }
                    }
                    else
                    {
                        int errorCode = Marshal.GetLastWin32Error();
                        Logger.Warn($"Failed to lookup SeDebugPrivilege. Error code: {errorCode}");
                    }

                    CloseHandle(hToken);
                    Logger.Info("Closed process token handle");
                }
                else
                {
                    int errorCode = Marshal.GetLastWin32Error();
                    Logger.Warn($"Failed to open process token. Error code: {errorCode}");
                    Logger.Warn("This may indicate insufficient privileges - try running as administrator");
                }
            }
            catch (Exception ex)
            {
                Logger.Error($"Exception in EnableDebugPrivilege: {ex.Message}");
                Logger.Error($"Exception type: {ex.GetType().Name}");
                // Continue anyway - privilege escalation is not critical
            }
            
            Logger.Info("=== DEBUG PRIVILEGES SETUP COMPLETED ===");
        }

        public async Task<DllInjectionResult> Inject(string processName, string dllPath)
        {
            if (!File.Exists(dllPath))
                return DllInjectionResult.DllNotFound;

            uint procId = 0;
            foreach (var proc in Process.GetProcessesByName(processName))
            {
                procId = (uint)proc.Id;
                break;
            }

            if (procId == 0)
                return DllInjectionResult.GameProcessNotFound;

            return await Inject(procId, dllPath, false);
        }

        public async Task<DllInjectionResult> Inject(uint processId, string dllPath, bool ignoreSecurity)
        {
            Logger.Info($"=== DLL INJECTION STARTED ===");
            Logger.Info($"Process ID: {processId}");
            Logger.Info($"DLL Path: {dllPath}");
            Logger.Info($"Ignore Security: {ignoreSecurity}");

            if (!File.Exists(dllPath))
            {
                Logger.Error($"DLL file not found: {dllPath}");
                return DllInjectionResult.DllNotFound;
            }

            Logger.Info($"DLL file exists, size: {new FileInfo(dllPath).Length} bytes");

            if (_injectedDlls.Any(dll => dll.ProcessId == processId && dll.DllPath == dllPath) && !ignoreSecurity)
            {
                Logger.Warn($"DLL already injected into process {processId}: {dllPath}");
                return DllInjectionResult.AlreadyInjected;
            }

            Process targetProcess;
            try
            {
                Logger.Info($"Getting process by ID: {processId}");
                targetProcess = Process.GetProcessById((int)processId);
                Logger.Info($"Process found: {targetProcess.ProcessName} (PID: {targetProcess.Id})");
            }
            catch (Exception ex)
            {
                Logger.Error($"Failed to get process by ID {processId}: {ex.Message}");
                return DllInjectionResult.GameProcessNotFound;
            }

            Logger.Info("Adding stealth delay...");
            await AddStealthDelay();

            Logger.Info("Starting internal DLL injection...");
            var result = InjectDllInternal(processId, dllPath, ignoreSecurity);
            
            Logger.Info($"Internal injection result - Success: {result.Success}, ModuleHandle: {result.ModuleHandle}, Result: {result.Result}");
            
            if (result.Success)
            {
                Logger.Info($"Injection successful, adding to tracking list");
                _injectedDlls.Add(new InjectedDll
                {
                    ProcessId = processId,
                    ProcessName = targetProcess.ProcessName,
                    DllPath = dllPath,
                    ModuleHandle = result.ModuleHandle,
                    InjectionTime = DateTime.Now
                });
                Logger.Info($"=== DLL INJECTION COMPLETED SUCCESSFULLY ===");
                return DllInjectionResult.Success;
            }

            Logger.Error($"Injection failed with result: {result.Result}");
            return result.Result;
        }

        public async Task<DllInjectionResult> Eject(uint processId, string dllPath)
        {
            var injectedDll = _injectedDlls.FirstOrDefault(dll => dll.ProcessId == processId && dll.DllPath == dllPath);
            if (injectedDll == null)
                return DllInjectionResult.NotInjected;

            try
            {
                var process = Process.GetProcessById((int)processId);
                if (process.HasExited)
                {
                    _injectedDlls.Remove(injectedDll);
                    return DllInjectionResult.Success;
                }
            }
            catch
            {
                _injectedDlls.Remove(injectedDll);
                return DllInjectionResult.Success;
            }

            await AddStealthDelay();

            if (EjectDllSafe(processId, injectedDll.ModuleHandle))
            {
                _injectedDlls.Remove(injectedDll);
                return DllInjectionResult.Success;
            }

            return DllInjectionResult.EjectionFailed;
        }

        public DllInjectionResult RemoveFromTracking(uint processId, string dllPath)
        {
            var injectedDll = _injectedDlls.FirstOrDefault(dll => dll.ProcessId == processId && dll.DllPath == dllPath);
            if (injectedDll == null)
                return DllInjectionResult.NotInjected;

            _injectedDlls.Remove(injectedDll);
            return DllInjectionResult.Success;
        }

        public static void CleanupExitedProcesses()
        {
            var toRemove = new List<InjectedDll>();
            foreach (var dll in _injectedDlls)
            {
                try
                {
                    var process = Process.GetProcessById((int)dll.ProcessId);
                    if (process.HasExited)
                        toRemove.Add(dll);
                }
                catch
                {
                    toRemove.Add(dll);
                }
            }
            foreach (var dll in toRemove)
            {
                _injectedDlls.Remove(dll);
            }
        }

        private async Task AddStealthDelay()
        {
            var random = new Random();
            int delay = random.Next(50, 201);
            await Task.Delay(delay);
        }

        private (bool Success, IntPtr ModuleHandle, DllInjectionResult Result) InjectDllInternal(uint processId, string dllPath, bool ignoreSecurity)
        {
            Logger.Info($"=== INTERNAL DLL INJECTION STARTED ===");
            Logger.Info($"Opening process with PROCESS_ALL_ACCESS (0x{PROCESS_ALL_ACCESS:X}) for PID: {processId}");
            
            IntPtr hProcess = OpenProcess(PROCESS_ALL_ACCESS, false, processId);
            if (hProcess == INTPTR_ZERO)
            {
                int errorCode = Marshal.GetLastWin32Error();
                Logger.Error($"Failed to open process {processId}. Error code: {errorCode}");
                Logger.Error($"Access denied - process may require administrator privileges or be protected");
                return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.AccessDenied);
            }

            Logger.Info($"Successfully opened process handle: {hProcess}");

            try
            {
                Logger.Info("Getting LoadLibraryA address from kernel32.dll");
                IntPtr kernel32Handle = GetModuleHandle("kernel32.dll");
                Logger.Info($"kernel32.dll handle: {kernel32Handle}");
                
                IntPtr loadLibraryAddr = GetProcAddress(kernel32Handle, "LoadLibraryA");
                if (loadLibraryAddr == INTPTR_ZERO)
                {
                    Logger.Error("Failed to get LoadLibraryA address");
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }
                Logger.Info($"LoadLibraryA address: 0x{loadLibraryAddr:X}");

                byte[] dllBytes = Encoding.ASCII.GetBytes(dllPath + "\0");
                Logger.Info($"DLL path bytes length: {dllBytes.Length}");
                Logger.Info($"DLL path bytes: {BitConverter.ToString(dllBytes)}");

                Logger.Info($"Allocating memory in target process (size: {dllBytes.Length} bytes)");
                IntPtr allocMemAddress = VirtualAllocEx(hProcess, IntPtr.Zero, (IntPtr)dllBytes.Length, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
                if (allocMemAddress == INTPTR_ZERO)
                {
                    int errorCode = Marshal.GetLastWin32Error();
                    Logger.Error($"Failed to allocate memory in target process. Error code: {errorCode}");
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }
                Logger.Info($"Memory allocated at address: 0x{allocMemAddress:X}");

                Logger.Info("Writing DLL path to target process memory");
                if (!WriteProcessMemory(hProcess, allocMemAddress, dllBytes, (uint)dllBytes.Length, out UIntPtr bytesWritten))
                {
                    int errorCode = Marshal.GetLastWin32Error();
                    Logger.Error($"Failed to write process memory. Error code: {errorCode}");
                    VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }
                Logger.Info($"Successfully wrote {bytesWritten} bytes to target process");

                Logger.Info("Creating remote thread to call LoadLibraryA");
                IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, IntPtr.Zero, loadLibraryAddr, allocMemAddress, 0, IntPtr.Zero);
                if (hThread == INTPTR_ZERO)
                {
                    int errorCode = Marshal.GetLastWin32Error();
                    Logger.Error($"Failed to create remote thread. Error code: {errorCode}");
                    VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }
                Logger.Info($"Remote thread created with handle: {hThread}");

                Logger.Info("Waiting for remote thread to complete (timeout: 5000ms)");
                uint waitResult = WaitForSingleObject(hThread, 5000);
                Logger.Info($"WaitForSingleObject result: {waitResult} (0=WAIT_OBJECT_0, 258=WAIT_TIMEOUT)");

                if (waitResult == 258) // WAIT_TIMEOUT
                {
                    Logger.Warn("Remote thread timed out - DLL may still be loading");
                }

                Logger.Info("Getting module handle in target process");
                IntPtr moduleHandle = GetModuleHandleInProcess(hProcess, dllPath);
                Logger.Info($"Module handle found: {moduleHandle}");

                Logger.Info("Cleaning up resources");
                CloseHandle(hThread);
                VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);

                Logger.Info($"=== INTERNAL DLL INJECTION COMPLETED - Success: true, ModuleHandle: {moduleHandle} ===");
                return (true, moduleHandle, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.Success);
            }
            catch (Exception ex)
            {
                Logger.Error($"Exception in InjectDllInternal: {ex.Message}");
                Logger.Error($"Exception type: {ex.GetType().Name}");
                Logger.Error($"Stack trace: {ex.StackTrace}");
                return (false, IntPtr.Zero, DllInjectionResult.InjectionFailed);
            }
            finally
            {
                Logger.Info($"Closing process handle: {hProcess}");
                CloseHandle(hProcess);
            }
        }

        private bool EjectDllSafe(uint processId, IntPtr moduleHandle)
        {
            IntPtr hProcess = OpenProcess(PROCESS_ALL_ACCESS, false, processId);
            if (hProcess == INTPTR_ZERO)
                return false;

            try
            {
                if (!IsModuleStillLoaded(hProcess, moduleHandle))
                    return true;

                // For safety, do not attempt to eject if not sure.
                return false;
            }
            finally
            {
                CloseHandle(hProcess);
            }
        }

        private bool IsModuleStillLoaded(IntPtr processHandle, IntPtr moduleHandle)
        {
            try
            {
                IntPtr[] modules = new IntPtr[1024];
                uint needed;
                if (EnumProcessModules(processHandle, modules, (uint)(modules.Length * IntPtr.Size), out needed))
                {
                    int count = (int)(needed / IntPtr.Size);
                    for (int i = 0; i < count; i++)
                    {
                        if (modules[i] == moduleHandle)
                            return true;
                    }
                }
            }
            catch
            {
                return false;
            }
            return false;
        }

        private IntPtr GetModuleHandleInProcess(IntPtr processHandle, string dllPath)
        {
            Logger.Info($"=== GETTING MODULE HANDLE IN PROCESS ===");
            Logger.Info($"Process handle: {processHandle}");
            Logger.Info($"DLL path: {dllPath}");
            
            try
            {
                IntPtr[] modules = new IntPtr[1024];
                uint needed;
                
                Logger.Info("Enumerating process modules");
                if (EnumProcessModules(processHandle, modules, (uint)(modules.Length * IntPtr.Size), out needed))
                {
                    int count = (int)(needed / IntPtr.Size);
                    Logger.Info($"Found {count} modules in process");
                    
                    string fileName = Path.GetFileName(dllPath).ToLowerInvariant();
                    Logger.Info($"Looking for module: {fileName}");
                    
                    for (int i = 0; i < count; i++)
                    {
                        StringBuilder sb = new StringBuilder(1024);
                        if (GetModuleFileNameEx(processHandle, modules[i], sb, (uint)sb.Capacity) > 0)
                        {
                            string modulePath = sb.ToString();
                            string moduleName = Path.GetFileName(modulePath).ToLowerInvariant();
                            
                            if (i < 10) // Log first 10 modules for debugging
                            {
                                Logger.Info($"Module {i}: {moduleName} (handle: {modules[i]})");
                            }
                            
                            if (moduleName == fileName)
                            {
                                Logger.Info($"FOUND TARGET MODULE: {moduleName} at handle {modules[i]}");
                                return modules[i];
                            }
                        }
                    }
                    
                    Logger.Warn($"Target module '{fileName}' not found in process");
                }
                else
                {
                    int errorCode = Marshal.GetLastWin32Error();
                    Logger.Error($"Failed to enumerate process modules. Error code: {errorCode}");
                }
            }
            catch (Exception ex)
            {
                Logger.Error($"Exception in GetModuleHandleInProcess: {ex.Message}");
                Logger.Error($"Exception type: {ex.GetType().Name}");
                return new IntPtr(1);
            }
            
            Logger.Info("Returning IntPtr.Zero - module not found");
            return IntPtr.Zero;
        }

        // Legacy compatibility
        private bool bInject(uint processId, string dllPath)
        {
            var result = InjectDllInternal(processId, dllPath, false);
            return result.Success;
        }
    }
}
