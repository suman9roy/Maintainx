import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import { getAllRequests }   from '../../api/joinRequests';
import { getAllResidents }  from '../../api/residents';
import { getAllBills, getTotalCollected } from '../../api/maintenance';
import { getAllComplaints } from '../../api/complaints';
import { getFundSummary }  from '../../api/expenses';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [stats,            setStats]            = useState(null);
  const [loading,          setLoading]          = useState(true);
  const [recentRequests,   setRecentRequests]   = useState([]);
  const [recentComplaints, setRecentComplaints] = useState([]);
  const [error,            setError]            = useState('');

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      try {
        const results = await Promise.allSettled([
          getAllRequests(),
          getAllResidents(),
          getAllBills(),
          getTotalCollected(),
          getAllComplaints(),
          getFundSummary(),
        ]);

        if (!active) return;

        const [reqRes, resRes, billRes, collectedRes, compRes, fundRes] = results;

        const requests   = reqRes.status === 'fulfilled' ? reqRes.value.data ?? [] : [];
        const residents  = resRes.status === 'fulfilled' ? resRes.value.data ?? [] : [];
        const bills      = billRes.status === 'fulfilled' ? billRes.value.data ?? [] : [];
        const complaints = compRes.status === 'fulfilled' ? compRes.value.data ?? [] : [];
        const fund       = fundRes.status === 'fulfilled' ? fundRes.value.data ?? {} : {};
        const totalCollected = collectedRes.status === 'fulfilled' ? collectedRes.value.data ?? 0 : 0;

        if (results.some(r => r.status === 'rejected')) {
          console.error('Admin dashboard partial load failures:', results.filter(r => r.status === 'rejected'));
          setError('Some dashboard metrics could not be loaded. Showing available data.');
        }

        setStats({
          pendingRequests: requests.filter(r => r.status === 'PENDING').length,
          totalResidents:  residents.length,
          pendingBills:    bills.filter(b => b.paymentStatus === 'PENDING').length,
          totalCollected:  totalCollected,
          openComplaints:  complaints.filter(c => c.status === 'OPEN').length,
          remainingFund:   fund.remainingFund ?? 0,
        });

        setRecentRequests(requests.slice(-3).reverse());
        setRecentComplaints(complaints.filter(c => c.status === 'OPEN').slice(-3).reverse());
      } catch (err) {
        console.error(err);
        if (!active) return;
        setError('Unable to load dashboard data. Please try again later.');
        setStats({
          pendingRequests: 0,
          totalResidents: 0,
          pendingBills: 0,
          totalCollected: 0,
          openComplaints: 0,
          remainingFund: 0,
        });
      } finally {
        if (active) setLoading(false);
      }
    }

    loadDashboard();
    return () => { active = false; };
  }, []);

  const STATUS_COLOR = {
    PENDING:  { bg: '#fef3c7', fg: '#92400e' },
    APPROVED: { bg: '#d1fae5', fg: '#065f46' },
    REJECTED: { bg: '#fee2e2', fg: '#991b1b' },
    OPEN:     { bg: '#dbeafe', fg: '#1d4ed8' },
    IN_PROGRESS: { bg: '#fef3c7', fg: '#92400e' },
    RESOLVED: { bg: '#d1fae5', fg: '#065f46' },
  };

  const quickActions = [
    { label: 'Review Join Requests', to: '/admin/join-requests', color: '#7c3aed', icon: '📋', badge: stats?.pendingRequests },
    { label: 'Manage Bills',         to: '/admin/bills',         color: '#059669', icon: '💰', badge: stats?.pendingBills },
    { label: 'Open Complaints',      to: '/admin/complaints',    color: '#d97706', icon: '📢', badge: stats?.openComplaints },
    { label: 'View Residents',       to: '/admin/residents',     color: '#2563eb', icon: '👥', badge: null },
    { label: 'Add Expense',          to: '/admin/expenses',      color: '#0891b2', icon: '📊', badge: null },
    { label: 'Post Notice',          to: '/admin/notices',       color: '#dc2626', icon: '📌', badge: null },
  ];

  if (loading) return <Layout><p style={s.info}>Loading dashboard…</p></Layout>;

  return (
    <Layout>
      <h2 style={s.heading}>Admin Dashboard</h2>
      {error && <div style={s.error}>{error}</div>}
      <p style={s.sub}>Society overview at a glance.</p>

      <div style={s.statsGrid}>
        {[
          { label: 'Pending Join Requests', value: stats.pendingRequests,   color: '#7c3aed', icon: '📋' },
          { label: 'Total Residents',       value: stats.totalResidents,    color: '#2563eb', icon: '👥' },
          { label: 'Pending Bills',         value: stats.pendingBills,      color: '#d97706', icon: '💰' },
          { label: 'Total Collected',       value: `₹${Number(stats.totalCollected).toLocaleString('en-IN')}`, color: '#059669', icon: '💵' },
          { label: 'Open Complaints',       value: stats.openComplaints,    color: '#dc2626', icon: '📢' },
          { label: 'Remaining Fund',        value: `₹${Number(stats.remainingFund).toLocaleString('en-IN')}`,  color: '#0891b2', icon: '🏦' },
        ].map(stat => (
          <div key={stat.label} style={s.statCard}>
            <div style={s.statIcon}>{stat.icon}</div>
            <div style={{ ...s.statVal, color: stat.color }}>{stat.value}</div>
            <div style={s.statLabel}>{stat.label}</div>
          </div>
        ))}
      </div>

      <section style={s.section}>
        <h3 style={s.sectionTitle}>Quick Actions</h3>
        <div style={s.actionsGrid}>
          {quickActions.map(a => (
            <button key={a.to} onClick={() => navigate(a.to)}
              style={{ ...s.actionBtn, borderLeft: `4px solid ${a.color}` }}>
              <span>{a.icon} {a.label}</span>
              {a.badge > 0 && <span style={s.urgentBadge}>{a.badge}</span>}
            </button>
          ))}
        </div>
      </section>

      <div style={s.twoCol}>
        <section style={s.section}>
          <div style={s.sectionHeader}>
            <h3 style={s.sectionTitle}>Recent Join Requests</h3>
            <button style={s.viewAllBtn} onClick={() => navigate('/admin/join-requests')}>View all →</button>
          </div>
          {recentRequests.length === 0
            ? <div style={s.empty}>No requests yet.</div>
            : recentRequests.map(r => (
              <div key={r.id} style={s.activityCard}>
                <div>
                  <div style={s.activityTitle}>{r.fullName}</div>
                  <div style={s.activityMeta}>Flat {r.flatNumber} · {r.residentType}</div>
                </div>
                <span style={{ ...s.badge, background: STATUS_COLOR[r.status]?.bg, color: STATUS_COLOR[r.status]?.fg }}>
                  {r.status}
                </span>
              </div>
            ))
          }
        </section>

        <section style={s.section}>
          <div style={s.sectionHeader}>
            <h3 style={s.sectionTitle}>Open Complaints</h3>
            <button style={s.viewAllBtn} onClick={() => navigate('/admin/complaints')}>View all →</button>
          </div>
          {recentComplaints.length === 0
            ? <div style={s.empty}>No open complaints 🎉</div>
            : recentComplaints.map(c => (
              <div key={c.id} style={s.activityCard}>
                <div>
                  <div style={s.activityTitle}>{c.title}</div>
                  <div style={s.activityMeta}>{c.category} · Flat {c.flatNumber}</div>
                </div>
                <span style={{ ...s.badge, background: STATUS_COLOR[c.status]?.bg, color: STATUS_COLOR[c.status]?.fg }}>
                  {c.status}
                </span>
              </div>
            ))
          }
        </section>
      </div>
    </Layout>
  );
}

