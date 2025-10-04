using KestrelClientInjector.utils;

namespace KestrelClientInjector.forms
{
    public partial class FrmMain : Form
    {
        private List<SafeProcessItem> allProcesses = new List<SafeProcessItem>();
        private HashSet<uint> injectedProcessIds = new HashSet<uint>();
        public SafeProcessItem? SelectedProcess { get; private set; }
        private string? selectedDllPath = null;

        public FrmMain()
        {
            InitializeComponent();
            SetupUI();
        }

        private void SetupUI()
        {
            btnNext.Text = "💉 INJECT";
            btnNext.Enabled = true;

            pbProgress.Visible = false;
            pbProgress.Value = 0;

            label1.Text = "Ready - Select Minecraft process and DLL file";

            txtDLLPath.PlaceholderText = "Select DLL file to inject...";
            txtDLLPath.ReadOnly = true;


            cbIgnoreSecurity.CheckedChanged += cbIgnoreSecurity_CheckedChanged;
            cbIgnoreSecurity.Checked = false;

            btnPickDLL.Click += btnPickDLL_Click;

#pragma warning disable CS8600 // Converting null literal or possible null value to non-nullable type.
            string lastDllPath = RegistryConfig.GetValue("last_dll_path", "");
#pragma warning restore CS8600 // Converting null literal or possible null value to non-nullable type.
            if (!string.IsNullOrEmpty(lastDllPath))
            {
                txtDLLPath.Text = lastDllPath;
            }
        }

        private void FrmMain_Load(object sender, EventArgs e)
        {
            bool eulaAccepted = RegistryConfig.GetValue("eula_accepted", false);
            if (!eulaAccepted)
            {
                string agreement = "Software Agreement\n\n" +
                    "By downloading, installing, or using this software (the Client), you agree to the following:\n\n" +
                    "No Responsibility for Bans\n" +
                    "The creators and owners of this Client are not responsible if you are banned, suspended, or penalized in any way from any service, platform, or server while using this software.\n\n" +
                    "No Responsibility for System Issues\n" +
                    "The Client is provided \"as is,\" without any guarantees. We are not responsible for any issues, damages, or malfunctions to your computer, operating system, files, or data that may occur while or after using the software.\n\n" +
                    "Use at Your Own Risk\n" +
                    "You accept that you use this Client entirely at your own risk.\n\n" +
                    "No Liability\n" +
                    "The owner(s) of this product hold no liability for any direct, indirect, incidental, or consequential damages resulting from the use of the Client.\n\n" +
                    "Do you accept and agree to these terms?";

                DialogResult result = MessageBox.Show(
                    agreement,
                    "Software Agreement",
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Warning,
                    MessageBoxDefaultButton.Button2
                );

                if (result == DialogResult.Yes)
                {
                    RegistryConfig.SetValue("eula_accepted", true);
                }
                else
                {
                    Application.Exit();
                    return;
                }
            }

            LoadProcesses();
        }

        private void LoadProcesses()
        {
            try
            {
                lbPrograms.Items.Clear();
                allProcesses.Clear();
                UpdateInjectButtonState();

                // Load all processes (not just Minecraft)
                allProcesses = ProcessFilter.GetSafeProcesses(false);

                RefreshProcessList();

                lblInstructions.Text = $"⛏️ Loaded {allProcesses.Count} processes. Critical Windows processes are filtered out.";
                lblInstructions.ForeColor = Color.FromArgb(175, 175, 175);
                UpdateInjectButtonState();
            }
            catch (Exception ex)
            {
                Logger.Error($"Error loading processes: {ex.Message}");
                Alert.Error($"Error loading processes: {ex.Message}");
            }
        }

