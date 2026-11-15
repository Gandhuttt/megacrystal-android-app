# 🧊 MegaCrystal - Sistem Distribusi Es Kristal

> **Distribusi Lancar, Es Tetap Segar.**
> 
> Proyek Aplikasi Mobile Terintegrasi untuk Manajemen Pemesanan dan Pengiriman Es Kristal dengan Alur Pembayaran Digital **SIMULASI**.

[▶ Tonton video demo MegaCrystal (MP4)](demo-app.mp4)

Video menampilkan katalog, pemesanan, checkout, riwayat, dan profil pelanggan. Rekaman berhenti sebelum membuat transaksi atau mengonfirmasi pembayaran QRIS.

---

## 📌 Latar Belakang
Bisnis distribusi es kristal memiliki tantangan unik: produk mudah mencair dan pelanggan (restoran, kafe, warung) membutuhkan pasokan yang cepat dan tepat. Selama ini, banyak distributor masih mengandalkan pencatatan manual via telepon atau aplikasi pesan instan. Sistem konvensional ini sering kali menyebabkan:
- **Pesanan Terlewat/Salah Catat:** Akibat penumpukan pesan saat jam sibuk.
- **Risiko *Fake Order* (Pesanan Fiktif):** Penggunaan sistem Cash on Delivery (COD) sering merugikan distributor ketika pesanan dibatalkan sepihak saat armada pengiriman sudah tiba di lokasi.
- **Ketidakpastian Ketersediaan:** Pelanggan tidak mengetahui secara pasti apakah stok es di gudang sedang tersedia atau kosong.

**MegaCrystal** hadir sebagai solusi digital untuk menghubungkan pelanggan langsung ke sistem gudang dan mempercepat respons pengiriman melalui dasbor khusus pekerja logistik. Alur pembayaran saat ini masih **SIMULASI**, bukan penerimaan dana QRIS sungguhan.

---

## 🎯 Tujuan Proyek
1. Mengotomatisasi alur pemesanan es kristal agar prosesnya lebih cepat, transparan, dan terstruktur.
2. Memodelkan alur pembayaran di awal transaksi untuk mengurangi risiko pesanan fiktif (*fake order*); pembayaran saat ini masih simulasi.
3. Memfasilitasi pekerja gudang dengan antrean pesanan yang telah dikonfirmasi dalam alur pembayaran simulasi, sehingga proses persiapan dan pengiriman lebih terstruktur. Status ini bukan bukti uang telah masuk.

---

## ✨ Fitur Utama

Aplikasi MegaCrystal menyediakan antarmuka Android untuk **pelanggan** dan **pekerja gudang**. Akun **admin** diarahkan menggunakan dashboard web untuk pengelolaan, bukan dashboard admin di Android.

Nama pada launcher adalah **MegaCrystal**. Ikon menggunakan gambar logo kristal yang diberikan pengguna, bukan ikon Android bawaan. Resource mencakup ikon normal dan bulat pada lima densitas, foreground adaptive berlatarkan putih, serta silhouette monochrome untuk themed icons.

### 👤 Role Pelanggan (Customer)
- **Katalog & Pemesanan:** Katalog diambil dari backend, termasuk ID produk, harga, dan stok. Pemesanan memakai ID produk yang dipilih dari katalog tersebut, bukan ID atau harga contoh yang ditanam di aplikasi; backend memvalidasi pesanan dan ketersediaan stok.
- **Checkout:** Menampilkan rincian pesanan dan total dari backend, bukan total contoh. Alur konfirmasi pembayaran yang tersedia adalah **SIMULASI**.
- **Riwayat Transaksi:** Mengambil riwayat dan status pesanan dari backend agar pelanggan dapat melihat perkembangan pengiriman.
- **Manajemen Profil:** Perubahan profil dan password disimpan melalui API backend, bukan hanya di tampilan atau sesi lokal.
- **Data Kosong & Kegagalan API:** Layar menampilkan keadaan kosong atau pesan kesalahan yang sebenarnya; tidak menggantinya dengan katalog, pesanan, atau profil demo saat backend gagal.

### 👷 Role Pekerja Gudang (Worker)
- **Antrean Pengiriman:** Mengambil antrean pesanan yang telah dikonfirmasi dalam alur pembayaran **SIMULASI**. Label pembayaran/status pesanan tidak membuktikan penerimaan dana.
- **Ambil Pesanan (*Claim*):** Pekerja mengambil pesanan dari antrean untuk ditugaskan ke akunnya; backend mengatur kepemilikan tugas agar satu pesanan tidak diambil dua pekerja.
- **Detail Pesanan:** Melihat rincian pesanan dan tujuan pengiriman dari backend.
- **Pembaruan Status Logistik:** Memproses tugas milik pekerja melalui alur `assigned` → `shipped` → `delivered` (ambil → kirim → selesai).

