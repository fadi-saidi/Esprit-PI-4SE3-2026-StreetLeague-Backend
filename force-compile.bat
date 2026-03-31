@echo off
echo Cleaning target directory and forcing recompilation...
rmdir /s /q target
echo Target directory cleaned.
echo Compiling project...
call mvnw.cmd clean compile
echo Compilation complete. Check for errors above.
echo.
echo Checking if Application.class was created...
if exist "target\classes\tn\esprit\pi\Application.class" (
    echo SUCCESS: Application.class found!
) else (
    echo ERROR: Application.class not found after compilation!
)
pause