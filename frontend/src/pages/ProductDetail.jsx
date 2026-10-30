import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import ErrorNote from "../components/ErrorNote";
import Photo from "../components/Photo";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { vnd } from "../format";

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { customer } = useAuth();
  const { add } = useCart();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState(null);
  const [missing, setMissing] = useState(false);

  useEffect(() => {
    setMissing(false);
    api("/api/v1/products/" + id)
      .then(setProduct)
      .catch((err) => {
        if (err.status === 404) setMissing(true);
        else setError(err);
      });
  }, [id]);

  async function onAdd(event) {
    event.preventDefault();
    if (!customer) {
      navigate("/login", { state: { from: `/products/${id}` } });
      return;
    }
    try {
      await add(product.id, Number(quantity));
      navigate("/cart");
    } catch (err) {
      setError(err);
    }
  }

  if (missing) {
    return <p>Không tìm thấy sản phẩm. <Link to="/">Về cửa hàng</Link></p>;
  }
  if (!product) return <p className="muted">Đang tải…</p>;

  return (
    <article className="detail">
      <Photo src={product.imageUrl} name={product.name} />
      <div>
        <p className="eyebrow">{product.categoryName}</p>
        <h1>{product.name}</h1>
        <p className="price">{vnd(product.price)}</p>
        <p>{product.description}</p>
        <p className={product.stockQuantity === 0 ? "stock out" : "stock"}>
          {product.stockQuantity === 0 ? "Hết hàng" : `Còn ${product.stockQuantity} trong kho`}
        </p>
        <ErrorNote error={error} />
        <form className="buy" onSubmit={onAdd}>
          <label>
            Số lượng
            <input
              type="number"
              min="1"
              max="99"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
            />
          </label>
          <button type="submit" disabled={product.stockQuantity === 0}>Cho vào giỏ</button>
        </form>
      </div>
    </article>
  );
}
