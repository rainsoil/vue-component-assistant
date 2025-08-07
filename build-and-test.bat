@echo off
echo ========================================
echo Element Plus Assistant 插件构建和测试
echo ========================================

REM 检查Gradle是否安装
gradle --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Gradle
    echo 请从 https://gradle.org/install/ 安装 Gradle
    pause
    exit /b 1
)

echo.
echo 🔧 清理之前的构建...
gradle clean

echo.
echo 🏗️ 构建插件...
gradle buildPlugin

REM 检查构建是否成功
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)

echo.
echo ✅ 构建成功！
echo 📦 插件文件位置: build/distributions/
echo.

REM 列出生成的文件
echo 📋 生成的文件:
dir build\distributions\*.jar /b 2>nul
if errorlevel 1 (
    echo 未找到生成的插件文件
    pause
    exit /b 1
)

echo.
echo 📖 安装说明:
echo 1. 打开 IntelliJ IDEA
echo 2. 进入 File -> Settings -> Plugins
echo 3. 点击齿轮图标 -> Install Plugin from Disk
echo 4. 选择上面的 .jar 文件
echo 5. 重启 IntelliJ IDEA
echo.

echo 🧪 测试说明:
echo 1. 创建一个新的 .vue 文件
echo 2. 在 template 中输入 < 测试组件补全
echo 3. 输入 el-bu 测试前缀过滤
echo 4. 在组件标签内输入空格测试属性补全
echo 5. 在组件标签内输入 @ 测试事件补全
echo.

echo 🎯 新功能特性:
echo ✅ 组件补全包含详细描述
echo ✅ 属性补全只显示当前组件的属性
echo ✅ 事件补全只显示当前组件的事件
echo ✅ 智能上下文感知
echo ✅ 前缀过滤支持（如输入 typ 显示 type 属性）
echo ✅ 自动生成事件处理函数名
echo.

pause
