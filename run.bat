@echo off
cd /d "%~dp0"
if not exist dist\lordoc.jar call build.bat
java -jar dist\lordoc.jar %*
