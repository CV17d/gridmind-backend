import { BrowserRouter, Routes, Route, Navigate, NavLink, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { useEffect, useState } from 'react';
import { getUnreadCount } from './services/api';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import DevicesPage from './pages/DevicesPage';
import BillsPage from './pages/BillsPage';
import AlertsPage from './pages/AlertsPage';
import './index.css';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function AppLayout() {
  const { logout, userEmail } = useAuth();
  const location = useLocation();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    getUnreadCount()
      .then(res => setUnread(res.data?.unreadAlerts || 0))
      .catch(() => {});
  }, [location.pathname]);

  const navItems = [
    { to: '/', icon: '📊', label: 'Dashboard' },
    { to: '/devices', icon: '🔌', label: 'Dispositivos' },
    { to: '/bills', icon: '🧾', label: 'Facturas IA' },
    { to: '/alerts', icon: '🔔', label: 'Alertas', badge: unread },
  ];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="logo-icon">⚡</span>
          <h1>GridMind</h1>
        </div>
        <nav className="sidebar-nav">
          {navItems.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
              {item.badge ? <span className="badge">{item.badge}</span> : null}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: 12, padding: '0 4px' }}>
            {userEmail}
          </p>
          <button className="btn-logout" onClick={logout}>🚪 Cerrar Sesión</button>
        </div>
      </aside>
      <main className="main-content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/devices" element={<DevicesPage />} />
          <Route path="/bills" element={<BillsPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
