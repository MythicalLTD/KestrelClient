using KestrelClientInjector.utils;
using System.Net.Http;
using System.IO;
using System.Diagnostics;
using System.Drawing;
using System.Windows.Forms;

namespace KestrelClientInjector.forms
{
    public partial class FrmNew : Form
    {
        public static string github_link = "https://github.com/MythicalLTD/KestrelClient/releases/latest/download/";
        public static string dll_download = github_link + "KestrelClient.dll";
        public static string mod_download = github_link + "mythicalclient-1.0.0.jar";
        public static string libDir = Path.Combine(Program.appWorkDir, "libs");
        public static List<string> requiredSystem32Dlls = new List<string>
        {
            "IMM32.dll",
            "kernel32.dll",
            "user32.dll",
            "WININET.dll",
            "IPHLPAPI.dll",
            "SHELL32.dll",
            "advapi32.dll",
            "ole32.dll",
            "WS2_32.dll",
        };
        private HttpClient httpClient;

        public FrmNew()
        {
            InitializeComponent();
            httpClient = new HttpClient();
            httpClient.Timeout = TimeSpan.FromSeconds(30);
            httpClient.DefaultRequestHeaders.UserAgent.ParseAdd("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
        }

        private void btnInjector_Click(object sender, EventArgs e)
        {
            FrmMain frm = new FrmMain();
            frm.Show();
            this.Hide();
        }

        private async void FrmNew_Load(object sender, EventArgs e)
        {
            LoadMinecraftProcesses();
            CheckSystemDependencies();
            await CheckAndDownloadMissingFiles(); // Check if files exist, download if missing
            await InjectModAsync();
            UpdateInjectButtonState();
        }

        private void CheckSystemDependencies()
        {
            if (!CheckRequiredSystemDlls())
            {
                string missingDlls = GetMissingSystemDlls();
                Logger.Warn($"Missing system DLLs detected: {missingDlls}");
                lblProcessCount.Text = $"⚠️ Missing system DLLs: {missingDlls}";
                lblProcessCount.ForeColor = Color.FromArgb(255, 193, 7); // Orange
            }
        }

        private async Task CheckAndDownloadMissingFiles()
        {
            string dllPath = Path.Combine(libDir, "KestrelClient.dll");
            string modPath = Path.Combine(libDir, "mythicalclient-1.0.0.jar");
            
            bool needsDll = !File.Exists(dllPath) && !File.Exists(dllPath + ".new");
            bool needsMod = !File.Exists(modPath);
            
            if (needsDll || needsMod)
            {
                Logger.Info("Missing client files detected. Starting initial download...");
                lblProcessCount.Text = "⏳ Downloading client files for first time setup...";
                lblProcessCount.ForeColor = Color.FromArgb(33, 150, 243); // Blue
                
                try
                {
                    await DownloadMissingFiles(needsDll, needsMod);
                    Logger.Info("Initial client files download completed successfully");
                    lblProcessCount.Text = "✅ Client files ready!";
                    lblProcessCount.ForeColor = Color.FromArgb(76, 175, 80); // Green
                    
                    // Reset to normal status after 2 seconds
                    await Task.Delay(2000);
                    if (lstMinecraftProcesses.Items.Count > 0)
                    {
                        lblProcessCount.Text = $"Found {lstMinecraftProcesses.Items.Count} Minecraft-related processes";
                        lblProcessCount.ForeColor = Color.FromArgb(76, 175, 80);
                    }
                }
                catch (Exception ex)
                {
                    Logger.Error($"Failed to download initial client files: {ex.Message}");
                    lblProcessCount.Text = "❌ Failed to download client files. Use 'Update Client' button to retry.";
                    lblProcessCount.ForeColor = Color.FromArgb(244, 67, 54); // Red
                }
            }
        }

        private async Task DownloadMissingFiles(bool needsDll, bool needsMod)
        {
            // Ensure directories exist
            if (!Directory.Exists(Program.appWorkDir))
            {
                Directory.CreateDirectory(Program.appWorkDir);
            }
            if (!Directory.Exists(libDir))
            {
                Directory.CreateDirectory(libDir);
            }

            var downloadTasks = new List<Task>();
            
            if (needsDll)
            {
                downloadTasks.Add(DownloadFileWithErrorHandling(dll_download, Path.Combine(libDir, "KestrelClient.dll"), "KestrelClient.dll"));
            }
            
            if (needsMod)
            {
                downloadTasks.Add(DownloadFileWithErrorHandling(mod_download, Path.Combine(libDir, "mythicalclient-1.0.0.jar"), "mythicalclient-1.0.0.jar"));
            }
            
            if (downloadTasks.Count > 0)
            {
                await Task.WhenAll(downloadTasks);
            }
        }

        private async Task InjectModAsync()
        {
            await Task.Run(() => {
                var modPaths = new[]
                {
                    // Minecraft mods folder
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft", "mods"),
                    // Feather client mods folder
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".feather", "user-mods", "1.8.9")
                };

                string sourceModPath = Path.Combine(libDir, "mythicalclient-1.0.0.jar");
                
                if (!File.Exists(sourceModPath))
                    {
                        Logger.Error("mythicalclient-1.0.0.jar does not exist in libs folder");
                        Alert.Error("mythicalclient-1.0.0.jar does not exist in libs folder. Please restart the application to download the mod.");
                        return;
                    }

                bool anyModInjected = false;

                foreach (var modPath in modPaths)
                {
                    if (Directory.Exists(modPath))
                    {
                        try
                        {
                            string destModPath = Path.Combine(modPath, "mythicalclient-1.0.0.jar");
                            
                            // Delete existing mod if it exists
                            if (File.Exists(destModPath))
                            {
                                File.Delete(destModPath);
                            }
                            
                            // Copy the mod
                            File.Copy(sourceModPath, destModPath, true);
                            Logger.Info($"Successfully copied mythicalclient-1.0.0.jar to {modPath}");
                            anyModInjected = true;
                        }
                        catch (Exception ex)
                        {
                            Logger.Error($"Error copying mythicalclient-1.0.0.jar to {modPath}: {ex.Message}");
                        }
                    }
                }

                if (!anyModInjected)
                {
                    Logger.Warn("No mod directories found. Please make sure you have run Minecraft with Forge/Fabric or Feather client at least once.");
                    Alert.Warning("No mod directories found. Please make sure you have run Minecraft with Forge/Fabric or Feather client at least once.");
                }
            });
        }

