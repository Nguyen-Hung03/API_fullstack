const base = process.env.API_BASE_URL || "http://localhost:8080";

async function call(path, { method = "GET", token, body } = {}) {
  const response = await fetch(base + path, {
    method,
    headers: {
      Accept: "application/json",
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    const error = new Error(data?.message || response.statusText);
    error.status = response.status;
    error.body = data;
    throw error;
  }
  return data;
}

const stamp = Date.now();
const admin = await call("/api/v1/sessions", {
  method: "POST",
  body: { email: "admin@atelier.vn", password: "admin123" },
});
const categories = await call("/api/v1/categories", { token: admin.token });
const categoryId = categories[0].id;
const created = await call("/api/v1/products", {
  method: "POST",
  token: admin.token,
  body: {
    name: "Bình E2E " + stamp,
    price: 150000,
    stockQuantity: 3,
    categoryId,
    description: "Dùng cho kịch bản đặt hàng",
  },
});
const customer = await call("/api/v1/customers", {
  method: "POST",
  body: {
    fullName: "Khách E2E",
    email: `e2e${stamp}@atelier.vn`,
    phone: "0971234567",
    password: "secret1",
    address: "Huế",
  },
});
const session = await call("/api/v1/sessions", {
  method: "POST",
  body: { email: customer.email, password: "secret1" },
});
await call(`/api/v1/customers/${session.customer.id}/cart/items`, {
  method: "POST",
  token: session.token,
  body: { productId: created.id, quantity: 2 },
});
const order = await call("/api/v1/orders", {
  method: "POST",
  token: session.token,
  body: { customerId: session.customer.id, note: "E2E" },
});
const after = await call("/api/v1/products/" + created.id);
if (order.status !== "PENDING" || !order.code || after.stockQuantity !== 1) {
  throw new Error("Luồng đặt hàng không khớp: " + JSON.stringify({ order, stock: after.stockQuantity }));
}

let failed = false;
try {
  await call(`/api/v1/customers/${session.customer.id}/cart/items`, {
    method: "POST",
    token: session.token,
    body: { productId: created.id, quantity: 2 },
  });
  await call("/api/v1/orders", {
    method: "POST",
    token: session.token,
    body: { customerId: session.customer.id },
  });
} catch (error) {
  failed = error.status === 400 && error.body?.code === "ERR_STOCK_EMPTY";
}
const still = await call("/api/v1/products/" + created.id);
if (!failed || still.stockQuantity !== 1) {
  throw new Error("Kịch bản hết hàng không khớp");
}

console.log(`E2E_OK code=${order.code} stock=${still.stockQuantity}`);
