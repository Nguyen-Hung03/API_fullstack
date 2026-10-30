import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { api } from "../api";
import ErrorNote from "../components/ErrorNote";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { vnd } from "../format";

export default function CheckoutPage() {
  const { customer, token } = useAuth();
  const { cart, refresh } = useCart();
  const navigate = useNavigate();
  const [note, setNote] = useState("");
  const [error, setError] = useState(null);
  const [pending, setPending] = useState(false);

  if (!customer) return <Navigate to="/login" state={{ from: "/checkout" }} replace />;
  if (!cart) return <p className="muted">Đang tải…</p>;
  if (cart.items.length === 0) return <p>Giỏ đang trống. <Link to="/">Chọn sản phẩm</Link></p>;

  async function place(event) {
    event.preventDefault();
    setPending(true);
    setError(null);
    try {
      const order = await api("/api/v1/orders", {
        method: "POST",
        token,
        body: { customerId: customer.id, note },
      });
      sessionStorage.setItem("atelier_order", JSON.stringify(order));
      await refresh();
      navigate(`/orders/${order.code}`, { state: order });
    } catch (err) {
      setError(err);
    } finally {
      setPending(false);
    }
  }

  return (
    <section className="stack narrow">
      <h1>Đặt hàng</h1>
      <p>Giao tới: {customer.address || "chưa có địa chỉ — có thể bổ sung trong tài khoản."}</p>
      <ul className="plain">
        {cart.items.map((item) => (
          <li key={item.id}>{item.productName} × {item.quantity} — {vnd(item.lineTotal)}</li>
        ))}
      </ul>
      <p className="price">{vnd(cart.totalAmount)}</p>
      <ErrorNote error={error} />
      <form className="stack" onSubmit={place}>
        <label>
          Ghi chú
          <textarea value={note} maxLength={500} onChange={(event) => setNote(event.target.value)} />
        </label>
        <button type="submit" disabled={pending}>{pending ? "Đang đặt…" : "Xác nhận đặt hàng"}</button>
      </form>
    </section>
  );
}
