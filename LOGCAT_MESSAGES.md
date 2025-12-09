# Panduan Pesan Logcat
Selaraskan setiap logcat berikut dengan mekanisme logging (misalnya `Log.i`, `Log.w`, `Log.e`, atau Timber). Gunakan placeholder (`${...}`) untuk menyisipkan data runtime.

## Login Akun Google
- **Berhasil**: `Log.i("GoogleLogin", "Login Google berhasil untuk ${account.email}")`
- **Gagal**: `Log.e("GoogleLogin", "Login Google gagal: ${error.message}")`

## Pencatatan Transaksi Jual Beli Emas
- **Berhasil**: `Log.i("GoldTransaction", "Transaksi emas ${transactionId} tercatat: ${gram}gr @${pricePerGram}")`
- **Gagal**: `Log.e("GoldTransaction", "Gagal mencatat transaksi emas ${transactionId}: ${error.message}")`

## Sinkronisasi Offline/Online
- **Berhasil**: `Log.i("SyncEngine", "Sinkronisasi ${mode} selesai, ${records} item terselaraskan")`
- **Gagal**: `Log.e("SyncEngine", "Sinkronisasi ${mode} gagal di offset ${offset}: ${error.message}")`

## Manajemen Stok (Tambah/Edit/Hapus)
- **Tambah berhasil**: `Log.i("StockManager", "Stok baru ${productId} ditambahkan (${qty} unit)")`
- **Tambah gagal**: `Log.e("StockManager", "Gagal menambah stok ${productId}: ${error.message}")`
- **Edit berhasil**: `Log.i("StockManager", "Stok ${productId} diperbarui menjadi ${qty} unit")`
- **Edit gagal**: `Log.e("StockManager", "Gagal memperbarui stok ${productId}: ${error.message}")`
- **Hapus berhasil**: `Log.w("StockManager", "Stok ${productId} dihapus dari katalog")`
- **Hapus gagal**: `Log.e("StockManager", "Gagal menghapus stok ${productId}: ${error.message}")`

## Pembuatan Nota Digital PDF
- **Berhasil**: `Log.i("InvoiceGenerator", "Nota digital ${invoiceNumber}.pdf berhasil dibuat di ${filePath}")`
- **Gagal**: `Log.e("InvoiceGenerator", "Gagal membuat nota ${invoiceNumber}: ${error.message}")`

## Checkout Pesanan
- **Berhasil**: `Log.i("CheckoutFlow", "Checkout pesanan ${orderId} selesai, total ${totalAmount}")`
- **Gagal**: `Log.e("CheckoutFlow", "Checkout pesanan ${orderId} gagal: ${error.message}")`

## Pesanan Online Masuk ke Admin
- **Berhasil**: `Log.i("AdminOrders", "Pesanan online ${orderId} masuk ke dashboard admin")`
- **Gagal**: `Log.e("AdminOrders", "Gagal memuat pesanan online baru: ${error.message}")`

## Notifikasi Masuk (Admin & Pelanggan)
- **Admin berhasil**: `Log.i("NotificationAdmin", "Notifikasi admin ${notificationId} diterima")`
- **Admin gagal**: `Log.e("NotificationAdmin", "Notifikasi admin gagal diproses: ${error.message}")`
- **Pelanggan berhasil**: `Log.i("NotificationCustomer", "Notifikasi pelanggan ${notificationId} diterima")`
- **Pelanggan gagal**: `Log.e("NotificationCustomer", "Notifikasi pelanggan gagal diproses: ${error.message}")`

## Perubahan Status Pesanan di Admin
- **Berhasil**: `Log.i("OrderStatus", "Status pesanan ${orderId} berubah dari ${oldStatus} ke ${newStatus}")`
- **Gagal**: `Log.e("OrderStatus", "Gagal mengubah status pesanan ${orderId}: ${error.message}")`

## Navigasi Screen Penting
- **Riwayat transaksi admin berhasil**: `Log.i("AdminHistoryScreen", "Riwayat transaksi admin ditampilkan")`
- **Riwayat transaksi admin gagal**: `Log.e("AdminHistoryScreen", "Gagal membuka riwayat transaksi admin: ${error.message}")`
- **Laporan keuangan berhasil**: `Log.i("FinanceReportScreen", "Laporan keuangan berhasil dibuka")`
- **Laporan keuangan gagal**: `Log.e("FinanceReportScreen", "Gagal membuka laporan keuangan: ${error.message}")`
- **Katalog pelanggan berhasil**: `Log.i("CustomerCatalogScreen", "Katalog pelanggan siap digunakan")`
- **Katalog pelanggan gagal**: `Log.e("CustomerCatalogScreen", "Gagal memuat katalog pelanggan: ${error.message}")`
- **Riwayat pembelian berhasil**: `Log.i("PurchaseHistoryScreen", "Riwayat pembelian pelanggan ditampilkan")`
- **Riwayat pembelian gagal**: `Log.e("PurchaseHistoryScreen", "Gagal membuka riwayat pembelian: ${error.message}")`