        private void btnRefreshProcesses_Click(object sender, EventArgs e)
        {
            LoadMinecraftProcesses();
            UpdateInjectButtonState();
        }

        private async void btnUpdateClient_Click(object sender, EventArgs e)
        {
            await UpdateClientAsync();
        }

        private async Task UpdateClientAsync()
        {
            try
            {
                btnUpdateClient.Enabled = false;
                btnUpdateClient.Text = "⏳ Checking...";
                
                // Check if any Minecraft processes are running - BLOCK UPDATE if found
                if (AreMinecraftProcessesRunning())
                {
                    Logger.Warn("Update blocked: Minecraft processes are running");
                    Alert.Error("🚫 Close Minecraft first before updating!");
                    
                    btnUpdateClient.Text = "🚫 Close MC First";
                    await Task.Delay(3000);
                    btnUpdateClient.Text = "🔄 Update Client";
                    btnUpdateClient.Enabled = true;
                    return;
                }
                
                // Check if DLL is in use - BLOCK UPDATE if found
                string dllPath = Path.Combine(libDir, "KestrelClient.dll");
                if (IsDllInUse(dllPath))
                {
                    Logger.Warn("Update blocked: KestrelClient.dll is in use");
                    Alert.Error("🚫 DLL is in use! Close Minecraft first.");
                    
                    btnUpdateClient.Text = "🚫 DLL In Use";
                    await Task.Delay(3000);
                    btnUpdateClient.Text = "🔄 Update Client";
                    btnUpdateClient.Enabled = true;
                    return;
                }
                
                Logger.Info("Safety checks passed - proceeding with update");
                btnUpdateClient.Text = "⏳ Updating...";
                
                // Perform the update
                await DownloadLibsAsync();
                
                Alert.Success("Client update completed successfully!");
                btnUpdateClient.Text = "✅ Updated";
                
                // Update button states
                UpdateInjectButtonState();
                
                // Reset button after 3 seconds
                await Task.Delay(3000);
                btnUpdateClient.Text = "🔄 Update Client";
                btnUpdateClient.Enabled = true;
            }
            catch (Exception ex)
            {
                Alert.Error($"Update failed: {ex.Message}");
                Logger.Error($"Update error: {ex.Message}");
                btnUpdateClient.Text = "❌ Update Failed";
                await Task.Delay(3000);
                btnUpdateClient.Text = "🔄 Update Client";
                btnUpdateClient.Enabled = true;
            }
        }

