import { Link, useLocation, useParams } from "react-router-dom";
import { STATUS_LABEL, vnd } from "../format";

export default function OrderSuccess() {
  const { code } = useParams();
  const location = useLocation();
  const stored = (() => {
    try {
      return JSON.parse(sessionStorage.getItem("atelier_order") || "null");
    } catch {
      return null;
    }
  })();
  const order = location.state?.code === code ? location.state : stored?.code === code ? stored : null;

  return (
    <section className="stack narrow success">
      <p className="eyebrow">Đơn đã được ghi nhận</p>
      <h1>Mã đơn {code}</h1>
      <p>Trạng thái: {STATUS_LABEL[order?.status] || "Chờ xử lý"}</p>
      {order && <p className="price">{vnd(order.totalAmount)}</p>}
      <p>Bạn có thể theo dõi đơn trong trang tài khoản. Quản trị sẽ chuyển trạng thái khi thanh toán và giao hàng.</p>
      <Link className="button" to="/account">Xem lịch sử đơn</Link>
    </section>
  );
}
