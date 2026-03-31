@echo off
echo ========================================
echo Build Verification Script
echo ========================================

echo.
echo Checking Java version...
java -version

echo.
echo Checking project structure...
if exist "src\main\java" (
    echo [OK] Main source directory exists
) else (
    echo [ERROR] Main source directory missing
)

if exist "src\test\java" (
    echo [OK] Test source directory exists
) else (
    echo [ERROR] Test source directory missing
)

if exist "pom.xml" (
    echo [OK] Maven POM file exists
) else (
    echo [ERROR] Maven POM file missing
)

echo.
echo Listing test files in controller package...
dir "src\test\java\tn\esprit\pi\controller\*.java" /b 2>nul
if errorlevel 1 (
    echo [INFO] No test files found in controller package
)

echo.
echo Build verification complete.
echo ========================================