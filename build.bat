@echo off
echo Building Element Plus Assistant Plugin...

REM 检查Gradle是否安装
gradle --version >nul 2>&1
if errorlevel 1 (
    echo Error: Gradle is not installed or not in PATH
    echo Please install Gradle from https://gradle.org/install/
    pause
    exit /b 1
)

REM 清理之前的构建
echo Cleaning previous build...
gradle clean

REM 构建插件
echo Building plugin...
gradle buildPlugin

REM 检查构建是否成功
if errorlevel 1 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo Plugin file location: build/distributions/
echo.
echo To install the plugin:
echo 1. Open IntelliJ IDEA
echo 2. Go to File -> Settings -> Plugins
echo 3. Click the gear icon -> Install Plugin from Disk
echo 4. Select the .jar file from build/distributions/
echo 5. Restart IntelliJ IDEA
echo.
pause
