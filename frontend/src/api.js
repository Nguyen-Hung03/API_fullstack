const base = import.meta.env.VITE_API_BASE_URL || "";

export class ApiError extends Error {
  constructor(payload, status) {
    super(payload?.message || "Lỗi hệ thống");
    this.code = payload?.code || "ERR_INTERNAL";
    this.details = payload?.details || [];
    this.status = status;
  }
}

export async function api(path, { method = "GET", body, token } = {}) {
  const response = await fetch(base + path, {
    method,
    headers: {
      Accept: "application/json",
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (response.status === 204) {
    return null;
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiError(data, response.status);
  }
  return data;
}
