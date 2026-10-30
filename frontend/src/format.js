export function vnd(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(value || 0));
}

export const STATUS_LABEL = {
  PENDING: "Chờ xử lý",
  PAID: "Đã thanh toán",
  SHIPPED: "Đã giao",
  CANCELLED: "Đã hủy",
};

export function statusActions(status) {
  if (status === "PENDING") return ["PAID", "CANCELLED"];
  if (status === "PAID") return ["SHIPPED", "CANCELLED"];
  return [];
}

export const ACTION_LABEL = {
  PAID: "Đã thanh toán",
  SHIPPED: "Đã giao",
  CANCELLED: "Hủy đơn",
};
