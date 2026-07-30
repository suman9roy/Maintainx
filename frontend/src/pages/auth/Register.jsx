import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Register() {
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '', email: '', password: '', aadharNumber: '',
  });
  const [error,   setError]   = useState('');
  const [success, setSuccess] = useState('');

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Basic Aadhaar format check before sending
    if (!/^\d{12}$/.test(form.aadharNumber)) {
      setError('Aadhaar number must be exactly 12 digits');
      return;
    }

    try {
      const res = await register(form);
      setSuccess(`Account created! Please verify your email with the OTP sent to ${form.email}.`);
      setTimeout(() => navigate('/verify-email', { state: { email: form.email } }), 1200);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Create Account</h1>
        <p style={styles.subtitle}>Register as a society resident</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Full Name</label>
          <input
            name="name" required
            value={form.name} onChange={handleChange}
            style={styles.input} placeholder="Rahul Sharma"
          />

          <label style={styles.label}>Email</label>
          <input
            name="email" type="email" required
            value={form.email} onChange={handleChange}
            style={styles.input} placeholder="rahul@example.com"
          />

          <label style={styles.label}>Password</label>
          <input
            name="password" type="password" required minLength={8}
            value={form.password} onChange={handleChange}
            style={styles.input} placeholder="Min 8 characters"
          />

          <label style={styles.label}>Aadhaar Number</label>
          <input
            name="aadharNumber" required maxLength={12}
            value={form.aadharNumber} onChange={handleChange}
            style={styles.input} placeholder="12-digit Aadhaar number"
          />
          <p style={styles.hint}>
            Your Aadhaar is used by the admin to verify your flat ownership documents.
          </p>

          {error   && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.success}>{success}</p>}

          <button type="submit" disabled={loading} style={styles.btn}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p style={styles.footer}>
          Already registered?{' '}
          <Link to="/login" style={styles.link}>Sign in</Link>
          {' '}·{' '}
          <Link to="/verify-email" style={styles.link}>Verify email</Link>
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
    width: '100%', maxWidth: 420,
    boxShadow: '0 2px 16px rgba(0,0,0,0.08)',
  },
  title:    { margin: 0, fontSize: 'clamp(20px, 5vw, 26px)', fontWeight: 700, color: '#1a1a1a' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 'clamp(16px, 3vw, 24px)', fontSize: 'clamp(13px, 2vw, 14px)' },
  form:     { display: 'flex', flexDirection: 'column', gap: 6 },
  label:    { fontSize: 'clamp(12px, 2vw, 13px)', fontWeight: 500, color: '#444' },
  input:    {
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)', borderRadius: 8, fontSize: 'clamp(14px, 2vw, 16px)',
    border: '1.5px solid #ddd', outline: 'none', marginBottom: 6,
    width: '100%',
    boxSizing: 'border-box',
  },
  hint:    { fontSize: 'clamp(11px, 1.5vw, 12px)', color: '#888', margin: '-4px 0 8px' },
  btn: {
    marginTop: 10, padding: 'clamp(9px, 2vw, 11px) 0', borderRadius: 8,
    background: '#2563eb', color: '#fff', fontWeight: 600,
    fontSize: 'clamp(13px, 2vw, 15px)', border: 'none', cursor: 'pointer',
  },
  error:   { color: '#dc2626', fontSize: 'clamp(12px, 2vw, 13px)' },
  success: { color: '#16a34a', fontSize: 'clamp(12px, 2vw, 13px)' },
  footer:  { textAlign: 'center', marginTop: 20, fontSize: 'clamp(12px, 2vw, 14px)', color: '#555' },
  link:    { color: '#2563eb', textDecoration: 'none', fontWeight: 500 },
};
