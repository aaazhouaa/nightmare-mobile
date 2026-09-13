#!/bin/sh
# Container-side counterpart to tools/stage_backend.ps1.
#
# The .ps1 stages from backend-src/build/android (built on the Windows dev
# machine). There is no backend-src here, so this script stages the same
# payload from a prebuilt local directory instead: $QNN_LIBS_PATH, set in
# /etc/profile.d/android-sdk.sh to /opt/QNN/qnnlibs.
#
# Expected layout in the source dir: libstable_diffusion_core.so plus the 20
# QNN files (2 shared + 6 HTP arches x trio). A missing file is an error --
# the app dies at BackendProcess start with "no qnnlibs in assets", and a
# silently partial set is worse than no set.
set -eu

ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
SRC="${QNN_LIBS_PATH:-/opt/QNN/qnnlibs}"
JNI="$ROOT/app/src/main/jniLibs/arm64-v8a"
ASSETS="$ROOT/app/src/main/assets/qnnlibs"

if [ ! -d "$SRC" ]; then
    echo "stage_backend: no such source dir: $SRC (set QNN_LIBS_PATH)" >&2
    exit 1
fi

# Same arches as tools/stage_backend.ps1 and DeviceProbe.STAGED_ARCHES;
# keep the three in step.
ARCHES="V68 V69 V73 V75 V79 V81"
want="libQnnHtp.so libQnnSystem.so"
for a in $ARCHES; do
    want="$want libQnnHtp$a.so libQnnHtp${a}Stub.so libQnnHtp${a}Skel.so"
done

for n in $want; do
    [ -f "$SRC/$n" ] || { echo "stage_backend: missing QNN lib: $SRC/$n" >&2; exit 1; }
done
[ -f "$SRC/libstable_diffusion_core.so" ] || {
    echo "stage_backend: missing $SRC/libstable_diffusion_core.so" >&2
    exit 1
}

mkdir -p "$JNI" "$ASSETS"

# The backend is exec'd from nativeLibraryDir and must stay byte-exact and
# unstripped (build.gradle.kts: keepDebugSymbols). QNN libs ride in assets
# and are dlopen'd after BackendProcess unpacks this device's arch subset.
cp -f "$SRC/libstable_diffusion_core.so" "$JNI/"
chmod 755 "$JNI/libstable_diffusion_core.so"
for n in $want; do
    cp -f "$SRC/$n" "$ASSETS/"
done

echo "stage_backend: $(cd "$ASSETS" && ls -1 | wc -l) qnn libs (from $SRC) -> app/src/main/assets/qnnlibs"
echo "stage_backend: libstable_diffusion_core.so $(wc -c < "$JNI/libstable_diffusion_core.so") bytes -> app/src/main/jniLibs/arm64-v8a"
