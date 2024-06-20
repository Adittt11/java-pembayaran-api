
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



    - GET /customers/{id}/cards => daftar kartu kredit/debit milik pelanggan ``` ```



    - GET /customers/{id}/subscriptions => daftar semua subscriptions milik pelanggan ``` ```



    - GET /customers/{id}/subscriptions?subscriptions_status={active, cancelled,non-renewing} => daftar semua subscriptions milik pelanggan yg berstatus aktif / cancelled / non-renewing ``` ```


- POST Customers
    - POST /customers => buat pelanggan baru  ``` ```

 

- PUT Customers
    - PUT /customers/{id} ``` ```


    - PUT /customers/{id}/shipping_addresses/{id} ``` ```

 

- DELETE Customers 
    - DELETE /customers/{id}/cards/{id} => menghapus informasi kartu kredit pelanggan jika is_primary bernilai false ``` ```

 

    - DELETE /customers/{id} ``` ```


### Items
- GET Items
    - GET /items => daftar semua produk ``````



    - GET /items?is_active=true => daftar semua produk yg memiliki status aktif ``` ```


    - GET /items/{id} => informasi produk `` ```


- POST Items
    - POST /items => buat item baru ``` ```

   

- PUT Items
    - PUT /items/{id} ``` ```



- DELETE Items
    - DELETE /items/{id} => mengubah status item is_active menjadi false ``` ```


### Subcriptions
- GET Subcriptions
    - GET /subscriptions => daftar semua subscriptions ``` ```



    - GET /subscriptions?sort_by=current_term_end&sort_type=desc => daftar semua subscriptions diurutkan berdasarkan current_term_end secara descending ``` ```



    - GET /subscriptions/{id} =>
        + informasi subscription,
        + customer: id, first_name, last_name,
        + subscription_items: quantity, amount,
        + item: id, name, price, type 
        ```(127.0.0.1:9152/subscriptions/7)```


- POST Subcriptions
    - POST /subscriptions => buat subscription baru beserta dengan id customer, shipping address, card, dan item yg dibeli ``` ``` 







#### Terimkasih


