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
    let data = null;
    
    try {
        if (contentType.includes("application/json")) {
            data = await res.json();
        } else {
            data = await res.text();
        }
    } catch (e) {
        data = null;
    }

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        
        // Hantera JSON error-responses
        if (data && typeof data === "object" && data.message) {
            msg = data.message;
        }
        // Hantera vanlig text
        else if (typeof data === "string" && data) {
            msg = data;
        }
        
        throw new Error(msg);
    }

    return data;
}

// ADMIN: Skapa nytt lagerutrymme
export async function createStorageUnit(data) {
    return apiFetch("/storage-units", {
        method: "POST",
        body: JSON.stringify(data),
    });
}

// Hämta current user info
export async function getCurrentUser() {
    return apiFetch("/auth/me");
}
