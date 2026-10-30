import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api";
import ErrorNote from "../../components/ErrorNote";
import { useAuth } from "../../context/AuthContext";
import { ACTION_LABEL, STATUS_LABEL, statusActions, vnd } from "../../format";

export default function OrdersAdmin() {
  const { customer, token } = useAuth();
  const [status, setStatus] = useState("");
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (customer?.role !== "ADMIN") return;
    const query = status ? `?status=${status}&size=50` : "?size=50";
    api("/api/v1/orders" + query, { token })
      .then((page) => setOrders(page.content))
      .catch(setError);
  }, [customer, token, status]);

  if (!customer) return <p>Cần đăng nhập quản trị. <Link to="/login">Đăng nhập</Link></p>;
  if (customer.role !== "ADMIN") return <p>Chỉ tài khoản quản trị mới vào được khu này.</p>;

  async function change(order, next) {
    try {
      const updated = await api(`/api/v1/orders/${order.id}`, {
        method: "PATCH",
        token,
        body: { status: next },
      });
      setOrders((current) => current.map((item) => item.id === order.id ? updated : item));
      setError(null);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <section className="stack">
      <div className="row">
        <h1>Đơn hàng</h1>
        <Link to="/admin/products">Tồn kho</Link>
      </div>
      <div className="chips">
        <button type="button" className={status ? "" : "on"} onClick={() => setStatus("")}>Tất cả</button>
        {Object.entries(STATUS_LABEL).map(([key, label]) => (
          <button type="button" key={key} className={status === key ? "on" : ""} onClick={() => setStatus(key)}>{label}</button>
        ))}
      </div>
      <ErrorNote error={error} />
      {orders.length === 0 && <p>Chưa có đơn.</p>}
      {orders.map((order) => (
        <article className="panel" key={order.id}>
          <div className="row">
            <strong>{order.code}</strong>
            <span className={"pill " + order.status}>{STATUS_LABEL[order.status]}</span>
          </div>
          <p>{order.customerName} — {vnd(order.totalAmount)}</p>
          {order.note && <p className="muted">{order.note}</p>}
          <ul className="plain">
            {order.items.map((item) => (
              <li key={item.id}>{item.productName} × {item.quantity}</li>
            ))}
          </ul>
          <div className="actions">
            {statusActions(order.status).map((next) => (
              <button type="button" key={next} className={next === "CANCELLED" ? "danger" : ""} onClick={() => change(order, next)}>
                {ACTION_LABEL[next]}
              </button>
            ))}
          </div>
        </article>
      ))}
    </section>
  );
}
