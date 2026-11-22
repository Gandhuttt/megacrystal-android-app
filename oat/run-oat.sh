#!/usr/bin/env bash
# OAT runner: jalankan 3 skenario JMeter berurutan (read, login, transaksi order+cancel).
# DNS workers.dev pada jaringan XL Axiata merespons salah (blockpage), sehingga
# semua akses memakai --resolve? Tidak: JMeter memakai override DNS di .jmx
# (HTTP Request > Advanced > hosts) — di CLI kita set property -Jcfip agar mudah diganti.
set -euo pipefail
cd "$(dirname "$0")"

JMETER="${JMETER:-jmeter}"
HOST="megacrystal-web-api.gandhidhuta123123.workers.dev"
CFIP="${CFIP:-104.18.6.192}"
PLAN="megacrystal-oat.jmx"
RESULTS="results"
mkdir -p "$RESULTS"

run() {
  local name="$1"; shift
  echo "=== ${name} ==="
  "$JMETER" -n -t "$PLAN" -l "$RESULTS/${name}.jtl" \
    -Jjmeter.save.saveservice.output_format=csv \
    -Jsenario="${name}" -Jhost="${HOST}" -Jcfip="${CFIP}" "$@"
}

# 1) Katalog publik: 50 users, ramp 30s, durasi 300s
run oat01-katalog -Jthreads=50 -Jramp=30 -Jduration=300 -Jloops=0

# 2) Login berulang: 30 users, ramp 20s, 200 loop (cap 60s)
run oat02-login -Jthreads=30 -Jramp=20 -Jduration=60 -Jloops=200

# 3) Order+cancel transaksi: 10 users CSV, ramp 20s, 60 loop (cap 180s)
run oat03-ordercancel -Jthreads=10 -Jramp=20 -Jduration=180 -Jloops=60

# 4) Soak ringan campuran: 15 users, ramp 30s, durasi 600s
run oat04-soak -Jthreads=15 -Jramp=30 -Jduration=600 -Jloops=0

echo "Selesai. Hasil di $RESULTS/*.jtl"
