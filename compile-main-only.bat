@echo off
echo Compiling main code only (skipping tests)...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Main code compiled successfully!
    echo You can now run the application from IntelliJ IDEA.
    echo The Application class should be available.
) else (
    echo ERROR: Main code compilation failed.
)
pause