const TOKEN_KEY = "lagerlyft_token";
const API_BASE = "http://localhost:8080"; // backend

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

export async function apiFetch(path, options = {}) {
    const token = getToken();

    const url = `${API_BASE}/api${path.startsWith("/") ? path : `/${path}`}`;

    const headers = {
        ...(options.headers || {}),
    };

    // Lägg bara Content-Type om vi faktiskt skickar body
    if (options.body !== undefined && options.body !== null) {
        headers["Content-Type"] = "application/json";
    }

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(url, {
        ...options,
        headers,
    });

    const contentType = res.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
        ? await res.json().catch(() => null)
        : await res.text().catch(() => null);

    if (!res.ok) {
        const msg =
            (data && data.message) ||
            (typeof data === "string" && data) ||
            `HTTP ${res.status}`;
        throw new Error(msg);
    }

    return data;
}
