@echo off
start cmd /k "cd /d ""%~dp0fresh-admin"" && npm.cmd run dev"
timeout /t 1 /nobreak >nul
start cmd /k "cd /d ""%~dp0fresh-user"" && npm.cmd run dev"
echo.
echo Front-end dev servers started in two new windows.
echo Admin: http://localhost:5173  (admin/123456)
echo User H5: check the second window for its port (user1/leader1, pwd 123456)
echo Note: start MySQL, Redis and the SpringBoot backend (IDEA) first.
pause
