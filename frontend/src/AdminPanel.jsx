import { useState } from "react";
import { createStorageUnit } from "./api";

export function AdminPanel({ onRefresh }) {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [sizeM2, setSizeM2] = useState("");
    const [pricePerDay, setPricePerDay] = useState("");
    const [location, setLocation] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage("");
        setError("");

        try {
            const newUnit = {
                name: name.trim(),
                description: description.trim(),
                sizeM2: parseFloat(sizeM2),
                pricePerDay: parseFloat(pricePerDay),
                location: location.trim(),
                active: true,
            };

            const response = await createStorageUnit(newUnit);
            setMessage(response.message + ` (ID: ${response.id})`);
            
            setName("");
            setDescription("");
            setSizeM2("");
            setPricePerDay("");
            setLocation("");

            if (onRefresh) onRefresh();
            
            setTimeout(() => setMessage(""), 3000);
        } catch (e) {
            setError(e.message || "Fel vid skapande av lagerutrymme");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="section">
            <h2>➕ Skapa Nytt Lagerutrymme</h2>
            <div style={{background: 'white', padding: '2rem', borderRadius: '12px', border: '1px solid #eee'}}>
                <form onSubmit={handleSubmit}>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{fontSize:'0.75rem', fontWeight: 'bold', display: 'block', marginBottom: '5px'}}>NAMN</label>
                        <input 
                            type="text" 
                            value={name} 
                            onChange={(e) => setName(e.target.value)}
                            placeholder="T.ex. Lager Alpha"
                            required
                            style={{width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd'}}
                        />
                    </div>

                    <div style={{marginBottom: '15px'}}>
                        <label style={{fontSize:'0.75rem', fontWeight: 'bold', display: 'block', marginBottom: '5px'}}>BESKRIVNING</label>
                        <textarea 
                            value={description} 
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="T.ex. Perfect för flyttlådor"
                            rows="3"
                            style={{width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd'}}
                        />
                    </div>

                    <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px'}}>
                        <div>
                            <label style={{fontSize:'0.75rem', fontWeight: 'bold', display: 'block', marginBottom: '5px'}}>STORLEK (m²)</label>
                            <input 
                                type="number" 
                                step="0.01"
                                value={sizeM2} 
                                onChange={(e) => setSizeM2(e.target.value)}
                                placeholder="5.00"
                                required
                                style={{width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd'}}
                            />
                        </div>
                        <div>
                            <label style={{fontSize:'0.75rem', fontWeight: 'bold', display: 'block', marginBottom: '5px'}}>PRIS/DAG (kr)</label>
                            <input 
                                type="number" 
                                step="0.01"
                                value={pricePerDay} 
                                onChange={(e) => setPricePerDay(e.target.value)}
                                placeholder="49.00"
                                required
                                style={{width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd'}}
                            />
                        </div>
                    </div>

                    <div style={{marginBottom: '15px'}}>
                        <label style={{fontSize:'0.75rem', fontWeight: 'bold', display: 'block', marginBottom: '5px'}}>PLATS/SEKTION</label>
                        <input 
                            type="text" 
                            value={location} 
                            onChange={(e) => setLocation(e.target.value)}
                            placeholder="T.ex. Sektion A"
                            required
                            style={{width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #ddd'}}
                        />
                    </div>

                    {message && <p style={{color: '#28a745', marginBottom: '10px'}}>✅ {message}</p>}
                    {error && <p style={{color: '#ff4d4d', marginBottom: '10px'}}>❌ {error}</p>}

                    <button 
                        type="submit"
                        disabled={loading}
                        className="btn-add-new"
                        style={{width: '100%'}}
                    >
                        {loading ? "Skapar..." : "✅ Skapa Lagerutrymme"}
                    </button>
                </form>
            </div>
        </section>
    );
}

