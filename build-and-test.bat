@echo off
echo ========================================
echo VueKit 构建和测试脚本
echo ========================================
echo.

echo [1/4] 清理构建目录...
if exist build rmdir /s /q build
echo 清理完成
echo.

echo [2/4] 构建插件...
call gradlew buildPlugin
if %errorlevel% neq 0 (
    echo 构建失败！
    pause
    exit /b 1
)
echo 构建完成
echo.

echo [3/4] 运行测试...
call gradlew test
if %errorlevel% neq 0 (
    echo 测试失败！
    pause
    exit /b 1
)
echo 测试完成
echo.

echo [4/4] 检查构建结果...
if exist build\distributions\vuekit-*.zip (
    echo 插件构建成功！
    echo 插件文件位置: build\distributions\
    dir build\distributions\*.zip
) else (
    echo 插件构建失败！
    pause
    exit /b 1
)
echo.

echo ========================================
echo 构建和测试完成！
echo ========================================
echo.
echo 下一步操作：
echo 1. 在 IntelliJ IDEA 中安装插件
echo 2. 测试插件功能
echo 3. 发布到插件市场
echo.
pause
