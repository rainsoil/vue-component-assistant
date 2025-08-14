@echo off
echo 构建 VueKit 插件...
echo.

echo 清理构建目录...
call gradlew clean

echo 构建插件...
call gradlew buildPlugin

if %errorlevel% equ 0 (
    echo.
    echo 构建成功！
    echo 插件文件位置: build\distributions\
    dir build\distributions\*.zip
) else (
    echo.
    echo 构建失败！
)

pause
