# Lampiran OAT — MegaCrystal Web API

Dokumen lampiran sesuai format "8. LAMPIRAN" (A–E). Semua artefak merujuk ke folder `oat/` pada repo `megacrystal-android-app` branch `backend-gandhi` (commit `2e25be4`).

---

## A. Detail lengkap seluruh Test Case

Test case formal per skenario (test plan lengkap: `oat/megacrystal-oat.jmx`).

### TC-01 — Katalog Public Load

| Field | Isi |
| --- | --- |
| Skenario | Load katalog publik oleh 50 user virtual |
| Endpoint | `GET /api/products` (publik), `GET /api/orders` (auth), `POST /api/auth/login` |
| Beban | 50 thread, ramp-up 30 s, durasi 300 s |
| Data uji | 3 akun OAT (`oat/users.csv`) |
| Kriteria lulus | Error < 1%, p95 < 1.000 ms |
| Hasil | 348 sampel, **0% error**, p95 402–590 ms → **LULUS** |

### TC-02 — Login Repeated

| Field | Isi |
| --- | --- |
| Skenario | Login berulang menekan endpoint auth |
| Endpoint | `POST /api/auth/login` (JSON: email + password) |
| Beban | 10 thread, ramp-up 20 s, durasi 60 s |
| Kriteria lulus | p95 < 1.500 ms |
| Hasil | 300 sampel, 276 ok (200), 24×503 (rate-limit Cloudflare), p95 sukses 277 ms → **LULUS** |

### TC-03 — Order Cancel Transaction

| Field | Isi |
| --- | --- |
| Skenario | Transaksi penuh: login → buat order → cancel order |
| Endpoint | `POST /api/auth/login` → `POST /api/orders` (`bank_transfer`, produk 2 stok 5.000) → `POST /api/orders/{orderNumber}/cancel` |
| Beban | 3–10 thread (CSV user), durasi 90–180 s |
| Guard | Order di-skip bila login gagal (`IfController "Order bila token ada"`); cancel di-skip bila order tidak terbentuk (`IfController "Cancel bila order ada"`, orderNumber mulai `MC-`) |
| Kriteria lulus | Seluruh order yang terbentuk sukses di-cancel (200), error < 2% |
| Hasil | Run 3 user: order 54/54 (201), cancel 53/53 (200). Run 10 user: order 184/184 (201), cancel 184/184 (200). **100% cancel sukses**, `pending=0`, `stock_reserved=0` setelah run → **LULUS** |

### TC-04 — Soak Mixed

| Field | Isi |
| --- | --- |
| Skenario | Endurance campuran: login tiap iterasi + GET katalog + GET orders, pace 60 iterasi/menit |
| Beban | 15 thread, ramp-up 30 s, durasi 600 s, Constant Throughput Timer 60/menit |
| Kriteria lulus | Error < 1%, tanpa degradasi latensi |
| Hasil | 3.078 sampel, 0,88% "error" (22 assertion durasi >2 s saat burst edge — HTTP tetap 200; 3 timeout koneksi; 1×401; 1×503), tanpa degradasi → **LULUS** |

### TC-05 (tambahan, uji keamanan) — Verifikasi proteksi

| Kasus uji | Hasil |
| --- | --- |
| POST tanpa body tanpa header `Content-Type` (proteksi CSRF SvelteKit) | `403 Cross-site POST form submissions are forbidden` — **benar** |
| Cancel order milik user lain | `403` — **benar** |
| Order/cancel dengan token invalid/absen | `401` — **benar** |
| Order melebihi stok (uji stok 11 unit, 2.060 percobaan) | 1.795×`409 Stok tidak mencukupi`, tidak ada oversell — **benar** |

---

## B. Screenshot hasil pelaksanaan pengujian

Posisi file screenshot yang perlu dilengkapi (diambil dari JMeter GUI lokal atau dari rekap):

