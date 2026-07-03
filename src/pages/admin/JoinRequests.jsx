import { useEffect, useState, useCallback } from 'react';
import Layout from '../../components/Layout';
import {
  getAllRequests,
  approveRequest,
  rejectRequest,
  downloadDocument,
} from '../../api/joinRequests';

const FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED'];

const STATUS_STYLE = {
  PENDING:  { bg: '#fef3c7', fg: '#92400e' },
  APPROVED: { bg: '#d1fae5', fg: '#065f46' },
  REJECTED: { bg: '#fee2e2', fg: '#991b1b' },
};

const TYPE_BADGE = {
  OWNER:         { bg: '#dbeafe', fg: '#1d4ed8' },
  TENANT:        { bg: '#e0e7ff', fg: '#3730a3' },
  FAMILY_MEMBER: { bg: '#f0fdf4', fg: '#166534' },
};

export default function AdminJoinRequests() {
  const [requests,  setRequests]  = useState([]);
  const [filter,    setFilter]    = useState('PENDING');
  const [loading,   setLoading]   = useState(true);
  const [actionId,  setActionId]  = useState(null); // which row is loading
  const [error,     setError]     = useState('');
  const [success,   setSuccess]   = useState('');

  // Reject modal state
  const [rejectModal, setRejectModal] = useState(null); // holds the request object
  const [rejectReason, setRejectReason] = useState('');
  const [rejecting, setRejecting] = useState(false);

  // PDF preview modal
  const [pdfUrl, setPdfUrl] = useState(null);
  const [pdfName, setPdfName] = useState('');
  const [pdfLoading, setPdfLoading] = useState(false);

  const load = useCallback((status) => {
    setLoading(true);
    setError('');
    getAllRequests(status === 'ALL' ? null : status)
      .then(res => setRequests(res.data ?? []))
      .catch(() => setError('Failed to load join requests.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(filter); }, [filter]);

  // ── Approve ──────────────────────────────────────────────────────────────────
  async function handleApprove(req) {
    setActionId(req.id);
    setError(''); setSuccess('');
    try {
      await approveRequest(req.id);
      setSuccess(`✅ ${req.fullName}'s request for flat ${req.flatNumber} approved.`);
      load(filter);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Approval failed.');
    } finally {
      setActionId(null);
    }
  }

  // ── Reject modal ─────────────────────────────────────────────────────────────
  function openRejectModal(req) {
    setRejectModal(req);
    setRejectReason('');
    setError(''); setSuccess('');
  }

  async function handleRejectConfirm() {
    if (!rejectReason.trim()) {
      setError('Please enter a rejection reason.');
      return;
    }
    setRejecting(true);
    setError('');
    try {
      await rejectRequest(rejectModal.id, rejectReason.trim());
      setSuccess(`❌ ${rejectModal.fullName}'s request rejected.`);
      setRejectModal(null);
      load(filter);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Rejection failed.');
    } finally {
      setRejecting(false);
    }
  }

  // ── PDF preview ──────────────────────────────────────────────────────────────
  // Fetches the blob and creates an object URL so the PDF opens inline
  // in a modal — admin doesn't have to leave the page to verify documents.
  async function handleViewDocument(req) {
    setPdfLoading(true);
    setError('');
    try {
      const res = await downloadDocument(req.id);
      const url = URL.createObjectURL(
        new Blob([res.data], { type: 'application/pdf' })
      );
      setPdfUrl(url);
      setPdfName(req.documentName ?? `document-${req.id}.pdf`);
    } catch {
      setError('Failed to load document. It may not have been uploaded.');
    } finally {
      setPdfLoading(false);
    }
  }

  function closePdf() {
    if (pdfUrl) URL.revokeObjectURL(pdfUrl); // free memory
    setPdfUrl(null);
    setPdfName('');
  }

  const pending = requests.filter(r => r.status === 'PENDING').length;

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>Join Requests</h2>
          <p style={s.sub}>Review, approve or reject flat registration requests.</p>
        </div>
        {pending > 0 && (
          <div style={s.pendingAlert}>
            ⚠️ {pending} request{pending > 1 ? 's' : ''} awaiting review
          </div>
        )}
      </div>

      {error   && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      {/* ── Filter tabs ──────────────────────────────────────────── */}
      <div style={s.tabs}>
        {FILTERS.map(f => (
          <button key={f} onClick={() => setFilter(f)}
            style={{ ...s.tab, ...(filter === f ? s.tabActive : {}) }}>
            {f}
          </button>
        ))}
      </div>

      {loading && <p style={s.info}>Loading…</p>}

      {!loading && requests.length === 0 && (
        <div style={s.empty}>No {filter.toLowerCase()} requests found.</div>
      )}

      {/* ── Request cards ────────────────────────────────────────── */}
      {requests.map(req => (
        <div key={req.id} style={s.card}>

          {/* Header row */}
          <div style={s.cardHeader}>
            <div style={s.headerLeft}>
              <span style={s.name}>{req.fullName}</span>
              <span style={{
                ...s.typeBadge,
                background: TYPE_BADGE[req.residentType]?.bg,
                color: TYPE_BADGE[req.residentType]?.fg,
              }}>
                {req.residentType}
              </span>
            </div>
            <span style={{
              ...s.statusBadge,
              background: STATUS_STYLE[req.status]?.bg,
              color: STATUS_STYLE[req.status]?.fg,
            }}>
              {req.status}
            </span>
          </div>

          {/* Details grid */}
          <div style={s.detailsGrid}>
            <Detail label="Flat"    value={req.flatNumber} />
            <Detail label="Block"   value={req.blockName} />
            <Detail label="Floor"   value={req.floorNumber} />
            <Detail label="Email"   value={req.residentEmail} />
            <Detail label="Phone"   value={req.phoneNumber} />
            <Detail label="Submitted" value={
              req.requestedAt
                ? new Date(req.requestedAt).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
                : '—'
            } />
            {req.reviewedAt && (
              <Detail label="Reviewed" value={
                new Date(req.reviewedAt).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
              } />
            )}
          </div>

          {/* Rejection reason if present */}
          {req.status === 'REJECTED' && req.rejectionReason && (
            <div style={s.rejectReason}>
              <strong>Rejection reason:</strong> {req.rejectionReason}
            </div>
          )}

          {/* Action row */}
          <div style={s.actionRow}>
            {/* Document button — always shown if doc exists */}
            {req.documentName ? (
              <button
                style={s.docBtn}
                disabled={pdfLoading}
                onClick={() => handleViewDocument(req)}
              >
                {pdfLoading ? 'Loading…' : `📎 View Document`}
              </button>
            ) : (
              <span style={s.noDoc}>No document uploaded</span>
            )}

            {/* Approve / Reject — only for PENDING */}
            {req.status === 'PENDING' && (
              <div style={s.decisionBtns}>
                <button
                  style={s.rejectBtn}
                  disabled={actionId === req.id}
                  onClick={() => openRejectModal(req)}
                >
                  Reject
                </button>
                <button
                  style={s.approveBtn}
                  disabled={actionId === req.id}
                  onClick={() => handleApprove(req)}
                >
                  {actionId === req.id ? 'Approving…' : 'Approve ✓'}
                </button>
              </div>
            )}
          </div>
        </div>
      ))}

      {/* ── PDF Preview Modal ────────────────────────────────────── */}
      {pdfUrl && (
        <div style={s.overlay} onClick={closePdf}>
          <div style={s.pdfModal} onClick={e => e.stopPropagation()}>
            <div style={s.pdfHeader}>
              <span style={s.pdfName}>📎 {pdfName}</span>
              <div style={s.pdfActions}>
                <a href={pdfUrl} download={pdfName} style={s.downloadLink}>
                  ⬇ Download
                </a>
                <button style={s.closeBtn} onClick={closePdf}>✕ Close</button>
              </div>
            </div>
            <iframe
              src={pdfUrl}
              title="Document Preview"
              style={s.pdfFrame}
            />
          </div>
        </div>
      )}

      {/* ── Reject Reason Modal ──────────────────────────────────── */}
      {rejectModal && (
        <div style={s.overlay} onClick={() => setRejectModal(null)}>
          <div style={s.rejectModalBox} onClick={e => e.stopPropagation()}>
            <h3 style={s.modalTitle}>Reject Request</h3>
            <p style={s.modalSub}>
              Rejecting <strong>{rejectModal.fullName}</strong>'s request for flat{' '}
              <strong>{rejectModal.flatNumber}</strong>.
              The resident will be notified by email.
            </p>
            <label style={s.label}>Reason for rejection *</label>
            <textarea
              rows={4}
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
              placeholder="e.g. Flat already occupied by another approved resident"
              style={s.textarea}
            />
            {error && <div style={s.errorBox}>{error}</div>}
            <div style={s.modalActions}>
              <button
                style={s.cancelBtn}
                onClick={() => setRejectModal(null)}
                disabled={rejecting}
              >
                Cancel
              </button>
              <button
                style={s.confirmRejectBtn}
                onClick={handleRejectConfirm}
                disabled={rejecting}
              >
                {rejecting ? 'Rejecting…' : 'Confirm Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

function Detail({ label, value }) {
  return (
    <div style={s.detail}>
      <span style={s.detailLabel}>{label}</span>
      <span style={s.detailValue}>{value ?? '—'}</span>
    </div>
  );
}

const s = {
  topRow:       { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  heading:      { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:          { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  pendingAlert: { background: '#fef3c7', color: '#92400e', padding: '8px 16px', borderRadius: 8, fontSize: 13, fontWeight: 600, border: '1px solid #fde68a' },
  errorBox:     { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:   { background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  tabs:         { display: 'flex', gap: 6, margin: '20px 0 16px', borderBottom: '2px solid #e2e8f0', paddingBottom: 0 },
  tab:          { padding: '8px 18px', borderRadius: '6px 6px 0 0', border: 'none', background: 'none', fontSize: 13, fontWeight: 500, color: '#64748b', cursor: 'pointer', marginBottom: -2 },
  tabActive:    { background: '#fff', borderTop: '2px solid #2563eb', borderLeft: '2px solid #e2e8f0', borderRight: '2px solid #e2e8f0', color: '#2563eb', fontWeight: 600 },
  info:         { color: '#64748b' },
  empty:        { background: '#fff', borderRadius: 10, padding: '2.5rem', textAlign: 'center', color: '#94a3b8', border: '2px dashed #e2e8f0' },
  card:         { background: '#fff', borderRadius: 10, padding: '1.25rem', marginBottom: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  cardHeader:   { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
  headerLeft:   { display: 'flex', alignItems: 'center', gap: 10 },
  name:         { fontSize: 16, fontWeight: 600, color: '#0f172a' },
  typeBadge:    { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20 },
  statusBadge:  { fontSize: 12, fontWeight: 600, padding: '4px 12px', borderRadius: 20 },
  detailsGrid:  { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px 0', marginBottom: 14, background: '#f8fafc', borderRadius: 8, padding: '12px 14px' },
  detail:       { display: 'flex', flexDirection: 'column', gap: 2 },
  detailLabel:  { fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: 600 },
  detailValue:  { fontSize: 13, color: '#1e293b', fontWeight: 500 },
  rejectReason: { background: '#fef2f2', borderRadius: 6, padding: '8px 12px', fontSize: 13, color: '#dc2626', marginBottom: 12 },
  actionRow:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: 12, borderTop: '1px solid #f1f5f9' },
  docBtn:       { padding: '7px 14px', borderRadius: 6, background: '#f1f5f9', border: '1px solid #e2e8f0', fontSize: 13, fontWeight: 500, color: '#1e293b', cursor: 'pointer' },
  noDoc:        { fontSize: 13, color: '#94a3b8' },
  decisionBtns: { display: 'flex', gap: 8 },
  rejectBtn:    { padding: '7px 18px', borderRadius: 6, background: '#fff', border: '1.5px solid #dc2626', color: '#dc2626', fontWeight: 600, fontSize: 13, cursor: 'pointer' },
  approveBtn:   { padding: '7px 18px', borderRadius: 6, background: '#059669', border: 'none', color: '#fff', fontWeight: 600, fontSize: 13, cursor: 'pointer' },
  // PDF modal
  overlay:      { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' },
  pdfModal:     { background: '#fff', borderRadius: 12, width: '100%', maxWidth: 900, height: '90vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  pdfHeader:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 18px', borderBottom: '1px solid #e2e8f0' },
  pdfName:      { fontSize: 14, fontWeight: 600, color: '#1e293b' },
  pdfActions:   { display: 'flex', gap: 10, alignItems: 'center' },
  downloadLink: { padding: '6px 14px', borderRadius: 6, background: '#f1f5f9', color: '#1e293b', fontSize: 13, fontWeight: 500, textDecoration: 'none', border: '1px solid #e2e8f0' },
  closeBtn:     { padding: '6px 14px', borderRadius: 6, background: '#1e293b', color: '#fff', fontSize: 13, fontWeight: 500, border: 'none', cursor: 'pointer' },
  pdfFrame:     { flex: 1, border: 'none', width: '100%' },
  // Reject modal
  rejectModalBox: { background: '#fff', borderRadius: 12, padding: '1.75rem', width: '100%', maxWidth: 480, boxShadow: '0 20px 60px rgba(0,0,0,0.2)' },
  modalTitle:   { margin: '0 0 8px', fontSize: 18, fontWeight: 700, color: '#0f172a' },
  modalSub:     { margin: '0 0 18px', fontSize: 14, color: '#64748b', lineHeight: 1.5 },
  label:        { fontSize: 13, fontWeight: 500, color: '#374151', display: 'block', marginBottom: 6 },
  textarea:     { width: '100%', padding: '10px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, fontFamily: 'inherit', resize: 'vertical', boxSizing: 'border-box' },
  modalActions: { display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 16 },
  cancelBtn:    { padding: '9px 20px', borderRadius: 7, background: '#f1f5f9', border: 'none', fontSize: 13, fontWeight: 500, color: '#64748b', cursor: 'pointer' },
  confirmRejectBtn: { padding: '9px 20px', borderRadius: 7, background: '#dc2626', border: 'none', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' },
};
