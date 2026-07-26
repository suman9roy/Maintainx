import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

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

  return (
    <div style={s.shell}>

      {/* ── Sidebar ───────────────────────────────────────────────── */}
      <aside style={s.sidebar}>
        <div style={s.brand} onClick={() => navigate(role === 'ADMIN' ? '/admin' : '/dashboard')}>
          <span style={s.brandIcon}>🏢</span>
          <span style={s.brandName}>MaintainX</span>
        </div>

        <div style={s.roleTag}>
          {role === 'ADMIN' ? '⚡ Admin' : '👤 Resident'}
        </div>

        <nav style={s.nav}>
          {links.map(link => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/admin' || link.to === '/dashboard'}
              style={({ isActive }) => ({
                ...s.navLink,
                ...(isActive ? s.navLinkActive : {}),
              })}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div style={s.sidebarFooter}>
          <div style={s.userEmail} title={user?.email}>{user?.email ?? '—'}</div>
          <button style={s.logoutBtn} onClick={logout}>Sign out</button>
        </div>
      </aside>

      {/* ── Main content ──────────────────────────────────────────── */}
      <main style={s.main}>
        {children}
      </main>

    </div>
  );
}

const s = {
  shell: {
    display: 'flex', minHeight: '100vh',
    background: '#f8fafc', fontFamily: 'system-ui, sans-serif',
  },
  sidebar: {
    width: 220, flexShrink: 0,
    background: '#1e293b', display: 'flex',
    flexDirection: 'column', position: 'sticky',
    top: 0, height: '100vh', overflowY: 'auto',
  },
  brand: {
    display: 'flex', alignItems: 'center', gap: 10,
    padding: '1.4rem 1.2rem 1rem', cursor: 'pointer',
  },
  brandIcon: { fontSize: 22 },
  brandName: { fontWeight: 700, fontSize: 18, color: '#f1f5f9' },
  roleTag: {
    margin: '0 1rem 1rem', padding: '4px 10px',
    background: '#334155', borderRadius: 20,
    fontSize: 11, fontWeight: 600, color: '#94a3b8',
    textAlign: 'center', letterSpacing: '0.5px',
  },
  nav: { flex: 1, display: 'flex', flexDirection: 'column', padding: '0 0.6rem' },
  navLink: {
    display: 'block', padding: '9px 14px', borderRadius: 8,
    textDecoration: 'none', fontSize: 14, fontWeight: 500,
    color: '#94a3b8', marginBottom: 2, transition: 'all .15s',
  },
  navLinkActive: {
    background: '#2563eb', color: '#fff',
  },
  sidebarFooter: {
    padding: '1rem', borderTop: '1px solid #334155',
  },
  userEmail: {
    fontSize: 11, color: '#64748b', marginBottom: 8,
    overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
  },
  logoutBtn: {
    width: '100%', padding: '8px 0', borderRadius: 6,
    background: '#ef4444', color: '#fff', fontWeight: 600,
    fontSize: 13, border: 'none', cursor: 'pointer',
  },
  main: {
    flex: 1, padding: '2rem', overflowX: 'hidden', maxWidth: '100%',
  },
};
