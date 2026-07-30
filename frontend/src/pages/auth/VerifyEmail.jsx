import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { verifyEmail, resendOtp } from '../../api/auth';

export default function VerifyEmail() {
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState(location.state?.email || '');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  function extractMessage(err, fallback) {
    return err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || fallback;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await verifyEmail(email, otp);
      setSuccess('Email verified! Redirecting to login…');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(extractMessage(err, 'Verification failed'));
    } finally {
      setLoading(false);
    }
  }

  async function handleResend() {
    setError('');
    setSuccess('');

    if (!email) {
      setError('Enter your email first');
      return;
    }

    setResending(true);
    try {
      await resendOtp(email);
      setSuccess('A new OTP has been sent to your email.');
    } catch (err) {
      setError(extractMessage(err, 'Could not resend OTP'));
    } finally {
      setResending(false);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Verify Your Email</h1>
        <p style={styles.subtitle}>
          We have sent a 6-digit OTP to your email. It is valid for 10 minutes.
        </p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input}
            placeholder="you@example.com"
          />

          <label style={styles.label}>OTP</label>
          <input
            required
            maxLength={6}
            inputMode="numeric"
            pattern="\d{6}"
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
            style={{ ...styles.input, letterSpacing: 4, textAlign: 'center', fontSize: 20 }}
            placeholder="000000"
          />

          {error && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.success}>{success}</p>}

          <button type="submit" disabled={loading} style={styles.btn}>
            {loading ? 'Verifying…' : 'Verify Email'}
          </button>
        </form>

        <p style={styles.footer}>
          Didn&apos;t get an OTP?{' '}
          <button type="button" onClick={handleResend} disabled={resending} style={styles.linkBtn}>
            {resending ? 'Sending…' : 'Resend OTP'}
          </button>
        </p>
        <p style={styles.footer}>
          <Link to="/login" style={styles.link}>Back to login</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f5f5f5',
    padding: 'clamp(1rem, 3vw, 2rem)',
  },
  card: {
    background: '#fff',
    borderRadius: 12,
    padding: 'clamp(1.5rem, 4vw, 2.5rem)',
    width: '100%',
    maxWidth: 400,
    boxShadow: '0 2px 16px rgba(0,0,0,0.08)',
  },
  title: { margin: 0, fontSize: 'clamp(20px, 5vw, 26px)', fontWeight: 700, color: '#1a1a1a' },
  subtitle: { color: '#666', marginTop: 4, marginBottom: 'clamp(16px, 3vw, 24px)', fontSize: 'clamp(13px, 2vw, 14px)' },
  form: { display: 'flex', flexDirection: 'column', gap: 8 },
  label: { fontSize: 'clamp(12px, 2vw, 13px)', fontWeight: 500, color: '#444' },
  input: {
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)',
    borderRadius: 8,
    fontSize: 'clamp(14px, 2vw, 16px)',
    border: '1.5px solid #ddd',
    outline: 'none',
    marginBottom: 8,
    width: '100%',
    boxSizing: 'border-box',
  },
  btn: {
    marginTop: 8,
    padding: 'clamp(9px, 2vw, 11px) 0',
    borderRadius: 8,
    background: '#2563eb',
    color: '#fff',
    fontWeight: 600,
    fontSize: 'clamp(13px, 2vw, 15px)',
    border: 'none',
    cursor: 'pointer',
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
  success: {
    backgroundColor: '#dcfce7',
    border: '1px solid #bbf7d0',
    color: '#16a34a',
    fontSize: 'clamp(12px, 2vw, 13px)',
    padding: 'clamp(8px, 2vw, 10px) clamp(10px, 2vw, 12px)',
    borderRadius: 6,
    margin: '8px 0',
    fontWeight: 500,
  },
  footer: { textAlign: 'center', marginTop: 12, fontSize: 'clamp(12px, 2vw, 14px)', color: '#555' },
  link: { color: '#2563eb', textDecoration: 'none', fontWeight: 500 },
  linkBtn: {
    background: 'none',
    border: 'none',
    color: '#2563eb',
    fontWeight: 500,
    cursor: 'pointer',
    fontSize: 'inherit',
    padding: 0,
    textDecoration: 'underline',
  },
};
