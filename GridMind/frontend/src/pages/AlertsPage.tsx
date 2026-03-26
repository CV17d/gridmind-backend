import { useState, useEffect } from 'react';
import { getAlerts, markAlertAsRead } from '../services/api';

interface Alert {
  id: number;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchAlerts = async () => {
    try {
      const res = await getAlerts();
      setAlerts(res.data || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchAlerts(); }, []);

  const handleMarkRead = async (id: number) => {
    await markAlertAsRead(id);
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, read: true } : a));
  };

  if (loading) return <div className="loading-spinner"><div className="spinner" /></div>;

  return (
    <>
      <div className="page-header">
        <h2>Alertas</h2>
        <p>Notificaciones inteligentes sobre tu consumo energético</p>
      </div>

      {alerts.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <div className="empty-icon">🔔</div>
            <h3>No tienes alertas</h3>
            <p>Las alertas aparecerán aquí cuando se detecte un consumo inusual.</p>
          </div>
        </div>
      ) : (
        <div className="card" style={{ padding: 0 }}>
          {alerts.map(alert => (
            <div key={alert.id} className={`alert-item ${!alert.read ? 'unread' : ''}`}>
              <span className="alert-icon">{alert.read ? '🔕' : '🚨'}</span>
              <div className="alert-content">
                <p className="alert-msg">{alert.message}</p>
                <span className="alert-time">
                  {new Date(alert.createdAt).toLocaleString()}
                </span>
              </div>
              {!alert.read && (
                <button className="btn btn-secondary" style={{ flexShrink: 0, padding: '8px 16px', fontSize: '0.8rem' }} onClick={() => handleMarkRead(alert.id)}>
                  ✅ Marcar leída
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  );
}
