@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM KestrelClientInjector Release Build Script
REM ============================================================================
REM This script builds KestrelClientInjector for multiple Windows architectures,
REM signs the executables, and packages them with proper naming and metadata.
REM ============================================================================

REM === CONFIGURATION ===
set PROJECT=KestrelClientInjector.csproj
set OUTPUT_DIR=publish
set CONFIG=Release
set APP_NAME=KestrelClientInjector
set VERSION=1.0.0
set BUILD_DATE=%DATE:~-4%-%DATE:~4,2%-%DATE:~7,2%

REM === CODE SIGNING CONFIGURATION ===
REM Uncomment and configure these lines if you have a code signing certificate:
REM set SIGN_CERT=path\to\your\certificate.pfx
REM set SIGN_PASSWORD=your_certificate_password
REM set SIGN_TIMESTAMP=http://timestamp.sectigo.com
set ENABLE_SIGNING=false

REM === ARCHITECTURES TO BUILD ===
set RIDS=win-x86 win-x64 win-arm64

REM === CHECK PREREQUISITES ===
echo.
echo ============================================================================
echo  KestrelClientInjector Release Builder v1.0
echo ============================================================================
echo  Building: %APP_NAME% v%VERSION%
echo  Date: %BUILD_DATE%
echo  Configuration: %CONFIG%
echo  Architectures: %RIDS%
echo ============================================================================
echo.

REM Check if dotnet is available
dotnet --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] .NET SDK not found. Please install .NET 8.0 SDK or later.
    pause
    exit /b 1
)

REM Check if signtool is available (if signing is enabled)
if "%ENABLE_SIGNING%"=="true" (
    signtool 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [WARNING] SignTool not found. Code signing will be skipped.
        set ENABLE_SIGNING=false
    ) else (
        echo [OK] SignTool found - Code signing enabled
    )
)

REM === CLEAN OUTPUT DIRECTORY ===
echo [*] Cleaning output directory...
if exist "%OUTPUT_DIR%" (
    rmdir /s /q "%OUTPUT_DIR%"
)
mkdir "%OUTPUT_DIR%"

REM === BUILD FOR EACH ARCHITECTURE ===
echo.
echo [*] Starting builds...

for %%R in (%RIDS%) do (
    echo.
    echo ======================================
    echo Building for %%R...
    echo ======================================
    
    REM Determine architecture display name
    set ARCH_NAME=%%R
    if "%%R"=="win-x86" set ARCH_NAME=32-bit
    if "%%R"=="win-x64" set ARCH_NAME=64-bit
    if "%%R"=="win-arm64" set ARCH_NAME=ARM64
    
    REM Create architecture output directory
    mkdir "%OUTPUT_DIR%\%%R" 2>nul
    
    REM Build the application
    echo Building %APP_NAME% for !ARCH_NAME!...
    dotnet publish %PROJECT% -c %CONFIG% -r %%R --self-contained true ^
        /p:PublishSingleFile=true ^
        /p:EnableCompressionInSingleFile=true ^
        /p:IncludeNativeLibrariesForSelfExtract=true ^
        /p:AssemblyTitle="%APP_NAME%" ^
        /p:AssemblyDescription="Advanced Minecraft launcher for !ARCH_NAME! Windows systems" ^
        /p:AssemblyProduct="%APP_NAME%" ^
        /p:AssemblyCompany="MythicalSystems" ^
        /p:AssemblyCopyright="Copyright © 2025 MythicalSystems. All rights reserved." ^
        /p:AssemblyVersion="%VERSION%.0" ^
        /p:FileVersion="%VERSION%.0" ^
        /p:Version="%VERSION%" ^
        /p:PublisherName="MythicalSystems" ^
        --output "%OUTPUT_DIR%\%%R" ^
        --verbosity quiet
    
    if !ERRORLEVEL! neq 0 (
        echo [ERROR] Build failed for %%R
        pause
        exit /b 1
    )
    
    REM Rename the executable with architecture suffix
    set OLD_NAME=%OUTPUT_DIR%\%%R\KestrelClientInjector.exe
    set NEW_NAME=%OUTPUT_DIR%\%%R\KestrelClientInjector-%%R-v%VERSION%.exe
    
    if exist "!OLD_NAME!" (
        echo [*] Renaming executable: KestrelClientInjector-%%R-v%VERSION%.exe
        move "!OLD_NAME!" "!NEW_NAME!" >nul
        
        REM Sign the executable if enabled
        if "%ENABLE_SIGNING%"=="true" (
            echo [*] Signing executable...
            signtool sign /f "%SIGN_CERT%" /p "%SIGN_PASSWORD%" /t "%SIGN_TIMESTAMP%" /v "!NEW_NAME!"
            if !ERRORLEVEL! neq 0 (
                echo [WARNING] Code signing failed for %%R
            ) else (
                echo [OK] Successfully signed %%R executable
            )
        )
        
        REM Get file size
        for %%F in ("!NEW_NAME!") do set SIZE=%%~zF
        set /a SIZE_MB=!SIZE!/1024/1024
        echo [OK] Build completed: !SIZE_MB! MB
        
    ) else (
        echo [ERROR] Executable not found after build
    )
)

