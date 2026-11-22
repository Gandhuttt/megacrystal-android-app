# OAT JMeter — MegaCrystal Web API

Operational Acceptance Testing terhadap backend Cloudflare Worker (`https://megacrystal-web-api.gandhidhuta123123.workers.dev`) memakai Apache JMeter 5.6.3.

## Skenario (`megacrystal-oat.jmx`)

| # | Thread Group | Beban | Durasi/Loop | Kriteria lulus |
| --- | --- | --- | --- | --- |
| 1 | `01 Katalog Public Load` | 50 users, ramp-up 30 s | 300 s | p95 < 1000 ms, error < 1% |
| 2 | `02 Login Repeated` | 30 users, ramp-up 20 s | 200 loop/thread (max 60 s) | p95 < 1500 ms, error < 1% |
| 3 | `03 Order Cancel Transaction` | 10 users CSV (`users.csv`), ramp-up 20 s | 60 loop (max 180 s) | error < 2% |
| 4 | `04 Soak Mixed` | 15 users, ramp-up 30 s | 600 s | error < 1% |

- Skenario 3 memakai CSV `users.csv` (email,password) dan mengeksekusi `POST /api/orders` (`paymentMethod: bank_transfer`, 1 item produk 2) lalu `POST /api/orders/<orderNumber>/cancel`. Cancel melepas reservasi stok seketika, sehingga beban transaksi tidak menguras stok.
- Skenario 4 (soak) mencampur GET katalog dan POST login, runtime 10 menit.
- Semua respons diverifikasi JSON assertion `$.success` tidak dicek; cukup HTTP 200/201/200-poli dan kegagalan jaringan dihitung error oleh JMeter.

## Menjalankan

```sh
nix-shell -p jmeter --run "oat/run-oat.sh"          # dari root repo android
# atau langsung:
jmeter -n -t oat/megacrystal-oat.jmx -l oat/results/aggregate.jtl -e -o oat/results/report -Dusers_csv=oat/users.csv
```

`run-oat.sh` menjalankan keempat skenario berurutan dengan hasil terpisah di `oat/results/<skenario>/`.

> **Catatan operasional:** target adalah worker production berbagi akun Cloudflare gratis dan kredensial Midtrans sandbox. Beban dirancang rendah (≤50 concurrent, ≤10 menit) agar tetap dalam batas wajar; jangan menaikkan thread/durasi tanpa pertimbangan.

## Hasil

Rekap angka setelah eksekusi: [HASIL-OAT.md](HASIL-OAT.md).
