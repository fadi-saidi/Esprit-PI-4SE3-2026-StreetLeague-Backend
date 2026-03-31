@echo off
echo Refreshing IDE configuration...
echo Deleting .idea folder to force reimport...
rmdir /s /q .idea
echo .idea folder deleted. 
echo Now reopen the project in IntelliJ IDEA.
echo The Maven sidebar should reappear after reimport.
pause