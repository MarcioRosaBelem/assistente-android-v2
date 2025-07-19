@echo off
cd %~dp0
cloudflared.exe tunnel --url http://localhost:5000
pause