        private bool AreMinecraftProcessesRunning()
        {
            try
            {
                var allProcesses = Process.GetProcesses();
                foreach (var process in allProcesses)
                {
                    try
                    {
                        if (process.HasExited) continue;
                        
                        string processName = process.ProcessName.ToLower();
                        if (IsMinecraftRelatedProcess(processName) && !string.IsNullOrEmpty(process.MainWindowTitle))
                        {
                            return true;
                        }
                    }
                    catch
                    {
                        continue;
                    }
                }
                }
                catch (Exception ex)
            {
                Logger.Error($"Error checking for running processes: {ex.Message}");
            }
            
            return false;
        }

        private bool IsDllInUse(string dllPath)
        {
            if (!File.Exists(dllPath))
                return false;
                
            try
            {
                // Try to open the file for exclusive write access
                using (var fs = new FileStream(dllPath, FileMode.Open, FileAccess.ReadWrite, FileShare.None))
                {
                    // If we can open it exclusively, it's not in use
                    return false;
                }
            }
            catch (UnauthorizedAccessException)
            {
                // File is in use
                return true;
            }
            catch (IOException)
            {
                // File is in use
                return true;
            }
            catch
            {
                // Other error, assume not in use
                return false;
            }
        }

        private bool IsDllInUseByProcess(string dllPath, int processId)
        {
            if (!File.Exists(dllPath))
                return false;

            try
            {
                // Get the process to check
                var process = Process.GetProcessById(processId);
                if (process.HasExited)
                    return false;

                // Check if the DLL is loaded in this process by looking at modules
                var modules = process.Modules;
                foreach (ProcessModule module in modules)
                {
                    if (module.FileName != null && 
                        Path.GetFileName(module.FileName).Equals(Path.GetFileName(dllPath), StringComparison.OrdinalIgnoreCase))
                    {
                        Logger.Info($"Found DLL {Path.GetFileName(dllPath)} already loaded in process {process.ProcessName} (PID: {processId})");
                        return true;
                    }
                }

                return false;
            }
            catch (Exception ex)
            {
                Logger.Warn($"Error checking if DLL is in use by process {processId}: {ex.Message}");
                return false;
            }
        }

        private void LoadMinecraftProcesses()
        {
            lstMinecraftProcesses.Items.Clear();
            
            try
            {
                var minecraftProcesses = GetMinecraftProcesses();
                
                foreach (var process in minecraftProcesses)
                {
                    lstMinecraftProcesses.Items.Add(process);
                }
                
                lblProcessCount.Text = $"Found {minecraftProcesses.Count} Minecraft-related processes";
                
                if (minecraftProcesses.Count == 0)
                {
                    lblProcessCount.Text = "No Minecraft processes found. Start Minecraft, Feather Client, or Java applications.";
                    lblProcessCount.ForeColor = Color.FromArgb(255, 193, 7); // Orange
                }
                else
                {
                    lblProcessCount.ForeColor = Color.FromArgb(76, 175, 80); // Green
                }
            }
            catch (Exception ex)
            {
                Logger.Error($"Error loading Minecraft processes: {ex.Message}");
                lblProcessCount.Text = $"Error loading processes: {ex.Message}";
                lblProcessCount.ForeColor = Color.FromArgb(244, 67, 54); // Red
            }
        }

        private void lstMinecraftProcesses_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateInjectButtonState();
        }

        private void UpdateInjectButtonState()
        {
            bool hasProcess = lstMinecraftProcesses.SelectedItem != null;
            bool hasDll = HasValidDll();
            bool hasSystemDlls = CheckRequiredSystemDlls();
            
            Logger.Info($"UpdateInjectButtonState - hasProcess: {hasProcess}, hasDll: {hasDll}, hasSystemDlls: {hasSystemDlls}");
            
            btnInject.Enabled = hasProcess && hasDll && hasSystemDlls;
            
            if (!hasProcess)
            {
                btnInject.Text = "💉 Select Process";
            }
            else if (!hasDll)
            {
                btnInject.Text = "⏳ Downloading DLL...";
            }
            else if (!hasSystemDlls)
            {
                btnInject.Text = "❌ Missing System DLLs";
            }
            else
            {
                btnInject.Text = "💉 INJECT";
            }
            
            Logger.Info($"Button state updated - Enabled: {btnInject.Enabled}, Text: {btnInject.Text}");
        }

        private bool HasValidDll()
        {
            string dllPath = Path.Combine(libDir, "KestrelClient.dll");
            string newDllPath = Path.Combine(libDir, "KestrelClient.dll.new");
            
            Logger.Info($"HasValidDll - Checking DLL paths:");
            Logger.Info($"  Main DLL: {dllPath} - Exists: {File.Exists(dllPath)}");
            Logger.Info($"  New DLL: {newDllPath} - Exists: {File.Exists(newDllPath)}");
            
            // Check for main DLL first
            if (File.Exists(dllPath))
            {
                Logger.Info($"HasValidDll - Found main DLL");
                return true;
            }
            
            // Check for new version
            if (File.Exists(newDllPath))
            {
                Logger.Info($"HasValidDll - Found new DLL");
                return true;
            }
            
            Logger.Warn($"HasValidDll - No valid DLL found");
            return false;
        }

