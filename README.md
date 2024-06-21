
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
![WhatsApp Image 2024-06-21 at 13 57 38_bdd337ca](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/17b53667-f83b-4c31-9fd0-8cd8eaf36db3)


- GET /customers/{id} => informasi pelanggan dan alamatnya ``` ```

![WhatsApp Image 2024-06-21 at 15 28 45_ac80386e](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/1559d19c-dc28-449d-8c1d-ec75c9c10f62)



-  GET /customers/{id}/cards => daftar kartu kredit/debit milik pelanggan ``` ```

![WhatsApp Image 2024-06-21 at 17 01 45_2e151f05](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/2e7c6260-4df8-4e72-b377-62700a2a26ea)

- GET /customers/{id}/subscriptions => daftar semua subscriptions milik pelanggan ``` ```
![WhatsApp Image 2024-06-21 at 17 34 07_c086c838](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/6317698c-cdc3-407b-8a5e-b6711b954658)



 - GET /customers/{id}/subscriptions?subscriptions_status={active, cancelled,non-renewing} => daftar semua subscriptions milik pelanggan yg berstatus aktif / cancelled / non-renewing ``` ```


### Subcriptions
- GET Subcriptions
    - GET /subscriptions => daftar semua subscriptions ``` ```
![WhatsApp Image 2024-06-21 at 16 19 38_ebfd1ecc](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/b9b1a7cf-3a5c-48d2-9d5e-c060c66a81c3)



    - GET /subscriptions?sort_by=current_term_end&sort_type=desc => daftar semua subscriptions diurutkan berdasarkan current_term_end secara descending ``` ```
![WhatsApp Image 2024-06-21 at 16 31 55_7c1da18c](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/10d09620-3a48-410a-b6fa-826189087663)



    - GET /subscriptions/{id} =>
        + informasi subscription,
        + customer: id, first_name, last_name,
        + subscription_items: quantity, amount,
        + item: id, name, price, type 
        ``` ```
![WhatsApp Image 2024-06-21 at 17 26 06_188f2314](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/42a385ab-5925-4fb3-8bfc-1a080fbd080a)

- POST Customers
    - POST /customers => buat pelanggan baru  ``` ```
![WhatsApp Image 2024-06-21 at 13 43 59_ef8069e4](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/844ee641-6837-4145-af1c-2b9e41b3659f)

 

- PUT Customers
    - PUT /customers/{id} ``` ```
![WhatsApp Image 2024-06-21 at 16 59 47_d890fa44](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/85fe3c07-7cd2-4ecc-803e-82bc149d1469)

    - PUT /customers/{id}/shipping_addresses/{id} ``` ```

 

- DELETE Customers 
    - DELETE /customers/{id}/cards/{id} => menghapus informasi kartu kredit pelanggan jika is_primary bernilai false ``` ```
![WhatsApp Image 2024-06-21 at 17 05 50_9fe30bc4](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/c54555ac-d2e4-4ad7-8b32-8481cba4bc42)



### Items
- GET Items
    - GET /items => daftar semua produk ``````
![WhatsApp Image 2024-06-21 at 17 11 38_c2b5aa4a](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/016e0e56-7bd8-4f9d-9bff-11af7691a5f7)

-GET /items?is_active=true => daftar semua produk yg memiliki status aktif

GET /items/{id} => informasi produk

- POST Items
    - POST /items => buat item baru ``` ```

![WhatsApp Image 2024-06-21 at 16 22 34_ef906b75](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/64dc76d4-1a1f-4c06-ab8b-4221d4474bb8)


- PUT Items
    - PUT /items/{id} ``` ```

![WhatsApp Image 2024-06-21 at 13 44 03_f01e5c73](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/78e10ccf-f628-45fd-a2af-2b162ed66e0d)


- DELETE Items
    - DELETE /items/{id} => 

![WhatsApp Image 2024-06-21 at 13 44 02_b108dc93](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/dd2f1f4e-29e8-4cbc-baea-cea4fe5242ef)





- POST Subcriptions
    - POST /subscriptions => buat subscription baru beserta dengan id customer, shipping address, card, dan item yg dibeli ``` ``` 
![WhatsApp Image 2024-06-21 at 16 36 01_ab5e90e2](https://github.com/Adittt11/java-pembayaran-api/assets/146901357/ca928f8e-a9cd-4eab-a250-a5f88bc6bd89)







#### Terimkasih


