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
      setSuccess(`Account created! Your user ID: ${res.userId}. Please log in.`);
      setTimeout(() => navigate('/login'), 3000);
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
  },
  card: {
    background: '#fff', borderRadius: 12,
    padding: '2.5rem 2rem', width: '100%', maxWidth: 420,
    boxShadow: '0 2px 16px rgba(0,0,0,0.08)',
  },
  title:    { margin: 0, fontSize: 26, fontWeight: 700, color: '#1a1a1a' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 24 },
  form:     { display: 'flex', flexDirection: 'column', gap: 6 },
  label:    { fontSize: 13, fontWeight: 500, color: '#444' },
  input:    {
    padding: '10px 12px', borderRadius: 8, fontSize: 14,
    border: '1.5px solid #ddd', outline: 'none', marginBottom: 6,
  },
  hint:    { fontSize: 12, color: '#888', margin: '-4px 0 8px' },
  btn: {
    marginTop: 10, padding: '11px 0', borderRadius: 8,
    background: '#2563eb', color: '#fff', fontWeight: 600,
    fontSize: 15, border: 'none', cursor: 'pointer',
  },
  error:   { color: '#dc2626', fontSize: 13 },
  success: { color: '#16a34a', fontSize: 13 },
  footer:  { textAlign: 'center', marginTop: 20, fontSize: 14, color: '#555' },
  link:    { color: '#2563eb', textDecoration: 'none', fontWeight: 500 },
};
