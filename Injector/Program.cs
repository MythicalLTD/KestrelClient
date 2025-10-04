using KestrelClientInjector.forms;
using KestrelClientInjector.utils;
using System.Diagnostics;
using System.Windows.Forms;

namespace KestrelClientInjector
{
    internal static class Program
    {

        public static string appWorkDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
            ".kestrelclient"
        );
        public static string appVersion = "1.0.0";

        /// <summary>
        ///  The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            // To customize application configuration such as set high DPI settings or default font,
            // see https://aka.ms/applicationconfiguration.
            ApplicationConfiguration.Initialize();
            try
            {
                // Check for multiple instances
                if (!CheckMultipleInstances())
                {
                    Logger.Info("Another instance is already running and multiple instances are disabled.");
                    return;
                }
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
                Application.Run(new FrmNew());

            }
            catch (Exception ex)
            {
                Logger.Error($"Unhandled exception: {ex.Message}");
                Logger.Error(ex.StackTrace ?? "");
                // Optionally show a message box or alert to the user
                MessageBox.Show("An unexpected error occurred. Please check the log for details.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }


        public static void stop()
        {
            Logger.Info("Application stopping...");

            Application.Exit();
        }

        public static void restart()
        {
            Logger.Info("Application restarting...");
            Application.Restart();
        }

        private static bool CheckMultipleInstances()
        {
            try
            {
                // Check if multiple instances are allowed
                bool allowMultiple = RegistryConfig.GetUserPreference("AllowMultipleInstances", false);

                if (allowMultiple)
                {
                    Logger.Info("Multiple instances allowed");
                    return true;
                }

                // Check if another instance is already running
                string processName = Process.GetCurrentProcess().ProcessName;
                Process[] processes = Process.GetProcessesByName(processName);

                if (processes.Length > 1)
                {
                    Logger.Warning("Another instance is already running");

                    // Try to bring the existing instance to the front
                    foreach (var process in processes)
                    {
                        if (process.Id != Process.GetCurrentProcess().Id)
                        {
                            try
                            {
                                // Find the main window and bring it to front
                                if (process.MainWindowHandle != IntPtr.Zero)
                                {
                                    ShowWindow(process.MainWindowHandle, SW_RESTORE);
                                    SetForegroundWindow(process.MainWindowHandle);
                                }
                            }
                            catch (Exception ex)
                            {
                                Logger.Warning($"Failed to bring existing instance to front: {ex.Message}");
                            }
                            break;
                        }
                    }
                    return false;
                }

                Logger.Info("No other instances found");
                return true;
            }
            catch (Exception ex)
            {
                Logger.Error($"Error checking multiple instances: {ex.Message}");
                return true; // Allow startup if check fails
            }
        }

        // Windows API functions for bringing window to front
        [System.Runtime.InteropServices.DllImport("user32.dll")]
        private static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);

        [System.Runtime.InteropServices.DllImport("user32.dll")]
        private static extern bool SetForegroundWindow(IntPtr hWnd);

        private const int SW_RESTORE = 9;
    }

}