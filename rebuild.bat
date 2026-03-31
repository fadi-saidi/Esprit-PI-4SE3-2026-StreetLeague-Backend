@echo off
echo Cleaning and rebuilding project...
call mvnw.cmd clean
call mvnw.cmd compile
echo Build complete. Check for errors above.
pause