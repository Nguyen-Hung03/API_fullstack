import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import ErrorNote from "../components/ErrorNote";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState(null);

  async function onSubmit(event) {
    event.preventDefault();
    try {
      const session = await login(form.email, form.password);
      const fallback = session.customer.role === "ADMIN" ? "/admin" : "/";
      navigate(location.state?.from || fallback);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <section className="stack narrow">
      <h1>Đăng nhập</h1>
      <ErrorNote error={error} />
      <form className="stack" onSubmit={onSubmit}>
        <label>
          Email
          <input type="email" required value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
        </label>
        <label>
          Mật khẩu
          <input type="password" required minLength={6} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
        </label>
        <button type="submit">Vào cửa hàng</button>
      </form>
      <div className="demo">
        <p>Tài khoản mẫu</p>
        <p>Khách: lan.nguyen@atelier.vn / customer123</p>
        <p>Quản trị: admin@atelier.vn / admin123</p>
      </div>
      <p>Chưa có tài khoản? <Link to="/register">Đăng ký</Link></p>
    </section>
  );
}
