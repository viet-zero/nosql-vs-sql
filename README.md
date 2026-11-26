<div align="center">

# 🧭 NoSQL vs SQL
### Database Visual Lab

**Một phòng lab Java Swing để xem cùng dữ liệu được tổ chức và truy vấn theo hai cách.**

![Java 17](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-437291)
![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-CC2927?logo=microsoftsqlserver&logoColor=white)
![MongoDB](https://img.shields.io/badge/Database-MongoDB-47A248?logo=mongodb&logoColor=white)
![Single file](https://img.shields.io/badge/Source-single%20Java%20file-6E56CF)

</div>

---

## 📚 Mục lục

- [Phòng lab này làm gì?](#-phòng-lab-này-làm-gì)
- [So sánh nhanh](#-so-sánh-nhanh)
- [Yêu cầu trước khi chạy](#-yêu-cầu-trước-khi-chạy)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Kịch bản trình bày](#-kịch-bản-trình-bày)
- [Cấu trúc dữ liệu](#-cấu-trúc-dữ-liệu)
- [Giới hạn](#-giới-hạn)

## 🔬 Phòng lab này làm gì?

Ứng dụng dùng **SQL Server và MongoDB thật** để bạn thao tác rồi quan sát hai cách lưu cùng một dữ liệu sinh viên và điểm:

- CRUD: thêm, sửa, xóa và truy vấn sinh viên.
- Hiển thị bảng quan hệ bên SQL Server và document JSON bên MongoDB cạnh nhau.
- Xem câu lệnh SQL Server và MongoDB tương ứng với thao tác.
- So sánh `JOIN` với dữ liệu nhúng trong document.
- Thêm field riêng vào một document để minh họa schema linh hoạt.
- So sánh SQL `GROUP BY` với MongoDB aggregation pipeline.
- Tab toán tử tham khảo: `$eq`, `$in`, `$and`, `$set`, `$push`, `deleteOne()` và các toán tử liên quan.

> Tab toán tử là tài liệu tham khảo; không phải tất cả toán tử trong danh sách đều có nút thao tác trong ứng dụng.

## ⚖️ So sánh nhanh

| Nội dung | SQL Server | MongoDB |
|---|---|---|
| Mô hình | Bảng, hàng và cột có kiểu dữ liệu | Collection chứa document BSON |
| Điểm của sinh viên | Bảng `Scores` liên kết bằng `StudentId` | Mảng `scores` nhúng trong document |
| Kết hợp dữ liệu | `JOIN` | Đọc document đã nhúng; có thể dùng aggregation |
| Thay đổi cấu trúc | Thường dùng DDL như `ALTER TABLE` | Có thể thêm field vào từng document |
| Tổng hợp | `GROUP BY` | Aggregation pipeline |

## 🧰 Yêu cầu trước khi chạy

- Windows 10/11.
- JDK 17 trở lên.
- SQL Server Express đang chạy; mặc định instance là `localhost\SQLEXPRESS`.
- MongoDB Server đang chạy; mặc định `localhost:27017`.
- Tài khoản Windows hiện tại phải có quyền kết nối SQL Server, tạo database/bảng và ghi dữ liệu.
- MongoDB trong cấu hình demo cần cho phép kết nối local không xác thực.

**Không cần Maven, JDBC driver hoặc thư viện MongoDB cài thêm.** SQL Server được gọi qua Windows PowerShell/.NET SqlClient. Client MongoDB tối giản dùng OP_MSG/BSON được viết ngay trong file Java.

## ▶️ Chạy ứng dụng

Mở PowerShell tại thư mục chứa `NoSqlVsSqlDemo.java`:

```powershell
javac -encoding UTF-8 NoSqlVsSqlDemo.java
java NoSqlVsSqlDemo
```

Khi mở, ứng dụng thử kết nối tới SQL Server và MongoDB theo địa chỉ mặc định. Có thể đổi instance SQL, host/port MongoDB và tên database MongoDB trên giao diện.

Lần kết nối đầu tiên, ứng dụng tạo database `NoSqlComparison` và hai bảng `Students`, `Scores` nếu chưa có và tài khoản SQL có đủ quyền.

## 🎓 Kịch bản trình bày

1. Nhấn **KẾT NỐI & CHUẨN BỊ**.
2. Chọn ID và dùng **INSERT**, **UPDATE** hoặc **DELETE**; quan sát hai cách tổ chức dữ liệu.
3. Dùng **QUERY** để xem SQL `JOIN` và truy vấn document MongoDB.
4. Dùng **GROUP BY / AGGREGATE** để đối chiếu cách tính điểm trung bình theo môn.
5. Thêm field như `hobby` vào document. Lệnh `ALTER TABLE` bên SQL chỉ hiện để minh họa, không được chạy.
6. Mở các tab **Câu lệnh đang dùng**, **Giải thích khác biệt** và **Toán tử MongoDB ↔ SQL**.

## 🗂️ Cấu trúc dữ liệu

### SQL Server · mô hình quan hệ

Hai bảng riêng được nối qua `StudentId`:

```sql
SELECT s.StudentId, s.Name, c.Subject, c.Score
FROM dbo.Students AS s
LEFT JOIN dbo.Scores AS c ON c.StudentId = s.StudentId;
```

### MongoDB · mô hình document

Collection `students` lưu một document cho mỗi sinh viên; điểm được nhúng trong mảng `scores`:

```javascript
{
  _id: 1,
  name: "Lan",
  scores: [{ subject: "DBMS", score: 9 }]
}
```

## ⚠️ Giới hạn

- Đây là demo học tập về mô hình dữ liệu và lệnh cơ bản, không phải ứng dụng production hay benchmark.
- Hai database không dùng chung distributed transaction. Nếu một phía ghi thành công rồi phía kia lỗi, cần đối chiếu dữ liệu.
- Client MongoDB trong file chỉ hỗ trợ phần wire protocol/BSON cần cho demo; không hỗ trợ xác thực MongoDB hoặc toàn bộ tính năng MongoDB. Hãy dùng cấu hình MongoDB local không bật auth.
- Demo không triển khai replication, sharding, phân quyền người dùng hoặc đo hiệu năng.
- Database và dữ liệu được lưu trên các dịch vụ chạy ở máy người dùng; repository không chứa dữ liệu database.

## 📦 File trong repository

```text
NoSQL_vs_SQL_JavaSwing_Demo/
├── NoSqlVsSqlDemo.java   # Toàn bộ ứng dụng Swing và logic kết nối
├── README.md             # Hướng dẫn và nội dung trình bày
└── .gitignore
```