        private void RefreshProcessList()
        {
            lbPrograms.Items.Clear();
            string searchText = txtSearch.Text;

            var filteredProcesses = ProcessFilter.FilterProcesses(allProcesses, searchText);

            foreach (var process in filteredProcesses)
            {
                if (injectedProcessIds.Contains(process.ProcessId))
                {
                    var modifiedProcess = new SafeProcessItem
                    {
                        Process = process.Process,
                        ProcessId = process.ProcessId,
                        ProcessName = process.ProcessName,
                        IsRisky = process.IsRisky,
                        HasMainWindow = process.HasMainWindow,
                        MainWindowTitle = process.MainWindowTitle,
                        FilePath = process.FilePath,
                        IsMinecraft = process.IsMinecraft,
                        DisplayText = process.DisplayText + " ✅[INJECTED]"
                    };
                    lbPrograms.Items.Add(modifiedProcess);
                }
                else
                {
                    lbPrograms.Items.Add(process);
                }
            }

            if (string.IsNullOrWhiteSpace(searchText))
            {
                lblInstructions.Text = $"⛏️ Showing {filteredProcesses.Count} processes";
            }
            else
            {
                lblInstructions.Text = $"🔍 Found {filteredProcesses.Count} processes matching '{searchText}'";
            }
            lblInstructions.ForeColor = Color.FromArgb(175, 175, 175);
        }

        private void txtSearch_TextChanged(object sender, EventArgs e)
        {
            RefreshProcessList();
        }


        private void cbIgnoreSecurity_CheckedChanged(object? sender, EventArgs e)
        {
            UpdateInjectButtonState();
        }

        private void lbPrograms_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (lbPrograms.SelectedItem is SafeProcessItem selectedProcess)
            {
                SelectedProcess = selectedProcess;

                if (injectedProcessIds.Contains(selectedProcess.ProcessId))
                {
                    if (cbIgnoreSecurity.Checked)
                    {
                        lblInstructions.Text = $"⚠️ {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId}) already has DLL injected! Ignore Security is enabled.";
                        lblInstructions.ForeColor = Color.FromArgb(255, 193, 7);
                    }
                    else
                    {
                        lblInstructions.Text = $"🚫 {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId}) already has DLL injected!";
                        lblInstructions.ForeColor = Color.FromArgb(240, 71, 71);
                    }
                }
                else if (selectedProcess.IsRisky)
                {
                    lblInstructions.Text = $"⚠️ WARNING: {selectedProcess.ProcessName} may be risky to inject into. Proceed with caution.";
                    lblInstructions.ForeColor = Color.FromArgb(255, 193, 7);
                }
                else if (selectedProcess.IsMinecraft)
                {
                    lblInstructions.Text = $"⛏️ Selected Minecraft: {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId})";
                    lblInstructions.ForeColor = Color.FromArgb(67, 181, 129);
                }
                else
                {
                    lblInstructions.Text = $"✅ Selected: {selectedProcess.ProcessName} (PID: {selectedProcess.ProcessId})";
                    lblInstructions.ForeColor = Color.FromArgb(40, 167, 69);
                }

                UpdateInjectButtonState();
            }
            else
            {
                SelectedProcess = null;
                UpdateInjectButtonState();
                RefreshProcessList();
            }
        }

