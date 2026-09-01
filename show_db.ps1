# PowerShell Live Database Inspector Script for SafeJourneyAI
$adb = "C:\Users\Suraj\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$targetDb = Join-Path $PSScriptRoot "scratch_db.db"

Write-Host "`n[+] Fetching live database from connected Android device..." -ForegroundColor Cyan

$b64 = & $adb shell "run-as com.safejourneyai.app base64 databases/safejourney.db"
$b64 = $b64 -replace "\r","" -replace "\n",""
[System.IO.File]::WriteAllBytes($targetDb, [System.Convert]::FromBase64String($b64))

Write-Host "[+] Live database extracted successfully!`n" -ForegroundColor Green

python "C:\Users\Suraj\.gemini\antigravity-ide\brain\912d1b64-67b8-43d9-b44b-a18bb6a8bc0d\scratch\inspect_db.py"

Remove-Item -Force $targetDb -ErrorAction SilentlyContinue
