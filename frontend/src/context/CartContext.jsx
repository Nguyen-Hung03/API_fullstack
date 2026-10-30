import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { customer, token } = useAuth();
  const [cart, setCart] = useState(null);

  const refresh = useCallback(async () => {
    if (!customer) {
      setCart(null);
      return null;
    }
    const data = await api(`/api/v1/customers/${customer.id}/cart`, { token });
    setCart(data);
    return data;
  }, [customer, token]);

  useEffect(() => {
    refresh().catch(() => setCart(null));
  }, [refresh]);

  async function add(productId, quantity) {
    const data = await api(`/api/v1/customers/${customer.id}/cart/items`, {
      method: "POST",
      token,
      body: { productId, quantity },
    });
    setCart(data);
    return data;
  }

  async function update(itemId, quantity) {
    const data = await api(`/api/v1/customers/${customer.id}/cart/items/${itemId}`, {
      method: "PUT",
      token,
      body: { quantity },
    });
    setCart(data);
    return data;
  }

  async function remove(itemId) {
    await api(`/api/v1/customers/${customer.id}/cart/items/${itemId}`, {
      method: "DELETE",
      token,
    });
    await refresh();
  }

  const count = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

  return (
    <CartContext.Provider value={{ cart, count, refresh, add, update, remove }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
