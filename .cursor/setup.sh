#!/usr/bin/env bash
set -euo pipefail

SDK_ROOT="$HOME/android-sdk"
GRADLE_VER="9.3.1"
GRADLE_DIR="$HOME/gradle-dist/gradle-${GRADLE_VER}"
CMDLINE_ZIP_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"

# 1. Android SDK command-line tools
if [ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Installing Android command-line tools..."
  mkdir -p "$SDK_ROOT/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -sSL -o "$tmp/cmdtools.zip" "$CMDLINE_ZIP_URL"
  rm -rf "$SDK_ROOT/cmdline-tools/latest"
  unzip -q "$tmp/cmdtools.zip" -d "$tmp"
  mv "$tmp/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
  rm -rf "$tmp"
fi
export PATH="$SDK_ROOT/cmdline-tools/latest/bin:$SDK_ROOT/platform-tools:$PATH"

# 2. SDK packages (idempotent: skips already-installed) + license acceptance
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager --install "platform-tools" "platforms;android-36" "platforms;android-36.1" "build-tools;36.1.0" "build-tools;36.0.0" >/dev/null

# 3. Gradle
if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  echo "Installing Gradle ${GRADLE_VER}..."
  mkdir -p "$HOME/gradle-dist"
  tmp="$(mktemp -d)"
  curl -sSL -o "$tmp/gradle.zip" "https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip"
  unzip -q "$tmp/gradle.zip" -d "$HOME/gradle-dist"
  rm -rf "$tmp"
fi

# 4. Persist toolchain env for interactive shells
if ! grep -q "Android/Gradle toolchain" "$HOME/.bashrc" 2>/dev/null; then
  {
    echo ''
    echo '# --- Android/Gradle toolchain (Cloud Agent environment) ---'
    echo 'export ANDROID_HOME="$HOME/android-sdk"'
    echo 'export ANDROID_SDK_ROOT="$ANDROID_HOME"'
    echo "export GRADLE_HOME=\"\$HOME/gradle-dist/gradle-${GRADLE_VER}\""
    echo 'export PATH="$GRADLE_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"'
    echo '# --- end toolchain ---'
  } >> "$HOME/.bashrc"
fi

# 5. Project-local, gitignored config
echo "sdk.dir=$SDK_ROOT" > local.properties
if [ ! -f debug.keystore ]; then
  keytool -genkeypair -v -keystore debug.keystore -storepass android \
    -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 \
    -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi

# 6. Warm the build / verify the app compiles
"$GRADLE_DIR/bin/gradle" --no-daemon :app:assembleDebug
