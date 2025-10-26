@echo off
echo Building Realtek Driver Cleaner...

if not exist bin mkdir bin

javac -d bin src\RealtekCleanerGUI.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Creating JAR file...
    jar cfe RealtekCleanerGUI.jar RealtekCleanerGUI -C bin .
    echo Done! RealtekCleanerGUI.jar created successfully!
) else (
    echo Compilation failed!
)

pause