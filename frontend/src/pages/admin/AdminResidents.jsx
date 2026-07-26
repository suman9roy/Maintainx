import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAllResidents, getResidentById, deleteResident } from '../../api/residents';

const TYPE_BADGE = {
  OWNER:         { bg: '#dbeafe', fg: '#1d4ed8' },
  TENANT:        { bg: '#e0e7ff', fg: '#3730a3' },
  FAMILY_MEMBER: { bg: '#f0fdf4', fg: '#166534' },
};

export default function AdminResidents() {
  const [residents,  setResidents]  = useState([]);
  const [filtered,   setFiltered]   = useState([]);
  const [search,     setSearch]     = useState('');
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState('');
  const [success,         setSuccess]         = useState('');
  const [confirmId,       setConfirmId]       = useState(null); // delete confirmation
  const [deleting,        setDeleting]        = useState(false);
  const [selectedId,      setSelectedId]      = useState(null);
  const [residentDetails, setResidentDetails] = useState(null);
  const [detailLoading,   setDetailLoading]   = useState(false);
  const [detailError,     setDetailError]     = useState('');

  useEffect(() => {
    load();
  }, []);

  // Re-filter whenever search or list changes
  useEffect(() => {
    const q = search.toLowerCase().trim();
    if (!q) { setFiltered(residents); return; }
    setFiltered(residents.filter(r =>
      r.fullName?.toLowerCase().includes(q)   ||
      r.flatNumber?.toLowerCase().includes(q) ||
      r.email?.toLowerCase().includes(q)      ||
      r.blockName?.toLowerCase().includes(q)
    ));
  }, [search, residents]);

  function load() {
    setLoading(true);
    setError('');
    getAllResidents()
      .then(res => setResidents(res.data ?? []))
      .catch(() => setError('Failed to load residents.'))
      .finally(() => setLoading(false));
  }

  async function handleDelete() {
    setDeleting(true);
    setError(''); setSuccess('');
    try {
      await deleteResident(confirmId);
      setSuccess('Resident removed successfully.');
      setConfirmId(null);
      load();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to delete resident.');
      setConfirmId(null);
    } finally {
      setDeleting(false);
    }
  }

  async function loadResidentDetails(id) {
    setSelectedId(id);
    setResidentDetails(null);
    setDetailError('');
    setDetailLoading(true);

    try {
      const res = await getResidentById(id);
      setResidentDetails(res.data ?? null);
    } catch (err) {
      setDetailError(err.response?.data?.message ?? 'Failed to load resident details.');
      setSelectedId(null);
    } finally {
      setDetailLoading(false);
    }
  }

  function closeResidentDetails() {
    setSelectedId(null);
    setResidentDetails(null);
    setDetailError('');
    setDetailLoading(false);
  }

  // Group residents by flat number for a cleaner view
  const byFlat = filtered.reduce((acc, r) => {
    const key = r.flatNumber ?? 'Unknown';
    if (!acc[key]) acc[key] = [];
    acc[key].push(r);
    return acc;
  }, {});

  const flatNumbers = Object.keys(byFlat).sort();
  const confirmResident = residents.find(r => r.id === confirmId);

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>Residents</h2>
          <p style={s.sub}>
            {residents.length} approved resident{residents.length !== 1 ? 's' : ''} across{' '}
            {flatNumbers.length} flat{flatNumbers.length !== 1 ? 's' : ''}.
          </p>
        </div>
        <input
          style={s.search}
          placeholder="🔍  Search by name, flat, email…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
      </div>

      {error   && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      {loading && <p style={s.info}>Loading residents…</p>}

      {!loading && filtered.length === 0 && (
        <div style={s.empty}>
          {search ? `No residents matching "${search}".` : 'No approved residents yet.'}
        </div>
      )}

      {/* ── Grouped by flat number ──────────────────────────────── */}
      {flatNumbers.map(flatNo => (
        <div key={flatNo} style={s.flatGroup}>

          <div style={s.flatHeader}>
            <span style={s.flatLabel}>🏠 Flat {flatNo}</span>
            <span style={s.flatCount}>
              {byFlat[flatNo].length} resident{byFlat[flatNo].length > 1 ? 's' : ''}
            </span>
          </div>

          {byFlat[flatNo].map(r => (
            <div key={r.id} style={s.residentRow}>

              {/* Avatar + name */}
              <div style={s.avatar}>
                {(r.fullName?.[0] ?? '?').toUpperCase()}
              </div>

              <div style={s.info}>
                <div style={s.name}>{r.fullName}</div>
                <div style={s.email}>{r.email ?? '—'}</div>
              </div>

              <div style={s.meta}>
                <span style={{
                  ...s.typeBadge,
                  background: TYPE_BADGE[r.residentType]?.bg,
                  color:      TYPE_BADGE[r.residentType]?.fg,
                }}>
                  {r.residentType}
                </span>
                <span style={s.metaText}>
                  Block {r.blockName} · Floor {r.floorNumber}
                </span>
              </div>

              <div style={s.phone}>{r.phoneNumber ?? '—'}</div>

              <div style={s.actionsCell}>
                <button
                  style={s.viewBtn}
                  onClick={() => loadResidentDetails(r.id)}
                >
                  View
                </button>
                <button
                  style={s.deleteBtn}
                  onClick={() => { setConfirmId(r.id); setError(''); setSuccess(''); }}
                >
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      ))}

      {/* ── Delete confirmation modal ────────────────────────────── */}
      {confirmId && confirmResident && (
        <div style={s.overlay} onClick={() => setConfirmId(null)}>
          <div style={s.modal} onClick={e => e.stopPropagation()}>
            <div style={s.modalIcon}>⚠️</div>
            <h3 style={s.modalTitle}>Remove Resident?</h3>
            <p style={s.modalBody}>
              You are about to remove <strong>{confirmResident.fullName}</strong> from flat{' '}
              <strong>{confirmResident.flatNumber}</strong> ({confirmResident.residentType}).
            </p>
            <p style={s.modalWarning}>
              This action cannot be undone. The resident will need to resubmit
              a join request to be re-added.
            </p>
            <div style={s.modalActions}>
              <button
                style={s.cancelBtn}
                onClick={() => setConfirmId(null)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                style={s.confirmDeleteBtn}
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Removing…' : 'Yes, Remove'}
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedId && (
        <div style={s.overlay} onClick={closeResidentDetails}>
          <div style={s.detailModal} onClick={e => e.stopPropagation()}>
            <div style={s.modalHeader}>
              <div>
                <h3 style={s.modalTitle}>Resident details</h3>
                <p style={s.modalBody}>Fetched by ID for admin review.</p>
              </div>
              <button style={s.closeBtn} onClick={closeResidentDetails}>✕</button>
            </div>

            {detailLoading && <p style={s.info}>Loading details…</p>}
            {detailError && <div style={s.errorBox}>{detailError}</div>}

            {residentDetails && (
              <div style={s.detailGrid}>
                <Detail label="Full name" value={residentDetails.fullName} />
                <Detail label="Email" value={residentDetails.email} />
                <Detail label="Phone" value={residentDetails.phoneNumber} />
                <Detail label="Resident type" value={residentDetails.residentType} />
                <Detail label="Flat" value={residentDetails.flatNumber} />
                <Detail label="Block" value={residentDetails.blockName} />
                <Detail label="Floor" value={residentDetails.floorNumber} />
                <Detail label="Joined at" value={residentDetails.joinedAt ? new Date(residentDetails.joinedAt).toLocaleDateString('en-IN') : '—'} />
                <Detail label="Database ID" value={residentDetails.id} />
                <Detail label="User ID" value={residentDetails.userId || '—'} />
              </div>
            )}

            {!detailLoading && !detailError && !residentDetails && (
              <div style={s.empty}>No resident details available.</div>
            )}
          </div>
        </div>
      )}
    </Layout>
  );
}

function Detail({ label, value }) {
  return (
    <div>
      <div style={s.detailLabel}>{label}</div>
      <div style={s.detailValue}>{value ?? '—'}</div>
    </div>
  );
}

const s = {
  topRow:    { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 },
  heading:   { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:       { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  search:    { padding: '9px 14px', borderRadius: 8, border: '1.5px solid #e2e8f0', fontSize: 14, width: 280, outline: 'none', background: '#fff' },
  errorBox:  { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  info:      { color: '#64748b' },
  empty:     { background: '#fff', borderRadius: 10, padding: '2.5rem', textAlign: 'center', color: '#94a3b8', border: '2px dashed #e2e8f0' },
  flatGroup: { marginBottom: 20 },
  flatHeader:{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 14px', background: '#1e293b', borderRadius: '8px 8px 0 0' },
  flatLabel: { fontSize: 14, fontWeight: 600, color: '#f1f5f9' },
  flatCount: { fontSize: 12, color: '#94a3b8' },
  residentRow:{
    display: 'flex', alignItems: 'center', gap: 16,
    background: '#fff', padding: '14px 16px',
    borderBottom: '1px solid #f1f5f9',
    transition: 'background .1s',
  },
  avatar:    {
    width: 38, height: 38, borderRadius: '50%',
    background: '#2563eb', color: '#fff',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontWeight: 700, fontSize: 16, flexShrink: 0,
  },
  info:      { flex: 1, minWidth: 0 },
  name:      { fontSize: 14, fontWeight: 600, color: '#0f172a', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  email:     { fontSize: 12, color: '#64748b', marginTop: 2 },
  meta:      { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4, minWidth: 140 },
  typeBadge: { fontSize: 11, fontWeight: 600, padding: '2px 10px', borderRadius: 20 },
  metaText:  { fontSize: 12, color: '#64748b' },
  phone:     { fontSize: 13, color: '#374151', minWidth: 110, textAlign: 'right' },
  deleteBtn: {
    padding: '6px 14px', borderRadius: 6, background: '#fff',
    border: '1.5px solid #fecaca', color: '#dc2626',
    fontSize: 12, fontWeight: 600, cursor: 'pointer', flexShrink: 0,
  },
  actionsCell: { display: 'flex', gap: 8, alignItems: 'center', minWidth: 120 },
  viewBtn: {
    padding: '6px 14px', borderRadius: 6, background: '#2563eb',
    border: 'none', color: '#fff', fontSize: 12, fontWeight: 600,
    cursor: 'pointer', flexShrink: 0,
  },
  // Modal
  overlay:   { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' },
  modal:     { background: '#fff', borderRadius: 12, padding: '2rem', width: '100%', maxWidth: 420, textAlign: 'center', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' },
  modalIcon: { fontSize: 40, marginBottom: 12 },
  detailModal: {
    background: '#fff', borderRadius: 12, width: '100%', maxWidth: 680, padding: '1.5rem', boxShadow: '0 20px 60px rgba(0,0,0,0.2)', maxHeight: '90vh', overflowY: 'auto', textAlign: 'left'
  },
  modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, marginBottom: 16 },
  detailGrid: { display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 14, background: '#f8fafc', borderRadius: 12, padding: 16 },
  detailLabel: { fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: 600 },
  detailValue: { fontSize: 14, color: '#0f172a', fontWeight: 500 },
  modalTitle:{ margin: '0 0 10px', fontSize: 18, fontWeight: 700, color: '#0f172a' },
  modalBody: { margin: '0 0 10px', fontSize: 14, color: '#374151', lineHeight: 1.5 },
  closeBtn: { padding: '6px 12px', borderRadius: 8, border: 'none', background: '#e2e8f0', color: '#0f172a', fontSize: 14, cursor: 'pointer' },
  modalWarning: { margin: '0 0 20px', fontSize: 13, color: '#dc2626', background: '#fef2f2', padding: '8px 12px', borderRadius: 6 },
  modalActions:  { display: 'flex', gap: 10, justifyContent: 'center' },
  cancelBtn:     { padding: '9px 24px', borderRadius: 7, background: '#f1f5f9', border: 'none', fontSize: 13, fontWeight: 500, color: '#64748b', cursor: 'pointer' },
  confirmDeleteBtn: { padding: '9px 24px', borderRadius: 7, background: '#dc2626', border: 'none', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' },
};
