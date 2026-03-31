@echo off
echo Starting Spring Boot Application...
call mvnw.cmd clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)
call mvnw.cmd spring-boot:run
pause