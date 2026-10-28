#!/usr/bin/env bash
set -euo pipefail

KEYSTORE_FILE="release.keystore"
KEY_ALIAS="paper1mock"
STORE_PASS="paper1mockpass"
APK_OUTPUT_PATH="app/build/outputs/apk/release/app-release.apk"

echo "=== [1/4] Checking Signing Keystore ==="
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Generating local release keystore ($KEYSTORE_FILE)..."
    keytool -genkeypair \
        -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$STORE_PASS" \
        -keypass "$STORE_PASS" \
        -dname "CN=Paper 1 Mock, OU=Education, O=IndoreFlorist, L=Indore, ST=MP, C=IN"
else
    echo "Using existing keystore: $KEYSTORE_FILE"
fi

echo "=== [2/4] Building Signed Release APK ==="
if [ -x "./gradlew" ]; then
    ./gradlew clean assembleRelease --no-daemon
elif command -v gradle >/dev/null 2>&1; then
    gradle clean assembleRelease --no-daemon
else
    echo "Error: Neither ./gradlew nor system 'gradle' was found in PATH."
    exit 1
fi

echo "=== [3/4] Measuring Output APK Size ==="
if [ -f "$APK_OUTPUT_PATH" ]; then
    BYTES=$(wc -c < "$APK_OUTPUT_PATH" | tr -d ' ')
    KB=$(awk "BEGIN {printf \"%.2f\", $BYTES / 1024}")
    MB=$(awk "BEGIN {printf \"%.4f\", $BYTES / (1024 * 1024)}")
    echo "--------------------------------------------------------"
    echo "Release APK Built Successfully!"
    echo "Location : $APK_OUTPUT_PATH"
    echo "Size     : ${BYTES} bytes (${KB} KB / ${MB} MB)"
    echo "--------------------------------------------------------"
else
    echo "Error: Expected APK not found at $APK_OUTPUT_PATH"
    exit 1
fi
