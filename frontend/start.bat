@echo off
setlocal
cd /d "%~dp0"
where node >nul 2>nul
if errorlevel 1 (
  echo Node.js is required. Install Node.js 20 or newer, then run this file again.
  pause
  exit /b 1
)
if not exist node_modules (
  echo Installing frontend dependencies for the first run...
  call npm install
  if errorlevel 1 (
    echo Dependency installation failed. Check your network connection and npm setup.
    pause
    exit /b 1
  )
)
echo Starting EnterpriseHub at http://localhost:5173
echo Keep this window open while using the app. Press Ctrl+C to stop the server.
call npm run dev
endlocal
