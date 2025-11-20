#!/bin/bash

# Script to run the Chinese to Vietnamese translation
echo "Starting Chinese to Vietnamese translation process..."

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python3 is not installed. Please install Python3 first."
    exit 1
fi

# Check if required packages are installed
echo "Checking required packages..."
python3 -c "import requests" 2>/dev/null || {
    echo "Installing requests package..."
    pip3 install requests
}

# Run the translation script
echo "Running translation script..."
python3 translate_chinese_to_vietnamese.py

echo "Translation process completed!"