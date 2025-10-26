@echo off
echo Building Realtek Driver Cleaner...

if not exist bin mkdir bin

echo Compiling for Java 8 compatibility...
javac -source 8 -target 8 -d bin src\RealtekCleanerGUI.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Creating manifest...
    echo Main-Class: RealtekCleanerGUI> manifest.txt
    echo.>> manifest.txt
    
    echo Creating JAR file...
    jar cfm RealtekCleanerGUI.jar manifest.txt -C bin .
    
    del manifest.txt
    echo Done! RealtekCleanerGUI.jar created successfully!
) else (
    echo Compilation failed!
)

pause