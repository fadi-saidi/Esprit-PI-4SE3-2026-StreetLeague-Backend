@echo off
echo Compiling main application code only...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Application compiled successfully!
    echo.
    echo Starting Spring Boot application...
    call mvnw.cmd spring-boot:run -DskipTests
) else (
    echo ERROR: Compilation failed.
)
pause