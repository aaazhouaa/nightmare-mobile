#!/bin/sh
# Release APK for this tree, with JVM sizes that actually fit the phone.
#
# ⚠ TaiXu's ~/.gradle/gradle.properties wins over this project's
# gradle.properties and pins -Xmx1024m + SerialGC + 2 workers. That is why
# assembleRelease ran 6m28s, swapped, exhausted Metaspace, and then looked
# like a "timeout": the agent wrapper's 360s cap cut the client off ~28s
# after Gradle had already written the APK.
#
# Gradle has already started its JVM by the time project properties are
# read, so the only reliable override is to launch java ourselves.
set -eu

ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-arm64}"
JAVA_EXEC="${JAVA_HOME}/bin/java"
GRADLE_HOME="${GRADLE_HOME:-/opt/gradle-8.14.2}"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export JAVA_HOME ANDROID_HOME ANDROID_SDK_ROOT="${ANDROID_HOME}"
export _JAVA_OPTIONS="${_JAVA_OPTIONS:--Djava.security.egd=file:/dev/urandom}"

AAPT2="${TAIXU_AAPT2_PATH:-$ANDROID_HOME/build-tools/35.0.0/aapt2}"
OUT="$ROOT/nightmare-mobile-1.4.14-release-signed.apk"

# 2g heap / 512m metaspace / parallel GC: measured 14Gi RAM, previous
# daemon reported 682MiB effective heap and expired on Metaspace.
JVM="-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+UseParallelGC -Dfile.encoding=UTF-8"

echo "==> nightmare assembleRelease"
echo "    java=$JAVA_EXEC"
echo "    jvm=$JVM"

"$JAVA_EXEC" $JVM \
    -Dorg.gradle.appname=gradle \
    -Dorg.gradle.installation.dir="$GRADLE_HOME" \
    -Djava.security.egd=file:/dev/urandom \
    -Dorg.gradle.native=false \
    -classpath "$GRADLE_HOME/lib/*" \
    org.gradle.launcher.GradleMain \
    assembleRelease \
    --console=plain \
    --no-daemon \
    --parallel \
    --max-workers=4 \
    --stacktrace \
    -Dorg.gradle.native=false \
    -Dorg.gradle.jvmargs="$JVM" \
    -Dkotlin.daemon.jvmargs="-Xmx1024m -XX:MaxMetaspaceSize=384m -Dfile.encoding=UTF-8" \
    -Pandroid.builder.sdkDownload=false \
    -Pandroid.aapt2FromMavenOverride="$AAPT2" \
    "$@"

APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK" ]; then
    cp -f "$APK" "$OUT"
    echo "==> APK $OUT"
    ls -lah "$OUT"
fi