        private string GetDllPath()
        {
            string dllPath = Path.Combine(libDir, "KestrelClient.dll");
            string newDllPath = Path.Combine(libDir, "KestrelClient.dll.new");
            
            Logger.Info($"GetDllPath - Checking DLL paths:");
            Logger.Info($"  Main DLL: {dllPath} - Exists: {File.Exists(dllPath)}");
            Logger.Info($"  New DLL: {newDllPath} - Exists: {File.Exists(newDllPath)}");
            
            // Prefer the new version if it exists
            if (File.Exists(newDllPath))
            {
                Logger.Info($"GetDllPath - Using new DLL: {newDllPath}");
                return newDllPath;
            }
            
            Logger.Info($"GetDllPath - Using main DLL: {dllPath}");
            return dllPath;
        }

        private bool CheckRequiredSystemDlls()
        {
            string system32Path = Environment.GetFolderPath(Environment.SpecialFolder.System);
            
            foreach (string dllName in requiredSystem32Dlls)
            {
                string dllPath = Path.Combine(system32Path, dllName);
                if (!File.Exists(dllPath))
                {
                    Logger.Warn($"Required system DLL not found: {dllName} at {dllPath}");
                    return false;
                }
            }
            
            return true;
        }

        private string GetMissingSystemDlls()
        {
            string system32Path = Environment.GetFolderPath(Environment.SpecialFolder.System);
            var missingDlls = new List<string>();
            
            foreach (string dllName in requiredSystem32Dlls)
            {
                string dllPath = Path.Combine(system32Path, dllName);
                if (!File.Exists(dllPath))
                {
                    missingDlls.Add(dllName);
                }
            }
            
            return string.Join(", ", missingDlls);
        }

        private List<ProcessInfo> GetMinecraftProcesses()
        {
            var processes = new List<ProcessInfo>();
            var processGroups = new Dictionary<string, List<ProcessInfo>>();
            
            try
            {
                var allProcesses = Process.GetProcesses();
                
                foreach (var process in allProcesses)
                {
                    try
                    {
                        if (process.HasExited) continue;
                        
                        string processName = process.ProcessName.ToLower();
                        
                        // Check if it's a Minecraft-related process
                        if (IsMinecraftRelatedProcess(processName))
                        {
                            string windowTitle = process.MainWindowTitle ?? "";
                            
                            // Skip background processes without windows
                            if (string.IsNullOrEmpty(windowTitle)) continue;
                            
                            // Skip launcher processes (only show actual game instances)
                            if (IsLauncherProcess(process.ProcessName, windowTitle)) continue;
                            
                            string processPath = GetProcessPath(process);
                            Icon processIcon = ExtractProcessIcon(processPath);
                            
                            var processInfo = new ProcessInfo
                            {
                                Process = process,
                                ProcessName = process.ProcessName,
                                ProcessId = process.Id,
                                WindowTitle = windowTitle,
                                HasWindow = !string.IsNullOrEmpty(windowTitle),
                                ProcessPath = processPath,
                                Icon = processIcon,
                                ClientType = DetermineClientType(processName, windowTitle)
                            };
                            
                            // Group processes by name
                            string groupKey = process.ProcessName.ToLower();
                            if (!processGroups.ContainsKey(groupKey))
                            {
                                processGroups[groupKey] = new List<ProcessInfo>();
                            }
                            processGroups[groupKey].Add(processInfo);
                        }
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
                        // Single process
                        processes.Add(groupProcesses[0]);
                    }
                    else
                    {
                        // Multiple processes with same name - show the one with the best window title
                        var bestProcess = groupProcesses
                            .OrderByDescending(p => p.WindowTitle.Length)
                            .ThenBy(p => p.ProcessId)
                            .First();
                        
                        // Update display to show count
                        bestProcess.ProcessCount = groupProcesses.Count;
                        processes.Add(bestProcess);
                    }
                }
                }
                catch (Exception ex)
            {
                Logger.Error($"Error getting processes: {ex.Message}");
            }
            
            return processes.OrderByDescending(p => p.HasWindow)
                           .ThenBy(p => p.ProcessName)
                           .ToList();
        }

