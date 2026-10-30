import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Layout() {
  const { customer, logout } = useAuth();
  const { count } = useCart();
  const admin = customer?.role === "ADMIN";

  return (
    <>
      <header className="top">
        <Link to="/" className="mark">Atelier</Link>
        <nav>
          <NavLink to="/" end>Cửa hàng</NavLink>
          <NavLink to="/cart">Giỏ {count > 0 ? `(${count})` : ""}</NavLink>
          {admin && <NavLink to="/admin">Quản trị</NavLink>}
          {customer ? (
            <NavLink to="/account">{customer.fullName}</NavLink>
          ) : (
            <NavLink to="/login">Đăng nhập</NavLink>
          )}
        </nav>
        {customer && (
          <button className="text" type="button" onClick={logout}>Thoát</button>
        )}
      </header>
      <main>
        <Outlet />
      </main>
      <footer>
        <span>Atelier</span>
        <span>Đồ gốm, vải, đèn và gỗ cho nhà ở.</span>
      </footer>
    </>
  );
}
