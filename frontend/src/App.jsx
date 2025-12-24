import { useEffect, useMemo, useState } from "react";

function daysBetween(start, end) {
    const s = new Date(start);
    const e = new Date(end);
    const ms = e.getTime() - s.getTime();
    const days = Math.floor(ms / (1000 * 60 * 60 * 24));
    return Number.isFinite(days) ? days : 0;
}

export default function App() {
    const [units, setUnits] = useState([]);
    const [cart, setCart] = useState([]);
    const [bookings, setBookings] = useState([]);

    const [error, setError] = useState("");
    const [apiMessage, setApiMessage] = useState("");

    const [startDate, setStartDate] = useState("2026-02-01");
    const [endDate, setEndDate] = useState("2026-02-04");

    const userId = 1; // MOCKAD USER

    // 🔹 HÄMTA LAGER
    useEffect(() => {
        fetch("http://localhost:8080/api/storage-units")
            .then((res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json();
            })
            .then(setUnits)
            .catch((e) => setError(e.message));
    }, []);

    // 🔹 HÄMTA BOKNINGAR (Mina bokningar)
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

    // 🔹 CART
    const addToCart = (unit) => {
        if (cart.find((u) => u.id === unit.id)) return;
        setCart([...cart, unit]);
    };

    const removeFromCart = (id) => {
        setCart(cart.filter((u) => u.id !== id));
    };

    // 🔹 PRISBERÄKNING
    const totalDays = useMemo(() => {
        if (!startDate || !endDate) return 0;
        return daysBetween(startDate, endDate);
    }, [startDate, endDate]);

    const totalPrice = useMemo(() => {
        if (totalDays <= 0) return 0;
        const sumPerDay = cart.reduce(
            (acc, u) => acc + Number(u.pricePerDay || 0),
            0
        );
        return sumPerDay * totalDays;
    }, [cart, totalDays]);

    const canCheckout =
        cart.length > 0 && startDate && endDate && totalDays > 0;

    // 🔹 CHECKOUT → BACKEND
    const checkout = () => {
        setError("");
        setApiMessage("");

        const payload = {
            userId,
            storageUnitIds: cart.map((u) => u.id),
            startDate,
            endDate,
        };

        fetch("http://localhost:8080/api/bookings", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        })
            .then(async (res) => {
                const data = await res.json();
                if (!res.ok) throw new Error(data.message || "Booking failed");
                return data;
            })
            .then(() => {
                setApiMessage("Booking created successfully ✅");
                setCart([]);
                loadBookings(); // 🔥 uppdatera listan direkt
            })
            .catch((e) => setError(e.message));
    };

    return (
        <div style={{ padding: 16, fontFamily: "system-ui, Arial" }}>
            <h1>Storage Units</h1>

            {error && <p style={{ color: "red" }}>Error: {error}</p>}
            {apiMessage && <p style={{ color: "green" }}>{apiMessage}</p>}

            <ul>
                {units.map((u) => (
                    <li key={u.id} style={{ marginBottom: 8 }}>
                        <b>{u.name}</b> — {u.location} — {u.pricePerDay} / day
                        <button style={{ marginLeft: 8 }} onClick={() => addToCart(u)}>
                            Add to cart
                        </button>
                    </li>
                ))}
            </ul>

            <hr />

            <h2>Cart</h2>
            {cart.length === 0 && <p>Cart is empty</p>}

            {cart.length > 0 && (
                <ul>
                    {cart.map((u) => (
                        <li key={u.id}>
                            {u.name} ({u.pricePerDay} / day)
                            <button
                                style={{ marginLeft: 8 }}
                                onClick={() => removeFromCart(u.id)}
                            >
                                Remove
                            </button>
                        </li>
                    ))}
                </ul>
            )}

            <hr />

            <h2>Choose dates</h2>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                <label>
                    Start date{" "}
                    <input
                        type="date"
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                    />
                </label>

                <label>
                    End date{" "}
                    <input
                        type="date"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                    />
                </label>
            </div>

            <p>
                Days: <b>{totalDays}</b>
            </p>

            <h2>Order summary</h2>
            <p>Items: {cart.length}</p>
            <p>Total price: {totalPrice.toFixed(2)}</p>

            <button disabled={!canCheckout} onClick={checkout}>
                Checkout
            </button>

            <hr />

            <h2>My bookings</h2>
            {bookings.length === 0 ? (
                <p>No bookings yet.</p>
            ) : (
                <ul>
                    {bookings.map((b) => (
                        <li key={b.id} style={{ marginBottom: 10 }}>
                            <div>
                                <b>Booking #{b.id}</b> — {b.startDate} → {b.endDate}
                            </div>
                            <div>Status: {b.status}</div>
                            <div>Total: {b.totalPrice}</div>
                            <div>
                                Units:{" "}
                                {Array.isArray(b.items)
                                    ? b.items.map((it) => it.storageUnit?.name || it.id).join(", ")
                                    : "-"}
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
