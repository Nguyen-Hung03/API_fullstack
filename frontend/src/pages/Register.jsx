import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import ErrorNote from "../components/ErrorNote";
import { useAuth } from "../context/AuthContext";

const EMPTY = { fullName: "", email: "", phone: "", password: "", address: "" };

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState(null);

  function set(key) {
    return (event) => setForm({ ...form, [key]: event.target.value });
  }

  async function onSubmit(event) {
    event.preventDefault();
    try {
      await register(form);
      navigate("/");
    } catch (err) {
      setError(err);
    }
  }

  return (
    <section className="stack narrow">
      <h1>Đăng ký</h1>
      <p className="muted">Email đúng định dạng. Số điện thoại di động Việt Nam, ví dụ 0901234567.</p>
      <ErrorNote error={error} />
      <form className="stack" onSubmit={onSubmit}>
        <label>Họ tên<input required minLength={2} value={form.fullName} onChange={set("fullName")} /></label>
        <label>Email<input type="email" required value={form.email} onChange={set("email")} /></label>
        <label>Số điện thoại<input required pattern="^(0|\+84)(3|5|7|8|9)[0-9]{8}$" value={form.phone} onChange={set("phone")} /></label>
        <label>Mật khẩu<input type="password" required minLength={6} value={form.password} onChange={set("password")} /></label>
        <label>Địa chỉ<input value={form.address} onChange={set("address")} /></label>
        <button type="submit">Tạo tài khoản</button>
      </form>
      <p>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
    </section>
  );
}
