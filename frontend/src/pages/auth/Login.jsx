import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login, loading } = useAuth();
  const navigate = useNavigate();

  const [form, setForm]   = useState({ email: '', password: '' });
  const [error, setError] = useState('');

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      const user = await login(form.email, form.password);
      // Redirect based on role after successful login
      if (user?.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>MaintainX</h1>
        <p style={styles.subtitle}>Society Management Portal</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input
            name="email" type="email" required
            value={form.email} onChange={handleChange}
            style={styles.input} placeholder="you@example.com"
          />

          <label style={styles.label}>Password</label>
          <input
            name="password" type="password" required
            value={form.password} onChange={handleChange}
            style={styles.input} placeholder="••••••••"
          />

          {error && <p style={styles.error}>{error}</p>}

          <button type="submit" disabled={loading} style={styles.btn}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p style={styles.footer}>
          New resident?{' '}
          <Link to="/register" style={styles.link}>Create account</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    background: '#f5f5f5',
    padding: 'clamp(1rem, 3vw, 2rem)',
  },
  card: {
    background: '#fff', borderRadius: 12,
    padding: 'clamp(1.5rem, 4vw, 2.5rem)', 
    width: '100%', maxWidth: 400,
    boxShadow: '0 2px 16px rgba(0,0,0,0.08)',
  },
  title:    { margin: 0, fontSize: 'clamp(20px, 5vw, 28px)', fontWeight: 700, color: '#1a1a1a' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 'clamp(16px, 3vw, 28px)', fontSize: 'clamp(13px, 2vw, 14px)' },
  form:     { display: 'flex', flexDirection: 'column', gap: 8 },
  label:    { fontSize: 'clamp(12px, 2vw, 13px)', fontWeight: 500, color: '#444' },
  input:    {
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)', borderRadius: 8, fontSize: 'clamp(14px, 2vw, 16px)',
    border: '1.5px solid #ddd', outline: 'none',
    marginBottom: 8,
    width: '100%',
    boxSizing: 'border-box',
  },
  btn: {
    marginTop: 8, padding: 'clamp(9px, 2vw, 11px) 0', borderRadius: 8,
    background: '#2563eb', color: '#fff', fontWeight: 600,
    fontSize: 'clamp(13px, 2vw, 15px)', border: 'none', cursor: 'pointer',
    transition: 'background 0.2s ease',
  },
  error: {
    backgroundColor: '#fee2e2',
    border: '1px solid #fecaca',
    color: '#dc2626',
    fontSize: 'clamp(12px, 2vw, 13px)',
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)',
    borderRadius: 6,
    margin: '8px 0',
    fontWeight: 500,
  },
  footer: { textAlign: 'center', marginTop: 20, fontSize: 'clamp(12px, 2vw, 14px)', color: '#555' },
  link:   { color: '#2563eb', textDecoration: 'none', fontWeight: 500 },
};
