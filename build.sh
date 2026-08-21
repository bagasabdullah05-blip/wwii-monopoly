#!/bin/bash
echo "========================================"
echo "  WWII MONOPOLY - BUILD APK"
echo "========================================"
echo ""

if command -v gradle &> /dev/null; then
    echo "Building with system Gradle..."
    gradle assembleDebug
elif [ -f "gradlew" ]; then
    echo "Building with Gradle Wrapper..."
    chmod +x gradlew
    ./gradlew assembleDebug
else
    echo ""
    echo "ERROR: No Gradle found!"
    echo ""
    echo "Options:"
    echo "  1. Install Gradle: https://gradle.org/install/"
    echo "  2. Open this folder in Android Studio"
    echo "  3. Run: gradle wrapper --gradle-version 8.11.1"
    echo ""
    exit 1
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "  BUILD SUCCESS!"
    echo "  APK: app/build/outputs/apk/debug/app-debug.apk"
    echo "========================================"
else
    echo ""
    echo "BUILD FAILED!"
fi
