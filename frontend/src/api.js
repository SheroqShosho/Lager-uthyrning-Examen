const API_BASE = "http://localhost:8080/api";

export function getToken() {
    return localStorage.getItem("token");
}

export function setToken(token) {
    localStorage.setItem("token", token);
}

export function clearToken() {
    localStorage.removeItem("token");
}

export async function apiFetch(path, options = {}) {
    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {}),
    };

    // Lägg bara till Authorization om token finns
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers,
    });

    // Försök läsa JSON om det går
    let data = null;
    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        data = await res.json().catch(() => null);
    } else {
        data = await res.text().catch(() => null);
    }

    if (!res.ok) {
        const message =
            (data && data.message) ||
            (typeof data === "string" && data) ||
            `HTTP ${res.status}`;
        throw new Error(message);
    }

    return data;
}
