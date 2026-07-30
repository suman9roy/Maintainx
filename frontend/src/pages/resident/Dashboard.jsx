import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { getMyResidents } from '../../api/residents';
import { getMyRequests } from '../../api/joinRequests';
import { getAllNotices } from '../../api/notices';
import {
  getJoinRequestCounts,
  getApprovedJoinRequests,
  getUserApartmentIds,
  normalizeJoinRequests,
  normalizeJoinRequestStatus,
} from '../../utils/joinRequestStatus';

export default function ResidentDashboard() {
  const { user, refreshToken } = useAuth();
  const navigate = useNavigate();

  const [residents, setResidents] = useState([]);
  const [requests,  setRequests]  = useState([]);
  const [notices,   setNotices]   = useState([]);
  const [loading,   setLoading]   = useState(true);

  function loadDashboardData() {
    return Promise.allSettled([
      getMyResidents(),
      getMyRequests(),
      getAllNotices(),
    ]).then(([residentsResult, requestsResult, noticesResult]) => {

      if (residentsResult.status === 'rejected') {
        console.error('Failed to load residents:', residentsResult.reason);
      }
      if (requestsResult.status === 'rejected') {
        console.error('Failed to load join requests:', requestsResult.reason);
      }
      if (noticesResult.status === 'rejected') {
        // Expected for a resident with no approved apartment yet — notices
        // are apartment-scoped, so this call can 400/403 before approval.
        // Don't let it wipe out residents/requests, which loaded fine.
        console.error('Failed to load notices:', noticesResult.reason);
      }

      const residentList = residentsResult.status === 'fulfilled'
        ? (residentsResult.value.data ?? []) : [];
      const requestList = requestsResult.status === 'fulfilled'
        ? normalizeJoinRequests(requestsResult.value.data) : [];
      const apartmentIds = getUserApartmentIds(residentList, requestList);

      setResidents(residentList);
      setRequests(requestList);
      setNotices(
        noticesResult.status === 'fulfilled'
          ? (noticesResult.value.data ?? [])
              .filter((notice) => {
                const noticeApartment = notice.apartmentId ?? notice.apartmentID ?? notice.apartment_id;
                return !noticeApartment || apartmentIds.includes(noticeApartment);
              })
              .slice(0, 3)
          : []
      );

      // The current JWT was issued at login and only carries the
      // apartmentId the user had *then*. If a join request has since
      // been approved, our token is stale — the backend has already
      // linked the apartment (see the auth-service Kafka consumer),
      // but we're still holding a token with no/old apartmentId claim
      // until we ask for a new one. Silently swap it in so every
      // apartment-scoped call (maintenance, expenses, notices, etc.)
      // starts working without the user having to log out.
      const tokenApartmentId = user?.apartmentId ?? null;
      const staleToken = apartmentIds.length > 0
        && !apartmentIds.includes(tokenApartmentId);

      if (staleToken) {
        refreshToken().then((refreshed) => {
          if (refreshed) loadDashboardData();
        });
      }
    });
  }

  useEffect(() => {
    loadDashboardData()
      .catch(console.error)
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // While the resident has a request still awaiting a decision, poll
  // periodically so an approval shows up (and the token gets refreshed,
  // per the effect above) without requiring a manual page reload.
  useEffect(() => {
    const hasPending = requests.some(
      (r) => normalizeJoinRequestStatus(r?.status ?? r?.requestStatus) === 'PENDING'
    );
    if (!hasPending) return;

    const interval = setInterval(() => {
      loadDashboardData().catch(console.error);
    }, 20000);

    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requests]);
  const apartmentIds = getUserApartmentIds(residents, requests);
  const { pending, approved, rejected } = getJoinRequestCounts(requests);

  // Approved flats can come from two places: residents already linked
  // via getMyResidents(), or join requests that were approved but
  // haven't produced a resident record view yet. Dedupe by flat so the
  // same flat doesn't show twice if it appears in both lists.
  const approvedRequestFlats = getApprovedJoinRequests(requests);
  const uniqueApprovedFlats = [...residents, ...approvedRequestFlats].filter(
    (flat, index, all) =>
      index === all.findIndex(
        (f) =>
          (f.id && f.id === flat.id) ||
          (f.flatNumber === flat.flatNumber && f.blockName === flat.blockName)
      )
  );

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
          { label: 'Approved Flats', value: uniqueApprovedFlats.length, color: '#059669' },
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
      {uniqueApprovedFlats.length > 0 && (
        <section style={s.section}>
          <h3 style={s.sectionTitle}>My Approved Flats</h3>
          <div style={s.flatGrid}>
            {uniqueApprovedFlats.map((r, index) => (
              <div key={`${r.id ?? r.apartmentId ?? `${r.flatNumber}-${r.blockName}-${index}`}`}
                style={s.flatCard}
              >
                <div style={s.flatNum}>{r.flatNumber}</div>
                <div style={s.flatMeta}>{r.blockName} · Floor {r.floorNumber}</div>
                <span style={s.residentTypeBadge}>{r.residentType ?? 'RESIDENT'}</span>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* ── No flat yet ───────────────────────────────────────────── */}
      {uniqueApprovedFlats.length === 0 && (
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
