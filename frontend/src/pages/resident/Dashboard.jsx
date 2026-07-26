import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { getMyResidents } from '../../api/residents';
import { getMyRequests } from '../../api/joinRequests';
import { getAllNotices } from '../../api/notices';

export default function ResidentDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [residents, setResidents] = useState([]);
  const [requests,  setRequests]  = useState([]);
  const [notices,   setNotices]   = useState([]);
  const [loading,   setLoading]   = useState(true);

  useEffect(() => {
    Promise.all([
      getMyResidents(),
      getMyRequests(),
      getAllNotices(),
    ]).then(([r, rq, n]) => {
      setResidents(r.data ?? []);
      setRequests(rq.data ?? []);
      setNotices((n.data ?? []).slice(0, 3)); // show latest 3
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const pending  = requests.filter(r => r.status === 'PENDING').length;
  const approved = requests.filter(r => r.status === 'APPROVED').length;
  const rejected = requests.filter(r => r.status === 'REJECTED').length;

  const quickLinks = [
    { label: '📋 Submit Join Request', to: '/join-request', color: '#2563eb' },
    { label: '📬 View My Requests',    to: '/my-requests',  color: '#7c3aed' },
    { label: '💰 View My Bills',       to: '/my-bills',     color: '#059669' },
    { label: '📢 Raise Complaint',     to: '/my-complaints',color: '#d97706' },
    { label: '📊 Society Expenses',    to: '/expenses',     color: '#0891b2' },
    { label: '📌 Notices',            to: '/notices',      color: '#dc2626' },
  ];

  if (loading) return <Layout><p style={s.loading}>Loading…</p></Layout>;

  return (
    <Layout>
      <h2 style={s.heading}>Welcome back 👋</h2>
      <p style={s.sub}>Here's what's happening in your society.</p>

      {/* ── Stats ─────────────────────────────────────────────────── */}
      <div style={s.statsRow}>
        {[
          { label: 'Approved Flats', value: residents.length, color: '#059669' },
          { label: 'Pending Requests', value: pending,         color: '#d97706' },
          { label: 'Approved Requests', value: approved,       color: '#2563eb' },
          { label: 'Rejected Requests', value: rejected,       color: '#dc2626' },
        ].map(stat => (
          <div key={stat.label} style={s.statCard}>
            <div style={{ ...s.statVal, color: stat.color }}>{stat.value}</div>
            <div style={s.statLabel}>{stat.label}</div>
          </div>
        ))}
      </div>

      {/* ── Approved flats ────────────────────────────────────────── */}
      {residents.length > 0 && (
        <section style={s.section}>
          <h3 style={s.sectionTitle}>My Approved Flats</h3>
          <div style={s.flatGrid}>
            {residents.map(r => (
              <div key={r.id} style={s.flatCard}>
                <div style={s.flatNum}>{r.flatNumber}</div>
                <div style={s.flatMeta}>{r.blockName} · Floor {r.floorNumber}</div>
                <span style={s.residentTypeBadge}>{r.residentType}</span>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* ── No flat yet ───────────────────────────────────────────── */}
      {residents.length === 0 && (
        <div style={s.emptyBox}>
          <p style={s.emptyText}>You don't have any approved flat registrations yet.</p>
          <button style={s.emptyBtn} onClick={() => navigate('/join-request')}>
            Submit Join Request →
          </button>
        </div>
      )}

      {/* ── Quick links ───────────────────────────────────────────── */}
      <section style={s.section}>
        <h3 style={s.sectionTitle}>Quick Actions</h3>
        <div style={s.quickGrid}>
          {quickLinks.map(link => (
            <button
              key={link.to}
              onClick={() => navigate(link.to)}
              style={{ ...s.quickBtn, borderLeft: `4px solid ${link.color}` }}
            >
              {link.label}
            </button>
          ))}
        </div>
      </section>

      {/* ── Recent notices ────────────────────────────────────────── */}
      {notices.length > 0 && (
        <section style={s.section}>
          <h3 style={s.sectionTitle}>Latest Notices</h3>
          {notices.map(n => (
            <div key={n.id} style={s.noticeCard}>
              <div style={s.noticeTitle}>{n.title}</div>
              <div style={s.noticeType}>{n.type}</div>
            </div>
          ))}
          <button style={s.viewAll} onClick={() => navigate('/notices')}>
            View all notices →
          </button>
        </section>
      )}
    </Layout>
  );
}

const s = {
  heading: { margin: '0 0 4px', fontSize: 26, fontWeight: 700, color: '#0f172a' },
  sub:     { margin: '0 0 24px', color: '#64748b', fontSize: 15 },
  loading: { color: '#64748b', padding: '2rem' },
  statsRow: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 14, marginBottom: 28 },
  statCard: {
    background: '#fff', borderRadius: 10, padding: '1.2rem 1rem',
    boxShadow: '0 1px 4px rgba(0,0,0,0.07)', textAlign: 'center',
  },
  statVal:   { fontSize: 32, fontWeight: 700 },
  statLabel: { fontSize: 12, color: '#64748b', marginTop: 4 },
  section:   { marginBottom: 28 },
  sectionTitle: { margin: '0 0 14px', fontSize: 17, fontWeight: 600, color: '#1e293b' },
  flatGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px,1fr))', gap: 12 },
  flatCard: {
    background: '#fff', borderRadius: 10, padding: '1rem',
    boxShadow: '0 1px 4px rgba(0,0,0,0.07)', border: '1px solid #e2e8f0',
  },
  flatNum:  { fontSize: 22, fontWeight: 700, color: '#1e293b', marginBottom: 4 },
  flatMeta: { fontSize: 13, color: '#64748b', marginBottom: 8 },
  residentTypeBadge: {
    fontSize: 11, fontWeight: 600, padding: '2px 10px', borderRadius: 20,
    background: '#dbeafe', color: '#1d4ed8',
  },
  emptyBox: {
    background: '#fff', borderRadius: 10, padding: '2.5rem',
    textAlign: 'center', marginBottom: 28, border: '2px dashed #cbd5e1',
  },
  emptyText: { color: '#64748b', marginBottom: 16, fontSize: 15 },
  emptyBtn: {
    padding: '10px 24px', borderRadius: 8, background: '#2563eb',
    color: '#fff', fontWeight: 600, fontSize: 14, border: 'none', cursor: 'pointer',
  },
  quickGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 },
  quickBtn: {
    padding: '14px 16px', borderRadius: 8, background: '#fff',
    textAlign: 'left', fontSize: 13, fontWeight: 500, color: '#1e293b',
    border: '1px solid #e2e8f0', cursor: 'pointer',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
    transition: 'box-shadow .15s',
  },
  noticeCard: {
    background: '#fff', borderRadius: 8, padding: '12px 16px',
    marginBottom: 8, display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', border: '1px solid #e2e8f0',
  },
  noticeTitle: { fontSize: 14, fontWeight: 500, color: '#1e293b' },
  noticeType: {
    fontSize: 11, fontWeight: 600, padding: '2px 10px', borderRadius: 20,
    background: '#fef3c7', color: '#92400e',
  },
  viewAll: {
    background: 'none', border: 'none', color: '#2563eb',
    fontSize: 13, fontWeight: 500, cursor: 'pointer', marginTop: 4,
  },
};
