@echo off
cd /d "%~dp0"

echo Starting Realtek Driver Cleaner...
echo.
echo NOTE: This tool requires Administrator privileges.
echo If you see the admin prompt, click Yes to continue.
echo.
pause

java -jar RealtekCleanerGUI.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error running the application.
    echo Make sure Java is installed: https://www.java.com/download/
    pause
)