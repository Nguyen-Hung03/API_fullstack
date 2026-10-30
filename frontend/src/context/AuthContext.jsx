import { createContext, useContext, useMemo, useState } from "react";
import { api } from "../api";

const KEY = "atelier_session";
const AuthContext = createContext(null);

function readSession() {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession);

  function persist(next) {
    setSession(next);
    if (next) {
      localStorage.setItem(KEY, JSON.stringify(next));
    } else {
      localStorage.removeItem(KEY);
    }
  }

  const value = useMemo(() => {
    async function login(email, password) {
      const data = await api("/api/v1/sessions", { method: "POST", body: { email, password } });
      persist(data);
      return data;
    }

    async function register(form) {
      await api("/api/v1/customers", { method: "POST", body: form });
      return login(form.email, form.password);
    }

    function logout() {
      persist(null);
    }

    function replaceCustomer(customer) {
      persist({ ...session, customer });
    }

    return {
      session,
      token: session?.token || "",
      customer: session?.customer || null,
      login,
      register,
      logout,
      replaceCustomer,
    };
  }, [session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
