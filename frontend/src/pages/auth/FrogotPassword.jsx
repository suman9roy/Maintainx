import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { forgotPassword } from '../../api/auth';

function ForgotPassword() {
  const navigate = useNavigate();

  const [email, setEmail]     = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await forgotPassword(email);
      setSuccess('If an account exists for this email, a reset OTP has been sent.');
      setTimeout(() => navigate('/reset-password', { state: { email } }), 1500);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Something went wrong';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Forgot Password</h1>
        <p style={styles.subtitle}>Enter your email and we'll send you a reset OTP.</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input
            type="email" required
            value={email} onChange={(e) => setEmail(e.target.value)}
            style={styles.input} placeholder="you@example.com"
          />

          {error   && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.success}>{success}</p>}

          <button type="submit" disabled={loading} style={styles.btn}>
            {loading ? 'Sending…' : 'Send Reset OTP'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an OTP?{' '}
          <Link to="/reset-password" style={styles.link}>Reset password</Link>
        </p>
        <p style={styles.footer}>
          <Link to="/login" style={styles.link}>Back to login</Link>
        </p>
      </div>
    </div>
  );
}

export default ForgotPassword;

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
