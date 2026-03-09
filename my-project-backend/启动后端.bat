@echo off
chcp 65001 >nul
echo ========================================
echo 后端项目启动脚本
echo ========================================
echo.

cd /d %~dp0

echo [1/5] 检查 Java 版本...
java -version
echo.

echo [2/5] 清理并打包项目...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ❌ 打包失败！
    pause
    exit /b 1
)
echo ✅ 打包成功！
echo.

echo [3/5] 停止旧进程...
taskkill /F /IM java.exe >nul 2>&1
echo.

echo [4/5] 启动后端服务...
start /B java -jar target\my-project-backend-0.0.1-SNAPSHOT.jar > startup.log 2>&1

echo [5/5] 等待服务启动...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo 正在检查服务状态...
echo ========================================
type startup.log | findstr /C:"Started MyProjectBackendApplication"

echo.
echo ========================================
echo 📊 服务信息
echo ========================================
echo 🌐 Swagger 文档: http://localhost:8080/swagger-ui/index.html
echo 📧 邮件日志: 查看控制台
echo 🐰 RabbitMQ 管理界面: http://localhost:15672
echo 📝 启动日志: startup.log
echo.
echo ✅ 后端启动完成！
echo.
echo 按 Ctrl+C 停止服务，或关闭此窗口
pause
