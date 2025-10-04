using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace KestrelClientInjector.utils
{
    public enum LogLevel
    {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    public static class Logger
    {
        private static readonly string LogDirectory = Path.Combine(Program.appWorkDir, "logs");
        private static readonly string LogFile = Path.Combine(LogDirectory, "log.txt");
        private static readonly object LockObj = new object();

        static Logger()
        {
            // Ensure logs directory exists
            if (!Directory.Exists(LogDirectory))
            {
                Directory.CreateDirectory(LogDirectory);
            }
        }

        public static void Log(LogLevel level, string message)
        {
            string timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
            string logMessage = $"[{timestamp}] [{level}] {message}";

            // Console output
            ConsoleColor originalColor = Console.ForegroundColor;
            Console.ForegroundColor = GetColorForLogLevel(level);
            Console.WriteLine(logMessage);
            Console.ForegroundColor = originalColor;

            // File output
            lock (LockObj)
            {
                // Check if we need to rotate logs
                if (File.Exists(LogFile))
                {
                    FileInfo fileInfo = new FileInfo(LogFile);
                    if (fileInfo.Length > 5 * 1024 * 1024) // 5MB per file
                    {
                        string oldLogName = Path.Combine(LogDirectory, $"log_{DateTime.Now:yyyy-MM-dd_HH-mm-ss}.txt");
                        File.Move(LogFile, oldLogName);
                    }
                }

                File.AppendAllText(LogFile, logMessage + Environment.NewLine);
                
                // Clean up old logs to maintain 50MB limit
                CleanupOldLogs();
            }
        }

        private static void CleanupOldLogs()
        {
            try
            {
                const long maxDirectorySize = 50 * 1024 * 1024; // 50MB
                
                // Get all log files (both .txt and C++ client logs)
                var allLogFiles = Directory.GetFiles(LogDirectory, "*.*")
                    .Where(file => {
                        string fileName = Path.GetFileName(file).ToLower();
                        return fileName.EndsWith(".txt") || 
                               (fileName.StartsWith("kestrelclient_") && fileName.EndsWith(".log"));
                    })
                    .Select(file => new FileInfo(file))
                    .OrderByDescending(f => f.CreationTime)
                    .ToList();

                // Calculate current directory size
                long currentSize = allLogFiles.Sum(f => f.Length);
                
                // If we're over the limit, delete oldest files
                if (currentSize > maxDirectorySize)
                {
                    Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] [INFO] Log directory size: {currentSize / (1024 * 1024)}MB, cleaning up old logs...");
                    
                    foreach (var file in allLogFiles.Skip(1)) // Skip the current log file (first in list)
                    {
                        if (currentSize <= maxDirectorySize)
                            break;
                            
                        long fileSize = file.Length;
                        try
                        {
                            file.Delete();
                            currentSize -= fileSize;
                            
                            string fileType = file.Name.EndsWith(".log") ? "C++ client log" : "injector log";
                            Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] [INFO] Deleted old {fileType}: {file.Name} ({fileSize / (1024 * 1024)}MB)");
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] [WARN] Failed to delete log file {file.Name}: {ex.Message}");
                        }
                    }
                    
                    Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] [INFO] Log cleanup completed. Current size: {currentSize / (1024 * 1024)}MB");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] [ERROR] Error during log cleanup: {ex.Message}");
            }
        }

        private static ConsoleColor GetColorForLogLevel(LogLevel level)
        {
            return level switch
            {
                LogLevel.INFO => ConsoleColor.White,
                LogLevel.WARN => ConsoleColor.Yellow,
                LogLevel.ERROR => ConsoleColor.Red,
                LogLevel.DEBUG => ConsoleColor.Gray,
                _ => ConsoleColor.White
            };
        }

        // Convenience methods
        public static void Info(string message) => Log(LogLevel.INFO, message);
        public static void Warn(string message) => Log(LogLevel.WARN, message);
        public static void Warning(string message) => Log(LogLevel.WARN, message);
        public static void Error(string message) => Log(LogLevel.ERROR, message);
        public static void Debug(string message) => Log(LogLevel.DEBUG, message);
    }
}
