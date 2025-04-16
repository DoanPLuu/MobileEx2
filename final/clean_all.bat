@echo off
setlocal enabledelayedexpansion

cd /d D:\Code\MobileEx\final

for /d %%i in (*) do (
    if exist "%%i\gradlew.bat" (
        echo.
        echo Cleaning project: %%i
        cd "%%i"
        call gradlew.bat clean
        cd ..
    ) else (
        echo Skipping %%i (no gradlew.bat found)
    )
)

echo.
echo ✅ Done cleaning all projects!
pause
