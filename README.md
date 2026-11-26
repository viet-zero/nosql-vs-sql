<div align="center">

# 🧭 NoSQL vs SQL
### Database Visual Lab · Java Swing

**Một ứng dụng học tập nhỏ để quan sát cùng dữ liệu theo mô hình quan hệ và mô hình document.**

![Java 17](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-437291)
![Simulation](https://img.shields.io/badge/Mode-in--memory%20simulation-6E56CF)
![Single file](https://img.shields.io/badge/Source-single%20Java%20file-6E56CF)

</div>

---

## 📚 Mục lục

- [Ứng dụng làm gì?](#-ứng-dụng-làm-gì)
- [So sánh hai mô hình](#-so-sánh-hai-mô-hình)
- [Yêu cầu và cách chạy](#-yêu-cầu-và-cách-chạy)
- [Kịch bản trình bày](#-kịch-bản-trình-bày)
- [Dữ liệu mẫu](#-dữ-liệu-mẫu)
- [Giới hạn](#-giới-hạn)
- [Cấu trúc repository](#-cấu-trúc-repository)

## 🔬 Ứng dụng làm gì?

Ứng dụng mô phỏng bằng Java hai cách tổ chức cùng dữ liệu sinh viên và điểm:

- **SQL Server:** hai bảng quan hệ `Students` và `Scores`; phần hiển thị JOIN chúng thành các hàng.
- **MongoDB:** collection `students`; điểm được nhúng trong mảng `scores` của mỗi document.
- Cùng thao tác **INSERT, UPDATE, DELETE, QUERY** trên cả hai mô hình.
- So sánh `SELECT ... JOIN` với `find()` và câu lệnh tương ứng ngay trong giao diện.
- So sánh SQL `GROUP BY` với MongoDB aggregation pipeline.
- Có tab **Viết SQL → NoSQL** để nhập `SELECT` đơn giản, xem `find()` tương ứng và lọc kết quả mô phỏng.
- Thêm một field riêng vào document để minh họa schema linh hoạt.
- Tab tham khảo các toán tử MongoDB như `$eq`, `$in`, `$and`, `$set`, `$push` và cách đối chiếu SQL.

> **Chế độ mô phỏng:** dữ liệu nằm trong bộ nhớ của chương trình. Không cần SQL Server, MongoDB, tài khoản Windows, dịch vụ nền hay kết nối mạng. Câu lệnh trong giao diện để học và so sánh; ứng dụng không gửi lệnh tới database thật. Dữ liệu trở về mẫu khi đóng chương trình.

## ⚖️ So sánh hai mô hình

| Khái niệm | Mô hình SQL Server | Mô hình MongoDB |
|---|---|---|
| Cấu trúc | Bảng, hàng, cột | Collection chứa document |
| Quan hệ điểm | Bảng `Scores` dùng `StudentId` | Mảng `scores` nhúng trong document |
| Đọc dữ liệu | `SELECT`, `JOIN`, `WHERE` | `find()` với điều kiện trên document |
| Tổng hợp | `GROUP BY`, `AVG`, `COUNT` | `$unwind`, `$group`, `$sort` |
| Field riêng | Thường cần đổi schema bảng | Có thể thêm vào từng document |

## 🧰 Yêu cầu và cách chạy

- Windows, macOS hoặc Linux có giao diện đồ họa.
- JDK 17 trở lên.
- Không cần cài database, Maven, JDBC driver hay thư viện ngoài.

Mở Terminal/PowerShell tại thư mục chứa `NoSqlVsSqlDemo.java` và chạy:

```powershell
javac -encoding UTF-8 NoSqlVsSqlDemo.java
java NoSqlVsSqlDemo
```

## 🎓 Kịch bản trình bày

1. Quan sát dữ liệu mẫu: SQL hiện nhiều hàng JOIN từ hai bảng; MongoDB hiện mỗi sinh viên dưới dạng document JSON.
2. Dùng **INSERT** với ID mới để thêm sinh viên ở cả hai mô hình mô phỏng.
3. Chọn ID có sẵn, sửa tên hoặc điểm rồi nhấn **UPDATE**.
4. Nhấn **DELETE** để xem cả các hàng điểm và document tương ứng biến mất.
5. Nhập tên ở ô **Tìm tên** rồi nhấn **QUERY**; xem `LIKE` và regex `find()`.
6. Nhấn **GROUP BY / AGGREGATE** để đối chiếu kết quả trung bình điểm theo môn.
7. Chọn ID sinh viên, nhập field như `hobby`, rồi nhấn **THÊM FIELD NoSQL**. Document thay đổi; cấu trúc bảng SQL vẫn giữ nguyên.
8. Mở các tab câu lệnh, giải thích khác biệt và bảng toán tử.
9. Mở **Viết SQL → NoSQL**, sửa câu `SELECT ... FROM Students ... WHERE ...`, rồi bấm **DỊCH SQL → MONGODB & LỌC**. Bộ dịch minh họa `StudentId`, `Name`, `Subject`, `Score` với toán tử so sánh, `LIKE` và điều kiện `AND`.

## 🗃️ Dữ liệu mẫu

### SQL · hai bảng riêng

```text
Students                 Scores
+-----------+------+     +------------+---------+-------+
| StudentId | Name |     | StudentId  | Subject | Score |
+-----------+------+     +------------+---------+-------+
| 1         | Lan  |     | 1          | DBMS    | 9     |
| 2         | Minh |     | 1          | Java    | 8.5   |
|           |      |     | 2          | DBMS    | 8     |
+-----------+------+     +------------+---------+-------+
```

### MongoDB · document nhúng điểm

```javascript
{
  _id: 1,
  name: "Lan",
  scores: [
    { subject: "DBMS", score: 9 },
    { subject: "Java", score: 8.5 }
  ]
}
```

## ⚠️ Giới hạn

- Đây là mô phỏng phục vụ học tập, không phải kết nối SQL Server/MongoDB thật.
- Dữ liệu chỉ tồn tại trong RAM và trở về mẫu ban đầu khi đóng rồi mở lại ứng dụng.
- Kết quả minh họa cấu trúc và cú pháp, không dùng để đo hiệu năng hoặc thay thế database.
- Bộ dịch SQL sang MongoDB chỉ hiểu một tập con rõ ràng: `SELECT ... FROM Students`, tùy chọn JOIN với `Scores`, và điều kiện `WHERE` hỗ trợ trong giao diện. Truy vấn SQL tổng quát, hàm phức tạp, `OR`, subquery và các cú pháp khác chưa được chuyển đổi.
- Chương trình không mô phỏng đầy đủ giao dịch, chỉ mục, phân quyền, replication hay sharding.

## 📦 Cấu trúc repository

```text
NoSQL_vs_SQL_JavaSwing_Demo/
├── NoSqlVsSqlDemo.java   # Toàn bộ giao diện Swing và logic mô phỏng
├── README.md             # Hướng dẫn chạy và nội dung trình bày
└── .gitignore
```
