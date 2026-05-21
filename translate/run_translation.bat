@echo off
echo Starting Chinese to Vietnamese translation process...

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

REM Run translation script
echo Running translation script...
python translate_chinese_to_vietnamese.py

echo Translation process completed!
pause