const s = {
  heading:      { margin: '0 0 4px', fontSize: 26, fontWeight: 700, color: '#0f172a' },
  sub:          { margin: '0 0 24px', color: '#64748b', fontSize: 15 },
  info:         { color: '#64748b', padding: '2rem' },
  error:        { background: '#fee2e2', border: '1px solid #fecaca', color: '#b91c1c', borderRadius: 8, padding: '1rem', marginBottom: 16 },
  statsGrid:    { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14, marginBottom: 28 },
  statCard:     { background: '#fff', borderRadius: 12, padding: '1.25rem', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', textAlign: 'center' },
  statIcon:     { fontSize: 26, marginBottom: 8 },
  statVal:      { fontSize: 28, fontWeight: 700, marginBottom: 4 },
  statLabel:    { fontSize: 12, color: '#64748b' },
  section:      { marginBottom: 24 },
  sectionHeader:{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  sectionTitle: { margin: 0, fontSize: 16, fontWeight: 600, color: '#1e293b' },
  viewAllBtn:   { background: 'none', border: 'none', color: '#2563eb', fontSize: 13, fontWeight: 500, cursor: 'pointer' },
  actionsGrid:  { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 },
  actionBtn:    { padding: '14px 16px', borderRadius: 8, background: '#fff', border: '1px solid #e2e8f0', cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 13, fontWeight: 500, color: '#1e293b', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' },
  urgentBadge:  { background: '#dc2626', color: '#fff', fontSize: 11, fontWeight: 700, padding: '2px 7px', borderRadius: 20 },
  twoCol:       { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 },
  activityCard: { background: '#fff', borderRadius: 8, padding: '12px 14px', marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid #f1f5f9', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' },
  activityTitle:{ fontSize: 14, fontWeight: 500, color: '#0f172a' },
  activityMeta: { fontSize: 12, color: '#64748b', marginTop: 2 },
  badge:        { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20, whiteSpace: 'nowrap' },
  empty:        { background: '#f8fafc', borderRadius: 8, padding: '1.5rem', textAlign: 'center', color: '#94a3b8', fontSize: 13 },
};

