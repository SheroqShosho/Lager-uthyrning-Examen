import { useEffect, useMemo, useState } from "react";
import "./App.css";
import { apiFetch, setToken, clearToken, getToken, getCurrentUser } from "./api";
import { AdminPanel } from "./AdminPanel";

function daysBetween(start, end) {
    if (!start || !end) return 0;
    const s = new Date(start + "T00:00:00");
    const e = new Date(end + "T00:00:00");
    const ms = e - s;
    const days = Math.ceil(ms / (1000 * 60 * 60 * 24));
    // Om end är före start, returnera 0, annars minst 1 dag
    return Number.isFinite(days) ? Math.max(days, 0) : 0;
}

function formatMoney(n) {
    const num = Number(n);
    if (!Number.isFinite(num)) return "0.00";
    return num.toFixed(2);
}

function Badge({ tone = "neutral", children }) {
    return <span className={`badge badge--${tone}`}>{children}</span>;
}

export default function App() {
    const [view, setView] = useState("lager");
    const [token, setTokenState] = useState(() => getToken() || "");
    const isAuthed = !!token;

    const [loginEmail, setLoginEmail] = useState("");
    const [loginPassword, setLoginPassword] = useState("");
    const [regEmail, setRegEmail] = useState("");
    const [regPassword, setRegPassword] = useState("");
    const [regFullName, setRegFullName] = useState("");

    const [units, setUnits] = useState([]);
    const [unitsLoading, setUnitsLoading] = useState(false);
    const [unitsError, setUnitsError] = useState("");

    const [cart, setCart] = useState([]);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    const [myBookings, setMyBookings] = useState([]);
    const [myBookingsLoading, setMyBookingsLoading] = useState(false);
    const [myBookingsError, setMyBookingsError] = useState("");

    const [iotLoading, setIotLoading] = useState(null);
    const [iotResult, setIotResult] = useState(null);
    const [latestBooking, setLatestBooking] = useState(null);
    const [lockStatus, setLockStatus] = useState({}); // Spåra låsstatus per unit
    const [currentUser, setCurrentUser] = useState(null);

    const isDateOccupied = (unit, startStr, endStr) => {
        if (!startStr || !endStr) return false;

        const start = new Date(startStr + "T00:00:00").getTime();
        const end = new Date(endStr + "T00:00:00").getTime();

        // 1. Kolla globala bokningar (bookingItems)
        const hasGlobalOverlap = unit.bookingItems?.some(item => {
            // Här är den kritiska biten - vi mappar mot booking-objektet
            const b = item.booking;
            if (!b || !b.startDate || !b.endDate) return false;

            const bStart = new Date(b.startDate + "T00:00:00").getTime();
            const bEnd = new Date(b.endDate + "T00:00:00").getTime();

            const overlap = (start <= bEnd && end >= bStart);
            if (overlap) {
                console.log(`❌ KROCK med befintlig bokning för ${unit.name}:`, b);
            }
            return overlap;
        });

        // 2. Kolla mot dina egna lokala bokningar
        const hasMyOverlap = myBookings?.some(b => {
            const isSameUnit = b.items?.some(it => it.storageUnitId === unit.id || it.storageUnit?.id === unit.id);
            if (!isSameUnit) return false;

            const bStart = new Date(b.startDate + "T00:00:00").getTime();
            const bEnd = new Date(b.endDate + "T00:00:00").getTime();

            const overlap = (start <= bEnd && end >= bStart);
            if (overlap) {
                console.log(`❌ KROCK med din egen bokning för ${unit.name}`);
            }
            return overlap;
        });

        const result = !!(hasGlobalOverlap || hasMyOverlap);
        console.log(`📊 isDateOccupied(${unit.name}, ${startStr}, ${endStr}):`, { hasGlobalOverlap, hasMyOverlap, result });
        return result;
    };

    async function loadUnits() {
        setUnitsLoading(true);
        setUnitsError("");
        try {
            const data = await apiFetch("/storage-units");
            // Denna logg är viktig! Klicka på pilen i konsolen för att se om 'bookingItems' finns med nu.
            console.log("📦 FULLSTÄNDIG DATA FRÅN BACKEND:", data);

            const unitsWithDates = data.map(u => ({
                ...u,
                tempStart: (() => {
                    const d = new Date();
                    return d.toISOString().slice(0, 10);  // Idag
                })(),
                tempEnd: (() => {
                    const d = new Date();
                    d.setDate(d.getDate() + 7);  // 7 dagar framåt från idag
                    return d.toISOString().slice(0, 10);
                })()
            }));
            setUnits(unitsWithDates);
        } catch (e) {
            setUnitsError(e?.message || "Kunde inte hämta lager.");
        } finally {
            setUnitsLoading(false);
        }
    }

    async function loadMyBookings() {
        setMyBookingsLoading(true);
        try {
            const data = await apiFetch("/bookings/my");
            setMyBookings(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
        } finally {
            setMyBookingsLoading(false);
        }
    }

    useEffect(() => { loadUnits(); }, []);
    useEffect(() => { if (isAuthed) loadMyBookings(); }, [isAuthed]);
    
    // Ladda current user info när man loggar in
    useEffect(() => {
        if (isAuthed) {
            getCurrentUser()
                .then(user => {
                    console.log("👤 Current user:", user);
                    setCurrentUser(user);
                })
                .catch(e => console.log("Could not load current user:", e));
        } else {
            setCurrentUser(null);
        }
    }, [token]);

    function handleTempDateChange(unitId, field, value) {
        setUnits(units.map(u => u.id === unitId ? { ...u, [field]: value } : u));
    }

    function addToCart(unit) {
        if (isDateOccupied(unit, unit.tempStart, unit.tempEnd)) {
            window.alert('Datumen är tyvärr redan bokade för detta lager. Välj andra datum.');
            setErrorMsg("❌ Datumen är tyvärr upptagna!");
            setTimeout(() => setErrorMsg(""), 3000);
            return;
        }

        if (!cart.find(u => u.id === unit.id)) {
            const itemWithDates = {
                ...unit,
                specificStartDate: unit.tempStart,
                specificEndDate: unit.tempEnd
            };
            setCart([...cart, itemWithDates]);
            setSuccessMsg(`✅ ${unit.name} tillagd!`);
            setTimeout(() => setSuccessMsg(""), 3000);
        }
    }

    function removeFromCart(unitId) {
        setCart(cart.filter(u => u.id !== unitId));
    }

    function updateCartItemDate(itemId, field, value) {
        setCart(cart.map(item =>
            item.id === itemId ? { ...item, [field]: value } : item
        ));
    }

    async function checkout() {
        if (cart.length === 0) return;
        setSubmitLoading(true);
        try {
            const items = cart.map(item => ({
                storageUnitId: item.id,
                startDate: item.specificStartDate,
                endDate: item.specificEndDate
            }));
            const result = await apiFetch("/bookings", {
                method: "POST",
                body: JSON.stringify({ items: items }),
            });
            setLatestBooking(result);
            setCart([]);
            await loadUnits(); // VIKTIGT: Hämta om allt för att se nya bokningar direkt
            await loadMyBookings();
            setView("payment");
        } catch (e) {
            console.error("❌ Checkout misslyckades:", e);
            setErrorMsg(`Fel: ${e?.message || "Kunde inte slutföra bokningen."}`);
            setTimeout(() => setErrorMsg(""), 4000);
        } finally {
            setSubmitLoading(false);
        }
    }

    async function handleLogin() {
        if (!loginEmail || !loginPassword) return;
        setSubmitLoading(true);
        try {
            const result = await apiFetch("/auth/login", {
                method: "POST",
                body: JSON.stringify({ email: loginEmail, password: loginPassword }),
            });
            setToken(result.token); setTokenState(result.token);
            setView("lager");
        } catch (e) { setErrorMsg(e?.message || "Inloggningsfel"); setTimeout(() => setErrorMsg(""), 4000); } finally { setSubmitLoading(false); }
    }

    function logout() {
        clearToken(); setTokenState(""); setCart([]); setView("lager");
    }

    async function handleIotAction(storageUnitId, action) {
        const actionKey = `${action}-${storageUnitId}`;
        setIotLoading(actionKey);
        try {
            await apiFetch(`/iot/storage-units/${storageUnitId}/${action}`, { method: "POST" });
            setIotResult({ success: true, message: action === "open" ? "🔓 Öppnat!" : "🔒 Låst!" });
            // Uppdatera lock-status
            setLockStatus(prev => ({ ...prev, [storageUnitId]: action === "open" ? true : false }));
            setTimeout(() => setIotResult(null), 3000);
        } catch (e) { setIotResult({ success: false, message: "❌ IoT-fel" }); setTimeout(() => setIotResult(null), 3000); } finally { setIotLoading(null); }
    }

    const toastBottomStyle = {
        position: 'fixed', bottom: '20px', left: '50%', transform: 'translateX(-50%)',
        zIndex: 9999, padding: '12px 24px', borderRadius: '8px', color: 'white',
        fontWeight: 'bold', boxShadow: '0 4px 12px rgba(0,0,0,0.15)', minWidth: '200px', textAlign: 'center'
    };

    return (
        <div className="app">
            <header className="header">
                <div className="header-content">
                    <h1 className="logo" onClick={() => setView("lager")}>🏢 Lagerlyft</h1>
                    <nav className="nav">
                        <button className={`nav-btn ${view === "lager" ? "active" : ""}`} onClick={() => setView("lager")}>Lagerlista</button>
                        <button className={`nav-btn ${view === "bookings" ? "active" : ""}`} onClick={() => setView("bookings")}>Mina Bokningar</button>
                        <button className={`nav-btn ${view === "cart" ? "active" : ""}`} onClick={() => setView("cart")}>🛒 Varukorg {cart.length > 0 && <span>({cart.length})</span>}</button>
                        {currentUser?.role === "ADMIN" && <button className={`nav-btn ${view === "admin" ? "active" : ""}`} onClick={() => setView("admin")}>⚙️ Admin</button>}
                        {isAuthed ? <button className="nav-btn" onClick={logout}>🚪 Logga ut</button> :
                            <button className="nav-btn" onClick={() => setView("login")}>🔐 Logga in</button>}
                    </nav>
                </div>
            </header>

            {successMsg && <div style={{...toastBottomStyle, background: '#28a745'}}>{successMsg}</div>}
            {errorMsg && <div style={{...toastBottomStyle, background: '#dc3545'}}>{errorMsg}</div>}
            {iotResult && <div style={{...toastBottomStyle, background: iotResult.success ? '#28a745' : '#dc3545'}}>{iotResult.message}</div>}

            <main className="content">
                {view === "lager" && (
                    <section className="section">
                        <h2>📦 1. Välj ett lagerutrymme</h2>
                        <div className="grid">
                            {units.map((u) => {
                                const days = daysBetween(u.tempStart, u.tempEnd);
                                const isInCart = cart.find(c => c.id === u.id);
                                const isOccupied = isDateOccupied(u, u.tempStart, u.tempEnd);

                                return (
                                    <div key={u.id} className={`card ${isInCart ? "selected-card" : ""}`}>
                                        <div style={{ textAlign: 'center', marginBottom: '10px' }}>📦</div>
                                        <h3 style={{ marginTop: '0' }}>{u.name}</h3>
                                        <p style={{ fontSize: '0.9rem', color: '#666' }}>{u.description}</p>
                                        <div style={{ margin: '15px 0', borderTop: '1px solid #eee', paddingTop: '15px' }}>
                                            <div>📐 <strong>Storlek: {u.sizeM2} m²</strong></div>
                                            <div>📍 Plats: {u.location}</div>
                                            <div style={{ color: '#28a745', fontWeight: 'bold' }}>💰 {formatMoney(u.pricePerDay)} kr/dag</div>
                                        </div>

                                        <div className="card-date-picker">
                                            <label style={{fontSize: '0.7rem', fontWeight: 'bold'}}>FRÅN</label>
                                            <input
                                                type="date"
                                                value={u.tempStart}
                                                onChange={(e) => handleTempDateChange(u.id, 'tempStart', e.target.value)}
                                                style={{ borderLeft: isOccupied ? '5px solid #dc3545' : '5px solid #28a745' }}
                                            />
                                            <label style={{fontSize: '0.7rem', fontWeight: 'bold', marginTop: '10px', display: 'block'}}>TILL</label>
                                            <input
                                                type="date"
                                                value={u.tempEnd}
                                                onChange={(e) => handleTempDateChange(u.id, 'tempEnd', e.target.value)}
                                                style={{ borderLeft: isOccupied ? '5px solid #dc3545' : '5px solid #28a745' }}
                                            />
                                            <div style={{marginTop: '5px', fontSize: '0.7rem', textAlign: 'right', color: isOccupied ? '#dc3545' : '#28a745'}}>
                                                {isOccupied ? "❌ Redan bokat" : `✓ Ledigt`}
                                            </div>
                                        </div>

                                        {isAuthed ? (
                                            <button
                                                className={`btn-add-new ${isInCart ? "in-cart" : ""}`}
                                                onClick={() => addToCart(u)}
                                                disabled={!u.tempStart || !u.tempEnd || new Date(u.tempEnd) < new Date(u.tempStart) || isOccupied}
                                            >
                                                {isOccupied ? "Fullbokat" : (isInCart ? "✅ I varukorg" : "Lägg till")}
                                            </button>
                                        ) : <p style={{ fontSize: '0.8rem', textAlign: 'center', color: '#666' }}>🔐 Logga in för att boka</p>}
                                    </div>
                                );
                            })}
                        </div>
                    </section>
                )}

                {view === "cart" && (
                    <section className="section">
                        <div className="cart-layout">
                            <div style={{background: 'white', padding: '2rem', borderRadius: '12px', border: '1px solid #eee'}}>
                                <h2>🛒 Min Varukorg</h2>
                                {cart.length === 0 ? <p>Din varukorg är tom.</p> : cart.map((item) => (
                                    <div key={item.id} className="cart-card" style={{padding: '1.5rem 0', borderBottom: '1px solid #eee'}}>
                                        <div style={{display:'flex', justifyContent:'space-between', alignItems: 'start'}}>
                                            <div>
                                                <h3 style={{margin: 0}}>{item.name}</h3>
                                                <p style={{fontSize: '0.8rem', color: '#666'}}>{item.location} • {item.sizeM2} m²</p>
                                            </div>
                                            <button onClick={() => removeFromCart(item.id)} style={{color:'#ff4d4d', border:'none', background:'none', cursor:'pointer', fontWeight: 'bold'}}>✕ Ta bort</button>
                                        </div>
                                        <div style={{display:'flex', gap:'15px', marginTop: '15px'}}>
                                            <div style={{flex: 1}}>
                                                <label style={{fontSize:'0.7rem', fontWeight: 'bold'}}>FRÅN</label>
                                                <input type="date" value={item.specificStartDate} onChange={(e) => updateCartItemDate(item.id, 'specificStartDate', e.target.value)} />
                                            </div>
                                            <div style={{flex: 1}}>
                                                <label style={{fontSize:'0.7rem', fontWeight: 'bold'}}>TILL</label>
                                                <input type="date" value={item.specificEndDate} onChange={(e) => updateCartItemDate(item.id, 'specificEndDate', e.target.value)} />
                                            </div>
                                            <div style={{ textAlign: 'right', minWidth: '80px' }}>
                                                <div style={{fontSize: '0.7rem', color: '#2563eb'}}>{daysBetween(item.specificStartDate, item.specificEndDate)} dagar</div>
                                                <div style={{fontWeight: 'bold'}}>{formatMoney(item.pricePerDay * daysBetween(item.specificStartDate, item.specificEndDate))} kr</div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="cart-summary">
                                <h3>📊 Sammanfattning</h3>
                                <div style={{ display: 'flex', justifyContent: 'space-between', margin: '20px 0' }}>
                                    <span style={{ fontWeight: 'bold' }}>TOTALT:</span>
                                    <h2 style={{color: '#28a745', margin: 0}}>{formatMoney(cart.reduce((sum, i) => sum + (i.pricePerDay * daysBetween(i.specificStartDate, i.specificEndDate)), 0))} kr</h2>
                                </div>
                                <button className="btn-add-new" style={{width:'100%', background: '#111827'}} onClick={checkout} disabled={cart.length === 0}>
                                    ✅ Bekräfta & Boka
                                </button>
                            </div>
                        </div>
                    </section>
                )}

                {view === "payment" && (
                    <section className="section" style={{maxWidth: '500px', margin: '0 auto'}}>
                        <div className="card" style={{textAlign: 'center', padding: '3rem'}}>
                            <h2>Betalning</h2>
                            <h2 style={{color: '#28a745'}}>{formatMoney(latestBooking?.totalPrice)} kr</h2>
                            <button className="btn-add-new" style={{width: '100%'}} onClick={() => setView("bookings")}>Betala</button>
                        </div>
                    </section>
                )}

                {view === "bookings" && isAuthed && (
                    <section className="section">
                        <h2>📋 Mina Bokningar</h2>
                        {myBookings.map((b) => {
                            // Kontrollera om bokningsperioden är giltig (är vi inom datumen?)
                            const today = new Date().toISOString().slice(0, 10);
                            const isWithinBookingPeriod = today >= b.startDate && today <= b.endDate;

                            return (
                                <div key={b.id} className="booking-card">
                                    <h3>Bokning #{b.id} ({b.startDate} → {b.endDate})</h3>
                                    {b.items?.map(it => {
                                        // Debug: se vad vi får från backend
                                        console.log("📦 BookingItem data:", { it, storageUnitId: it.storageUnitId, unitName: it.storageUnit?.name });
                                        const unitId = it.storageUnitId;
                                        const isOpen = lockStatus[unitId] === true; // true = öppen/upplåst
                                        const isClosed = lockStatus[unitId] === false; // false = låst/stängd
                                        
                                        // Endast visa knappar om vi är inom bokningsperioden
                                        if (!isWithinBookingPeriod) {
                                            return (
                                                <div key={it.id} className="item-row">
                                                    <span>📦 {it.storageUnit?.name || "Okänd enhet"}</span>
                                                    <span style={{ fontSize: '0.8rem', color: '#999' }}>Låsning tillgänglig: {b.startDate} → {b.endDate}</span>
                                                </div>
                                            );
                                        }
                                        
                                        return (
                                            <div key={it.id} className="item-row">
                                                <span>📦 {it.storageUnit?.name || "Okänd enhet"}</span>
                                                <div style={{ display: 'flex', gap: '10px' }}>
                                                    {isClosed || lockStatus[unitId] === undefined ? (
                                                        <button 
                                                            className="btn-iot" 
                                                            onClick={() => handleIotAction(unitId, "open")}
                                                            disabled={!unitId}
                                                        >
                                                            🔓 Lås upp
                                                        </button>
                                                    ) : null}
                                                    {isOpen ? (
                                                        <button 
                                                            className="btn-iot" 
                                                            onClick={() => handleIotAction(unitId, "lock")}
                                                            disabled={!unitId}
                                                            style={{ background: '#666' }}
                                                        >
                                                            🔒 Låsa
                                                        </button>
                                                    ) : null}
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            );
                        })}
                    </section>
                )}

                {view === "login" && (
                    <section className="section" style={{maxWidth:'400px', margin:'auto'}}>
                        <div className="card">
                            <h2>🔐 Logga in</h2>
                            <input type="email" placeholder="Email" style={{width: '100%', marginBottom: '10px', padding: '10px'}} onChange={(e) => setLoginEmail(e.target.value)} />
                            <input type="password" placeholder="Lösenord" style={{width: '100%', marginBottom: '10px', padding: '10px'}} onChange={(e) => setLoginPassword(e.target.value)} />
                            <button className="btn-add-new" onClick={handleLogin}>Logga in</button>
                        </div>
                    </section>
                )}

                {view === "admin" && currentUser?.role === "ADMIN" && (
                    <AdminPanel onRefresh={() => { loadUnits(); }} />
                )}
            </main>
            <footer className="footer" style={{ marginTop: 'auto' }}>© 2026 Lagerlyft</footer>
        </div>
    );
}