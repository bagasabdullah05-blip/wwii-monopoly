@echo off
echo ========================================
echo   WWII MONOPOLY - BUILD APK
echo ========================================
echo.

where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Building with system Gradle...
    gradle assembleDebug
) else (
    echo Building with Gradle Wrapper...
    if exist "gradlew.bat" (
        call gradlew.bat assembleDebug
    ) else (
        echo.
        echo ERROR: No Gradle found!
        echo.
        echo Options:
        echo   1. Install Gradle: https://gradle.org/install/
        echo   2. Open this folder in Android Studio (it will create wrapper)
        echo   3. Run: gradle wrapper --gradle-version 8.11.1
        echo.
    )
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   BUILD SUCCESS!
    echo   APK: app\build\outputs\apk\debug\app-debug.apk
    echo ========================================
) else (
    echo.
    echo BUILD FAILED!
)
pause