### Batasan Pembayaran
Belum ada integrasi provider QRIS nyata atau kredensial pembayaran produksi. Konfirmasi pembayaran saat ini hanya **SIMULASI**: tidak menampilkan QRIS untuk dipindai, tidak menghasilkan Dynamic QRIS produksi, tidak memverifikasi dana masuk, dan tidak boleh dipakai sebagai bukti pelunasan. Pembayaran nyata memerlukan provider QRIS, kredensial yang sah, serta integrasi dan verifikasi notifikasi pembayaran dari provider di backend.

---

## Menjalankan Debug dengan Backend Lokal

Backend harus tersedia secara lokal atau sudah dideploy dengan migrasi terkini serta API profil/password dan API pekerja yang digunakan aplikasi. Backend lama yang belum diperbarui tidak cukup untuk menjalankan alur tersebut.

Dari direktori `megacrystal-android-app`, buat APK debug untuk emulator Android dengan backend lokal di port `8787`:

```sh
./gradlew :app:assembleDebug -PmegacrystalApiUrl=http://10.0.2.2:8787/
```

Alamat `10.0.2.2` mengarah ke host dari emulator Android. Properti `megacrystalApiUrl` tersebut hanya berlaku untuk build **debug**; konfigurasi HTTPS pada build **release** tetap tidak berubah. Manifest khusus debug mengizinkan HTTP (*cleartext*) untuk API lokal, sedangkan izin tersebut tidak ikut masuk ke manifest release.

Pada Android API 37+ (Android 17), akses API lokal juga memerlukan izin runtime jaringan lokal. Manifest **debug saja** menyertakan `ACCESS_LOCAL_NETWORK`; setelah APK debug dipasang, berikan izin melalui Settings → Nearby devices atau jalankan:

```sh
adb shell pm grant com.example.megacrystal_android_app android.permission.ACCESS_LOCAL_NETWORK
```

Langkah ini khusus pengembangan dengan API lokal; API Android yang lebih rendah tidak memerlukan pemberian izin tersebut. Tanpa izin pada API 37+, koneksi aplikasi ke server lokal dapat mengalami timeout meskipun server dapat dijangkau dari shell emulator. Konfigurasi release tidak mendapat izin debug ini. Lihat [panduan izin jaringan lokal Android](https://developer.android.com/privacy-and-security/local-network-permission).

Alternatif melalui koneksi loopback yang dipakai pada verifikasi emulator:

```sh
adb reverse tcp:8787 tcp:8787
./gradlew :app:assembleDebug -PmegacrystalApiUrl=http://127.0.0.1:8787/
```

Pasang APK hasil build tersebut sebelum menjalankan aplikasi. Port reverse ini hanya tersedia selama koneksi ADB aktif.

### Cakupan Verifikasi
Build Android `assembleDebug` berhasil, dengan peringatan deprecation yang sudah ada. Smoke test backend D1 lokal terisolasi mencakup autentikasi/peran, persistensi profil/password, katalog, pembayaran simulasi, serta claim dan transisi pengiriman. Verifikasi emulator mencakup katalog produk 7 kg yang dibuat melalui dasbor, checkout dengan total server, kegagalan konfirmasi yang tetap gagal, pembayaran simulasi berhasil, profil tersimpan ke server, riwayat, serta pekerja mengambil, mengirim, dan menyelesaikan pesanan. Kegagalan pengiriman juga diuji tanpa memajukan status. Ini bukan deployment produksi atau pengujian QRIS nyata. Video di atas tetap hanya menunjukkan alur yang dijelaskan pada keterangannya.

---

## 🎨 Tautan Desain (Figma)
Rancangan antarmuka pengguna (UI/UX) aplikasi ini disusun dengan mematuhi pedoman antarmuka Google Material 3 (M3). Anda dapat menjelajahi desainnya melalui tautan berikut:
👉 **[Figma Design: MegaCrystal](https://www.figma.com/design/v0F4guamuBNHOmDGs5JoW7/Design-MegaCrystal?node-id=0-1&t=XQLObxiNuyWAE8Ol-1)**

---

## 👥 Identitas Tim Pengembang
Proyek ini disusun dan dikembangkan oleh **Kelompok 4** untuk memenuhi tugas mata kuliah **Pemrograman Mobile B**:

| Nama Lengkap | NIM | Peran Utama |
| :--- | :--- | :--- |
| Ahmad Fikri Zakaria | H1D024062 | Frontend Developer |
| Muhammad Faqih Muhyiddin | H1D024068 | Backend Developer |
| Gandhi Dhuta Nirvana | H1D024071 | API Integration |
| Nalendra Wicaksana | H1D024073 | UI/UX Designer |
