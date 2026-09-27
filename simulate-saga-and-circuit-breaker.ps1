# ===============================================================================
# RIKKEIBANK API - KỊCH BẢN KIỂM THỬ SAGA PATTERN, ROLLBACK & CIRCUIT BREAKER
# ===============================================================================

$baseUrl = "http://localhost:8080"
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  RIKKEIBANK MICROSERVICES - DEMO SAGA ROLLBACK & FAULT TOLERANCE" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Đăng nhập Admin
Write-Host "`n[Bước 1] Đăng nhập Admin..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "Admin@123"
} | ConvertTo-Json

try {
    $loginRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $adminToken = $loginRes.data.accessToken
    Write-Host "-> Đăng nhập Admin thành công! Token: $($adminToken.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "-> Lỗi kết nối API Gateway: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 2. Đăng nhập Khách hàng 1 (Customer 1 - TK: 1000000001)
Write-Host "`n[Bước 2] Đăng nhập Khách hàng 1 (customer1)..." -ForegroundColor Yellow
$custLoginBody = @{
    username = "customer1"
    password = "Customer@123"
} | ConvertTo-Json

$custRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $custLoginBody -ContentType "application/json"
$custToken = $custRes.data.accessToken
$refreshToken = $custRes.data.refreshToken
Write-Host "-> Khách hàng 1 đăng nhập thành công!" -ForegroundColor Green
Write-Host "-> Access Token (1h): $($custToken.Substring(0, 20))..." -ForegroundColor DarkGray
Write-Host "-> Refresh Token (30 ngày - Trải nghiệm duy trì phiên lâu dài): $($refreshToken.Substring(0, 20))..." -ForegroundColor DarkGray

$authHeader = @{ "Authorization" = "Bearer $custToken" }

# 3. Kiểm tra số dư ban đầu
Write-Host "`n[Bước 3] Kiểm tra số dư trước giao dịch..." -ForegroundColor Yellow
$acc1 = Invoke-RestMethod -Uri "$baseUrl/api/accounts/1000000001" -Method Get -Headers $authHeader
$acc2 = Invoke-RestMethod -Uri "$baseUrl/api/accounts/1000000002" -Method Get -Headers $authHeader
Write-Host "-> Tài khoản nguồn (1000000001): $($acc1.data.balance) VND" -ForegroundColor Cyan
Write-Host "-> Tài khoản đích   (1000000002): $($acc2.data.balance) VND" -ForegroundColor Cyan

# 4. Kịch bản 1: Chuyển khoản thành công (Saga Success Flow)
Write-Host "`n[Bước 4] Kịch bản 1: Chuyển khoản thông thường 1.000.000 VND (Saga Success)..." -ForegroundColor Yellow
$transferBody = @{
    sourceAccountNumber = "1000000001"
    destinationAccountNumber = "1000000002"
    amount = 1000000
    message = "Thanh toan tien nha thang 9"
} | ConvertTo-Json

$transferRes = Invoke-RestMethod -Uri "$baseUrl/api/transactions/transfer" -Method Post -Body $transferBody -ContentType "application/json" -Headers $authHeader
Write-Host "-> Mã giao dịch: $($transferRes.data.transactionRef)" -ForegroundColor Green
Write-Host "-> Trạng thái Saga: $($transferRes.data.status)" -ForegroundColor Green

$acc1_after = Invoke-RestMethod -Uri "$baseUrl/api/accounts/1000000001" -Method Get -Headers $authHeader
$acc2_after = Invoke-RestMethod -Uri "$baseUrl/api/accounts/1000000002" -Method Get -Headers $authHeader
Write-Host "-> Số dư mới TK Nguồn: $($acc1_after.data.balance) VND (đã trừ 1.000.000 VND)" -ForegroundColor Green
Write-Host "-> Số dư mới TK Đích  : $($acc2_after.data.balance) VND (đã cộng 1.000.000 VND)" -ForegroundColor Green

# 5. Kịch bản 2: Mô phỏng lỗi bước ghi có (Credit Fail) -> Kích hoạt Saga Compensating Rollback!
Write-Host "`n[Bước 5] Kịch bản 2: Gây lỗi giữa luồng chuyển khoản (Saga Compensating Rollback)..." -ForegroundColor Yellow
Write-Host "-> Gửi request với cờ simulateFailure = DEST_FAIL (Tài khoản đích từ chối hoặc lỗi mạng sau khi đã trừ tiền tài khoản nguồn)" -ForegroundColor Magenta

$rollbackBody = @{
    sourceAccountNumber = "1000000001"
    destinationAccountNumber = "1000000002"
    amount = 2500000
    message = "Test Saga Compensating Transaction"
    simulateFailure = "DEST_FAIL"
} | ConvertTo-Json

$rollbackRes = Invoke-RestMethod -Uri "$baseUrl/api/transactions/transfer" -Method Post -Body $rollbackBody -ContentType "application/json" -Headers $authHeader
Write-Host "-> Mã giao dịch: $($rollbackRes.data.transactionRef)" -ForegroundColor Magenta
Write-Host "-> Trạng thái kết quả: $($rollbackRes.data.status)" -ForegroundColor Red
Write-Host "-> Lý do thất bại & Hoàn tiền: $($rollbackRes.data.failureReason)" -ForegroundColor Red

# 6. Kiểm tra số dư tài khoản sau rollback
Write-Host "`n[Bước 6] Kiểm tra số dư sau Saga Rollback..." -ForegroundColor Yellow
$acc1_rollback = Invoke-RestMethod -Uri "$baseUrl/api/accounts/1000000001" -Method Get -Headers $authHeader
Write-Host "-> Số dư TK Nguồn sau rollback: $($acc1_rollback.data.balance) VND" -ForegroundColor Green
if ($acc1_rollback.data.balance -eq $acc1_after.data.balance) {
    Write-Host "-> XÁC NHẬN: Số dư được hoàn trả chính xác 100%! Không bị thất thoát bất kỳ đồng nào!" -ForegroundColor Green
} else {
    Write-Host "-> CẢNH BÁO: Số dư bị lệch!" -ForegroundColor Red
}

# 7. Xem lịch sử vết Saga Step Audit
Write-Host "`n[Bước 7] Xem lịch sử vết Audit của giao dịch Rollback..." -ForegroundColor Yellow
$auditRes = Invoke-RestMethod -Uri "$baseUrl/api/transactions/$($rollbackRes.data.transactionRef)/audits" -Method Get -Headers $authHeader
foreach ($step in $auditRes.data) {
    Write-Host "   - [$($step.timestamp)] Step: $($step.stepName) | Status: $($step.status) | $($step.details)" -ForegroundColor DarkCyan
}

# 8. Trải nghiệm phiên đăng nhập liền mạch (Refresh Token)
Write-Host "`n[Bước 8] Trải nghiệm đăng nhập liền mạch: Làm mới Token mà không cần nhập mật khẩu..." -ForegroundColor Yellow
$refreshBody = @{
    refreshToken = $refreshToken
} | ConvertTo-Json
$newSession = Invoke-RestMethod -Uri "$baseUrl/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
Write-Host "-> Nhận Access Token mới thành công: $($newSession.data.accessToken.Substring(0, 20))..." -ForegroundColor Green

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "  HOÀN THÀNH TẤT CẢ KỊCH BẢN KIỂM THỬ SAGA & MICROSERVICES!" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
