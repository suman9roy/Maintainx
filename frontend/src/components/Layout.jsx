import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import './Layout.css';

// Resident sidebar links
const residentLinks = [
  { to: '/dashboard',     label: '🏠 Dashboard' },
  { to: '/my-bills',      label: '💰 My Bills' },
  { to: '/join-request',  label: '📋 Join Request' },
  { to: '/my-requests',   label: '📬 My Requests' },
  { to: '/my-complaints', label: '📢 Complaints' },
  { to: '/expenses',      label: '📊 Expenses' },
  { to: '/notices',       label: '📌 Notices' },
];

// Admin sidebar links
const adminLinks = [
  { to: '/admin',                  label: '🏠 Dashboard' },
  { to: '/admin/join-requests',    label: '📋 Join Requests' },
  { to: '/admin/residents',        label: '👥 Residents' },
  { to: '/admin/bills',            label: '💰 Billing' },
  { to: '/admin/complaints',       label: '📢 Complaints' },
  { to: '/admin/expenses',         label: '📊 Expenses' },
  { to: '/admin/notices',          label: '📌 Notices' },
];

export default function Layout({ children }) {
  const { role, user, logout } = useAuth();
  const navigate = useNavigate();
  const links = role === 'ADMIN' ? adminLinks : residentLinks;
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLinkClick = () => {
    setSidebarOpen(false);
  };

  return (
    <div className="layout-shell">
      {/* ── Mobile Hamburger ───────────────────────────────────────── */}
      <div className="mobile-header">
        <button className="hamburger" onClick={() => setSidebarOpen(!sidebarOpen)}>
          ☰
        </button>
        <span className="mobile-title">MaintainX</span>
      </div>

      {/* ── Sidebar ───────────────────────────────────────────────── */}
      <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : 'sidebar-closed'}`}>
        <div className="close-btn-container">
          <button className="close-btn" onClick={() => setSidebarOpen(false)}>✕</button>
        </div>

        <div className="brand" onClick={() => { navigate(role === 'ADMIN' ? '/admin' : '/dashboard'); handleLinkClick(); }}>
          <span className="brand-icon">🏢</span>
          <span className="brand-name">MaintainX</span>
        </div>

        <div className="role-tag">
          {role === 'ADMIN' ? '⚡ Admin' : '👤 Resident'}
        </div>

        <nav className="nav">
          {links.map(link => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/admin' || link.to === '/dashboard'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              onClick={handleLinkClick}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-email" title={user?.email}>{user?.email ?? '—'}</div>
          <button className="logout-btn" onClick={logout}>Sign out</button>
        </div>
      </aside>

      {/* ── Main content ──────────────────────────────────────────── */}
      <main className="main">
        {children}
      </main>
    </div>
  );
}
