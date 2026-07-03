import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Home() {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
  }

  return (
    <div style={styles.page}>
      <nav style={styles.navbar}>
        <div style={styles.navContent}>
          <h1 style={styles.logo}>MaintainX</h1>
          <button onClick={handleLogout} style={styles.logoutBtn}>
            Logout
          </button>
        </div>
      </nav>

      <div style={styles.container}>
        <div style={styles.card}>
          <h2 style={styles.welcome}>Welcome back! 👋</h2>
          
          <div style={styles.userInfo}>
            <div style={styles.infoRow}>
              <span style={styles.label}>Email:</span>
              <span style={styles.value}>{user?.email || 'N/A'}</span>
            </div>
            
            <div style={styles.infoRow}>
              <span style={styles.label}>Role:</span>
              <span style={styles.badgeContainer}>
                <span style={{...styles.badge, ...getBadgeStyle(role)}}>
                  {role}
                </span>
              </span>
            </div>

            <div style={styles.infoRow}>
              <span style={styles.label}>User ID:</span>
              <span style={styles.value}>{user?.sub || 'N/A'}</span>
            </div>
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>Coming Soon</h3>
            <p style={styles.sectionText}>
              {role === 'ADMIN' 
                ? 'Admin features including resident management, billing, complaints, and more.' 
                : 'Resident features including bill payments, maintenance requests, notices, and more.'}
            </p>
          </div>

          <div style={styles.actionContainer}>
            <button 
              onClick={() => navigate('/login')} 
              style={styles.secondaryBtn}
            >
              Go to Login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function getBadgeStyle(role) {
  return {
    background: role === 'ADMIN' ? '#7c3aed' : '#2563eb',
  };
}

const styles = {
  page: {
    minHeight: '100vh',
    background: '#f5f5f5',
  },
  navbar: {
    background: '#fff',
    borderBottom: '1px solid #e5e7eb',
    position: 'sticky',
    top: 0,
    zIndex: 100,
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
  },
  navContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '1rem 2rem',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  logo: {
    margin: 0,
    fontSize: 24,
    fontWeight: 700,
    color: '#1a1a1a',
  },
  logoutBtn: {
    padding: '0.5rem 1rem',
    borderRadius: 6,
    background: '#ef4444',
    color: '#fff',
    fontWeight: 500,
    fontSize: 14,
    border: 'none',
    cursor: 'pointer',
    transition: 'background 0.2s',
  },
  container: {
    maxWidth: '800px',
    margin: '3rem auto',
    padding: '0 2rem',
  },
  card: {
    background: '#fff',
    borderRadius: 12,
    padding: '2.5rem',
    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
  },
  welcome: {
    margin: '0 0 2rem 0',
    fontSize: 28,
    fontWeight: 700,
    color: '#1a1a1a',
  },
  userInfo: {
    background: '#f9fafb',
    padding: '1.5rem',
    borderRadius: 8,
    marginBottom: '2rem',
    borderLeft: '4px solid #2563eb',
  },
  infoRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingBottom: '0.75rem',
    borderBottom: '1px solid #e5e7eb',
  },
  label: {
    fontWeight: 600,
    color: '#6b7280',
    fontSize: 14,
  },
  value: {
    color: '#1a1a1a',
    fontSize: 14,
    wordBreak: 'break-all',
    maxWidth: '60%',
    textAlign: 'right',
  },
  badgeContainer: {
    display: 'flex',
    alignItems: 'center',
  },
  badge: {
    padding: '0.25rem 0.75rem',
    borderRadius: 20,
    color: '#fff',
    fontWeight: 600,
    fontSize: 12,
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  section: {
    background: '#eff6ff',
    padding: '1.5rem',
    borderRadius: 8,
    marginBottom: '2rem',
    borderLeft: '4px solid #0ea5e9',
  },
  sectionTitle: {
    margin: '0 0 0.5rem 0',
    fontSize: 16,
    fontWeight: 600,
    color: '#1a1a1a',
  },
  sectionText: {
    margin: 0,
    fontSize: 14,
    color: '#6b7280',
    lineHeight: 1.5,
  },
  actionContainer: {
    display: 'flex',
    gap: '1rem',
    justifyContent: 'center',
  },
  secondaryBtn: {
    padding: '0.75rem 1.5rem',
    borderRadius: 8,
    background: '#e5e7eb',
    color: '#1a1a1a',
    fontWeight: 500,
    fontSize: 14,
    border: 'none',
    cursor: 'pointer',
    transition: 'background 0.2s',
  },
};
