import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getMyRequests } from '../../api/joinRequests';

const STATUS_STYLE = {
  PENDING:  { background: '#fef3c7', color: '#92400e' },
  APPROVED: { background: '#d1fae5', color: '#065f46' },
  REJECTED: { background: '#fee2e2', color: '#991b1b' },
};

export default function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');

  useEffect(() => {
    getMyRequests()
      .then(res => setRequests(res.data ?? []))
      .catch(() => setError('Failed to load your requests.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <h2 style={s.heading}>My Join Requests</h2>
      <p style={s.sub}>Track the status of your flat registration requests.</p>

      {loading && <p style={s.info}>Loading…</p>}
      {error   && <p style={s.errText}>{error}</p>}

      {!loading && requests.length === 0 && (
        <div style={s.empty}>
          <p>No join requests found. Submit one to get started!</p>
        </div>
      )}

      {requests.map(r => (
        <div key={r.id} style={s.card}>
          <div style={s.cardTop}>
            <div>
              <div style={s.flat}>{r.flatNumber} — {r.blockName}, Floor {r.floorNumber}</div>
              <div style={s.meta}>{r.residentType} · {r.fullName}</div>
            </div>
            <span style={{ ...s.badge, ...STATUS_STYLE[r.status] }}>{r.status}</span>
          </div>

          <div style={s.cardBody}>
            <div style={s.infoRow}>
              <span style={s.infoLabel}>Requested</span>
              <span>{r.requestedAt ? new Date(r.requestedAt).toLocaleDateString() : '—'}</span>
            </div>
            {r.reviewedAt && (
              <div style={s.infoRow}>
                <span style={s.infoLabel}>Reviewed</span>
                <span>{new Date(r.reviewedAt).toLocaleDateString()}</span>
              </div>
            )}
            {r.documentName && (
              <div style={s.infoRow}>
                <span style={s.infoLabel}>Document</span>
                <span>📎 {r.documentName}</span>
              </div>
            )}
            {r.status === 'REJECTED' && r.rejectionReason && (
              <div style={s.rejectReason}>
                <strong>Rejection reason:</strong> {r.rejectionReason}
              </div>
            )}
          </div>
        </div>
      ))}
    </Layout>
  );
}

const s = {
  heading:  { margin: '0 0 4px', fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:      { margin: '0 0 24px', color: '#64748b', fontSize: 14 },
  info:     { color: '#64748b' },
  errText:  { color: '#dc2626' },
  empty:    { background: '#fff', borderRadius: 10, padding: '2rem', textAlign: 'center', color: '#64748b', border: '2px dashed #e2e8f0' },
  card:     { background: '#fff', borderRadius: 10, marginBottom: 14, boxShadow: '0 1px 4px rgba(0,0,0,0.07)', overflow: 'hidden' },
  cardTop:  { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', padding: '1rem 1.25rem', borderBottom: '1px solid #f1f5f9' },
  flat:     { fontSize: 16, fontWeight: 600, color: '#0f172a' },
  meta:     { fontSize: 13, color: '#64748b', marginTop: 2 },
  badge:    { padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 600 },
  cardBody: { padding: '1rem 1.25rem' },
  infoRow:  { display: 'flex', gap: 12, marginBottom: 8, fontSize: 13, color: '#374151' },
  infoLabel:{ fontWeight: 500, minWidth: 90, color: '#64748b' },
  rejectReason: { marginTop: 8, padding: '8px 12px', background: '#fef2f2', borderRadius: 6, fontSize: 13, color: '#dc2626' },
};
