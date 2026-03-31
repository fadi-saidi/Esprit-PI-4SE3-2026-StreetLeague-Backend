@echo off
echo Testing compilation...
call mvnw.cmd compile
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Compilation completed without errors!
    echo Application.class should now be available.
) else (
    echo ERROR: Compilation failed. Check errors above.
)
pause