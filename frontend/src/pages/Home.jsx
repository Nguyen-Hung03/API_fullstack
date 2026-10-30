import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api";
import ErrorNote from "../components/ErrorNote";
import Photo from "../components/Photo";
import { vnd } from "../format";

export default function Home() {
  const [params, setParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [draft, setDraft] = useState({
    q: params.get("q") || "",
    minPrice: params.get("minPrice") || "",
    maxPrice: params.get("maxPrice") || "",
  });

  const q = params.get("q") || "";
  const categoryId = params.get("categoryId") || "";
  const minPrice = params.get("minPrice") || "";
  const maxPrice = params.get("maxPrice") || "";
  const page = Number(params.get("page") || 0);

  useEffect(() => {
    api("/api/v1/categories").then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    const query = new URLSearchParams();
    if (q) query.set("q", q);
    if (categoryId) query.set("categoryId", categoryId);
    if (minPrice) query.set("minPrice", minPrice);
    if (maxPrice) query.set("maxPrice", maxPrice);
    query.set("page", String(page));
    query.set("size", "8");
    let ignore = false;
    setLoading(true);
    api("/api/v1/products?" + query.toString())
      .then((data) => {
        if (!ignore) {
          setPageData(data);
          setError(null);
        }
      })
      .catch((err) => {
        if (!ignore) setError(err);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [q, categoryId, minPrice, maxPrice, page]);

  function apply(partial, resetPage = true) {
    const next = new URLSearchParams(params);
    Object.entries(partial).forEach(([key, value]) => {
      if (value === "" || value == null) next.delete(key);
      else next.set(key, String(value));
    });
    if (resetPage) next.delete("page");
    setParams(next);
  }

  return (
    <div className="shop">
      <section className="hero">
        <p className="eyebrow">Cửa hàng đồ nhà</p>
        <h1>Vật dụng để ở, chọn chậm.</h1>
        <p>Gốm, vải, đèn và gỗ. Lọc theo danh mục hoặc khoảng giá, rồi xem tồn kho trước khi đặt.</p>
      </section>

      <form className="filters" onSubmit={(event) => {
        event.preventDefault();
        apply({ q: draft.q.trim(), minPrice: draft.minPrice, maxPrice: draft.maxPrice });
      }}>
        <label>
          Tìm
          <input value={draft.q} onChange={(event) => setDraft({ ...draft, q: event.target.value })} placeholder="bình, đèn, trà..." />
        </label>
        <label>
          Giá từ
          <input inputMode="numeric" value={draft.minPrice} onChange={(event) => setDraft({ ...draft, minPrice: event.target.value })} />
        </label>
        <label>
          Đến
          <input inputMode="numeric" value={draft.maxPrice} onChange={(event) => setDraft({ ...draft, maxPrice: event.target.value })} />
        </label>
        <button type="submit">Lọc</button>
      </form>

      <div className="chips">
        <button type="button" className={categoryId ? "" : "on"} onClick={() => apply({ categoryId: "" })}>Tất cả</button>
        {categories.map((category) => (
          <button
            type="button"
            key={category.id}
            className={String(category.id) === categoryId ? "on" : ""}
            onClick={() => apply({ categoryId: category.id })}
          >
            {category.name}
          </button>
        ))}
      </div>

      <ErrorNote error={error} />
      {loading && <p className="muted">Đang tải sản phẩm…</p>}

      <div className="grid">
        {pageData?.content?.map((product) => (
          <Link className="card" key={product.id} to={`/products/${product.id}`}>
            <Photo src={product.imageUrl} name={product.name} />
            <p className="eyebrow">{product.categoryName}</p>
            <h2>{product.name}</h2>
            <div className="row">
              <strong>{vnd(product.price)}</strong>
              <span className={product.stockQuantity === 0 ? "stock out" : "stock"}>
                {product.stockQuantity === 0 ? "Hết hàng" : `Còn ${product.stockQuantity}`}
              </span>
            </div>
          </Link>
        ))}
      </div>

      {pageData && pageData.totalPages > 1 && (
        <div className="pager">
          {Array.from({ length: pageData.totalPages }, (_, index) => (
            <button
              type="button"
              key={index}
              className={index === pageData.page ? "on" : ""}
              onClick={() => apply({ page: index }, false)}
            >
              {index + 1}
            </button>
          ))}
        </div>
      )}
      {pageData && pageData.content.length === 0 && !loading && <p>Không có sản phẩm khớp bộ lọc.</p>}
    </div>
  );
}
