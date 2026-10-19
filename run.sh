#!/usr/bin/env bash
# ====================================================================
# Smart Campus Lost & Found Management System - Linux / Mac Script
# ====================================================================

set -e

echo "[1/3] Preparing output directory..."
mkdir -p out

echo "[2/3] Compiling Java backend sources..."
javac -encoding UTF-8 -d out $(find backend/src -name "*.java")

echo "[3/3] Starting Smart Campus Lost & Found Web Server on port 8080..."
echo "Open your browser at: http://localhost:8080"
java -cp out Main
