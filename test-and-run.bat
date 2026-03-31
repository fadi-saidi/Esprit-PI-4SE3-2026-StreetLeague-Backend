@echo off
echo Testing compilation with Spring Security Test dependency...
call mvnw.cmd clean compile
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Application compiled successfully!
    echo.
    echo Starting Spring Boot application...
    call mvnw.cmd spring-boot:run
) else (
    echo ERROR: Compilation failed.
)
pause