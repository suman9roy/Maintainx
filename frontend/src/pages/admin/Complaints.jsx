import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAllComplaints, updateComplaintStatus } from '../../api/complaints';

const STATUS_STYLE = {
  OPEN:        { background: '#dbeafe', color: '#1d4ed8' },
  IN_PROGRESS: { background: '#fef3c7', color: '#92400e' },
  RESOLVED:    { background: '#d1fae5', color: '#065f46' },
  REJECTED:    { background: '#fee2e2', color: '#991b1b' },
};

const STATUSES = ['OPEN','IN_PROGRESS','RESOLVED','REJECTED'];

export default function Complaints() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => { load(); }, []);

  function load() {
    setLoading(true); setError('');
    getAllComplaints()
      .then(res => setComplaints(res.data ?? []))
      .catch(() => setError('Failed to load complaints.'))
      .finally(() => setLoading(false));
  }

  async function handleChangeStatus(id, status) {
    setUpdatingId(id); setError(''); setSuccess('');
    try {
      await updateComplaintStatus(id, status);
      setSuccess('Complaint status updated.');
      // update locally for snappier UI
      setComplaints(prev => prev.map(c => c.id === id ? { ...c, status } : c));
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to update status.');
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>All Complaints</h2>
          <p style={s.sub}>View and manage complaints raised by residents.</p>
        </div>
      </div>

      {error && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      {loading && <p style={s.info}>Loading complaints…</p>}

      {!loading && complaints.length === 0 && (
        <div style={s.empty}>No complaints found.</div>
      )}

      {complaints.map(c => (
        <div key={c.id} style={s.card}>
          <div style={s.cardTop}>
            <div>
              <div style={s.title}>{c.title}</div>
              <div style={s.meta}>{c.category} · Flat {c.flatNumber} · {c.residentEmail}</div>
            </div>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <select value={c.status} onChange={e => handleChangeStatus(c.id, e.target.value)} style={s.select} disabled={updatingId === c.id}>
                {STATUSES.map(sv => <option key={sv} value={sv}>{sv}</option>)}
              </select>
              <span style={{ ...s.badge, ...STATUS_STYLE[c.status] }}>{c.status}</span>
            </div>
          </div>
          <p style={s.description}>{c.description}</p>
          {c.createdAt && <div style={s.date}>Raised {new Date(c.createdAt).toLocaleString()}</div>}
        </div>
      ))}
    </Layout>
  );
}

const s = {
  topRow:   { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 },
  heading:  { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:      { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  errorBox: { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  info:     { color: '#64748b' },
  empty:    { background: '#fff', borderRadius: 10, padding: '2rem', textAlign: 'center', color: '#64748b', border: '2px dashed #e2e8f0' },
  card:     { background: '#fff', borderRadius: 10, padding: '1rem 1.25rem', marginBottom: 10, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  cardTop:  { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  title:    { fontSize: 15, fontWeight: 600, color: '#0f172a' },
  meta:     { fontSize: 12, color: '#64748b', marginTop: 2 },
  badge:    { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20, whiteSpace: 'nowrap' },
  description: { margin: '0 0 8px', fontSize: 13, color: '#374151', lineHeight: 1.5 },
  date:     { fontSize: 11, color: '#94a3b8' },
  select:   { padding: '6px 10px', borderRadius: 6, border: '1px solid #e2e8f0', background: '#f8fafc' },
};
