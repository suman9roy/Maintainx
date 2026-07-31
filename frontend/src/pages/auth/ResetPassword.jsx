import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { resetPassword } from '../../api/auth';

function ResetPassword() {
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({
    email: location.state?.email || '',
    otp: '',
    newPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  function handleChange(e) {
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === 'otp' ? value.replace(/\D/g, '') : value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await resetPassword(form.email, form.otp, form.newPassword);
      setSuccess('Password reset! Redirecting to login…');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Reset failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Reset Password</h1>
        <p style={styles.subtitle}>Enter the OTP sent to your email and choose a new password.</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input
            name="email" type="email" required
            value={form.email} onChange={handleChange}
            style={styles.input} placeholder="you@example.com"
          />

          <label style={styles.label}>OTP</label>
          <input
            name="otp" required maxLength={6} inputMode="numeric" pattern="\d{6}"
            value={form.otp} onChange={handleChange}
            style={{ ...styles.input, letterSpacing: 4, textAlign: 'center', fontSize: 20 }}
            placeholder="000000"
          />

          <label style={styles.label}>New Password</label>
          <input
            name="newPassword" type="password" required minLength={8}
            value={form.newPassword} onChange={handleChange}
            style={styles.input} placeholder="Min 8 characters"
          />

          {error   && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.success}>{success}</p>}

          <button type="submit" disabled={loading} style={styles.btn}>
            {loading ? 'Resetting…' : 'Reset Password'}
          </button>
        </form>

        <p style={styles.footer}>
          <Link to="/forgot-password" style={styles.link}>Didn't get an OTP? Request again</Link>
        </p>
        <p style={styles.footer}>
          <Link to="/login" style={styles.link}>Back to login</Link>
        </p>
      </div>
    </div>
  );
}

export default ResetPassword;

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
  title:    { margin: 0, fontSize: 'clamp(20px, 5vw, 26px)', fontWeight: 700, color: '#1a1a1a' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 'clamp(16px, 3vw, 24px)', fontSize: 'clamp(13px, 2vw, 14px)' },
  form:     { display: 'flex', flexDirection: 'column', gap: 8 },
  label:    { fontSize: 'clamp(12px, 2vw, 13px)', fontWeight: 500, color: '#444' },
  input:    {
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)', borderRadius: 8, fontSize: 'clamp(14px, 2vw, 16px)',
    border: '1.5px solid #ddd', outline: 'none', marginBottom: 8,
    width: '100%', boxSizing: 'border-box',
  },
  btn: {
    marginTop: 8, padding: 'clamp(9px, 2vw, 11px) 0', borderRadius: 8,
    background: '#2563eb', color: '#fff', fontWeight: 600,
    fontSize: 'clamp(13px, 2vw, 15px)', border: 'none', cursor: 'pointer',
  },
  error:   {
    backgroundColor: '#fee2e2', border: '1px solid #fecaca', color: '#dc2626',
    fontSize: 'clamp(12px, 2vw, 13px)', padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)',
    borderRadius: 6, margin: '8px 0', fontWeight: 500,
  },
  success: {
    backgroundColor: '#dcfce7', border: '1px solid #bbf7d0', color: '#16a34a',
    fontSize: 'clamp(12px, 2vw, 13px)', padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)',
    borderRadius: 6, margin: '8px 0', fontWeight: 500,
  },
  footer:  { textAlign: 'center', marginTop: 12, fontSize: 'clamp(12px, 2vw, 14px)', color: '#555' },
  link:    { color: '#2563eb', textDecoration: 'none', fontWeight: 500 },
};
