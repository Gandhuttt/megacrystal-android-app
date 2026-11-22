# Lingkungan pengembangan Android untuk MegaCrystal di NixOS.
#
#   nix-shell            # masuk ke shell
#   mg-build             # build APK debug (aapt2 otomatis di-patch)
#   mg-test              # unit test
#   mg-install           # pasang APK ke device/emulator yang terhubung
#
# NixOS tidak bisa menjalankan binary glibc generik, sedangkan aapt2 diunduh
# Gradle sebagai binary glibc. Shell ini menyediakan patchelf + glibc agar
# aapt2 tersebut bisa dijalankan tanpa programs.nix-ld.enable.
#
# Emulator SDK tetap butuh nix-ld (`programs.nix-ld.enable = true`) karena ia
# memuat banyak library bersama yang tidak praktis di-patch satu per satu.
# `adb` di sini berasal dari nixpkgs, jadi sudah bisa dipakai langsung.

{ pkgs ? import <nixpkgs> { } }:

let
  sdkFallback = "$HOME/Android/Sdk";
in
pkgs.mkShell {
  name = "megacrystal-android";

  packages = with pkgs; [
    jdk25
    patchelf
    android-tools # adb, fastboot
    zlib
  ];

  # Path dipakai oleh mg-patch-aapt2; sengaja tidak di-hardcode ke store hash.
  AAPT2_LIBS = pkgs.lib.makeLibraryPath [ pkgs.glibc pkgs.stdenv.cc.cc.lib pkgs.zlib ];
  AAPT2_LD = "${pkgs.glibc}/lib/ld-linux-x86-64.so.2";

  shellHook = ''
    export JAVA_HOME="${pkgs.jdk25.home}"
    export PATH="$JAVA_HOME/bin:$PATH"

    sdk_dir=""
    if [ -f local.properties ]; then
      sdk_dir=$(sed -n 's/^sdk\.dir=//p' local.properties | head -1)
    fi
    if [ -z "$sdk_dir" ]; then
      sdk_dir="${sdkFallback}"
    fi
    if [ -d "$sdk_dir" ]; then
      export ANDROID_HOME="$sdk_dir"
      export ANDROID_SDK_ROOT="$sdk_dir"
      # platform-tools di belakang PATH: adb dari nixpkgs yang dipakai, karena
      # binary SDK tidak dapat dieksekusi di NixOS tanpa nix-ld.
      export PATH="$PATH:$sdk_dir/platform-tools"
    else
      echo "megacrystal: SDK Android tidak ditemukan; set ANDROID_HOME manual." >&2
    fi

    export AAPT2_OVERRIDE="$PWD/.nix-aapt2/aapt2"

    mg-patch-aapt2() {
      local gradle_home="''${GRADLE_USER_HOME:-$HOME/.gradle}"
      local src
      src=$(find "$gradle_home/caches" -path '*aapt2-*-linux/aapt2' -type f -printf '%T@ %p\n' 2>/dev/null |
        sort -rn | head -1 | cut -d' ' -f2-)
      if [ -z "$src" ]; then
        echo "megcrystal: aapt2 belum ada di cache Gradle." >&2
        echo "  Jalankan 'mg-build' sekali; aapt2 diunduh lalu otomatis di-patch." >&2
        return 1
      fi
      mkdir -p "$(dirname "$AAPT2_OVERRIDE")"
      cp -f "$src" "$AAPT2_OVERRIDE"
      chmod +w "$AAPT2_OVERRIDE"
      patchelf \
        --set-interpreter "$AAPT2_LD" \
        --set-rpath "$AAPT2_LIBS" \
        "$AAPT2_OVERRIDE"
      echo "megacrystal: aapt2 di-patch dari $src"
    }

    mg-gradle() {
      ./gradlew -Pandroid.aapt2FromMavenOverride="$AAPT2_OVERRIDE" "$@"
    }

    mg-build() {
      if [ ! -x "$AAPT2_OVERRIDE" ]; then
        mg-patch-aapt2 >/dev/null 2>&1 || true
      fi
      if ! mg-gradle :app:assembleDebug; then
        echo "megacrystal: build gagal; mencoba patch aapt2 lalu ulangi." >&2
        mg-patch-aapt2 || return 1
        mg-gradle :app:assembleDebug
      fi
    }

    mg-test() {
      if [ ! -x "$AAPT2_OVERRIDE" ]; then
        mg-patch-aapt2 >/dev/null 2>&1 || true
      fi
      mg-gradle :app:testDebugUnitTest "$@"
    }

    mg-install() {
      adb install -r app/build/outputs/apk/debug/app-debug.apk
    }

    echo "megacrystal-android: java $(java -version 2>&1 | head -1 | sed 's/.*"\(.*\)".*/\1/')"
    echo "  mg-build | mg-test | mg-install | mg-patch-aapt2"
    if [ ! -e /run/current-system/sw/share/nix-ld/lib ]; then
      echo "  catatan: programs.nix-ld.enable belum aktif; emulator SDK tidak dapat dijalankan (adb tetap jalan)." >&2
    fi
  '';
}
