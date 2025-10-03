@echo off
title KestrelClient Build
cls
echo Building KestrelClient...
msbuild KestrelClient.vcxproj /p:Configuration=Release /p:Platform=x64