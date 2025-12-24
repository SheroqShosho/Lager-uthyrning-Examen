import { useEffect, useState } from "react";

export default function App() {
    const [units, setUnits] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        fetch("http://localhost:8080/api/storage-units")
            .then((res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return res.json();
            })
            .then(setUnits)
            .catch((e) => setError(e.message));
    }, []);

    return (
        <div style={{ padding: 16, fontFamily: "system-ui, Arial" }}>
            <h1>Storage Units</h1>

            {error && <p style={{ color: "red" }}>Error: {error}</p>}

            {!error && units.length === 0 && <p>No units yet.</p>}

            {units.length > 0 && (
                <ul>
                    {units.map((u) => (
                        <li key={u.id}>
                            <b>{u.name}</b> — {u.location} — {u.pricePerDay} / day
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
