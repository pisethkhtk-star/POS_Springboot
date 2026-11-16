@echo off
title POS Spring Boot Backend (Port 5000)
cd /d "%~dp0"
echo ========================================================
echo Starting POS Spring Boot Backend on port 5000...
echo Ensure PostgreSQL is running on localhost:5432 (pos_db)
echo ========================================================
call gradlew.bat bootRun
pause
