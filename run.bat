@echo off
setlocal enabledelayedexpansion
echo ==============================================
echo   Campus Connect Build Script (Windows)
echo ==============================================
echo.

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: javac not found. Install JDK 11+ and add to PATH.
    pause
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out
mkdir data 2>nul

echo Collecting source files...
if exist sources.txt del /f /q sources.txt
for /r src %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!" >> sources.txt
)

echo Compiling...
javac -d out -sourcepath src -source 11 -target 11 @sources.txt

if %errorlevel% neq 0 (
    echo Compilation FAILED.
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting Campus Connect server...
java -cp out Main
pause
