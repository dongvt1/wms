@echo off
echo Starting Chinese to English translation for code files...

REM Check if Python is installed
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Python is not installed. Please install Python first.
    pause
    exit /b 1
)

REM Check if required packages are installed
echo Checking required packages...
python -c "import requests" >nul 2>&1
if %errorlevel% neq 0 (
    echo Installing requests package...
    pip install requests
)

REM Get directory from command line or use default
set DIRECTORY=%1
if "%DIRECTORY%"=="" set DIRECTORY=.

echo Searching for Chinese text in: %DIRECTORY%

REM Run translation script
echo Running translation script...
echo %DIRECTORY% | python translate_chinese_in_code.py

echo Translation process completed!
pause