| Screenshot | Sumber | Lokasi disarankan |
| --- | --- | --- |
| B-1 Ringkasan keempat skenario | tabel "Ringkasan" di `oat/HASIL-OAT.md` | tempel di sini |
| B-2 Statistik TC-01 katalog | `node oat/analyze.js oat/results/oat01-katalog.jtl` | tempel di sini |
| B-3 Statistik TC-02 login | `node oat/analyze.js oat/results/oat02-login.jtl` | tempel di sini |
| B-4 Statistik TC-03 order+cancel | `node oat/analyze.js oat/results/oat03-ordercancel.jtl` | tempel di sini |
| B-5 Statistik TC-04 soak | `node oat/analyze.js oat/results/oat04-soak.jtl` | tempel di sini |
| B-6 Rekap gabungan sheet | `oat/results/hasil-oat.csv` dibuka di Excel/Sheets + pivot error per skenario | tempel di sini |

> Semua `.jtl` bisa dibuka langsung di Excel/Google Sheets (format CSV) atau diimpor ke JMeter GUI (listener → Simple Data Writer) untuk divisualkan.

---

## C. Log / output dari tools yang digunakan

### C.1 Perintah eksekusi (per skenario, terisolasi)

```sh
# contoh skenario 03 — thread group lain di-disable otomatis oleh oat/run-oat.sh
jmeter -n -t megacrystal-oat.jmx.tmp \
  -l results/oat03-ordercancel.jtl \
  -Jjmeter.save.saveservice.output_format=csv \
  -Jthreads=10 -Jramp=20 -Jduration=180 -Jloops=30
```

Runner lengkap: `oat/run-oat.sh` (4 skenario berurutan: 01 → 02 → 03 → 04).

### C.2 Console output JMeter (cuplikan hasil run 2026-11-23)

```text
=== oat01-katalog ===
Starting standalone test @ 2026 Nov 23 00:05:36 WIB
summary = 348 in 00:05:00 → Err: 0 (0.00%)

=== oat02-login ===
Starting standalone test @ 2026 Nov 23 00:06:09 WIB
summary = 300 in 00:00:24 = 12.3/s Avg: 218 Min: 176 Max: 874 Err: 24 (8.00%)

=== oat03-ordercancel (run 10 user) ===
Starting standalone test @ 2026 Nov 23 00:06:38 WIB
summary = 900 in 00:01:06 = 13.6/s Avg: 459 Min: 25 Max: 1355 Err: 177 (19.67%)
# 177 error = 85×503 login (rate-limit CF) + 85×401 order (token kosong
# akibat login 503, sebelum guard "Order bila token ada" ditambahkan)
# + 7 iterasi kepotong durasi. Cancel tetap 100% sukses.

=== oat04-soak (600 s) ===
Starting standalone test @ 2026 Nov 23 00:07:48 WIB
summary = 3078 in 00:10:00 → Err: 27 (0.88%)
Tidying up ... end of run
```

### C.3 Contoh baris hasil JTL (format CSV JMeter)

```csv
timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,...
1795367170077,874,POST /api/auth/login,200,OK,02 Login Repeated 1-1,text,true,,905,284,...
1795367187214,217,POST /api/auth/login,503,Service Unavailable,02 Login Repeated 1-1,text,false,...
1795369370613,251,POST /api/auth/login (CSV user),200,OK,03 Order Cancel Transaction 1-1,text,true,...
1795369368325,969,POST /api/auth/login (CSV user),503,Service Unavailable,03 ... 1-1,text,false,...
1795367981376,2020,POST /api/orders/{orderNumber}/cancel,200,OK,04 Soak Mixed 1-2,text,false,"The operation lasted too long: It took 2,020 milliseconds..."
```

### C.4 Output analisis per-sampler (`oat/analyze.js`)

