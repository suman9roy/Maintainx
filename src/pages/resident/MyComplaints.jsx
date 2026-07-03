import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { createComplaint, getComplaintsByResident } from '../../api/complaints';
import { getMyResidents } from '../../api/residents';

const CATEGORIES = ['PLUMBING','ELECTRICITY','SECURITY','CLEANING','LIFT','WATER','NOISE','OTHER'];
const STATUS_STYLE = {
  OPEN:        { background: '#dbeafe', color: '#1d4ed8' },
  IN_PROGRESS: { background: '#fef3c7', color: '#92400e' },
  RESOLVED:    { background: '#d1fae5', color: '#065f46' },
  REJECTED:    { background: '#fee2e2', color: '#991b1b' },
};

export default function MyComplaints() {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [myFlats,    setMyFlats]    = useState([]);
  const [categories, setCategories] = useState(CATEGORIES);
  
  const [loading,    setLoading]    = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showForm,   setShowForm]   = useState(false);
  const [error,      setError]      = useState('');
  const [success,    setSuccess]    = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  const [form, setForm] = useState({
    residentEmail: user?.email ?? '',
    flatNumber: '', title: '', description: '', category: 'PLUMBING',
  });

  // Ensure residentEmail is kept in sync when auth context becomes available
  useEffect(() => {
    if (user?.email) setForm(prev => ({ ...prev, residentEmail: user.email }));
  }, [user?.email]);

  useEffect(() => {
    Promise.all([
      getComplaintsByResident(user?.email ?? ''),
      getMyResidents(),
    ]).then(([c, f]) => {
      setComplaints(c.data ?? []);
      setMyFlats(f.data ?? []);
      // Pre-fill first flat if available
      if (f.data?.[0]) {
        setForm(prev => ({ ...prev, flatNumber: f.data[0].flatNumber }));
      }
    }).catch(() => setError('Failed to load complaints.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    setSubmitting(true);
    try {
      // Basic client-side validation
      if (!form.flatNumber || !form.title || !form.description) {
        setError('Please fill flat number, title and description.');
        setSubmitting(false);
        return;
      }
      // Ensure payload matches backend DTO fields exactly
      const payload = {
        residentEmail: form.residentEmail || user?.email || '',
        flatNumber: form.flatNumber,
        title: form.title,
        description: form.description,
        category: form.category,
      };
      console.log('Submitting complaint payload', payload);
      await createComplaint(payload);
      setSuccess('Complaint raised successfully!');
      setShowForm(false);
      // Refresh list
      const res = await getComplaintsByResident(user?.email ?? '');
      setComplaints(res.data ?? []);
    } catch (err) {
      // Surface backend validation errors when available
      setFieldErrors({});
      const respData = err?.response?.data;
      let backendMsg = err.message || 'Failed to raise complaint.';
      if (respData) {
        // Top-level message
        if (respData.message) backendMsg = respData.message;

        // Try to extract field-specific errors from common shapes
        const fe = {};
        if (Array.isArray(respData.errors)) {
          respData.errors.forEach(e => {
            const field = e.field || e.property || e.name || e.parameterName;
            const msg = e.defaultMessage || e.message || e.error || e.rejectedValue || JSON.stringify(e);
            if (field) fe[field] = msg;
          });
        }
        if (respData.fieldErrors && typeof respData.fieldErrors === 'object') {
          Object.assign(fe, respData.fieldErrors);
        }
        if (Array.isArray(respData.violations)) {
          respData.violations.forEach(v => {
            const field = v.field || v.propertyPath || v.property || v.name;
            const msg = v.message || JSON.stringify(v);
            if (field) fe[field] = msg;
          });
        }
        // Spring style: errors array with defaultMessage and field
        if (Array.isArray(respData.fieldErrorsList)) {
          respData.fieldErrorsList.forEach(e => { if (e.field) fe[e.field] = e.defaultMessage || e.message; });
        }

        if (Object.keys(fe).length > 0) {
          setFieldErrors(fe);
        }
      }
      setError(backendMsg ?? 'Failed to raise complaint.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>My Complaints</h2>
          <p style={s.sub}>Raise and track maintenance complaints.</p>
        </div>
        <button style={s.raiseBtn} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Raise Complaint'}
        </button>
      </div>

      {error   && <div style={s.error}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      {/* Raise complaint form */}
      {showForm && (
        <form onSubmit={handleSubmit} style={s.form}>
          <h3 style={s.formTitle}>New Complaint</h3>

          <div style={s.row}>
            <div style={s.field}>
              <label style={s.label}>Resident Email *</label>
              <input value={form.residentEmail} readOnly style={{ ...s.input, background: '#f1f5f9' }} />
              {fieldErrors.residentEmail && <div style={s.fieldError}>{fieldErrors.residentEmail}</div>}
            </div>
            <div style={s.field}>
              <label style={s.label}>Flat Number *</label>
              {myFlats.length > 0 ? (
                <select name="flatNumber" value={form.flatNumber}
                  onChange={e => { console.log('flatNumber change', e.target.value); setForm({...form, flatNumber: e.target.value}); }} style={s.select}>
                  <option value="">Select flat</option>
                  {myFlats.map(f => <option key={f.flatNumber} value={f.flatNumber}>{f.flatNumber}</option>)}
                </select>
              ) : (
                <input
                  required
                  value={form.flatNumber}
                  onChange={e => setForm({...form, flatNumber: e.target.value})}
                  placeholder="Enter flat number (e.g. A-101)"
                  style={s.input}
                />
              )}
              {fieldErrors.flatNumber && <div style={s.fieldError}>{fieldErrors.flatNumber}</div>}
            </div>
            <div style={s.field}>
              <label style={s.label}>Category *</label>
              <select value={form.category}
                  onChange={e => { console.log('category change', e.target.value); setForm({...form, category: e.target.value}); }} style={s.select}>
                  {categories.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
                <div style={s.smallNote}>Selected: {form.category}</div>
                {fieldErrors.category && <div style={s.fieldError}>{fieldErrors.category}</div>}
            </div>
          </div>

          <div style={s.field}>
            <label style={s.label}>Title *</label>
            <input required maxLength={150}
              value={form.title} onChange={e => setForm({...form, title: e.target.value})}
              style={s.input} placeholder="Brief description of the issue" />
            {fieldErrors.title && <div style={s.fieldError}>{fieldErrors.title}</div>}
          </div>

          <div style={s.field}>
            <label style={s.label}>Description *</label>
            <textarea required maxLength={2000} rows={4}
              value={form.description} onChange={e => setForm({...form, description: e.target.value})}
              style={s.textarea} placeholder="Describe the issue in detail…" />
            {fieldErrors.description && <div style={s.fieldError}>{fieldErrors.description}</div>}
          </div>

          <button type="submit" disabled={submitting} style={s.submitBtn}>
            {submitting ? 'Submitting…' : 'Submit Complaint'}
          </button>
        </form>
      )}

      {loading && <p style={s.info}>Loading…</p>}

      {!loading && complaints.length === 0 && (
        <div style={s.empty}><p>No complaints raised yet.</p></div>
      )}

      {complaints.map(c => (
        <div key={c.id} style={s.card}>
          <div style={s.cardTop}>
            <div>
              <div style={s.title}>{c.title}</div>
              <div style={s.meta}>{c.category} · Flat {c.flatNumber}</div>
            </div>
            <span style={{ ...s.badge, ...STATUS_STYLE[c.status] }}>{c.status}</span>
          </div>
          <p style={s.description}>{c.description}</p>
          {c.createdAt && (
            <div style={s.date}>Raised {new Date(c.createdAt).toLocaleDateString()}</div>
          )}
        </div>
      ))}
    </Layout>
  );
}

const s = {
  topRow:   { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 },
  heading:  { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:      { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  raiseBtn: { padding: '9px 18px', borderRadius: 8, background: '#2563eb', color: '#fff', fontWeight: 600, fontSize: 13, border: 'none', cursor: 'pointer' },
  info:     { color: '#64748b' },
  error:    { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 16 },
  successBox: { background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 16 },
  empty:    { background: '#fff', borderRadius: 10, padding: '2rem', textAlign: 'center', color: '#64748b', border: '2px dashed #e2e8f0' },
  form:     { background: '#fff', borderRadius: 10, padding: '1.5rem', marginBottom: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  formTitle:{ margin: '0 0 16px', fontSize: 16, fontWeight: 600, color: '#1e293b' },
  row:      { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14, marginBottom: 14 },
  field:    { display: 'flex', flexDirection: 'column', gap: 5, marginBottom: 14 },
  label:    { fontSize: 13, fontWeight: 500, color: '#374151' },
  fieldError:{ fontSize: 12, color: '#b91c1c', marginTop: 6 },
  input:    { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, outline: 'none' },
  select:   { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #e2e8f0', fontSize: 14, background: '#f8fafc', color: '#0f172a', appearance: 'none' },
  smallNote: { fontSize: 12, color: '#64748b', marginTop: 6 },
  
  textarea: { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, resize: 'vertical', fontFamily: 'inherit' },
  submitBtn:{ padding: '10px 24px', borderRadius: 7, background: '#2563eb', color: '#fff', fontWeight: 600, fontSize: 14, border: 'none', cursor: 'pointer' },
  card:     { background: '#fff', borderRadius: 10, padding: '1rem 1.25rem', marginBottom: 10, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  cardTop:  { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  title:    { fontSize: 15, fontWeight: 600, color: '#0f172a' },
  meta:     { fontSize: 12, color: '#64748b', marginTop: 2 },
  badge:    { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20, whiteSpace: 'nowrap' },
  description: { margin: '0 0 8px', fontSize: 13, color: '#374151', lineHeight: 1.5 },
  date:     { fontSize: 11, color: '#94a3b8' },
};