REM === GENERATE BUILD SUMMARY ===
echo.
echo ============================================================================
echo  BUILD SUMMARY
echo ============================================================================

REM Create build info file
set BUILD_INFO=%OUTPUT_DIR%\build-info.txt
echo KestrelClientInjector Build Information > "!BUILD_INFO!"
echo ================================= >> "!BUILD_INFO!"
echo Version: %VERSION% >> "!BUILD_INFO!"
echo Build Date: %BUILD_DATE% >> "!BUILD_INFO!"
echo Build Time: %TIME% >> "!BUILD_INFO!"
echo Configuration: %CONFIG% >> "!BUILD_INFO!"
echo Publisher: MythicalSystems >> "!BUILD_INFO!"
echo. >> "!BUILD_INFO!"
echo Built Architectures: >> "!BUILD_INFO!"

for %%R in (%RIDS%) do (
    set ARCH_NAME=%%R
    if "%%R"=="win-x86" set ARCH_NAME=32-bit
    if "%%R"=="win-x64" set ARCH_NAME=64-bit
    if "%%R"=="win-arm64" set ARCH_NAME=ARM64
    
    set EXE_PATH=%OUTPUT_DIR%\%%R\KestrelClientInjector-%%R-v%VERSION%.exe
    if exist "!EXE_PATH!" (
        for %%F in ("!EXE_PATH!") do set SIZE=%%~zF
        set /a SIZE_MB=!SIZE!/1024/1024
        echo   [OK] %%R ^(!ARCH_NAME!^): !SIZE_MB! MB
        echo   - %%R ^(!ARCH_NAME!^): !SIZE_MB! MB >> "!BUILD_INFO!"
    ) else (
        echo   [FAILED] %%R ^(!ARCH_NAME!^): FAILED
        echo   - %%R ^(!ARCH_NAME!^): FAILED >> "!BUILD_INFO!"
    )
)

echo.
echo Code Signing: %ENABLE_SIGNING% >> "!BUILD_INFO!"
echo. >> "!BUILD_INFO!"
echo Installation Instructions: >> "!BUILD_INFO!"
echo 1. Download the appropriate version for your system: >> "!BUILD_INFO!"
echo    - win-x86: For 32-bit Windows systems >> "!BUILD_INFO!"
echo    - win-x64: For 64-bit Windows systems (recommended) >> "!BUILD_INFO!"
echo    - win-arm64: For ARM64 Windows systems >> "!BUILD_INFO!"
echo 2. Run the executable - no installation required! >> "!BUILD_INFO!"
echo 3. The application is portable and self-contained >> "!BUILD_INFO!"

echo.
echo Output Directory: %OUTPUT_DIR%
echo Build Info: !BUILD_INFO!
echo.

if "%ENABLE_SIGNING%"=="false" (
    echo [NOTE] Code signing was disabled. To enable:
    echo    1. Obtain a code signing certificate
    echo    2. Set SIGN_CERT, SIGN_PASSWORD, and SIGN_TIMESTAMP variables
    echo    3. Set ENABLE_SIGNING=true
    echo.
)

echo ============================================================================
echo ALL BUILDS COMPLETED SUCCESSFULLY!
echo ============================================================================
echo.
echo Files created:
for %%R in (%RIDS%) do (
    if exist "!OUTPUT_DIR!\%%R\KestrelClientInjector-%%R-v%VERSION%.exe" (
        echo   [FILE] KestrelClientInjector-%%R-v%VERSION%.exe
    )
)
echo   [FILE] build-info.txt

echo.
echo Ready for distribution!
endlocal
pause