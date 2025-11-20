#!/bin/bash

# Script to run Chinese to English translation for code files
echo "Starting Chinese to English translation for code files..."

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

# Get directory from command line or use default
DIRECTORY=${1:-.}

echo "Searching for Chinese text in: $DIRECTORY"

# Run translation script
echo "Running translation script..."
python3 translate_chinese_in_code.py << EOF
$DIRECTORY
EOF

echo "Translation process completed!"