# Build APK UNO Offline lewat GitHub

1. Buat repository GitHub, misalnya `UNO-Offline`.
2. Upload **seluruh isi folder proyek ini**, termasuk folder `.github`.
3. Pastikan file berikut terlihat di repository:
   - `.github/workflows/build-apk.yml`
   - `app/`
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
4. Buka tab **Actions**.
5. Pilih workflow **Build UNO Offline APK**.
6. Tekan **Run workflow**.
7. Tunggu sampai job selesai dengan tanda hijau.
8. Buka hasil workflow tersebut.
9. Pada bagian **Artifacts**, download `UNO-Offline-debug-APK`.
10. Ekstrak ZIP artifact untuk mendapatkan `app-debug.apk`.

Workflow juga akan berjalan otomatis ketika ada push ke branch `main` atau `master`.

Catatan:
- Workflow menggunakan JDK 17.
- Android SDK platform 35 dan build-tools 35.0.0 dipasang saat build.
- APK yang dihasilkan adalah debug APK untuk pengujian.
