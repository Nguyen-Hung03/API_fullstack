import { Link } from "react-router-dom";
import Photo from "../components/Photo";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { vnd } from "../format";
import { useState } from "react";
import ErrorNote from "../components/ErrorNote";

export default function CartPage() {
  const { customer } = useAuth();
  const { cart, update, remove } = useCart();
  const [error, setError] = useState(null);

  if (!customer) {
    return <p>Đăng nhập để xem giỏ hàng. <Link to="/login">Đăng nhập</Link></p>;
  }
  if (!cart) return <p className="muted">Đang tải giỏ…</p>;

  return (
    <section className="stack">
      <h1>Giỏ hàng</h1>
      <ErrorNote error={error} />
      {cart.items.length === 0 && <p>Giỏ đang trống. <Link to="/">Chọn sản phẩm</Link></p>}
      {cart.items.map((item) => (
        <article className="line" key={item.id}>
          <Photo src={item.imageUrl} name={item.productName} />
          <div>
            <h2>{item.productName}</h2>
            <p>{vnd(item.unitPrice)}</p>
            <label>
              Số lượng
              <input
                type="number"
                min="1"
                max="99"
                defaultValue={item.quantity}
                onBlur={(event) => {
                  const next = Number(event.target.value);
                  if (next !== item.quantity && next >= 1) {
                    update(item.id, next).catch(setError);
                  }
                }}
              />
            </label>
            <button className="text" type="button" onClick={() => remove(item.id).catch(setError)}>Xóa</button>
          </div>
          <strong>{vnd(item.lineTotal)}</strong>
        </article>
      ))}
      {cart.items.length > 0 && (
        <div className="total">
          <span>Tổng</span>
          <strong>{vnd(cart.totalAmount)}</strong>
          <Link className="button" to="/checkout">Đặt hàng</Link>
        </div>
      )}
    </section>
  );
}
