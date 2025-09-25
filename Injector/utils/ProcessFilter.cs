using System.Diagnostics;

namespace KestrelClientInjector.utils
{
    public static class ProcessFilter
    {
        // Critical Windows processes that should never be injected into
        private static readonly HashSet<string> CriticalProcesses = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            // Core Windows processes
            "csrss", "winlogon", "wininit", "services", "lsass", "svchost", 
            "smss", "dwm", "explorer", "taskhost", "taskhostw",
            
            // Security processes
            "windefend", "msmpeng", "nissrv", "securityhealthservice", "securityhealthsystray",
            
            // System processes
            "registry", "system", "idle", "ntoskrnl", "kernel32", "kernelbase",
            
            // Windows Update and maintenance
            "wuauclt", "trustedinstaller", "tiworker", "dism", 
            
            // Audio/Video drivers
            "audiodg", "conhost",
            
            // Network and communication
            "spoolsv", "netlogon", "dns", "dhcp",
            
            // Virtual machines and containers
            "vmware", "vbox", "docker", "vmms", "vmsp", "vmwp",
            
            // Antivirus common processes (partial list)
            "avast", "avg", "norton", "mcafee", "kaspersky", "bitdefender", "eset", "sophos"
        };

        // Additional processes that are risky to inject into
        private static readonly HashSet<string> RiskyProcesses = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            "cmd", "powershell", "powershell_ise", "regedit", "taskmgr", "msconfig",
            "services", "mmc", "devenv", "iisexpress", "w3wp", "sqlservr"
        };

        // Popular Minecraft processes and clients
        private static readonly HashSet<string> MinecraftProcesses = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            // Official Minecraft
            "minecraft", "javaw", "java", "MinecraftLauncher", "minecraft_server",
            
            // Popular Minecraft clients
            "feather", "lunar", "badlion", "pvplounge", "cheatbreaker", "forge", "fabric",
            "optifine", "technic", "atlauncher", "multimc", "polymc", "prismmc", "curseforge",
            "twitch", "ftb", "modrinth", "gdlauncher", "sklauncher", "tlauncher", "shiginima",
            
            // Modded Minecraft
            "FTBApp", "ATLauncher", "GDLauncher", "ModrinthApp", "CurseForge",
            
            // Cracked launchers (commonly used)
            "TLauncher", "MinecraftLauncher", "SKLauncher", "Shiginima"
        };

        public static List<SafeProcessItem> GetSafeProcesses(bool showMinecraftOnly = true)
        {
            var safeProcesses = new List<SafeProcessItem>();
            var processGroups = new Dictionary<string, List<SafeProcessItem>>();
            
            try
            {
                Process[] processes = Process.GetProcesses();
                
                foreach (Process process in processes)
                {
                    try
                    {
                        // Skip if process has exited
                        if (process.HasExited)
                            continue;

                        string processName = process.ProcessName.ToLower();
                        
                        // Skip critical processes
                        if (IsCriticalProcess(processName))
                            continue;

                        // Skip very low PID processes (usually system processes)
                        if (process.Id < 100)
                            continue;

                        // Skip processes we can't access
                        try
                        {
                            _ = process.MainModule?.FileName;
                        }
                        catch
                        {
                            continue; // Skip inaccessible processes
                        }

                        bool isMinecraftProcess = IsMinecraftProcess(processName);
                        
                        // If showMinecraftOnly is true, skip non-Minecraft processes
                        if (showMinecraftOnly && !isMinecraftProcess)
                            continue;

                        var tempProcess = new {
                            Process = process,
                            ProcessId = (uint)process.Id,
                            ProcessName = process.ProcessName,
                            IsRisky = IsRiskyProcess(processName),
                            HasMainWindow = !string.IsNullOrEmpty(process.MainWindowTitle),
                            MainWindowTitle = process.MainWindowTitle ?? "",
                            FilePath = GetProcessPath(process),
                            IsMinecraft = isMinecraftProcess
                        };

                        var safeProcess = new SafeProcessItem
                        {
                            Process = tempProcess.Process,
                            ProcessId = tempProcess.ProcessId,
                            ProcessName = tempProcess.ProcessName,
                            IsRisky = tempProcess.IsRisky,
                            HasMainWindow = tempProcess.HasMainWindow,
                            MainWindowTitle = tempProcess.MainWindowTitle,
                            FilePath = tempProcess.FilePath,
                            DisplayText = "", // Will be set after grouping
                            IsMinecraft = tempProcess.IsMinecraft
                        };

                        // Group by process name
                        string groupKey = tempProcess.ProcessName.ToLower();
                        if (!processGroups.ContainsKey(groupKey))
                        {
                            processGroups[groupKey] = new List<SafeProcessItem>();
                        }
                        processGroups[groupKey].Add(safeProcess);
                    }
                    catch
                    {
                        // Skip processes we can't access
                        continue;
                    }
                }

                // Process groups and create final list
                foreach (var group in processGroups)
                {
                    var groupProcesses = group.Value;
                    
                    if (groupProcesses.Count == 1)
                    {
                        // Single process - use standard display
                        var proc = groupProcesses[0];
                        proc.DisplayText = CreateMinecraftDisplayText(proc.ProcessName, proc.ProcessId, proc.IsRisky, proc.HasMainWindow, proc.MainWindowTitle, proc.IsMinecraft);
                        safeProcesses.Add(proc);
                    }
                    else
                    {
                        // Multiple processes with same name
                        // Prioritize those with main windows
                        var windowedProcesses = groupProcesses.Where(p => p.HasMainWindow).ToList();
                        var backgroundProcesses = groupProcesses.Where(p => !p.HasMainWindow).ToList();
                        
                        // Add windowed processes first
                        foreach (var proc in windowedProcesses)
                        {
                            if (windowedProcesses.Count > 1)
                            {
                                proc.DisplayText = CreateMinecraftDisplayTextWithCount(proc.ProcessName, proc.ProcessId, proc.IsRisky, proc.HasMainWindow, proc.MainWindowTitle, windowedProcesses.IndexOf(proc) + 1, proc.IsMinecraft);
                            }
                            else
                            {
                                proc.DisplayText = CreateMinecraftDisplayText(proc.ProcessName, proc.ProcessId, proc.IsRisky, proc.HasMainWindow, proc.MainWindowTitle, proc.IsMinecraft);
                            }
                            safeProcesses.Add(proc);
                        }
                        
                        // For background processes, only show one representative if there are many
                        if (backgroundProcesses.Count > 0)
                        {
                            if (backgroundProcesses.Count == 1)
                            {
                                var proc = backgroundProcesses[0];
                                proc.DisplayText = CreateMinecraftDisplayText(proc.ProcessName, proc.ProcessId, proc.IsRisky, proc.HasMainWindow, proc.MainWindowTitle, proc.IsMinecraft);
                                safeProcesses.Add(proc);
                            }
                            else
                            {
                                // Show grouped background processes
                                var representative = backgroundProcesses.OrderBy(p => p.ProcessId).First();
                                representative.DisplayText = CreateDisplayTextWithGroup(representative.ProcessName, backgroundProcesses.Count, representative.IsRisky);
                                safeProcesses.Add(representative);
                            }
                        }
                    }
                }

                // Sort: Minecraft processes first, then games/apps with windows, then other processes
                return safeProcesses
                    .OrderByDescending(p => p.IsMinecraft)
                    .ThenByDescending(p => p.HasMainWindow)
                    .ThenBy(p => p.IsRisky)
                    .ThenBy(p => p.ProcessName)
                    .ToList();
            }
            catch (Exception ex)
            {
                Logger.Error($"Error getting safe processes: {ex.Message}");
                return new List<SafeProcessItem>();
            }
        }

        public static List<SafeProcessItem> FilterProcesses(List<SafeProcessItem> processes, string searchText)
        {
            if (string.IsNullOrWhiteSpace(searchText))
                return processes;

            searchText = searchText.ToLower();
            return processes.Where(p => 
                p.ProcessName.ToLower().Contains(searchText) ||
                p.MainWindowTitle.ToLower().Contains(searchText) ||
                p.DisplayText.ToLower().Contains(searchText)
            ).ToList();
        }

        private static bool IsCriticalProcess(string processName)
        {
            return CriticalProcesses.Any(critical => processName.Contains(critical.ToLower()));
        }

        private static bool IsRiskyProcess(string processName)
        {
            return RiskyProcesses.Any(risky => processName.Contains(risky.ToLower()));
        }

        private static bool IsMinecraftProcess(string processName)
        {
            return MinecraftProcesses.Any(mc => processName.Contains(mc.ToLower()));
        }

        private static string GetProcessPath(Process process)
        {
            try
            {
                return process.MainModule?.FileName ?? "";
            }
            catch
            {
                return "";
            }
        }

        private static string CreateDisplayText(string processName, uint processId, bool isRisky, bool hasMainWindow, string mainWindowTitle)
        {
            string icon = hasMainWindow ? "🎮" : 
                         isRisky ? "⚠️" : "⚙️";

            string riskWarning = isRisky ? " [RISKY]" : "";
            
            if (hasMainWindow)
            {
                return $"{icon} {processName} (PID: {processId}) - {mainWindowTitle}{riskWarning}";
            }
            else
            {
                return $"{icon} {processName} (PID: {processId}){riskWarning}";
            }
        }

        private static string CreateDisplayTextWithCount(string processName, uint processId, bool isRisky, bool hasMainWindow, string mainWindowTitle, int instanceNumber)
        {
            string icon = hasMainWindow ? "🎮" : 
                         isRisky ? "⚠️" : "⚙️";

            string riskWarning = isRisky ? " [RISKY]" : "";
            
            if (hasMainWindow)
            {
                return $"{icon} {processName} #{instanceNumber} (PID: {processId}) - {mainWindowTitle}{riskWarning}";
            }
            else
            {
                return $"{icon} {processName} #{instanceNumber} (PID: {processId}){riskWarning}";
            }
        }

        private static string CreateDisplayTextWithGroup(string processName, int count, bool isRisky)
        {
            string icon = isRisky ? "⚠️" : "⚙️";
            string riskWarning = isRisky ? " [RISKY]" : "";
            
            return $"{icon} {processName} ({count} instances){riskWarning}";
        }

        private static string CreateMinecraftDisplayText(string processName, uint processId, bool isRisky, bool hasMainWindow, string mainWindowTitle, bool isMinecraft)
        {
            string icon = isMinecraft ? "⛏️" : 
                         hasMainWindow ? "🎮" : 
                         isRisky ? "⚠️" : "⚙️";

            string riskWarning = isRisky ? " [RISKY]" : "";
            string minecraftLabel = isMinecraft ? " [MINECRAFT]" : "";
            
            if (hasMainWindow)
            {
                return $"{icon} {processName} (PID: {processId}) - {mainWindowTitle}{minecraftLabel}{riskWarning}";
            }
            else
            {
                return $"{icon} {processName} (PID: {processId}){minecraftLabel}{riskWarning}";
            }
        }

        private static string CreateMinecraftDisplayTextWithCount(string processName, uint processId, bool isRisky, bool hasMainWindow, string mainWindowTitle, int instanceNumber, bool isMinecraft)
        {
            string icon = isMinecraft ? "⛏️" : 
                         hasMainWindow ? "🎮" : 
                         isRisky ? "⚠️" : "⚙️";

            string riskWarning = isRisky ? " [RISKY]" : "";
            string minecraftLabel = isMinecraft ? " [MINECRAFT]" : "";
            
            if (hasMainWindow)
            {
                return $"{icon} {processName} #{instanceNumber} (PID: {processId}) - {mainWindowTitle}{minecraftLabel}{riskWarning}";
            }
            else
            {
                return $"{icon} {processName} #{instanceNumber} (PID: {processId}){minecraftLabel}{riskWarning}";
            }
        }
    }

    public class SafeProcessItem
    {
        public required Process Process { get; set; }
        public uint ProcessId { get; set; }
        public required string ProcessName { get; set; }
        public required string DisplayText { get; set; }
        public bool IsRisky { get; set; }
        public bool HasMainWindow { get; set; }
        public required string MainWindowTitle { get; set; }
        public required string FilePath { get; set; }
        public bool IsMinecraft { get; set; }

        public override string ToString()
        {
            return DisplayText;
        }
    }
} 