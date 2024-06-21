
# Tugas 2 PBO API

**Made Aditya Nugraha Arya Putra 2305551132**

**Dewa Gede Aries Pratama 2305551132**

****DESKRIPSI PROGRAM****

*API,atau Application Programming Interface, adalah sekumpulan aturan dan mekanisme yang memungkinkan berbagai aplikasi berkomunikasi satu sama lain. API mendefinisikan cara komponen perangkat lunak harus berinteraksi dan dapat digunakan untuk menghubungkan berbagai sistem, aplikasi, atau layanan.*

Dengan method Request method pada API:

- GET: Mengambil data dari server.
- POST: Mengirim data ke server untuk membuat sumber daya baru.
- PUT: Memperbarui sumber daya yang ada di server.
- DELETE: Menghapus sumber daya dari server

Dan akan ditest pada aplikasi Postmen untuk menguji API tersebut.
## Spesifikasi API

### Customer
- GET Customers
    - GET /customers => daftar semua pelanggan ``` ```


    - GET /customers/{id} => informasi pelanggan dan alamatnya ``` ```

![WhatsApp Image 2024-06-21 at 15 28 45_ac80386e](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/1559d19c-dc28-449d-8c1d-ec75c9c10f62)



    - GET /customers/{id}/cards => daftar kartu kredit/debit milik pelanggan ``` ```


    - GET /customers/{id}/subscriptions => daftar semua subscriptions milik pelanggan ``` ```



    - GET /customers/{id}/subscriptions?subscriptions_status={active, cancelled,non-renewing} => daftar semua subscriptions milik pelanggan yg berstatus aktif / cancelled / non-renewing ``` ```



- POST Customers
    - POST /customers => buat pelanggan baru  ``` ```
![WhatsApp Image 2024-06-21 at 13 43 59_ef8069e4](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/844ee641-6837-4145-af1c-2b9e41b3659f)

 

- PUT Customers
    - PUT /customers/{id} ``` ```


    - PUT /customers/{id}/shipping_addresses/{id} ``` ```

 

- DELETE Customers 
    - DELETE /customers/{id}/cards/{id} => menghapus informasi kartu kredit pelanggan jika is_primary bernilai false ``` ```

 

    - DELETE /customers/{id} ``` ```


### Items
- GET Items
    - GET /items => daftar semua produk ``````

- POST Items
    - POST /items => buat item baru ``` ```

   ![WhatsApp Image 2024-06-21 at 13 44 00_172d8f8a](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/181388bb-50ef-4d67-9137-2f7ee83a1e2f)


- PUT Items
    - PUT /items/{id} ``` ```

![WhatsApp Image 2024-06-21 at 13 44 03_f01e5c73](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/78e10ccf-f628-45fd-a2af-2b162ed66e0d)


- DELETE Items
    - DELETE /items/{id} => 

![WhatsApp Image 2024-06-21 at 13 44 02_b108dc93](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/dd2f1f4e-29e8-4cbc-baea-cea4fe5242ef)


### Subcriptions
- GET Subcriptions
    - GET /subscriptions => daftar semua subscriptions ``` ```



    - GET /subscriptions?sort_by=current_term_end&sort_type=desc => daftar semua subscriptions diurutkan berdasarkan current_term_end secara descending ``` ```



    - GET /subscriptions/{id} =>
        + informasi subscription,
        + customer: id, first_name, last_name,
        + subscription_items: quantity, amount,
        + item: id, name, price, type 
        ``` ```


- POST Subcriptions
    - POST /subscriptions => buat subscription baru beserta dengan id customer, shipping address, card, dan item yg dibeli ``` ``` 







#### Terimkasih


