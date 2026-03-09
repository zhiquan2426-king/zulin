@echo off
chcp 65001 >nul
echo ========================================
echo 停止后端服务
echo ========================================
echo.

taskkill /F /IM java.exe
if errorlevel 1 (
    echo ❌ 没有运行中的 Java 进程
) else (
    echo ✅ 已停止后端服务
)

echo.
pause
