@echo off
echo Final compilation test...
call mvnw.cmd clean compile
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: All code compiled successfully!
    echo.
    echo Starting Spring Boot application...
    call mvnw.cmd spring-boot:run
) else (
    echo ERROR: Compilation failed.
)
pause