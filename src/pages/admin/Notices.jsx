import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { createNotice, getAllNotices, getNoticesByType } from '../../api/notices';

const NOTICE_TYPES = [
    'GENERAL',
    'MEETING',
    'EMERGENCY',
    'MAINTENANCE',
    'EVENT',
    'PAYMENT_REMINDER'];

export default function Notices() {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [adding, setAdding] = useState(false);
  const [filterType, setFilterType] = useState('');
  const [form, setForm] = useState({ title: '', message: '', type: 'GENERAL', meetingTime: '' });
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => { load(); }, []);

  function load() {
    setLoading(true); setError('');
    getAllNotices()
      .then(res => setNotices(res.data ?? []))
      .catch(() => setError('Failed to load notices.'))
      .finally(() => setLoading(false));
  }

  async function handleAdd(e) {
    e.preventDefault();
    setError(''); setSuccess(''); setFieldErrors({});
    setAdding(true);
    try {
      if (!form.title || !form.message || !form.type) {
        setError('Please fill title, message and type.');
        setAdding(false);
        return;
      }
      if (form.type === 'MEETING' && !form.meetingTime) {
        setError('Meeting time is required for MEETING notices.');
        setAdding(false);
        return;
      }
      const payload = {
        title: form.title,
        message: form.message,
        type: form.type,
        meetingTime: form.meetingTime ? new Date(form.meetingTime).toISOString() : null,
      };
      await createNotice(payload);
      setSuccess('Notice created.');
      setForm({ title: '', message: '', type: form.type, meetingTime: '' });
      load();
    } catch (err) {
      const resp = err?.response?.data;
      if (resp) {
        if (resp.message) setError(resp.message);
        if (resp.fieldErrors) setFieldErrors(resp.fieldErrors);
        if (Array.isArray(resp.violations)) {
          const fe = {};
          resp.violations.forEach(v => { fe[v.field || v.propertyPath || v.property] = v.message; });
          setFieldErrors(fe);
        }
      } else {
        setError(err.message || 'Failed to create notice.');
      }
    } finally { setAdding(false); }
  }

  async function handleFilterChange(t) {
    setFilterType(t);
    setLoading(true); setError('');
    try {
      if (!t) {
        const res = await getAllNotices();
        setNotices(res.data ?? []);
      } else {
        const res = await getNoticesByType(t);
        setNotices(res.data ?? []);
      }
    } catch {
      setError('Failed to load notices.');
    } finally { setLoading(false); }
  }

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>Notices</h2>
          <p style={s.sub}>Create and review notices. Meeting notices require a meeting time.</p>
        </div>
      </div>

      {error && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      <div style={s.form}>
        <h3 style={s.formTitle}>Create Notice</h3>
        <div style={s.field}>
          <label style={s.label}>Title *</label>
          <input value={form.title} onChange={e => setForm({...form, title: e.target.value})} style={s.input} />
          {fieldErrors.title && <div style={s.fieldError}>{fieldErrors.title}</div>}
        </div>

        <div style={s.field}>
          <label style={s.label}>Message *</label>
          <textarea value={form.message} onChange={e => setForm({...form, message: e.target.value})} style={s.textarea} rows={4} />
          {fieldErrors.message && <div style={s.fieldError}>{fieldErrors.message}</div>}
        </div>

        <div style={s.row}>
          <div style={s.field}>
            <label style={s.label}>Type *</label>
            <select value={form.type} onChange={e => setForm({...form, type: e.target.value})} style={s.select}>
              {NOTICE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            {fieldErrors.type && <div style={s.fieldError}>{fieldErrors.type}</div>}
          </div>
          <div style={s.field}>
            <label style={s.label}>Meeting time (required for MEETING)</label>
            <input type="datetime-local" value={form.meetingTime} onChange={e => setForm({...form, meetingTime: e.target.value})} style={s.input} />
            {fieldErrors.meetingTime && <div style={s.fieldError}>{fieldErrors.meetingTime}</div>}
          </div>
        </div>

        <button onClick={handleAdd} disabled={adding} style={s.submitBtn}>{adding ? 'Creating…' : 'Create Notice'}</button>
      </div>

      <div style={{ marginTop: 20 }}>
        <h3 style={{ marginBottom: 8 }}>Filter</h3>
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <button style={s.filterBtn} onClick={() => handleFilterChange('')}>All</button>
          {NOTICE_TYPES.map(t => (
            <button key={t} style={s.filterBtn} onClick={() => handleFilterChange(t)}>{t}</button>
          ))}
        </div>
        <h3 style={{ marginBottom: 8 }}>All Notices</h3>
        {loading && <div style={s.info}>Loading…</div>}
        {!loading && notices.length === 0 && <div style={s.empty}>No notices yet.</div>}
        {notices.map(x => (
          <div key={x.id} style={s.card}>
            <div style={s.cardTop}>
              <div>
                <div style={s.title}>{x.title}</div>
                <div style={s.meta}>{x.type} · {x.meetingTime ? new Date(x.meetingTime).toLocaleString() : ''}</div>
              </div>
              <div style={s.date}>{x.createdAt ? new Date(x.createdAt).toLocaleDateString() : ''}</div>
            </div>
            <p style={s.description}>{x.message}</p>
          </div>
        ))}
      </div>
    </Layout>
  );
}

const s = {
  topRow:   { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 },
  heading:  { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:      { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  form:     { background: '#fff', borderRadius: 10, padding: '1rem', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' },
  formTitle:{ margin: '0 0 12px', fontSize: 16, fontWeight: 600 },
  row:      { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  field:    { display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 10 },
  label:    { fontSize: 13, fontWeight: 600, color: '#374151' },
  input:    { padding: '8px 10px', borderRadius: 8, border: '1px solid #e2e8f0' },
  select:   { padding: '8px 10px', borderRadius: 8, border: '1px solid #e2e8f0', background: '#f8fafc' },
  textarea: { padding: '8px 10px', borderRadius: 8, border: '1px solid #e2e8f0' },
  submitBtn:{ padding: '9px 16px', borderRadius: 8, background: '#2563eb', color: '#fff', border: 'none', cursor: 'pointer' },
  filterBtn:{ padding: '8px 12px', borderRadius: 8, background: '#fff', border: '1px solid #e2e8f0', cursor: 'pointer' },
  info:     { color: '#64748b' },
  empty:    { background: '#fff', borderRadius: 8, padding: 12, color: '#64748b' },
  card:     { background: '#fff', borderRadius: 8, padding: 12, marginBottom: 10, boxShadow: '0 1px 3px rgba(0,0,0,0.04)' },
  cardTop:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  title:    { fontSize: 14, fontWeight: 700 },
  meta:     { fontSize: 12, color: '#64748b' },
  description: { fontSize: 13, color: '#374151' },
  date:     { fontSize: 12, color: '#94a3b8' },
  fieldError:{ fontSize: 12, color: '#b91c1c', marginTop: 6 },
  errorBox: { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
};