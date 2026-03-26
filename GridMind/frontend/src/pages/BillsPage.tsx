import { useState, useRef } from 'react';
import { uploadBill, getMyBills } from '../services/api';
import { useEffect } from 'react';

interface Bill {
  id: number;
  fileUrl: string;
  totalKwh: number;
  totalAmount: number;
  aiRecommendations: string;
  uploadedAt: string;
}

export default function BillsPage() {
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<Bill | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    getMyBills().then(res => setBills(res.data || [])).catch(console.error).finally(() => setLoading(false));
  }, []);

  const handleUpload = async () => {
    if (!selectedFile) return;
    setUploading(true);
    setResult(null);
    try {
      const res = await uploadBill(selectedFile);
      setResult(res.data);
      setSelectedFile(null);
      const billsRes = await getMyBills();
      setBills(billsRes.data || []);
    } catch (err) { console.error(err); }
    finally { setUploading(false); }
  };

  if (loading) return <div className="loading-spinner"><div className="spinner" /></div>;

  return (
    <>
      <div className="page-header">
        <h2>Asesor de Facturas</h2>
        <p>Sube una foto de tu factura de luz y la IA te dará recomendaciones</p>
      </div>

      <div className="card" style={{ marginBottom: 24 }}>
        <div
          className="upload-zone"
          onClick={() => fileInputRef.current?.click()}
        >
          <div className="upload-icon">📸</div>
          <p>{selectedFile ? `📄 ${selectedFile.name}` : 'Haz clic para seleccionar la foto de tu factura'}</p>
          <p style={{ fontSize: '0.8rem', marginTop: 8, color: 'var(--text-muted)' }}>JPG, PNG o PDF</p>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*,.pdf"
          style={{ display: 'none' }}
          onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
        />
        {selectedFile && (
          <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
            <button className="btn btn-primary" onClick={handleUpload} disabled={uploading}>
              {uploading ? '🤖 La IA está analizando tu factura...' : '🧠 Analizar con IA'}
            </button>
            <button className="btn btn-secondary" onClick={() => setSelectedFile(null)}>Cancelar</button>
          </div>
        )}
      </div>

      {result && (
        <div className="bill-result">
          <h3>🤖 Resultados del Análisis</h3>
          <div className="stat-grid">
            <div className="stat-card green">
              <div className="stat-label">Consumo Detectado</div>
              <div className="stat-value">{result.totalKwh} kWh</div>
            </div>
            <div className="stat-card blue">
              <div className="stat-label">Monto de la Factura</div>
              <div className="stat-value">${result.totalAmount?.toLocaleString()}</div>
            </div>
          </div>
          <div className="advice">
            <strong>💡 Consejo del Asesor GridMind:</strong>
            <p style={{ marginTop: 8 }}>{result.aiRecommendations}</p>
          </div>
        </div>
      )}

      {bills.length > 0 && (
        <div className="card" style={{ marginTop: 24 }}>
          <div className="card-header">
            <span className="card-title">📋 Historial de Facturas</span>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>kWh</th>
                  <th>Monto</th>
                  <th>Recomendación</th>
                </tr>
              </thead>
              <tbody>
                {bills.map(b => (
                  <tr key={b.id}>
                    <td>{new Date(b.uploadedAt).toLocaleDateString()}</td>
                    <td style={{ color: 'var(--accent-green)', fontWeight: 600 }}>{b.totalKwh} kWh</td>
                    <td style={{ color: 'var(--accent-blue)', fontWeight: 600 }}>${b.totalAmount?.toLocaleString()}</td>
                    <td style={{ maxWidth: 300, color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                      {b.aiRecommendations?.substring(0, 100)}...
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  );
}
