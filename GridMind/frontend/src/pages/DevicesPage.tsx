import { useState, useEffect } from 'react';
import { getDevices, createDevice } from '../services/api';

interface Device {
  id: number;
  name: string;
  type: string;
  powerRating: number;
  esp32Id: string;
  apiKey: string;
  createdAt: string;
}

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ name: '', type: 'OUTLET', powerRating: 1000, esp32Id: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchDevices = async () => {
    try {
      const res = await getDevices();
      setDevices(Array.isArray(res.data) ? res.data : []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchDevices(); }, []);

  const handleCreate = async () => {
    setSubmitting(true);
    try {
      await createDevice(form);
      setShowModal(false);
      setForm({ name: '', type: 'OUTLET', powerRating: 1000, esp32Id: '' });
      await fetchDevices();
    } catch (err) { console.error(err); }
    finally { setSubmitting(false); }
  };

  if (loading) return <div className="loading-spinner"><div className="spinner" /></div>;

  return (
    <>
      <div className="page-header">
        <h2>Dispositivos</h2>
        <p>Gestiona tus enchufes y sensores inteligentes</p>
      </div>

      <div style={{ marginBottom: 24 }}>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          ➕ Nuevo Dispositivo
        </button>
      </div>

      {devices.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">🔌</div>
            <h3>No tienes dispositivos registrados</h3>
            <p>Agrega tu primer enchufe inteligente para empezar a monitorear.</p>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Tipo</th>
                  <th>Potencia (W)</th>
                  <th>ESP32 ID</th>
                  <th>API Key</th>
                </tr>
              </thead>
              <tbody>
                {devices.map(d => (
                  <tr key={d.id}>
                    <td style={{ fontWeight: 600 }}>{d.name}</td>
                    <td>{d.type}</td>
                    <td>{d.powerRating}W</td>
                    <td style={{ color: 'var(--accent-blue)' }}>{d.esp32Id}</td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontFamily: 'monospace' }}>
                      {d.apiKey?.substring(0, 16)}...
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Nuevo Dispositivo</h3>
            <div className="form-group">
              <label className="form-label">Nombre</label>
              <input className="form-input" placeholder="Ej: Enchufe Sala" value={form.name} onChange={e => setForm({...form, name: e.target.value})} />
            </div>
            <div className="form-group">
              <label className="form-label">Tipo</label>
              <select className="form-input" value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                <option value="OUTLET">Enchufe</option>
                <option value="LIGHT">Bombilla</option>
                <option value="SENSOR">Sensor</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Potencia (Watts)</label>
              <input type="number" className="form-input" value={form.powerRating} onChange={e => setForm({...form, powerRating: Number(e.target.value)})} />
            </div>
            <div className="form-group">
              <label className="form-label">ESP32 ID</label>
              <input className="form-input" placeholder="Ej: ESP32-001" value={form.esp32Id} onChange={e => setForm({...form, esp32Id: e.target.value})} />
            </div>
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleCreate} disabled={submitting}>
                {submitting ? '⏳ Creando...' : '✅ Crear'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
