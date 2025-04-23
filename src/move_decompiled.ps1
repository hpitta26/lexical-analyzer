# Windows PowerShell
$DecompiledDir = "decompiled"

if (-Not (Test-Path $DecompiledDir)) {
    New-Item -ItemType Directory -Path $DecompiledDir
}

Get-ChildItem -Filter "*.class" | Move-Item -Destination $DecompiledDir

Write-Host "Decompiled files moved to $DecompiledDir/"