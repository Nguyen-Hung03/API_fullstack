# Báo cáo kiểm thử

Ngày chạy: 2026-10-22. Test tự động dùng H2 trong bộ nhớ. Ứng dụng chạy trên MariaDB của XAMPP (`127.0.0.1:3307`, database `shopdb`) tại `http://localhost:8080`, giao diện tại `http://localhost:5173`.

## Backend

`mvnw test`

| Lớp | Kết quả |
| --- | --- |
| `OrderServiceTest` | 2 passed. Đặt hàng tạo đơn `PENDING`, trừ kho. Hết hàng ném `ERR_STOCK_EMPTY`, không ghi đơn, không đổi kho. |
| `ShopFlowIntegrationTest` | 3 passed. Email/số điện thoại sai trả 422 `ERR_VALIDATION`. Đặt hàng rồi hủy hoàn kho. Đặt quá tồn kho trả 400 và kho giữ nguyên. |

Tổng: 5 tests, 0 failures.

## Contract test

`npx newman run postman/shop-api.postman_collection.json`

13 request, 14 assertion, 0 failed. Gồm đăng ký không hợp lệ, đăng nhập, danh mục, phân trang sản phẩm, đặt hàng thành công trừ kho, và đặt hàng thất bại khi vượt tồn kho.

## E2E

`node e2e/checkout-flow.mjs` in `E2E_OK code=DH20261022-5400 stock=1`.

Trên giao diện, với tài khoản `lan.nguyen@atelier.vn`:

1. Lọc danh mục Gốm sứ, mở Chén cơm men xanh.
2. Thêm vào giỏ bắt buộc đăng nhập, sau đăng nhập quay lại đúng sản phẩm.
3. Giỏ hiện 1 dòng, bấm đặt hàng.
4. Trang thành công hiện mã `DH20261022-5278`, trạng thái Chờ xử lý, tổng 89.000 ₫. Giỏ về trống.
5. Tài khoản thấy đơn mới và đơn mẫu.
6. Khách thường vào `/admin` bị chặn.
7. Quản trị thấy danh sách đơn. Bấm Đã thanh toán trên đơn chén thì nút đổi thành Đã giao.
8. Trang tồn kho hiện số lượng sau đặt hàng: chén còn 39.

## Cơ sở dữ liệu

Ứng dụng dùng MariaDB 10.4 của XAMPP, cổng 3307, user `root`. Không dùng Docker.
