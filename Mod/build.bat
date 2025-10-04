@echo off
setlocal
set FILE_NAME=mythicalclient-1.0.0.jar
set DEST_PATH=C:\Users\NaysKutzu\AppData\Roaming\.feather\user-mods\1.8.9

echo Building project...
call .\gradlew build

if %errorlevel% neq 0 (
    echo Build failed!
    exit /b %errorlevel%
)

echo Copying file...
if exist build\libs\%FILE_NAME% (
    if exist %DEST_PATH%\%FILE_NAME% (
        del /Q %DEST_PATH%\%FILE_NAME%
        if %errorlevel% neq 0 (
            echo Delete failed!
            exit /b %errorlevel%
        )
    )
    copy /Y build\libs\%FILE_NAME% %DEST_PATH%
    if %errorlevel% neq 0 (
        echo Copy failed!
        exit /b %errorlevel%
    )
) else (
    echo File %FILE_NAME% not found!
    exit /b 1
)

echo Done!
endlocal
