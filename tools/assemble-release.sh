#!/bin/sh
# Release APK for this tree, with JVM sizes that actually fit the phone.
#
# Historical note: TaiXu's ~/.gradle/gradle.properties once overrode this
# project's gradle.properties and pinned -Xmx1024m + SerialGC + 2 workers.
# That is why assembleRelease ran 6m28s, swapped, exhausted Metaspace, and
# then looked like a "timeout" (the agent wrapper's 360s cap cut the client
# off ~28s after Gradle had already written the APK). The global file now
# only sets android.aapt2FromMavenOverride, but launching java ourselves with
# --no-daemon stays the reliable path.
set -eu

ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-arm64}"
JAVA_EXEC="${JAVA_HOME}/bin/java"
GRADLE_HOME="${GRADLE_HOME:-/opt/gradle-8.14.2}"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export JAVA_HOME ANDROID_HOME ANDROID_SDK_ROOT="${ANDROID_HOME}"
export _JAVA_OPTIONS="${_JAVA_OPTIONS:--Djava.security.egd=file:/dev/urandom}"

AAPT2="${TAIXU_AAPT2_PATH:-$ANDROID_HOME/build-tools/35.0.0/aapt2}"

# Stage the prebuilt backend + QNN libs from the local dir (QNN_LIBS_PATH,
# default /opt/QNN/qnnlibs) when it exists. Absence only warns: a tree
# without the binary still builds an APK -- failing here would make this
# script unusable on a fresh clone, same reasoning as the missing keystore.
QNN_SRC="${QNN_LIBS_PATH:-/opt/QNN/qnnlibs}"
if [ -d "$QNN_SRC" ]; then
    "$ROOT/tools/stage_backend.sh" || { echo "==> WARN: staging failed; APK builds WITHOUT the backend" >&2; }
else
    echo "==> WARN: $QNN_SRC absent; skipping backend staging (app runs UI-only)" >&2
fi

# Artifact name follows versionName (repo rule: patch bumps on every push),
# so this line cannot rot on the next release; APP_VERSION=... overrides.
APP_VERSION="${APP_VERSION:-$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' "$ROOT/app/build.gradle.kts" | head -1)}"
if [ -z "$APP_VERSION" ]; then
    echo "==> ERROR: cannot read versionName from app/build.gradle.kts; set APP_VERSION" >&2
    exit 1
fi
OUT="$ROOT/nightmare-mobile-$APP_VERSION-release-signed.apk"
UNSIGNED_FALLBACK="$ROOT/nightmare-mobile-$APP_VERSION-release-unsigned.apk"

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

for CAND in "$ROOT/app/build/outputs/apk/release/app-release.apk" \
            "$ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"; do
    if [ -f "$CAND" ]; then
        # ⚠ `app-release-unsigned.apk` should no longer appear: buildTypes.release
        # falls back to the DEBUG key when the formal keystore is absent, so every
        # build here is signed and installable. The branch stays because a
        # unsigned artifact means AGP ignored that fallback, and naming it
        # honestly beats printing `-signed` over a package nobody can install.
        case "$CAND" in
            *unsigned*)
                cp -f "$CAND" "$UNSIGNED_FALLBACK"
                echo "==> APK $UNSIGNED_FALLBACK" >&2
                echo "==> WARN: UNSIGNED — buildTypes.release did not fall back to the debug key." >&2
                echo "          This APK cannot be installed. Check the signingConfig in app/build.gradle.kts." >&2
                ls -lah "$UNSIGNED_FALLBACK" ;;
            *)
                cp -f "$CAND" "$OUT"
                echo "==> APK $OUT"
                ls -lah "$OUT" ;;
        esac
        break
    fi
done