        private bool IsMinecraftRelatedProcess(string processName)
        {
            var minecraftProcesses = new[]
            {
                "javaw", "java", "minecraft", "minecraftlauncher",
                "feather", "lunar", "badlion", "pvplounge", "cheatbreaker",
                "forge", "fabric", "optifine", "technic", "atlauncher",
                "multimc", "polymc", "prismmc", "curseforge", "twitch",
                "ftb", "modrinth", "gdlauncher", "sklauncher", "tlauncher",
                "shiginima", "ftbapp", "atlauncher", "gdlauncher",
                "modrinthapp", "curseforge", "tlauncher", "sklauncher"
            };
            
            return minecraftProcesses.Any(mc => processName.Contains(mc));
        }

        private bool IsLauncherProcess(string processName, string windowTitle)
        {
            var launcherKeywords = new[]
            {
                "launcher", "installer", "updater", "setup", "bootstrapper",
                "lunar launcher", "feather launcher", "minecraft launcher",
                "badlion launcher", "technic launcher", "atlauncher",
                "multimc", "polymc", "prismmc", "curseforge", "twitch",
                "ftb launcher", "modrinth app", "gdlauncher", "tlauncher",
                "sklauncher", "shiginima launcher"
            };
            
            string name = processName.ToLower();
            string title = windowTitle.ToLower();
            
            return launcherKeywords.Any(launcher => 
                name.Contains(launcher) || title.Contains(launcher));
        }

        private string DetermineClientType(string processName, string windowTitle)
        {
            string name = processName.ToLower();
            string title = windowTitle.ToLower();
            
            if (name.Contains("feather")) return "Feather Client";
            if (name.Contains("lunar")) return "Lunar Client";
            if (name.Contains("badlion")) return "Badlion Client";
            if (name.Contains("pvplounge")) return "PVP Lounge";
            if (name.Contains("cheatbreaker")) return "CheatBreaker";
            if (name.Contains("tlauncher") || name.Contains("sklauncher")) return "Cracked Launcher";
            if (name.Contains("forge")) return "Forge";
            if (name.Contains("fabric")) return "Fabric";
            if (title.Contains("minecraft") || name.Contains("minecraft")) return "Minecraft";
            if (name.Contains("java")) return "Java Application";
            
            return "Unknown";
        }

        private void lstMinecraftProcesses_DrawItem(object sender, DrawItemEventArgs e)
        {
            if (e.Index < 0 || e.Index >= lstMinecraftProcesses.Items.Count)
                return;

            e.DrawBackground();
            
            var processInfo = lstMinecraftProcesses.Items[e.Index] as ProcessInfo;
            if (processInfo == null) return;
            
            var bounds = e.Bounds;
            var iconSize = 32;
            var padding = 8;
            
            // Draw process icon
            if (processInfo.Icon != null)
            {
                var iconRect = new Rectangle(bounds.X + padding, bounds.Y + (bounds.Height - iconSize) / 2, iconSize, iconSize);
                e.Graphics.DrawIcon(processInfo.Icon, iconRect);
            }
            else
            {
                // Draw default icon using emoji
                var iconText = GetProcessIcon(processInfo.ClientType);
                var iconFont = new Font("Segoe UI Emoji", 20, FontStyle.Regular);
                var iconRect = new Rectangle(bounds.X + padding, bounds.Y + (bounds.Height - 24) / 2, 32, 24);
                var iconFormat = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Center };
                e.Graphics.DrawString(iconText, iconFont, Brushes.White, iconRect, iconFormat);
                iconFont.Dispose();
            }
            
            // Draw process name
            var nameFont = new Font("Trebuchet MS", 11, FontStyle.Bold);
            var nameRect = new Rectangle(bounds.X + iconSize + padding * 2, bounds.Y + 8, bounds.Width - iconSize - padding * 3, 20);
            e.Graphics.DrawString(processInfo.ProcessName, nameFont, Brushes.White, nameRect);
            
            // Draw window title
            var titleFont = new Font("Trebuchet MS", 9, FontStyle.Regular);
            var titleColor = Color.FromArgb(200, 200, 200);
            var titleRect = new Rectangle(bounds.X + iconSize + padding * 2, bounds.Y + 28, bounds.Width - iconSize - padding * 3, 16);
            e.Graphics.DrawString(processInfo.WindowTitle, titleFont, new SolidBrush(titleColor), titleRect);
            
