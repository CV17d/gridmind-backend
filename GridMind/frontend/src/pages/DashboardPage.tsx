import { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { getDailyAnalytics, getUnreadCount, getDevices } from '../services/api';
import { Client } from '@stomp/stompjs';

interface DailyData { date: string; totalKwh: number; }
interface LiveReading { esp32Id: string; consumption: number; timestamp: string; }

export default function DashboardPage() {
  const [chartData, setChartData] = useState<DailyData[]>([]);
  const [unread, setUnread] = useState(0);
  const [deviceCount, setDeviceCount] = useState(0);
  const [liveReadings, setLiveReadings] = useState<LiveReading[]>([]);
  const [isLive, setIsLive] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [analyticsRes, unreadRes, devicesRes] = await Promise.all([
          getDailyAnalytics(),
          getUnreadCount(),
          getDevices()
        ]);
        setChartData(analyticsRes.data || []);
        setUnread(unreadRes.data?.unreadAlerts || 0);
        setDeviceCount(Array.isArray(devicesRes.data) ? devicesRes.data.length : 0);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // WebSocket real-time connection
  useEffect(() => {
    const apiUrl = import.meta.env.VITE_API_URL || 'https://gridmind-backend.onrender.com';
    const wsUrl = apiUrl.replace('https://', 'wss://').replace('http://', 'ws://') + '/ws';
    
    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        setIsLive(true);
        client.subscribe('/topic/energy', (message) => {
          const data = JSON.parse(message.body) as LiveReading;
          setLiveReadings(prev => [data, ...prev].slice(0, 10));
        });
      },
      onDisconnect: () => setIsLive(false),
      onStompError: () => setIsLive(false),
    });

    client.activate();
    return () => { client.deactivate(); };
  }, []);

  const totalKwh = chartData.reduce((sum, d) => sum + (d.totalKwh || 0), 0);

  if (loading) return <div className="loading-spinner"><div className="spinner" /></div>;

  return (
    <>
      <div className="page-header">
        <h2>Dashboard</h2>
        <p>Resumen de tu consumo energético en tiempo real</p>
      </div>

      <div className="stat-grid">
        <div className="stat-card green">
          <div className="stat-label">Consumo Total</div>
          <div className="stat-value">{totalKwh.toFixed(1)} kWh</div>
        </div>
        <div className="stat-card blue">
          <div className="stat-label">Dispositivos</div>
          <div className="stat-value">{deviceCount}</div>
        </div>
        <div className="stat-card purple">
          <div className="stat-label">Días Registrados</div>
          <div className="stat-value">{chartData.length}</div>
        </div>
        <div className="stat-card red">
          <div className="stat-label">Alertas Sin Leer</div>
          <div className="stat-value">{unread}</div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 24 }}>
        <div className="card-header">
          <span className="card-title">📊 Consumo Diario (kWh)</span>
          {isLive && <span className="live-indicator"><span className="live-dot" /> EN VIVO</span>}
        </div>
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="colorKwh" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#00ff88" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#00ff88" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
            <XAxis dataKey="date" stroke="#64748b" fontSize={12} />
            <YAxis stroke="#64748b" fontSize={12} />
            <Tooltip
              contentStyle={{ background: '#1a2234', border: '1px solid #1e293b', borderRadius: 8 }}
              labelStyle={{ color: '#94a3b8' }}
              itemStyle={{ color: '#00ff88' }}
            />
            <Area type="monotone" dataKey="totalKwh" stroke="#00ff88" strokeWidth={2} fill="url(#colorKwh)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="card">
        <div className="card-header">
          <span className="card-title">📡 Lecturas en Tiempo Real</span>
          {isLive && <span className="live-indicator"><span className="live-dot" /> Conectado</span>}
        </div>
        {liveReadings.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📡</div>
            <h3>Esperando datos del ESP32...</h3>
            <p>Las lecturas aparecerán aquí en tiempo real cuando tus dispositivos envíen datos.</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Dispositivo</th>
                  <th>Consumo</th>
                  <th>Hora</th>
                </tr>
              </thead>
              <tbody>
                {liveReadings.map((r, i) => (
                  <tr key={i}>
                    <td>{r.esp32Id}</td>
                    <td style={{ color: 'var(--accent-green)', fontWeight: 600 }}>{r.consumption} kWh</td>
                    <td style={{ color: 'var(--text-muted)' }}>{new Date(r.timestamp).toLocaleTimeString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
