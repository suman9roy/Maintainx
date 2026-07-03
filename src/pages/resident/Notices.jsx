import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAllNotices } from '../../api/notices';

const TYPE_STYLE = {
  GENERAL:          { background: '#e0e7ff', color: '#3730a3' },
  MEETING:          { background: '#fef3c7', color: '#92400e' },
  EMERGENCY:        { background: '#fee2e2', color: '#991b1b' },
  MAINTENANCE:      { background: '#dbeafe', color: '#1d4ed8' },
  EVENT:            { background: '#d1fae5', color: '#065f46' },
  PAYMENT_REMINDER: { background: '#fce7f3', color: '#9d174d' },
};

const TYPE_ICON = {
  GENERAL: '📢', MEETING: '🤝', EMERGENCY: '🚨',
  MAINTENANCE: '🔧', EVENT: '🎉', PAYMENT_REMINDER: '💳',
};

export default function Notices() {
  const [notices, setNotices] = useState([]);
  const [filter,  setFilter]  = useState('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAllNotices()
      .then(res => setNotices(res.data ?? []))
      .finally(() => setLoading(false));
  }, []);

  const types    = ['ALL', ...Object.keys(TYPE_STYLE)];
  const filtered = filter === 'ALL' ? notices : notices.filter(n => n.type === filter);

  return (
    <Layout>
      <h2 style={s.heading}>Society Notices</h2>
      <p style={s.sub}>Stay updated with announcements from your society admin.</p>

      {/* Type filter */}
      <div style={s.filterRow}>
        {types.map(t => (
          <button key={t} onClick={() => setFilter(t)}
            style={{ ...s.filterBtn, ...(filter === t ? s.filterActive : {}) }}>
            {TYPE_ICON[t] ?? '📋'} {t}
          </button>
        ))}
      </div>

      {loading && <p style={s.info}>Loading…</p>}

      {!loading && filtered.length === 0 && (
        <div style={s.empty}><p>No notices found.</p></div>
      )}

      {filtered.map(n => (
        <div key={n.id} style={s.card}>
          <div style={s.cardTop}>
            <div style={s.noticeTitle}>
              {TYPE_ICON[n.type] ?? '📋'} {n.title}
            </div>
            <span style={{ ...s.badge, ...TYPE_STYLE[n.type] }}>{n.type}</span>
          </div>
          <p style={s.message}>{n.message}</p>
          {n.meetingTime && (
            <div style={s.meetingTime}>
              🕐 Meeting: {new Date(n.meetingTime).toLocaleString()}
            </div>
          )}
        </div>
      ))}
    </Layout>
  );
}

const s = {
  heading:     { margin: '0 0 4px', fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:         { margin: '0 0 20px', color: '#64748b', fontSize: 14 },
  info:        { color: '#64748b' },
  filterRow:   { display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 },
  filterBtn:   { padding: '6px 14px', borderRadius: 20, border: '1px solid #e2e8f0', background: '#fff', fontSize: 12, fontWeight: 500, cursor: 'pointer', color: '#374151' },
  filterActive:{ background: '#1e293b', color: '#fff', borderColor: '#1e293b' },
  empty:       { background: '#fff', borderRadius: 10, padding: '2rem', textAlign: 'center', color: '#64748b', border: '2px dashed #e2e8f0' },
  card:        { background: '#fff', borderRadius: 10, padding: '1.25rem', marginBottom: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  cardTop:     { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 },
  noticeTitle: { fontSize: 16, fontWeight: 600, color: '#0f172a' },
  badge:       { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20, whiteSpace: 'nowrap' },
  message:     { margin: '0 0 8px', fontSize: 14, color: '#374151', lineHeight: 1.6 },
  meetingTime: { fontSize: 13, color: '#7c3aed', fontWeight: 500, background: '#f5f3ff', padding: '6px 10px', borderRadius: 6, display: 'inline-block' },
};
