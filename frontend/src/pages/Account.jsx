import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import ErrorNote from "../components/ErrorNote";
import { useAuth } from "../context/AuthContext";
import { STATUS_LABEL, vnd } from "../format";

export default function Account() {
  const { customer, token, replaceCustomer } = useAuth();
  const [form, setForm] = useState(null);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (!customer) return;
    setForm({ fullName: customer.fullName, phone: customer.phone, address: customer.address || "" });
    api(`/api/v1/customers/${customer.id}/orders?size=20`, { token })
      .then((page) => setOrders(page.content))
      .catch(setError);
  }, [customer, token]);

  if (!customer) return <p>Cần đăng nhập. <Link to="/login">Đăng nhập</Link></p>;
  if (!form) return null;

  async function onSubmit(event) {
    event.preventDefault();
    setSaved(false);
    try {
      const updated = await api(`/api/v1/customers/${customer.id}`, { method: "PUT", token, body: form });
      replaceCustomer(updated);
      setSaved(true);
      setError(null);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <section className="stack">
      <h1>Tài khoản</h1>
      <p className="muted">{customer.email}</p>
      <ErrorNote error={error} />
      {saved && <p className="note">Đã lưu hồ sơ.</p>}
      <form className="stack narrow" onSubmit={onSubmit}>
        <label>Họ tên<input required minLength={2} value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} /></label>
        <label>Số điện thoại<input required value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
        <label>Địa chỉ<input value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} /></label>
        <button type="submit">Lưu hồ sơ</button>
      </form>
      <h2>Lịch sử đơn</h2>
      {orders.length === 0 && <p>Chưa có đơn.</p>}
      {orders.map((order) => (
        <article className="panel" key={order.id}>
          <div className="row">
            <strong>{order.code}</strong>
            <span>{STATUS_LABEL[order.status]}</span>
          </div>
          <p>{vnd(order.totalAmount)}</p>
          <ul className="plain">
            {order.items.map((item) => (
              <li key={item.id}>{item.productName} × {item.quantity}</li>
            ))}
          </ul>
        </article>
      ))}
    </section>
  );
}
