import { useEffect, useMemo, useState } from "react";
import "./App.css";
import { apiFetch, setToken, clearToken, getToken } from "./api";

function daysBetween(start, end) {
    if (!start || !end) return 0;
    const s = new Date(start + "T00:00:00");
    const e = new Date(end + "T00:00:00");
    const ms = e - s;
    const days = Math.ceil(ms / (1000 * 60 * 60 * 24));
    return Number.isFinite(days) ? Math.max(days, 0) : 0;
}

function formatMoney(n) {
    const num = Number(n);
    if (!Number.isFinite(num)) return "0.00";
    return num.toFixed(2);
}

function Badge({ tone = "neutral", children }) {
    return (
        <span className={`badge badge--${tone}`}>
      {children}
    </span>
    );
}

export default function App() {
    const [view, setView] = useState("lager"); // lager | bookings | login | register

    const [token, setTokenState] = useState(() => getToken() || "");
    const isAuthed = !!token;

    // Auth forms
    const [loginEmail, setLoginEmail] = useState("");
    const [loginPassword, setLoginPassword] = useState("");

    const [regEmail, setRegEmail] = useState("");
    const [regPassword, setRegPassword] = useState("");
    const [regFullName, setRegFullName] = useState("");

    // Lager (storage units)
    const [units, setUnits] = useState([]);
    const [unitsLoading, setUnitsLoading] = useState(false);
    const [unitsError, setUnitsError] = useState("");

    // Booking create
    const [startDate, setStartDate] = useState(() => {
        const d = new Date();
        return d.toISOString().slice(0, 10);
    });
    const [endDate, setEndDate] = useState(() => {
        const d = new Date();
        d.setDate(d.getDate() + 3);
        return d.toISOString().slice(0, 10);
    });

    const [cart, setCart] = useState([]); // array of storageUnit objects
    const [submitLoading, setSubmitLoading] = useState(false);
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    // My bookings
    const [myBookings, setMyBookings] = useState([]);
    const [myBookingsLoading, setMyBookingsLoading] = useState(false);
    const [myBookingsError, setMyBookingsError] = useState("");

    const rentalDays = useMemo(() => daysBetween(startDate, endDate), [startDate, endDate]);

    const totalPrice = useMemo(() => {
        const sumPerDay = cart.reduce((acc, u) => acc + Number(u.pricePerDay || 0), 0);
        return sumPerDay * rentalDays;
    }, [cart, rentalDays]);

    async function loadUnits() {
        setUnitsLoading(true);
        setUnitsError("");
        try {
            // Denna endpoint kräver JWT i ditt projekt => apiFetch skickar token om den finns
            const data = await apiFetch("/storage-units");
            setUnits(Array.isArray(data) ? data : []);
        } catch (e) {
            setUnitsError(e?.message || "Kunde inte hämta lager.");
            setUnits([]);
        } finally {
            setUnitsLoading(false);
        }
    }

    async function loadMyBookings() {
        setMyBookingsLoading(true);
        setMyBookingsError("");
        try {
            const data = await apiFetch("/bookings/my");
            setMyBookings(Array.isArray(data) ? data : []);
        } catch (e) {
            setMyBookingsError(e?.message || "Kunde inte hämta dina bokningar.");
            setMyBookings([]);
        } finally {
            setMyBookingsLoading(false);
        }
    }

    useEffect(() => {
        // Ladda lager direkt vid start
        loadUnits();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    function addToCart(unit) {
        setErrorMsg("");
        setSuccessMsg("");
        setCart((prev) => {
            if (prev.some((x) => x.id === unit.id)) return prev;
            return [...prev, unit];
        });
    }

    function removeFromCart(unitId) {
        setCart((prev) => prev.filter((x) => x.id !== unitId));
    }

    async function checkout() {
        setErrorMsg("");
        setSuccessMsg("");

        if (!isAuthed) {
            setErrorMsg("Du måste vara inloggad för att boka.");
            setView("login");
            return;
        }

        if (cart.length === 0) {
            setErrorMsg("Varukorgen är tom.");
            return;
        }

        if (!startDate || !endDate) {
            setErrorMsg("Välj start- och slutdatum.");
            return;
        }

        if (rentalDays <= 0) {
            setErrorMsg("Slutdatum måste vara efter startdatum.");
            return;
        }

        setSubmitLoading(true);
        try {
            const body = {
                storageUnitIds: cart.map((x) => x.id),
                startDate,
                endDate,
            };

            await apiFetch("/bookings", {
                method: "POST",
                body: JSON.stringify(body),
            });

            setSuccessMsg("Bokning skapad ✅");
            setCart([]);
            // uppdatera “mina bokningar” direkt
            await loadMyBookings();
            setView("bookings");
        } catch (e) {
            setErrorMsg(e?.message || "Kunde inte skapa bokningen.");
        } finally {
            setSubmitLoading(false);
        }
    }

    async function doLogin(e) {
        e.preventDefault();
        setErrorMsg("");
        setSuccessMsg("");

        try {
            const res = await apiFetch("/auth/login", {
                method: "POST",
                body: JSON.stringify({ email: loginEmail, password: loginPassword }),
            });

            const t = res?.token;
            if (!t) throw new Error("Inget token returnerades.");

            setToken(t);
            setTokenState(t);

            setSuccessMsg("Inloggad ✅");
            // när man loggar in: ladda om lager + mina bokningar
            await loadUnits();
            await loadMyBookings();
            setView("lager");
        } catch (e2) {
            setErrorMsg(e2?.message || "Fel vid inloggning.");
        }
    }

    async function doRegister(e) {
        e.preventDefault();
        setErrorMsg("");
        setSuccessMsg("");

        try {
            const res = await apiFetch("/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    email: regEmail,
                    password: regPassword,
                    fullName: regFullName,
                }),
            });

            const t = res?.token;
            if (!t) throw new Error("Inget token returnerades.");

            setToken(t);
            setTokenState(t);

            setSuccessMsg("Konto skapat & inloggad ✅");
            await loadUnits();
            await loadMyBookings();
            setView("lager");
        } catch (e2) {
            setErrorMsg(e2?.message || "Fel vid registrering.");
        }
    }

    function logout() {
        clearToken();
        setTokenState("");
        setCart([]);
        setMyBookings([]);
        setSuccessMsg("Utloggad.");
        setErrorMsg("");
        setView("lager");
    }

    return (
        <div className="page">
            <header className="topbar">
                <div className="brand">
                    <div className="brand__title">LagerLyft</div>
                    <div className="brand__subtitle">Uthyrning av lagerytmen — boka enkelt</div>
                </div>

                <nav className="nav">
                    <button className={`nav__btn ${view === "lager" ? "is-active" : ""}`} onClick={() => setView("lager")}>
                        Lager
                    </button>

                    <button
                        className={`nav__btn ${view === "bookings" ? "is-active" : ""}`}
                        onClick={() => {
                            setView("bookings");
                            if (isAuthed) loadMyBookings();
                        }}
                        disabled={!isAuthed}
                        title={!isAuthed ? "Logga in för att se dina bokningar" : ""}
                    >
                        Mina bokningar
                    </button>

                    {!isAuthed ? (
                        <>
                            <button className={`nav__btn ${view === "login" ? "is-active" : ""}`} onClick={() => setView("login")}>
                                Logga in
                            </button>
                            <button
                                className={`nav__btn ${view === "register" ? "is-active" : ""}`}
                                onClick={() => setView("register")}
                            >
                                Skapa konto
                            </button>
                        </>
                    ) : (
                        <button className="nav__btn" onClick={logout}>
                            Logga ut
                        </button>
                    )}
                </nav>
            </header>

            {(errorMsg || successMsg) && (
                <div className="flash">
                    {errorMsg && <div className="flash__error">Error: {errorMsg}</div>}
                    {successMsg && <div className="flash__ok">{successMsg}</div>}
                </div>
            )}

            <main className="content">
                {view === "lager" && (
                    <>
                        <section className="card">
                            <div className="card__head">
                                <h2>Tillgängliga lager</h2>
                                <div className="card__actions">
                                    <button className="btn" onClick={loadUnits} disabled={unitsLoading}>
                                        {unitsLoading ? "Hämtar..." : "Uppdatera"}
                                    </button>
                                </div>
                            </div>

                            {!isAuthed && (
                                <div className="hint">
                                    För att boka behöver du vara inloggad.
                                </div>
                            )}

                            <div className="dates">
                                <div className="field">
                                    <label>Startdatum</label>
                                    <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                                </div>
                                <div className="field">
                                    <label>Slutdatum</label>
                                    <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                                </div>
                                <div className="field field--small">
                                    <label>Dagar</label>
                                    <div className="stat">{rentalDays}</div>
                                </div>
                            </div>

                            {unitsError && <div className="errorText">{unitsError}</div>}

                            <ul className="list">
                                {units.map((u) => (
                                    <li key={u.id} className="list__row">
                                        <div className="list__main">
                                            <div className="list__title">
                                                <strong>{u.name}</strong>{" "}
                                                <span className="muted">— {u.location}</span>
                                            </div>
                                            <div className="list__meta">
                                                <Badge tone={u.active ? "ok" : "neutral"}>{u.active ? "Aktiv" : "Inaktiv"}</Badge>
                                                <span className="muted">• {u.sizeM2} m²</span>
                                                <span className="muted">• {formatMoney(u.pricePerDay)} / dag</span>
                                            </div>
                                        </div>

                                        <button className="btn btn--ghost" onClick={() => addToCart(u)} disabled={!u.active}>
                                            Lägg till
                                        </button>
                                    </li>
                                ))}

                                {(!unitsLoading && units.length === 0) && (
                                    <li className="list__empty">Inga lager hittades.</li>
                                )}
                            </ul>
                        </section>

                        <section className="grid">
                            <div className="card">
                                <div className="card__head">
                                    <h2>Varukorg</h2>
                                </div>

                                {cart.length === 0 ? (
                                    <div className="muted">Varukorgen är tom.</div>
                                ) : (
                                    <ul className="list">
                                        {cart.map((u) => (
                                            <li key={u.id} className="list__row">
                                                <div className="list__main">
                                                    <div className="list__title">
                                                        <strong>{u.name}</strong> <span className="muted">({formatMoney(u.pricePerDay)} / dag)</span>
                                                    </div>
                                                    <div className="list__meta muted">{u.location}</div>
                                                </div>
                                                <button className="btn btn--ghost" onClick={() => removeFromCart(u.id)}>
                                                    Ta bort
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>

                            <div className="card">
                                <div className="card__head">
                                    <h2>Sammanfattning</h2>
                                </div>

                                <div className="summary">
                                    <div className="summary__row">
                                        <span>Antal lager</span>
                                        <strong>{cart.length}</strong>
                                    </div>
                                    <div className="summary__row">
                                        <span>Dagar</span>
                                        <strong>{rentalDays}</strong>
                                    </div>
                                    <div className="summary__row">
                                        <span>Totalt</span>
                                        <strong>{formatMoney(totalPrice)}</strong>
                                    </div>
                                </div>

                                <button className="btn btn--primary" onClick={checkout} disabled={submitLoading}>
                                    {submitLoading ? "Skickar..." : "Boka nu"}
                                </button>
                            </div>
                        </section>
                    </>
                )}

                {view === "bookings" && (
                    <section className="card">
                        <div className="card__head">
                            <h2>Mina bokningar</h2>
                            <div className="card__actions">
                                <button className="btn" onClick={loadMyBookings} disabled={!isAuthed || myBookingsLoading}>
                                    {myBookingsLoading ? "Hämtar..." : "Uppdatera"}
                                </button>
                            </div>
                        </div>

                        {!isAuthed ? (
                            <div className="hint">Logga in för att se dina bokningar.</div>
                        ) : (
                            <>
                                {myBookingsError && <div className="errorText">{myBookingsError}</div>}

                                <ul className="list">
                                    {myBookings.map((b) => (
                                        <li key={b.id} className="booking">
                                            <div className="booking__top">
                                                <div>
                                                    <div className="booking__title">
                                                        Bokning #{b.id}{" "}
                                                        <Badge tone={b.status === "PENDING" ? "neutral" : "ok"}>
                                                            {b.status || "OK"}
                                                        </Badge>
                                                    </div>
                                                    <div className="muted">
                                                        {b.startDate} → {b.endDate}
                                                    </div>
                                                </div>

                                                <div className="booking__price">
                                                    {formatMoney(b.totalPrice)}
                                                </div>
                                            </div>

                                            {Array.isArray(b.items) && b.items.length > 0 && (
                                                <div className="booking__items">
                                                    <div className="muted">Lager:</div>
                                                    <ul className="chips">
                                                        {b.items.map((it) => (
                                                            <li key={it.id} className="chip">
                                                                #{it.storageUnitId ?? it.storageUnit?.id ?? "?"}{" "}
                                                                {it.storageUnit?.name ? `• ${it.storageUnit.name}` : ""}
                                                            </li>
                                                        ))}
                                                    </ul>
                                                </div>
                                            )}
                                        </li>
                                    ))}

                                    {(!myBookingsLoading && myBookings.length === 0) && (
                                        <li className="list__empty">Inga bokningar än.</li>
                                    )}
                                </ul>
                            </>
                        )}
                    </section>
                )}

                {view === "login" && (
                    <section className="card card--narrow">
                        <div className="card__head">
                            <h2>Logga in</h2>
                        </div>

                        <form className="form" onSubmit={doLogin}>
                            <div className="field">
                                <label>Email</label>
                                <input value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} placeholder="email" />
                            </div>

                            <div className="field">
                                <label>Lösenord</label>
                                <input
                                    type="password"
                                    value={loginPassword}
                                    onChange={(e) => setLoginPassword(e.target.value)}
                                    placeholder="lösenord"
                                />
                            </div>

                            <button className="btn btn--primary" type="submit">
                                Logga in
                            </button>

                            <div className="muted">
                                Har du inget konto?{" "}
                                <button type="button" className="link" onClick={() => setView("register")}>
                                    Skapa konto
                                </button>
                            </div>
                        </form>
                    </section>
                )}

                {view === "register" && (
                    <section className="card card--narrow">
                        <div className="card__head">
                            <h2>Skapa konto</h2>
                        </div>

                        <form className="form" onSubmit={doRegister}>
                            <div className="field">
                                <label>Fullständigt namn</label>
                                <input value={regFullName} onChange={(e) => setRegFullName(e.target.value)} placeholder="Namn" />
                            </div>

                            <div className="field">
                                <label>Email</label>
                                <input value={regEmail} onChange={(e) => setRegEmail(e.target.value)} placeholder="email" />
                            </div>

                            <div className="field">
                                <label>Lösenord</label>
                                <input
                                    type="password"
                                    value={regPassword}
                                    onChange={(e) => setRegPassword(e.target.value)}
                                    placeholder="lösenord"
                                />
                            </div>

                            <button className="btn btn--primary" type="submit">
                                Skapa konto
                            </button>

                            <div className="muted">
                                Har du redan konto?{" "}
                                <button type="button" className="link" onClick={() => setView("login")}>
                                    Logga in
                                </button>
                            </div>
                        </form>
                    </section>
                )}
            </main>
        </div>
    );
}
