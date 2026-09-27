@echo off
chcp 65001 >nul
echo ===============================================================================
echo        KHỞI ĐỘNG HỆ THỐNG MICROSERVICES - NGAN HANG SO RIKKEIBANK API
echo ===============================================================================

echo [1/8] Khoi dong Eureka Discovery Server (Port 8761)...
start "Eureka Server [8761]" cmd /k "title Eureka Server [8761] && gradlew :eureka-server:bootRun"
echo Cho Eureka Server khoi dong trong 15 giay...
timeout /t 15 /nobreak >nul

echo [2/8] Khoi dong Spring Cloud Config Server (Port 8888)...
start "Config Server [8888]" cmd /k "title Config Server [8888] && gradlew :config-server:bootRun"
echo Cho Config Server khoi dong trong 10 giay...
timeout /t 10 /nobreak >nul

echo [3/8] Khoi dong Identity Service (Port 8081)...
start "Identity Service [8081]" cmd /k "title Identity Service [8081] && gradlew :identity-service:bootRun"

echo [4/8] Khoi dong Customer Service (Port 8082)...
start "Customer Service [8082]" cmd /k "title Customer Service [8082] && gradlew :customer-service:bootRun"

echo [5/8] Khoi dong Account Service (Port 8083)...
start "Account Service [8083]" cmd /k "title Account Service [8083] && gradlew :account-service:bootRun"

echo [6/8] Khoi dong Transaction Service (Port 8084)...
start "Transaction Service [8084]" cmd /k "title Transaction Service [8084] && gradlew :transaction-service:bootRun"

echo [7/8] Khoi dong Notification Service (Port 8085)...
start "Notification Service [8085]" cmd /k "title Notification Service [8085] && gradlew :notification-service:bootRun"

echo Cho cac Core Services dang ky vao Eureka trong 15 giay...
timeout /t 15 /nobreak >nul

echo [8/8] Khoi dong Spring Cloud API Gateway (Port 8080)...
start "API Gateway [8080]" cmd /k "title API Gateway [8080] && gradlew :api-gateway:bootRun"

echo ===============================================================================
echo TAT CA CAC DICH VU DANG DUOC KHOI DONG!
echo - Eureka Dashboard : http://localhost:8761
echo - Config Server    : http://localhost:8888/identity-service/default
echo - API Gateway      : http://localhost:8080
echo ===============================================================================
pause
