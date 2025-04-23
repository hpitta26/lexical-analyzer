:: Windows Command Prompt
@echo off

set DECOMPILED_DIR=decompiled

if not exist "%DECOMPILED_DIR%" (
    mkdir "%DECOMPILED_DIR%"
)

move *.class "%DECOMPILED_DIR%"

echo Decompiled files moved to %DECOMPILED_DIR%/