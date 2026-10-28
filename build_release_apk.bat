@echo off
setlocal

set KEYSTORE_FILE=release.keystore
set KEY_ALIAS=paper1mock
set STORE_PASS=paper1mockpass
set APK_OUTPUT_PATH=app\build\outputs\apk\release\app-release.apk

echo === [1/3] Checking Signing Keystore ===
if not exist "%KEYSTORE_FILE%" (
    echo Generating local release keystore...
    keytool -genkeypair -v -keystore "%KEYSTORE_FILE%" -alias "%KEY_ALIAS%" -keyalg RSA -keysize 2048 -validity 10000 -storepass "%STORE_PASS%" -keypass "%STORE_PASS%" -dname "CN=Paper 1 Mock, OU=Education, O=IndoreFlorist, L=Indore, ST=MP, C=IN"
)

echo === [2/3] Building Signed Release APK ===
if exist "gradlew.bat" (
    call gradlew.bat clean assembleRelease --no-daemon
) else (
    call gradle clean assembleRelease --no-daemon
)

echo === [3/3] Checking Output APK ===
if exist "%APK_OUTPUT_PATH%" (
    echo --------------------------------------------------------
    echo Release APK Built Successfully!
    echo Location: %APK_OUTPUT_PATH%
    for %%A in ("%APK_OUTPUT_PATH%") do echo Size: %%~zA bytes
    echo --------------------------------------------------------
) else (
    echo Error: APK not found at %APK_OUTPUT_PATH%
)
pause
