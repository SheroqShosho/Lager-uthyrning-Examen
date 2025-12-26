import { useEffect, useMemo, useState } from "react";
import "./App.css";
import { apiFetch, clearToken, getToken, setToken } from "./api";

function App() {
    const [tab, setTab] = useState("storage"); // storage | login | register | my
    const [authEmail, setAuthEmail] = useState("");
    const [authPassword, setAuthPassword] = useState("");
    const [authFullName, setAuthFullName] = useState("");

    const [token, setTokenState] = useState(getToken());

    const [storageUnits, setStorageUnits] = useState([]);
    const [loadingUnits, setLoadingUnits] = useState(false);
    const [unitsError, setUnitsError] = useState("");

    const [myBookings, setMyBookings] = useState([]);
    const [loadingBookings, setLoadingBookings] = useState(false);
    const [bookingsError, setBookingsError] = useState("");

    const [message, setMessage] = useState("");

    const isLoggedIn = useMemo(() => !!token, [token]);

    async function loadStorageUnits() {
        setLoadingUnits(true);
        setUnitsError("");
        try {
            const data = await apiFetch("/storage-units");
            setStorageUnits(Array.isArray(data) ? data : []);
        } catch (e) {
            setUnitsError(e.message || "Kunde inte hämta lager.");
        } finally {
            setLoadingUnits(false);
        }
    }

    async function loadMyBookings() {
        setLoadingBookings(true);
        setBookingsError("");
        try {
            const data = await apiFetch("/bookings/my");
            setMyBookings(Array.isArray(data) ? data : []);
        } catch (e) {
            setBookingsError(e.message || "Kunde inte hämta bokningar.");
        } finally {
            setLoadingBookings(false);
        }
    }

    useEffect(() => {
        // Ladda lager direkt när sidan öppnas
        loadStorageUnits();
    }, []);

    useEffect(() => {
        // Om man går till "my" och är inloggad, hämta bokningar
        if (tab === "my" && isLoggedIn) {
            loadMyBookings();
        }
    }, [tab, isLoggedIn]);

    async function handleLogin(e) {
        e.preventDefault();
        setMessage("");
        try {
            const res = await apiFetch("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: authEmail,
                    password: authPassword,
                }),
            });

            setToken(res.token);
            setTokenState(res.token);
            setMessage("Inloggning lyckades ✅");
            setAuthPassword("");
            setTab("my");
        } catch (err) {
            setMessage(err.message || "Inloggning misslyckades.");
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        setMessage("");
        try {
            const res = await apiFetch("/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    email: authEmail,
                    password: authPassword,
                    fullName: authFullName,
                }),
            });

            setToken(res.token);
            setTokenState(res.token);
            setMessage("Konto skapat & inloggad ✅");
            setAuthPassword("");
            setTab("my");
        } catch (err) {
            setMessage(err.message || "Registrering misslyckades.");
        }
    }

    function handleLogout() {
        clearToken();
        setTokenState(null);
        setMessage("Utloggad.");
        setTab("storage");
    }

    return (
        <div style={{ maxWidth: 1000, margin: "0 auto", padding: 16 }}>
            <header
                style={{
                    display: "flex",
                    gap: 12,
                    alignItems: "center",
                    justifyContent: "space-between",
                    marginBottom: 16,
                }}
            >
                <div>
                    <h1 style={{ margin: 0 }}>LagerLyft</h1>
                    <div style={{ opacity: 0.75, marginTop: 4 }}>
                        Uthyrning av lagerutrymmen – boka enkelt
                    </div>
                </div>

                <nav style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <button onClick={() => setTab("storage")}>Lager</button>

                    {isLoggedIn ? (
                        <>
                            <button onClick={() => setTab("my")}>Mina bokningar</button>
                            <button onClick={handleLogout}>Logga ut</button>
                        </>
                    ) : (
                        <>
                            <button onClick={() => setTab("login")}>Logga in</button>
                            <button onClick={() => setTab("register")}>Skapa konto</button>
                        </>
                    )}
                </nav>
            </header>

            {message && (
                <div
                    style={{
                        padding: 12,
                        border: "1px solid #ddd",
                        borderRadius: 8,
                        marginBottom: 16,
                    }}
                >
                    {message}
                </div>
            )}

            {tab === "storage" && (
                <section>
                    <h2 style={{ marginTop: 0 }}>Tillgängliga lager</h2>

                    {!isLoggedIn && (
                        <div
                            style={{
                                padding: 12,
                                border: "1px solid #eee",
                                borderRadius: 8,
                                marginBottom: 12,
                                background: "#fafafa",
                            }}
                        >
                            För att boka behöver du vara inloggad. Gå till{" "}
                            <b>Logga in</b> eller <b>Skapa konto</b>.
                        </div>
                    )}

                    <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
                        <button onClick={loadStorageUnits} disabled={loadingUnits}>
                            {loadingUnits ? "Laddar..." : "Uppdatera"}
                        </button>
                    </div>

                    {unitsError && (
                        <div style={{ color: "crimson", marginBottom: 12 }}>
                            {unitsError}
                        </div>
                    )}

                    <div style={{ display: "grid", gap: 12 }}>
                        {storageUnits.map((u) => (
                            <div
                                key={u.id}
                                style={{
                                    border: "1px solid #ddd",
                                    borderRadius: 10,
                                    padding: 12,
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "flex-start",
                                    gap: 12,
                                }}
                            >
                                <div>
                                    <div style={{ fontSize: 18, fontWeight: 700 }}>
                                        {u.name}{" "}
                                        <span style={{ opacity: 0.7, fontWeight: 500 }}>
                      ({u.sizeM2} m²)
                    </span>
                                    </div>
                                    <div style={{ opacity: 0.8, marginTop: 4 }}>
                                        {u.location}
                                    </div>
                                    {u.description && (
                                        <div style={{ marginTop: 8 }}>{u.description}</div>
                                    )}
                                </div>

                                <div style={{ textAlign: "right", minWidth: 160 }}>
                                    <div style={{ fontWeight: 700 }}>
                                        {u.pricePerDay} kr / dag
                                    </div>
                                    <div style={{ marginTop: 8, opacity: 0.8 }}>
                                        {u.active ? "✅ Aktiv" : "⛔ Inaktiv"}
                                    </div>

                                    <div style={{ marginTop: 10 }}>
                                        <button
                                            disabled={!isLoggedIn || !u.active}
                                            onClick={() => {
                                                setMessage(
                                                    "Nästa steg: vi bygger bokningsflöde + datum + varukorg."
                                                );
                                            }}
                                            title={
                                                !isLoggedIn
                                                    ? "Logga in för att boka"
                                                    : !u.active
                                                        ? "Denna enhet är inaktiv"
                                                        : "Boka"
                                            }
                                        >
                                            Boka
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {tab === "login" && (
                <section style={{ maxWidth: 520 }}>
                    <h2 style={{ marginTop: 0 }}>Logga in</h2>
                    <form onSubmit={handleLogin} style={{ display: "grid", gap: 10 }}>
                        <label style={{ display: "grid", gap: 6 }}>
                            E-post
                            <input
                                value={authEmail}
                                onChange={(e) => setAuthEmail(e.target.value)}
                                placeholder="t.ex. shero@test.com"
                                required
                            />
                        </label>

                        <label style={{ display: "grid", gap: 6 }}>
                            Lösenord
                            <input
                                value={authPassword}
                                onChange={(e) => setAuthPassword(e.target.value)}
                                type="password"
                                placeholder="••••••••"
                                required
                            />
                        </label>

                        <button type="submit">Logga in</button>
                    </form>
                </section>
            )}

            {tab === "register" && (
                <section style={{ maxWidth: 520 }}>
                    <h2 style={{ marginTop: 0 }}>Skapa konto</h2>
                    <form onSubmit={handleRegister} style={{ display: "grid", gap: 10 }}>
                        <label style={{ display: "grid", gap: 6 }}>
                            Fullständigt namn
                            <input
                                value={authFullName}
                                onChange={(e) => setAuthFullName(e.target.value)}
                                placeholder="Ditt namn"
                                required
                            />
                        </label>

                        <label style={{ display: "grid", gap: 6 }}>
                            E-post
                            <input
                                value={authEmail}
                                onChange={(e) => setAuthEmail(e.target.value)}
                                placeholder="t.ex. shero@test.com"
                                required
                            />
                        </label>

                        <label style={{ display: "grid", gap: 6 }}>
                            Lösenord
                            <input
                                value={authPassword}
                                onChange={(e) => setAuthPassword(e.target.value)}
                                type="password"
                                placeholder="minst 4 tecken"
                                required
                            />
                        </label>

                        <button type="submit">Skapa konto</button>
                    </form>
                </section>
            )}

            {tab === "my" && (
                <section>
                    <h2 style={{ marginTop: 0 }}>Mina bokningar</h2>

                    {!isLoggedIn ? (
                        <div>
                            Du måste logga in för att se dina bokningar.
                        </div>
                    ) : (
                        <>
                            <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
                                <button onClick={loadMyBookings} disabled={loadingBookings}>
                                    {loadingBookings ? "Laddar..." : "Uppdatera"}
                                </button>
                            </div>

                            {bookingsError && (
                                <div style={{ color: "crimson", marginBottom: 12 }}>
                                    {bookingsError}
                                </div>
                            )}

                            {myBookings.length === 0 ? (
                                <div>Inga bokningar ännu.</div>
                            ) : (
                                <div style={{ display: "grid", gap: 12 }}>
                                    {myBookings.map((b) => (
                                        <div
                                            key={b.id}
                                            style={{
                                                border: "1px solid #ddd",
                                                borderRadius: 10,
                                                padding: 12,
                                            }}
                                        >
                                            <div style={{ fontWeight: 700 }}>
                                                Bokning #{b.id} — {b.status}
                                            </div>
                                            <div style={{ marginTop: 6, opacity: 0.85 }}>
                                                Period: <b>{b.startDate}</b> → <b>{b.endDate}</b>
                                            </div>
                                            <div style={{ marginTop: 6 }}>
                                                Total: <b>{b.totalPrice} kr</b>
                                            </div>
                                            {Array.isArray(b.items) && b.items.length > 0 && (
                                                <div style={{ marginTop: 8, opacity: 0.9 }}>
                                                    Enheter:{" "}
                                                    {b.items
                                                        .map((it) => it.storageUnit?.name || it.storageUnitId || it.id)
                                                        .join(", ")}
                                                </div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </>
                    )}
                </section>
            )}
        </div>
    );
}

export default App;