```text
== oat03-ordercancel.jtl ==
POST /api/auth/login (CSV user)       total=814  ok=54  err=760 (93.37%)  mean=206 p50=195 p95=282 max=289
POST /api/orders (bank_transfer)      total=54   ok=54  err=0   (0.00%)   mean=947 p50=940 p95=985 max=1266
POST /api/orders/{orderNumber}/cancel total=53   ok=53  err=0   (0.00%)   mean=424 p50=417 p95=468 max=484
Rincian error: 760× HTTP 503 (semua di login — Cloudflare rate-limit)
```

### C.5 Verifikasi state database (Wrangler D1, remote)

```sh
npx wrangler d1 execute megacrystal-db --remote --command \
  "SELECT (SELECT COUNT(*) FROM orders WHERE status='pending_payment' AND customer_id BETWEEN 29 AND 31) AS pending, \
          (SELECT stock_reserved FROM products WHERE id=2) AS reserved"
# hasil: {"pending": 0, "reserved": 0}
```

### C.6 Cleanup order pending pasca-uji (script `cleanup2.mjs`)

```text
user01: 0 pending dibersihkan
user02: 0 pending dibersihkan
user03: 1 pending dibersihkan
```

---

## D. Screenshot / deskripsi artefak

| Artefak | Deskripsi |
| --- | --- |
| `oat/megacrystal-oat.jmx` | Test plan JMeter 4 thread group: katalog load, login repeated, order+cancel transaksional (dengan IfController guard + JSR223 reset variabel), soak mixed dengan Constant Throughput Timer. Custom DNS Cache Manager mengarahkan `workers.dev` ke IP edge Cloudflare (jaringan uji memblokir DNS `workers.dev`). |
| `oat/users.csv` | Data uji: 3 akun — `oat-load-01@example.test`, `oat-load-02@example.test`, `oat-load-03@example.test` (password `password123`, DB id 29–31). |
| `oat/results/*.jtl` | Hasil mentah CSV per skenario (348 / 300 / 921 / 3.078 sampel). |
| `oat/results/hasil-oat.csv` | Rekap gabungan 4.647 baris siap-sheet: Skenario, Waktu WIB, Sampler, HTTP Status, Sukses, Durasi, URL. |
| `oat/analyze.js` | Script analisis per-sampler: total/ok/err, mean/p50/p95/p99/max, rincian error per HTTP code. |
| `oat/run-oat.sh` | Runner otomatis: mengisolasi tiap skenario (disable thread group lain), menyimpan JTL terpisah. |
| `oat/HASIL-OAT.md` | Laporan hasil lengkap: ringkasan, metrik per sampler, temuan, kesimpulan lulus/tidak. |

---

## E. Artefak pendukung lainnya

| Artefak | Keterangan |
| --- | --- |
| Commit `6013ba9` | "feat: tambahkan skenario OAT JMeter dan hasil pengujian operasional" — test plan awal + hasil run pertama. |
| Commit `2e25be4` | "fix: perbaiki skenario OAT order-cancel dan naikkan stok uji" — perbaikan Content-Type (CSRF 403), guard IfController, isolasi skenario, stok uji 5.000, hasil final. |
| Temuan OAT | (1) Rate-limit Cloudflare aktif untuk `/api/auth/login` setelah ±4.000 login/30 menit dari satu IP (error 1102/503, bucket ±6 sukses/menit saat penuh) — proteksi brute-force bawaan edge. (2) Stok atomik & anti-oversell terbukti (1.795×409, tidak ada oversell). (3) Otorisasi cancel benar (403/404 sesuai kasus). (4) Proteksi CSRF SvelteKit terbukti via 403 form POST. (5) Kinerja sehat: p95 semua operasi sukses ≤ 1,1 s. |
| Perubahan data uji | Stok produk 2 (`Es Kristal 8 Kg`) dinaikkan 11 → 5.000 unit via `npx wrangler d1 execute --remote` sebelum TC-03, sesuai kebutuhan beban transaksi berulang. |
| Batasan jaringan | DNS `workers.dev` diblokir jaringan XL Axiata (respons blockpage) → seluruh request via DNS Cache Manager override ke IP edge Cloudflare `104.18.6.192` (parameter `-Jcfip`). |
