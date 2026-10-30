# Atelier Shop

Đồ án fullstack API-first: cửa hàng đồ nhà, backend Spring Boot 3.5 / Java 21, hợp đồng OpenAPI 3.0, giao diện React.

## Cấu trúc

| Thư mục | Nội dung |
| --- | --- |
| `docs/BA.md` | User story, luồng đặt hàng, hủy đơn, máy trạng thái |
| `api/shop-api.yaml` | Đặc tả OpenAPI 3.0, nguồn sự thật của API |
| `api/contract.feature` | Kịch bản Gherkin |
| `postman/` | Collection Newman cho contract test |
| `backend/` | Spring Boot, JPA, codegen, seeding Faker |
| `frontend/` | Storefront và trang quản trị |
| `e2e/checkout-flow.mjs` | Luồng đặt hàng và hết hàng qua HTTP |

## Chạy nhanh

CSDL là MariaDB trong XAMPP (cổng 3307, user `root`, mật khẩu `123456`, database `shopdb`). Bật MySQL trong XAMPP Control Panel trước khi chạy backend. Hibernate tự tạo bảng và seed dữ liệu mẫu lần đầu.

Terminal 1:

```powershell
cd backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
.\mvnw.cmd spring-boot:run
```

Terminal 2:

```powershell
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173. Swagger: http://localhost:8080/swagger-ui.html. File đặc tả: http://localhost:8080/openapi/shop-api.yaml.

Tài khoản mẫu sau khi seed:

- Khách: `lan.nguyen@atelier.vn` / `customer123`
- Quản trị: `admin@atelier.vn` / `admin123`

## XAMPP

MySQL/MariaDB của XAMPP trên máy này lắng nghe cổng **3307**, không phải 3306. Kết nối nằm trong `backend/src/main/resources/application.yml`:

- Host: `127.0.0.1:3307`
- Database: `shopdb` (tự tạo nếu chưa có)
- User / mật khẩu: `root` / `123456`

Xem lại trong phpMyAdmin: http://localhost/phpmyadmin, chọn database `shopdb`.

Test tự động vẫn dùng H2 trong bộ nhớ (`application-test.yml`) để không ghi đè dữ liệu XAMPP.

## API-first

1. Sửa `api/shop-api.yaml`.
2. `mvnw generate-sources` sinh interface và DTO vào `backend/target/generated-sources/openapi`.
3. Mock server cho UI trước khi có backend:

```powershell
npx @stoplight/prism-cli mock api/shop-api.yaml -p 4010
cd frontend
npm run dev -- --mode mock
```

`--mode mock` đọc `frontend/.env.mock` và trỏ UI sang `http://localhost:4010`.

## Kiểm thử

```powershell
cd backend
.\mvnw.cmd test
```

Unit test: đặt hàng trừ kho, và thất bại khi hết hàng không ghi đơn.

Integration test (MockMvc, H2): đăng ký sai email/số điện thoại trả 422, đặt hàng thành công rồi hủy hoàn kho, đặt quá tồn kho trả `ERR_STOCK_EMPTY`.

Contract test, khi backend đang chạy:

```powershell
npx newman run postman/shop-api.postman_collection.json
```

E2E HTTP:

```powershell
node e2e/checkout-flow.mjs
```

## Quy tắc nghiệp vụ đã chốt

- `PENDING` sang `PAID` hoặc `CANCELLED`.
- `PAID` sang `SHIPPED` hoặc `CANCELLED`.
- Hủy đơn chưa giao thì cộng lại tồn kho.
- Đặt hàng nằm trong một `@Transactional`: hết hàng thì không tạo đơn và không trừ kho.
- Hàm đọc dùng `@Transactional(readOnly = true)`.
