Feature: Hợp đồng API cửa hàng Atelier

  Scenario: Từ chối đăng ký khi email hoặc số điện thoại sai
    When khách gửi POST /api/v1/customers với email "khong-phai-email" và phone "123"
    Then phản hồi 422
    And body có code "ERR_VALIDATION", message và details

  Scenario: Đăng ký và đăng nhập
    When khách đăng ký email hợp lệ và số điện thoại 0901234567
    Then phản hồi 201 và có id, không có mật khẩu
    When khách đăng nhập đúng mật khẩu
    Then phản hồi 200 và có token cùng hồ sơ

  Scenario: Đặt hàng thành công trừ tồn kho
    Given một sản phẩm còn tồn kho và khách đã thêm đúng số lượng vào giỏ
    When khách gửi POST /api/v1/orders
    Then phản hồi 201, trạng thái PENDING, có mã đơn
    And tồn kho giảm đúng số lượng đã mua
    And giỏ của khách trống

  Scenario: Đặt hàng thất bại vì hết hàng
    Given khách đã có hàng trong giỏ nhưng tồn kho không còn đủ
    When khách gửi POST /api/v1/orders
    Then phản hồi 400 với code "ERR_STOCK_EMPTY"
    And không tạo đơn
    And tồn kho không đổi

  Scenario: Hủy đơn hoàn lại tồn kho
    Given một đơn PENDING đã trừ kho
    When quản trị PATCH trạng thái CANCELLED
    Then đơn chuyển CANCELLED
    And tồn kho được cộng lại

  Scenario: Không nhảy trạng thái sai chiều
    Given một đơn SHIPPED
    When quản trị PATCH về PENDING
    Then phản hồi 400 với code "ERR_INVALID_STATUS"
