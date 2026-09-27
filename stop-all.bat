@echo off
chcp 65001 >nul
echo ===============================================================================
echo            DỪNG TẤT CẢ CÁC MICROSERVICES CỦA RIKKEIBANK API
echo ===============================================================================

echo Dang giai phong cac cong: 8761, 8888, 8080, 8081, 8082, 8083, 8084, 8085...
for %%p in (8761 8888 8080 8081 8082 8083 8084 8085) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%%p " ^| findstr "LISTENING"') do (
        echo Killing PID %%a on port %%p...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo Dung cac tien trinh Java chay rikkeibank...
taskkill /F /FI "WINDOWTITLE eq Eureka Server*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Config Server*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Identity Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Customer Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Account Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Transaction Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Notification Service*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq API Gateway*" >nul 2>&1

echo Hoan tat dung he thong!
pause
