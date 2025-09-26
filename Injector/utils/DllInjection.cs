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
            try
            {
                IntPtr hToken;
                if (OpenProcessToken(Process.GetCurrentProcess().Handle, 0x0020 | 0x0008, out hToken))
                {
                    TOKEN_PRIVILEGES tp = new TOKEN_PRIVILEGES();
                    tp.PrivilegeCount = 1;
                    tp.Attributes = 0x0002; // SE_PRIVILEGE_ENABLED

                    if (LookupPrivilegeValue(null!, "SeDebugPrivilege", out tp.Luid))
                    {
                        AdjustTokenPrivileges(hToken, false, ref tp, 0, IntPtr.Zero, IntPtr.Zero);
                    }

                    CloseHandle(hToken);
                }
            }
            catch
            {
                // Ignore privilege escalation errors
            }
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
            if (!File.Exists(dllPath))
                return DllInjectionResult.DllNotFound;

            if (_injectedDlls.Any(dll => dll.ProcessId == processId && dll.DllPath == dllPath) && !ignoreSecurity)
                return DllInjectionResult.AlreadyInjected;

            Process targetProcess;
            try
            {
                targetProcess = Process.GetProcessById((int)processId);
            }
            catch
            {
                return DllInjectionResult.GameProcessNotFound;
            }

            await AddStealthDelay();

            var result = InjectDllInternal(processId, dllPath, ignoreSecurity);
            if (result.Success)
            {
                _injectedDlls.Add(new InjectedDll
                {
                    ProcessId = processId,
                    ProcessName = targetProcess.ProcessName,
                    DllPath = dllPath,
                    ModuleHandle = result.ModuleHandle,
                    InjectionTime = DateTime.Now
                });
                return DllInjectionResult.Success;
            }

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
            IntPtr hProcess = OpenProcess(PROCESS_ALL_ACCESS, false, processId);
            if (hProcess == INTPTR_ZERO)
                return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.AccessDenied);

            try
            {
                IntPtr loadLibraryAddr = GetProcAddress(GetModuleHandle("kernel32.dll"), "LoadLibraryA");
                if (loadLibraryAddr == INTPTR_ZERO)
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);

                byte[] dllBytes = Encoding.ASCII.GetBytes(dllPath + "\0");
                IntPtr allocMemAddress = VirtualAllocEx(hProcess, IntPtr.Zero, (IntPtr)dllBytes.Length, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
                if (allocMemAddress == INTPTR_ZERO)
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);

                if (!WriteProcessMemory(hProcess, allocMemAddress, dllBytes, (uint)dllBytes.Length, out _))
                {
                    VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }

                IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, IntPtr.Zero, loadLibraryAddr, allocMemAddress, 0, IntPtr.Zero);
                if (hThread == INTPTR_ZERO)
                {
                    VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);
                    return (false, IntPtr.Zero, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
                }

                WaitForSingleObject(hThread, 5000);

                IntPtr moduleHandle = GetModuleHandleInProcess(hProcess, dllPath);

                CloseHandle(hThread);
                VirtualFreeEx(hProcess, allocMemAddress, 0, 0x8000);

                return (true, moduleHandle, ignoreSecurity ? DllInjectionResult.Success : DllInjectionResult.InjectionFailed);
            }
            finally
            {
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
            try
            {
                IntPtr[] modules = new IntPtr[1024];
                uint needed;
                if (EnumProcessModules(processHandle, modules, (uint)(modules.Length * IntPtr.Size), out needed))
                {
                    int count = (int)(needed / IntPtr.Size);
                    string fileName = Path.GetFileName(dllPath).ToLowerInvariant();
                    for (int i = 0; i < count; i++)
                    {
                        StringBuilder sb = new StringBuilder(1024);
                        if (GetModuleFileNameEx(processHandle, modules[i], sb, (uint)sb.Capacity) > 0)
                        {
                            string moduleName = Path.GetFileName(sb.ToString()).ToLowerInvariant();
                            if (moduleName == fileName)
                                return modules[i];
                        }
                    }
                }
            }
            catch
            {
                return new IntPtr(1);
            }
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
