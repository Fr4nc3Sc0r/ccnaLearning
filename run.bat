@echo off
cd /d "%~dp0"
if not exist dist\ccna-examlab.jar call build.bat
java -jar dist\ccna-examlab.jar %*
