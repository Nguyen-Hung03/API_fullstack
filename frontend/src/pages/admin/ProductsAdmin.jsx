import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api";
import ErrorNote from "../../components/ErrorNote";
import { useAuth } from "../../context/AuthContext";
import { vnd } from "../../format";

export default function ProductsAdmin() {
  const { customer, token } = useAuth();
  const [products, setProducts] = useState([]);
  const [stock, setStock] = useState({});
  const [error, setError] = useState(null);
  const [savedId, setSavedId] = useState(null);

  useEffect(() => {
    if (customer?.role !== "ADMIN") return;
    api("/api/v1/products?size=100", { token })
      .then((page) => {
        setProducts(page.content);
        const values = {};
        page.content.forEach((product) => {
          values[product.id] = product.stockQuantity;
        });
        setStock(values);
      })
      .catch(setError);
  }, [customer, token]);

  if (!customer) return <p>Cần đăng nhập quản trị. <Link to="/login">Đăng nhập</Link></p>;
  if (customer.role !== "ADMIN") return <p>Chỉ tài khoản quản trị mới vào được khu này.</p>;

  async function save(product) {
    try {
      const updated = await api(`/api/v1/products/${product.id}`, {
        method: "PUT",
        token,
        body: {
          name: product.name,
          description: product.description || "",
          price: product.price,
          imageUrl: product.imageUrl || "",
          stockQuantity: Number(stock[product.id]),
          categoryId: product.categoryId,
        },
      });
      setProducts((current) => current.map((item) => item.id === product.id ? updated : item));
      setSavedId(product.id);
      setError(null);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <section className="stack">
      <div className="row">
        <h1>Tồn kho</h1>
        <Link to="/admin">Đơn hàng</Link>
      </div>
      <ErrorNote error={error} />
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Danh mục</th>
              <th>Giá</th>
              <th>Tồn</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{product.categoryName}</td>
                <td>{vnd(product.price)}</td>
                <td>
                  <input
                    className="stock-input"
                    type="number"
                    min="0"
                    value={stock[product.id] ?? 0}
                    onChange={(event) => setStock({ ...stock, [product.id]: event.target.value })}
                  />
                </td>
                <td>
                  <button type="button" onClick={() => save(product)}>Lưu</button>
                  {savedId === product.id && <span className="muted"> Đã lưu</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
