import { useEffect, useState, useCallback } from 'react';
import Layout from '../../components/Layout';
import {
  listApartments,
  onboardApartment,
  suspendApartment,
  reactivateApartment,
} from '../../api/superAdmin';

const emptyForm = {
  apartmentName: '',
  address: '',
  city: '',
  adminName: '',
  adminEmail: '',
  adminPassword: '',
};

export default function SuperAdminApartments() {
  const [apartments, setApartments] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');
  const [actionId, setActionId]     = useState(null); // which row is loading

  // Onboard form
  const [showForm, setShowForm] = useState(false);
  const [form, setForm]         = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError]   = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    listApartments()
      .then(res => setApartments(res.data ?? []))
      .catch(() => setError('Failed to load apartments.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  function handleFormChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleOnboardSubmit(e) {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      await onboardApartment(form);
      setSuccess(`✅ "${form.apartmentName}" onboarded — admin account created for ${form.adminEmail}.`);
      setForm(emptyForm);
      setShowForm(false);
      load();
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to onboard apartment.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleToggleActive(apartment) {
    setActionId(apartment.id);
    setError(''); setSuccess('');
    try {
      if (apartment.active) {
        await suspendApartment(apartment.id);
        setSuccess(`Suspended "${apartment.name}".`);
      } else {
        await reactivateApartment(apartment.id);
        setSuccess(`Reactivated "${apartment.name}".`);
      }
      load();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Action failed.');
    } finally {
      setActionId(null);
    }
  }

  return (
    <Layout>
      <div style={styles.header}>
        <div>
          <h1 style={styles.title}>Apartments</h1>
          <p style={styles.subtitle}>Onboard new apartments and manage existing ones.</p>
        </div>
        <button
          style={styles.primaryBtn}
          onClick={() => { setShowForm(s => !s); setFormError(''); }}
        >
          {showForm ? 'Cancel' : '+ Onboard Apartment'}
        </button>
      </div>

      {success && <div style={styles.successBanner}>{success}</div>}
      {error && <div style={styles.errorBanner}>{error}</div>}

      {showForm && (
        <form onSubmit={handleOnboardSubmit} style={styles.formCard}>
          <h2 style={styles.formTitle}>New Apartment</h2>

          <div style={styles.formGrid}>
            <Field label="Apartment name" name="apartmentName" value={form.apartmentName} onChange={handleFormChange} required />
            <Field label="City" name="city" value={form.city} onChange={handleFormChange} />
            <Field label="Address" name="address" value={form.address} onChange={handleFormChange} fullWidth />
          </div>

          <h3 style={styles.formSubTitle}>First Admin Account</h3>
          <div style={styles.formGrid}>
            <Field label="Admin name" name="adminName" value={form.adminName} onChange={handleFormChange} required />
            <Field label="Admin email" name="adminEmail" type="email" value={form.adminEmail} onChange={handleFormChange} required />
            <Field label="Admin password" name="adminPassword" type="password" value={form.adminPassword} onChange={handleFormChange} required minLength={8} />
          </div>

          {formError && <p style={styles.formError}>{formError}</p>}

          <button type="submit" disabled={submitting} style={styles.primaryBtn}>
            {submitting ? 'Onboarding…' : 'Onboard Apartment'}
          </button>
        </form>
      )}

      <div style={styles.tableCard}>
        {loading ? (
          <p style={styles.emptyState}>Loading apartments…</p>
        ) : apartments.length === 0 ? (
          <p style={styles.emptyState}>No apartments onboarded yet.</p>
        ) : (
          <div className="table-scroll">
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Apartment ID</th>
                  <th style={styles.th}>Name</th>
                  <th style={styles.th}>City</th>
                  <th style={styles.th}>Status</th>
                  <th style={styles.th}>Onboarded</th>
                  <th style={styles.th}></th>
                </tr>
              </thead>
              <tbody>
                {apartments.map(a => {
                  const apartmentId = a.id ?? a.apartmentId ?? a.apartment_id;
                  return (
                    <tr key={apartmentId ?? a.name} style={styles.tr}>
                      <td style={styles.td}>
                        <span style={styles.idBadge}>{apartmentId ?? '—'}</span>
                      </td>
                      <td style={styles.td}>
                        <div style={{ fontWeight: 600 }}>{a.name}</div>
                        {a.address && <div style={styles.mutedSmall}>{a.address}</div>}
                      </td>
                    <td style={styles.td}>{a.city || '—'}</td>
                    <td style={styles.td}>
                      <span style={a.active ? styles.badgeActive : styles.badgeSuspended}>
                        {a.active ? 'Active' : 'Suspended'}
                      </span>
                    </td>
                    <td style={styles.td}>
                      {a.createdAt ? new Date(a.createdAt).toLocaleDateString() : '—'}
                    </td>
                      <td style={styles.td}>
                        <button
                          style={a.active ? styles.dangerBtn : styles.successBtn}
                          disabled={actionId === a.id}
                          onClick={() => handleToggleActive(a)}
                        >
                          {actionId === a.id ? '…' : a.active ? 'Suspend' : 'Reactivate'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
}

function Field({ label, name, value, onChange, type = 'text', required, minLength, fullWidth }) {
  return (
    <div style={{ ...styles.field, ...(fullWidth ? { gridColumn: '1 / -1' } : {}) }}>
      <label style={styles.label}>{label}{required && ' *'}</label>
      <input
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        minLength={minLength}
        style={styles.input}
      />
    </div>
  );
}

const styles = {
  header: {
    display: 'flex', flexWrap: 'wrap', gap: 12,
    alignItems: 'center', justifyContent: 'space-between',
    marginBottom: 20,
  },
  title: { margin: 0, fontSize: 'clamp(20px, 4vw, 26px)', fontWeight: 700, color: '#1a1a1a' },
  subtitle: { margin: '4px 0 0', color: '#666', fontSize: 14 },

  primaryBtn: {
    padding: '10px 18px', borderRadius: 8, border: 'none',
    background: '#2563eb', color: '#fff', fontWeight: 600,
    fontSize: 14, cursor: 'pointer', whiteSpace: 'nowrap',
  },
  successBanner: {
    background: '#d1fae5', color: '#065f46', border: '1px solid #a7f3d0',
    borderRadius: 8, padding: '10px 14px', marginBottom: 14, fontSize: 14, fontWeight: 500,
  },
  errorBanner: {
    background: '#fee2e2', color: '#991b1b', border: '1px solid #fecaca',
    borderRadius: 8, padding: '10px 14px', marginBottom: 14, fontSize: 14, fontWeight: 500,
  },

  formCard: {
    background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12,
    padding: 'clamp(16px, 3vw, 24px)', marginBottom: 24,
  },
  formTitle: { margin: '0 0 16px', fontSize: 18, fontWeight: 700, color: '#1a1a1a' },
  formSubTitle: { margin: '18px 0 12px', fontSize: 14, fontWeight: 700, color: '#444', textTransform: 'uppercase', letterSpacing: 0.4 },
  formGrid: {
    display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 14,
  },
  field: { display: 'flex', flexDirection: 'column', gap: 6 },
  label: { fontSize: 13, fontWeight: 500, color: '#444' },
  input: {
    padding: '9px 12px', borderRadius: 8, fontSize: 14,
    border: '1.5px solid #ddd', outline: 'none', boxSizing: 'border-box', width: '100%',
  },
  formError: {
    background: '#fee2e2', border: '1px solid #fecaca', color: '#dc2626',
    fontSize: 13, padding: '8px 12px', borderRadius: 6, margin: '14px 0', fontWeight: 500,
  },

  tableCard: {
    background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, overflow: 'hidden',
  },
  emptyState: { padding: 32, textAlign: 'center', color: '#888', fontSize: 14 },
  table: { width: '100%', borderCollapse: 'collapse', minWidth: 560 },
  th: {
    textAlign: 'left', padding: '12px 16px', fontSize: 12, fontWeight: 700,
    color: '#666', textTransform: 'uppercase', letterSpacing: 0.4,
    borderBottom: '1px solid #e5e7eb', background: '#f9fafb',
  },
  tr: { borderBottom: '1px solid #f1f5f9' },
  td: { padding: '14px 16px', fontSize: 14, color: '#1a1a1a', verticalAlign: 'middle' },
  idBadge: {
    display: 'inline-block',
    padding: '4px 8px',
    borderRadius: 999,
    background: '#e0f2fe',
    color: '#075985',
    fontSize: 12,
    fontWeight: 700,
    fontFamily: 'monospace',
  },
  mutedSmall: { fontSize: 12, color: '#888', marginTop: 2 },

  badgeActive: {
    background: '#d1fae5', color: '#065f46', fontSize: 12, fontWeight: 600,
    padding: '3px 10px', borderRadius: 20,
  },
  badgeSuspended: {
    background: '#fee2e2', color: '#991b1b', fontSize: 12, fontWeight: 600,
    padding: '3px 10px', borderRadius: 20,
  },

  dangerBtn: {
    padding: '6px 12px', borderRadius: 6, border: '1px solid #fecaca',
    background: '#fff', color: '#dc2626', fontWeight: 600, fontSize: 13, cursor: 'pointer',
  },
  successBtn: {
    padding: '6px 12px', borderRadius: 6, border: '1px solid #a7f3d0',
    background: '#fff', color: '#059669', fontWeight: 600, fontSize: 13, cursor: 'pointer',
  },
};