            // Draw client type and process count
            var infoFont = new Font("Trebuchet MS", 8, FontStyle.Italic);
            var infoColor = Color.FromArgb(150, 150, 150);
            var infoText = processInfo.ClientType;
            if (processInfo.ProcessCount > 1)
            {
                infoText += $" ({processInfo.ProcessCount} instances)";
            }
            var infoRect = new Rectangle(bounds.X + iconSize + padding * 2, bounds.Y + 44, bounds.Width - iconSize - padding * 3, 12);
            e.Graphics.DrawString(infoText, infoFont, new SolidBrush(infoColor), infoRect);
            
            // Clean up fonts
            nameFont.Dispose();
            titleFont.Dispose();
            infoFont.Dispose();
        }

        private void lstMinecraftProcesses_MeasureItem(object sender, MeasureItemEventArgs e)
        {
            e.ItemHeight = 60;
        }

        private string GetProcessIcon(string clientType)
        {
            return clientType switch
            {
                "Minecraft" => "⛏️",
                "Feather Client" => "🪶",
                "Lunar Client" => "🌙",
                "Badlion Client" => "🦁",
                "PVP Lounge" => "⚔️",
                "CheatBreaker" => "🛡️",
                "Forge" => "🔧",
                "Fabric" => "🧵",
                "Cracked Launcher" => "🔓",
                "Java Application" => "☕",
                _ => "📦"
            };
        }

        private string GetProcessPath(Process process)
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

        private Icon ExtractProcessIcon(string processPath)
        {
            try
            {
                if (!string.IsNullOrEmpty(processPath) && File.Exists(processPath))
                {
                    return Icon.ExtractAssociatedIcon(processPath);
                }
            }
            catch
            {
                // Return default icon if we can't extract it
            }
            return null;
        }

        private async void btnInject_Click(object sender, EventArgs e)
        {
            Logger.Info("=== INJECTION STARTED ===");
            
            if (lstMinecraftProcesses.SelectedItem == null)
            {
                Logger.Warn("No process selected for injection");
                Alert.Warning("Please select a Minecraft process to inject into.");
                return;
            }

            var selectedProcess = lstMinecraftProcesses.SelectedItem as ProcessInfo;
            if (selectedProcess == null) 
            {
                Logger.Error("Selected process is null");
                return;
            }

            Logger.Info($"Selected process: {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId})");
            Logger.Info($"Process window title: {selectedProcess.WindowTitle}");
            Logger.Info($"Process client type: {selectedProcess.ClientType}");

            // Check if already injected (DLL is in use by this process)
            string dllPath = GetDllPath();
            if (IsDllInUseByProcess(dllPath, selectedProcess.ProcessId))
            {
                Logger.Warn($"Process {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId}) already has KestrelClient DLL in use");
                MessageBox.Show(
                    "🚫 Already Injected! 🚫\n\n" +
                    "KestrelClient is already injected into this process.\n\n" +
                    "Please do not try to inject again as you will glitch the injection process and you might either crash Minecraft or break the injection process and you will get caught by staff members doing checks on you :)\n\n" +
                    "If you need to restart, close Minecraft completely and start fresh!",
                    "Already Injected",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }
            Logger.Info($"Using DLL path: {dllPath}");
            
            if (!File.Exists(dllPath))
            {
                Logger.Error($"DLL file not found at: {dllPath}");
                Alert.Error("KestrelClient.dll not found. Please wait for download to complete or use 'Update Client' button.");
                return;
            }

            Logger.Info($"DLL file exists, size: {new FileInfo(dllPath).Length} bytes");

            // Check for required system DLLs
            if (!CheckRequiredSystemDlls())
            {
                string missingDlls = GetMissingSystemDlls();
                Logger.Error($"Missing system DLLs: {missingDlls}");
                Alert.Error($"Required system DLLs are missing: {missingDlls}\n\nPlease ensure your Windows installation is complete or reinstall Visual C++ Redistributables.");
                return;
            }

            Logger.Info("All pre-injection checks passed, starting injection...");
            await PerformInjection(selectedProcess, dllPath);
        }

        private async Task PerformInjection(ProcessInfo processInfo, string dllPath)
        {
            try
            {
                Logger.Info("=== PERFORMING INJECTION ===");
                btnInject.Enabled = false;
                btnInject.Text = "⏳ Injecting...";
                
                // Verify process is still running
                try
                {
                    Logger.Info($"Refreshing process {processInfo.ProcessName} (PID: {processInfo.ProcessId})");
                    processInfo.Process.Refresh();
                    
                    if (processInfo.Process.HasExited)
                    {
                        Logger.Warn($"Process {processInfo.ProcessName} has exited");
                        Alert.Error("The selected process has exited.");
                        LoadMinecraftProcesses();
                        return;
                    }
                    
                    Logger.Info($"Process {processInfo.ProcessName} is still running");
                }
                catch (Exception ex)
                {
                    Logger.Error($"Failed to access process {processInfo.ProcessName}: {ex.Message}");
                    Alert.Error("Failed to access the selected process. It may have exited or you may not have sufficient permissions.");
                    LoadMinecraftProcesses();
                    return;
                }

                // Perform injection
                Logger.Info($"Creating DllInjector instance");
                var dllInjector = DllInjector.Instance;
                
                Logger.Info($"Calling dllInjector.Inject with PID: {processInfo.ProcessId}, DLL: {dllPath}, IgnoreSecurity: true");
                var result = await dllInjector.Inject((uint)processInfo.ProcessId, dllPath, true);
                
                Logger.Info($"Injection result: {result}");

                switch (result)
                {
                    case DllInjectionResult.Success:
                        Logger.Info($"SUCCESS: Injected into {processInfo.ProcessName} (PID: {processInfo.ProcessId})");
                        Alert.Success($"Successfully injected KestrelClient.dll into {processInfo.ProcessName}!");
                        lblProcessCount.Text = $"✅ Injected into {processInfo.ProcessName} (PID: {processInfo.ProcessId})";
                        lblProcessCount.ForeColor = Color.FromArgb(76, 175, 80);
                        break;

                    case DllInjectionResult.AlreadyInjected:
                        Logger.Warn($"ALREADY INJECTED: {processInfo.ProcessName} (PID: {processInfo.ProcessId})");
                        Alert.Warning("DLL is already injected into this process, but injection was attempted anyway.");
                        lblProcessCount.Text = $"⚠️ Already injected into {processInfo.ProcessName}";
                        lblProcessCount.ForeColor = Color.FromArgb(255, 193, 7);
                        break;

                    case DllInjectionResult.DllNotFound:
                        Logger.Error($"DLL NOT FOUND: {dllPath}");
                        Alert.Error("The KestrelClient.dll file was not found.");
                        break;

                    case DllInjectionResult.GameProcessNotFound:
                        Logger.Error($"GAME PROCESS NOT FOUND: PID {processInfo.ProcessId}");
                        Alert.Error("The target process could not be found or accessed.");
                        break;

                    case DllInjectionResult.AccessDenied:
                        Logger.Error($"ACCESS DENIED: PID {processInfo.ProcessId}");
                        Alert.Error("Access denied. Try running as administrator.");
                        break;

                    case DllInjectionResult.InjectionFailed:
                        Logger.Error($"INJECTION FAILED: PID {processInfo.ProcessId}");
                        Alert.Error("DLL injection failed. Make sure you have sufficient privileges.");
                        break;
                        
                    default:
                        Logger.Error($"UNKNOWN RESULT: {result}");
                        Alert.Error($"Unknown injection result: {result}");
                        break;
                }
                
                Logger.Info("=== INJECTION COMPLETED ===");
            }
            catch (Exception ex)
            {
                Logger.Error($"EXCEPTION during injection: {ex.Message}");
                Logger.Error($"Exception type: {ex.GetType().Name}");
                Logger.Error($"Stack trace: {ex.StackTrace}");
                Alert.Error($"Error during injection: {ex.Message}");
            }
            finally
            {
                Logger.Info("Resetting injection button state");
                btnInject.Enabled = true;
                UpdateInjectButtonState();
            }
        }

        private async Task DownloadLibsAsync()
        {
            // Ensure directories exist
            if (!Directory.Exists(Program.appWorkDir))
            {
                Directory.CreateDirectory(Program.appWorkDir);
            }
            if (!Directory.Exists(libDir))
            {
                Directory.CreateDirectory(libDir);
            }

            // Download files with individual error handling
            var downloadTasks = new[]
            {
                DownloadFileWithErrorHandling(dll_download, Path.Combine(libDir, "KestrelClient.dll"), "KestrelClient.dll"),
                DownloadFileWithErrorHandling(mod_download, Path.Combine(libDir, "mythicalclient-1.0.0.jar"), "mythicalclient-1.0.0.jar")
            };

            try
            {
                await Task.WhenAll(downloadTasks);
                Logger.Info("Download process completed");
            }
            catch (Exception ex)
            {
                Logger.Error($"Error during download process: {ex.Message}");
            }
        }

        private async Task DownloadFileWithErrorHandling(string url, string filePath, string fileName)
        {
            try
            {
                await DownloadFileAsync(url, filePath, fileName);
            }
            catch (Exception ex)
            {
                Logger.Error($"Failed to download {fileName}: {ex.Message}");
                // Don't throw - let other downloads continue
            }
        }

        private async Task DownloadFileAsync(string url, string filePath, string fileName)
        {
            try
            {
                // Try to delete existing file with retry logic
                if (File.Exists(filePath))
                {
                    await TryDeleteFileWithRetry(filePath, fileName);
                }

                Logger.Info($"Starting download of {fileName}...");
                var response = await httpClient.GetAsync(url);
                
                if (response.IsSuccessStatusCode)
                {
                    // Download to temporary file first
                    string tempFilePath = filePath + ".tmp";
                    using (var fs = new FileStream(tempFilePath, FileMode.Create, FileAccess.Write, FileShare.None))
                    {
                        await response.Content.CopyToAsync(fs);
                    }
                    
                    // Try to replace the original file
                    await TryReplaceFile(tempFilePath, filePath, fileName);
                    Logger.Info($"Successfully downloaded {fileName}");
                }
                else
                {
                    Logger.Error($"Failed to download {fileName}: {response.StatusCode}");
                    throw new HttpRequestException($"Failed to download {fileName}: {response.StatusCode}");
                }
            }
            catch (Exception ex)
            {
                Logger.Error($"Error downloading {fileName}: {ex.Message}");
                throw;
            }
        }

        private async Task TryDeleteFileWithRetry(string filePath, string fileName, int maxRetries = 3)
        {
            for (int i = 0; i < maxRetries; i++)
            {
                try
                {
                    File.Delete(filePath);
                    return; // Success
                }
                catch (UnauthorizedAccessException)
                {
                    if (i == maxRetries - 1)
                    {
                        Logger.Warn($"Cannot delete {fileName} - file is in use. Will download to temporary location.");
                        return;
                    }
                    await Task.Delay(1000); // Wait 1 second before retry
                }
                catch (Exception ex)
                {
                    Logger.Warn($"Failed to delete {fileName} (attempt {i + 1}): {ex.Message}");
                    if (i == maxRetries - 1) return;
                    await Task.Delay(1000);
                }
            }
        }

        private async Task TryReplaceFile(string tempFilePath, string targetFilePath, string fileName)
        {
            try
            {
                // Check if target file exists
                if (File.Exists(targetFilePath))
                {
                    // Try to replace the file directly
                    File.Replace(tempFilePath, targetFilePath, null);
                    }
                    else
                    {
                    // Target file doesn't exist, just move temp file to target location
                    File.Move(tempFilePath, targetFilePath);
                }
            }
            catch (UnauthorizedAccessException)
            {
                // File is in use, keep the temp file and log warning
                Logger.Warn($"Cannot replace {fileName} - file is in use. Downloaded version saved as {tempFilePath}");
                
                // Try to rename temp file to a versioned name
                string versionedPath = $"{targetFilePath}.new";
                try
                {
                    File.Move(tempFilePath, versionedPath);
                    Logger.Info($"Downloaded version saved as {versionedPath}. Please restart the application to use the new version.");
                }
                catch
                {
                    // If even that fails, just keep the temp file
                    Logger.Info($"Downloaded version saved as {tempFilePath}. Please restart the application to use the new version.");
                }
            }
            catch (FileNotFoundException)
            {
                // Original file was deleted but temp file still exists, just move it
                try
                {
                    File.Move(tempFilePath, targetFilePath);
                    Logger.Info($"Successfully moved {fileName} to target location.");
                }
                catch (Exception moveEx)
                {
                    Logger.Error($"Failed to move {fileName}: {moveEx.Message}");
                    throw;
                }
            }
            catch (Exception ex)
            {
                Logger.Error($"Failed to replace {fileName}: {ex.Message}");
                
                // Try to save as versioned file instead of failing completely
                string versionedPath = $"{targetFilePath}.new";
                try
                {
                    File.Move(tempFilePath, versionedPath);
                    Logger.Info($"Downloaded version saved as {versionedPath} due to error: {ex.Message}");
                }
                catch (Exception moveEx)
                {
                    Logger.Error($"Failed to save versioned file: {moveEx.Message}");
                    // Clean up temp file
                    try
                    {
                        File.Delete(tempFilePath);
                    }
                    catch { }
                    throw;
                }
            }
        }
    }

    public class ProcessInfo
    {
        public Process Process { get; set; }
        public string ProcessName { get; set; }
        public int ProcessId { get; set; }
        public string WindowTitle { get; set; }
        public bool HasWindow { get; set; }
        public string ProcessPath { get; set; }
        public Icon Icon { get; set; }
        public string ClientType { get; set; }
        public int ProcessCount { get; set; } = 1;
    }
}
