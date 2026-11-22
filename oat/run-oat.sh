#!/usr/bin/env bash
# OAT runner: jalankan 4 skenario JMeter berurutan (katalog, login, order+cancel, soak).
# DNS workers.dev pada jaringan uji merespons salah (blockpage), sehingga semua akses
# memakai override DNS di .jmx; IP edge bisa diganti lewat env CFIP.
# PENTING: naikkan stok produk uji sebelum menjalankan skenario 03 (lihat README).
set -euo pipefail
cd "$(dirname "$0")"

JMETER="${JMETER:-jmeter}"
HOST="megacrystal-web-api.gandhidhuta123123.workers.dev"
CFIP="${CFIP:-104.18.6.192}"
PLAN="megacrystal-oat.jmx"
RESULTS="results"
mkdir -p "$RESULTS"

run() {
  local name="$1" tg="$2"; shift 2
  echo "=== ${name} ==="
  # Isolasi skenario: hanya thread group target yang aktif di salinan plan,
  # agar variabel (accessToken/orderNumber) tidak tercampur antar skenario.
  local tmp="${PLAN}.tmp"
  cp "$PLAN" "$tmp"
  for g in "01 Katalog Public Load" "02 Login Repeated" "03 Order Cancel Transaction" "04 Soak Mixed"; do
    if [[ "$g" != "$tg" ]]; then
      sed -i "s|testname=\"$g\" enabled=\"true\"|testname=\"$g\" enabled=\"false\"|" "$tmp"
    fi
  done
  "$JMETER" -n -t "$tmp" -l "$RESULTS/${name}.jtl" \
    -Jjmeter.save.saveservice.output_format=csv \
    -Jsenario="${name}" -Jhost="${HOST}" -Jcfip="${CFIP}" "$@"
  rm -f "$tmp"
}

# 1) Katalog publik: 50 users, ramp 30s, durasi 300s
run oat01-katalog "01 Katalog Public Load" -Jthreads=50 -Jramp=30 -Jduration=300 -Jloops=0

# 2) Login berulang: 10 users CSV, ramp 20s, 30 loop (cap 60s) — beban wajar
#    untuk akun Cloudflare gratis (30 thread menghasilkan 500/503 edge saturation).
run oat02-login "02 Login Repeated" -Jthreads=10 -Jramp=20 -Jduration=60 -Jloops=30

# 3) Order+cancel transaksi: 10 users CSV, ramp 20s, 30 loop (cap 180s).
#    Setiap iterasi: login -> order (201) -> cancel (200), stok kembali lagi.
run oat03-ordercancel "03 Order Cancel Transaction" -Jthreads=10 -Jramp=20 -Jduration=180 -Jloops=30

# 4) Soak ringan campuran: 15 users, ramp 30s, durasi 600s
run oat04-soak "04 Soak Mixed" -Jthreads=15 -Jramp=30 -Jduration=600 -Jloops=0


echo "Selesai. Hasil di $RESULTS/*.jtl"
