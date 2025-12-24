import { useEffect, useMemo, useState } from "react";
import "./App.css";

function daysBetween(start, end) {
    const s = new Date(start);
    const e = new Date(end);
    const ms = e.getTime() - s.getTime();
    const days = Math.floor(ms / (1000 * 60 * 60 * 24));
    return Number.isFinite(days) ? days : 0;
}

function formatMoneySEK(value) {
    const num = Number(value || 0);
    return new Intl.NumberFormat("sv-SE", { minimumFractionDigits: 0 }).format(num);
}

export default function App() {
    const [units, setUnits] = useState([]);
    const [cart, setCart] = useState([]);
    const [bookings, setBookings] = useState([]);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [iotMessage, setIotMessage] = useState("");

    const [startDate, setStartDate] = useState("2026-02-01");
    const [endDate, setEndDate] = useState("2026-02-04");

    const userId = 1;

    // ✅ Hämta lager
    useEffect(() => {
        fetch("http://localhost:8080/api/storage-units")
            .then((res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json();
            })
            .then(setUnits)
            .catch((e) => setError(e.message));
    }, []);

    // ✅ Mina bokningar
    const loadBookings = () => {
        fetch(`http://localhost:8080/api/bookings/user/${userId}`)
            .then((res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json();
            })
            .then(setBookings)
            .catch((e) => setError(e.message));
    };

    useEffect(() => {
        loadBookings();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // ✅ Cart
    const addToCart = (unit) => {
        setError("");
        setSuccess("");
        setIotMessage("");
        if (cart.find((u) => u.id === unit.id)) return;
        setCart([...cart, unit]);
    };

    const removeFromCart = (id) => {
        setError("");
        setSuccess("");
        setIotMessage("");
        setCart(cart.filter((u) => u.id !== id));
    };

    const clearCart = () => {
        setCart([]);
    };

    // ✅ Pricing
    const totalDays = useMemo(() => {
        if (!startDate || !endDate) return 0;
        return daysBetween(startDate, endDate);
    }, [startDate, endDate]);

    const sumPerDay = useMemo(() => {
        return cart.reduce((acc, u) => acc + Number(u.pricePerDay || 0), 0);
    }, [cart]);

    const totalPrice = useMemo(() => {
        if (totalDays <= 0) return 0;
        return sumPerDay * totalDays;
    }, [sumPerDay, totalDays]);

    const canCheckout = cart.length > 0 && totalDays > 0;

    // ✅ Checkout
    const checkout = () => {
        setError("");
        setSuccess("");
        setIotMessage("");

        const payload = {
            userId,
            storageUnitIds: cart.map((u) => u.id),
            startDate,
            endDate,
        };

        fetch("http://localhost:8080/api/bookings", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then(async (res) => {
                const data = await res.json();
                if (!res.ok) throw new Error(data.message || "Booking failed");
                return data;
            })
            .then(() => {
                setSuccess("Bokning skapad ✅ (status: PENDING)");
                clearCart();
                loadBookings();
            })
            .catch((e) => setError(e.message));
    };

    // ✅ Payment
    const payBooking = (bookingId) => {
        setError("");
        setSuccess("");
        setIotMessage("");

        fetch(`http://localhost:8080/api/payments/bookings/${bookingId}`, {
            method: "POST",
        })
            .then(async (res) => {
                const data = await res.json();
                if (!res.ok) throw new Error(data.message || "Payment failed");
                return data;
            })
            .then(() => {
                setSuccess("Betalning genomförd 💳✅ (status: CONFIRMED)");
                loadBookings();
            })
            .catch((e) => setError(e.message));
    };

    // ✅ Mock IoT
    const iotAction = (storageUnitId, action) => {
        setError("");
        setSuccess("");
        setIotMessage("");

        fetch(
            `http://localhost:8080/api/iot/storage-units/${storageUnitId}/${action}`,
            { method: "POST" }
        )
            .then(async (res) => {
                const data = await res.json();
                if (!res.ok) throw new Error(data.message || "IoT failed");
                return data;
            })
            .then((data) =>
                setIotMessage(`${data.action} OK för förråd ${data.storageUnitId} ✅`)
            )
            .catch((e) => setError(e.message));
    };

    // ✅ Liten UI-hjälp: försök visa m² om du har sizeM2
    const titleForUnit = (u) => {
        const size = u.sizeM2 != null ? `${u.sizeM2} m²` : u.name;
        return size;
    };

    return (
        <div className="page">
            <header className="topbar">
                <div className="brand">
                    <div className="logoMark">BOX</div>
                    <div className="brandText">
                        <div className="brandName">NordVault</div>
                        <div className="brandTag">Self-Storage & Uthyrning</div>
                    </div>
                </div>

                <nav className="nav">
                    <a href="#prices">Priser</a>
                    <a href="#sizes">Storlekar</a>
                    <a href="#bookings">Mina bokningar</a>
                    <a href="#support">Support</a>
                </nav>

                <div className="topRight">
                    <div className="pill">Ring oss: 010-123 45 67</div>
                </div>
            </header>

            <main className="container">
                <section className="hero" id="sizes">
                    <div>
                        <h1>
                            Magasinering i <span className="accent">Malmö</span>
                        </h1>
                        <p className="subtitle">
                            Välj förråd, välj datum och genomför en mockad betalning. Hantera dina
                            bokningar och testa IoT-knappar (Open/Lock).
                        </p>

                        <div className="heroBadges">
                            <div className="badge">TDD</div>
                            <div className="badge">Spring Boot + React</div>
                            <div className="badge">Docker + MySQL</div>
                        </div>
                    </div>

                    <div className="summaryCard">
                        <div className="summaryTitle">Din sammanfattning</div>

                        <div className="row">
                            <span>Valda förråd</span>
                            <b>{cart.length}</b>
                        </div>
                        <div className="row">
                            <span>Period</span>
                            <b>{totalDays > 0 ? `${totalDays} dagar` : "—"}</b>
                        </div>
                        <div className="row">
                            <span>Summa per dag</span>
                            <b>{formatMoneySEK(sumPerDay)} kr</b>
                        </div>

                        <div className="divider" />

                        <div className="totalRow">
                            <span>Totalpris</span>
                            <span className="total">{formatMoneySEK(totalPrice)} kr</span>
                        </div>

                        <button className="primaryBtn" disabled={!canCheckout} onClick={checkout}>
                            Boka nu
                        </button>

                        <p className="fineprint">
                            * Betalning är mockad. IoT-styrning är mockad via REST-API.
                        </p>
                    </div>
                </section>

                {(error || success || iotMessage) && (
                    <section className="messages">
                        {error && <div className="msg error">❌ {error}</div>}
                        {success && <div className="msg success">✅ {success}</div>}
                        {iotMessage && <div className="msg info">ℹ️ {iotMessage}</div>}
                    </section>
                )}

                <section className="section" id="prices">
                    <div className="sectionHeader">
                        <h2>Välj ett förråd</h2>
                        <p>Tryck “Lägg till” för att lägga i varukorgen.</p>
                    </div>

                    <div className="cardsGrid">
                        {units.map((u) => {
                            const inCart = !!cart.find((c) => c.id === u.id);
                            return (
                                <div className="card" key={u.id}>
                                    <div className="cardTop">
                                        <div className="size">{titleForUnit(u)}</div>
                                        <div className="location">{u.location}</div>
                                    </div>

                                    <div className="cardImg" aria-hidden="true">
                                        <div className="box3d" />
                                    </div>

                                    <div className="priceBlock">
                                        <div className="from">FRÅN</div>
                                        <div className="price">
                                            {formatMoneySEK(u.pricePerDay)} <span className="per">kr / dag</span>
                                        </div>
                                    </div>

                                    <button
                                        className={`cta ${inCart ? "ctaDisabled" : ""}`}
                                        onClick={() => addToCart(u)}
                                        disabled={inCart}
                                    >
                                        {inCart ? "Lagd i varukorg" : "Lägg till"}
                                    </button>
                                </div>
                            );
                        })}
                    </div>
                </section>

                <section className="section">
                    <div className="sectionHeader">
                        <h2>Varukorg & datum</h2>
                        <p>Välj hyresperiod innan bokning.</p>
                    </div>

                    <div className="cartGrid">
                        <div className="panel">
                            <h3>Varukorg</h3>

                            {cart.length === 0 ? (
                                <p className="muted">Din varukorg är tom.</p>
                            ) : (
                                <div className="cartList">
                                    {cart.map((u) => (
                                        <div key={u.id} className="cartItem">
                                            <div>
                                                <b>{u.name}</b>
                                                <div className="mutedSmall">{u.location}</div>
                                            </div>
                                            <div className="cartRight">
                        <span className="mutedSmall">
                          {formatMoneySEK(u.pricePerDay)} kr/dag
                        </span>
                                                <button className="ghostBtn" onClick={() => removeFromCart(u.id)}>
                                                    Ta bort
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        <div className="panel">
                            <h3>Datumintervall</h3>
                            <div className="dateRow">
                                <label className="label">
                                    Startdatum
                                    <input
                                        className="input"
                                        type="date"
                                        value={startDate}
                                        onChange={(e) => setStartDate(e.target.value)}
                                    />
                                </label>

                                <label className="label">
                                    Slutdatum
                                    <input
                                        className="input"
                                        type="date"
                                        value={endDate}
                                        onChange={(e) => setEndDate(e.target.value)}
                                    />
                                </label>
                            </div>

                            <div className="miniRow">
                                <span>Dagar</span>
                                <b className={totalDays <= 0 ? "bad" : ""}>{totalDays}</b>
                            </div>

                            {totalDays <= 0 && (
                                <div className="hint">
                                    Slutdatum måste vara efter startdatum.
                                </div>
                            )}
                        </div>
                    </div>
                </section>

                <section className="section" id="bookings">
                    <div className="sectionHeader">
                        <h2>Mina bokningar</h2>
                        <p>Se status, betala (mock) och styr dörren (mock).</p>
                    </div>

                    {bookings.length === 0 ? (
                        <div className="panel">
                            <p className="muted">Inga bokningar ännu.</p>
                        </div>
                    ) : (
                        <div className="bookingList">
                            {bookings.map((b) => {
                                const firstUnitId =
                                    Array.isArray(b.items) && b.items.length > 0
                                        ? b.items[0].storageUnit?.id
                                        : null;

                                return (
                                    <div className="bookingCard" key={b.id}>
                                        <div className="bookingHead">
                                            <div>
                                                <div className="bookingTitle">Bokning #{b.id}</div>
                                                <div className="mutedSmall">
                                                    {b.startDate} → {b.endDate}
                                                </div>
                                            </div>

                                            <div className={`status ${String(b.status || "").toLowerCase()}`}>
                                                {b.status}
                                            </div>
                                        </div>

                                        <div className="bookingBody">
                                            <div className="miniRow">
                                                <span>Totalpris</span>
                                                <b>{formatMoneySEK(b.totalPrice)} kr</b>
                                            </div>

                                            <div className="miniRow">
                                                <span>Förråd</span>
                                                <b>
                                                    {Array.isArray(b.items)
                                                        ? b.items
                                                            .map((it) => it.storageUnit?.name || `ID:${it.storageUnit?.id ?? it.id}`)
                                                            .join(", ")
                                                        : "—"}
                                                </b>
                                            </div>

                                            <div className="actions">
                                                {b.status === "PENDING" && (
                                                    <button className="primaryBtn" onClick={() => payBooking(b.id)}>
                                                        Betala (mock)
                                                    </button>
                                                )}

                                                <button
                                                    className="ghostBtn"
                                                    disabled={!firstUnitId}
                                                    onClick={() => iotAction(firstUnitId ?? 1, "open")}
                                                >
                                                    Open
                                                </button>

                                                <button
                                                    className="ghostBtn"
                                                    disabled={!firstUnitId}
                                                    onClick={() => iotAction(firstUnitId ?? 1, "lock")}
                                                >
                                                    Lock
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </section>

                <section className="footerGrid" id="support">
                    <div className="footerCard">
                        <h3>Hitta oss</h3>
                        <p>Staffanstorpsvägen 51, 212 23 Malmö</p>
                    </div>
                    <div className="footerCard">
                        <h3>Våra öppettider</h3>
                        <p>Måndag–Söndag 05:00–23:00</p>
                    </div>
                    <div className="footerCard">
                        <h3>Support</h3>
                        <p>Telefon: 010-123 45 67</p>
                    </div>
                </section>
            </main>
        </div>
    );
}