        private void btnPickDLL_Click(object? sender, EventArgs e)
        {
            using (OpenFileDialog openFileDialog = new OpenFileDialog())
            {
                openFileDialog.Filter = "Dynamic Link Library (*.dll)|*.dll|All Files (*.*)|*.*";
                openFileDialog.FilterIndex = 1;
                openFileDialog.RestoreDirectory = true;
                openFileDialog.Title = "Select DLL to Inject";

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    selectedDllPath = openFileDialog.FileName;
                    txtDLLPath.Text = selectedDllPath;
                    label1.Text = $"DLL Selected: {Path.GetFileName(selectedDllPath)}";
                    lblInstructions.Text = $"✅ DLL Selected: {Path.GetFileName(selectedDllPath)}";
                    RegistryConfig.SetValue("last_dll_path", selectedDllPath);
                    UpdateInjectButtonState();
                }
            }
        }

        private void UpdateInjectButtonState()
        {
            bool canInject = SelectedProcess != null && !string.IsNullOrEmpty(selectedDllPath);

            btnNext.Enabled = canInject;

            if (SelectedProcess != null && injectedProcessIds.Contains(SelectedProcess.ProcessId))
            {
                btnNext.Text = "💉 INJECT AGAIN";
            }
            else
            {
                btnNext.Text = "💉 INJECT";
            }
        }

        private void btnRefreshProcesses_Click(object sender, EventArgs e)
        {
            LoadProcesses();
            txtSearch.Text = "";
            label1.Text = "Process list refreshed!";
        }

        private async void btnNext_Click(object sender, EventArgs e)
        {
            if (SelectedProcess == null)
            {
                Alert.Warning("No process selected for injection.");
                return;
            }

            if (string.IsNullOrEmpty(selectedDllPath))
            {
                Alert.Error("No DLL selected for injection.");
                return;
            }

            if (injectedProcessIds.Contains(SelectedProcess.ProcessId) && !cbIgnoreSecurity.Checked)
            {
                Alert.Warning("This process already has a DLL injected. Multiple injections are not allowed to prevent crashes.");
                return;
            }

            try
            {
                SelectedProcess.Process.Refresh();
                if (SelectedProcess.Process.HasExited)
                {
                    Alert.Error("The selected process has exited.");
                    LoadProcesses();
                    return;
                }
            }
            catch
            {
                Alert.Error("Failed to access the selected process. It may have exited or you may not have sufficient permissions.");
                LoadProcesses();
                return;
            }

            if (SelectedProcess.IsRisky)
            {
                DialogResult result = MessageBox.Show(
                    $"⚠️ WARNING ⚠️\n\n" +
                    $"You selected '{SelectedProcess.ProcessName}' which is marked as RISKY.\n\n" +
                    $"Injecting into this process may cause system instability or crashes.\n\n" +
                    $"Are you sure you want to continue?",
                    "Risky Process Warning",
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Warning);

                if (result != DialogResult.Yes)
                {
                    return;
                }
            }

            await PerformInjection();
        }

        private async Task PerformInjection()
        {
            if (SelectedProcess == null || string.IsNullOrEmpty(selectedDllPath))
                return;

            try
            {
                ShowProgress(true);
                btnNext.Enabled = false;

                label1.Text = "Preparing injection...";
                pbProgress.Value = 20;
                await Task.Delay(500);

                label1.Text = "Injecting DLL into process...";
                pbProgress.Value = 60;

                DllInjector dllInjector = DllInjector.Instance;
                DllInjectionResult result = await dllInjector.Inject(SelectedProcess.ProcessId, selectedDllPath, cbIgnoreSecurity.Checked);

                pbProgress.Value = 100;
                await Task.Delay(300);

                switch (result)
                {
                    case DllInjectionResult.Success:
                        injectedProcessIds.Add(SelectedProcess.ProcessId);
                        Alert.Success($"Successfully injected {Path.GetFileName(selectedDllPath)} into {SelectedProcess.ProcessName}!");
                        label1.Text = $"✅ Successfully injected {Path.GetFileName(selectedDllPath)} into {SelectedProcess.ProcessName}!";
                        lblInstructions.Text = $"🎉 Injection successful! {SelectedProcess.ProcessName} is now enhanced.";
                        lblInstructions.ForeColor = Color.FromArgb(67, 181, 129);
                        RefreshProcessList();
                        break;

                    case DllInjectionResult.AlreadyInjected:
                        Alert.Error("DLL is already injected into this process.");
                        label1.Text = "⚠️ DLL is already injected into this process.";
                        break;

                    case DllInjectionResult.DllNotFound:
                        Alert.Error("The specified DLL file was not found.");
                        label1.Text = "❌ The specified DLL file was not found.";
                        break;

                    case DllInjectionResult.GameProcessNotFound:
                        Alert.Error("The target process could not be found or accessed.");
                        label1.Text = "❌ The target process could not be found or accessed.";
                        break;

                    case DllInjectionResult.AccessDenied:
                        Alert.Error("Access denied. Try running as administrator.");
                        label1.Text = "❌ Access denied. Try running as administrator.";
                        break;

                    case DllInjectionResult.InjectionFailed:
                        Alert.Error("DLL injection failed. Make sure you have sufficient privileges.");
                        label1.Text = "❌ DLL injection failed. Make sure you have sufficient privileges.";
                        break;
                }
            }
            catch (Exception ex)
            {
                Alert.Error($"Error during injection: {ex.Message}");
                label1.Text = $"❌ Error during injection: {ex.Message}";
                Logger.Error($"Injection error: {ex.Message}");
            }
            finally
            {
                ShowProgress(false);
                UpdateInjectButtonState();
            }
        }

        private void ShowProgress(bool visible)
        {
            pbProgress.Visible = visible;
            if (!visible)
            {
                pbProgress.Value = 0;
            }
        }

        private void btnPickDLL_Click_1(object sender, EventArgs e)
        {

        }
    }
}
