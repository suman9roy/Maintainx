import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import {
  getAllBills,
  generateBill,
  markBillPaid,
  getTotalCollected,
} from '../../api/maintenance';

const MONTHS = [
  'JANUARY','FEBRUARY','MARCH','APRIL','MAY','JUNE',
  'JULY','AUGUST','SEPTEMBER','OCTOBER','NOVEMBER','DECEMBER',
];

// TODO: confirm these match the backend PaymentMode enum exactly
const PAYMENT_MODES = ['ONLINE',
    'CASH',
    'UPI',
    'BANK_TRANSFER'];

const STATUS_STYLE = {
  PENDING: { bg: '#fef3c7', fg: '#92400e' },
  PAID:    { bg: '#d1fae5', fg: '#065f46' },
};

const EMPTY_FORM = {
  flatNumber: '', amount: '', month: 'JANUARY',
  year: new Date().getFullYear(), dueDate: '',
};

export default function AdminBills() {
  const [bills,          setBills]          = useState([]);
  const [filtered,       setFiltered]       = useState([]);
  const [totalCollected, setTotalCollected] = useState(0);
  const [loading,        setLoading]        = useState(true);
  const [showForm,       setShowForm]       = useState(false);
  const [form,           setForm]           = useState(EMPTY_FORM);
  const [generating,     setGenerating]     = useState(false);
  const [error,          setError]          = useState('');
  const [success,        setSuccess]        = useState('');
  const [search,         setSearch]         = useState('');
  const [statusFilter,   setStatusFilter]   = useState('ALL');

  // Mark-paid modal state
  const [payModal,    setPayModal]    = useState(null); // holds the bill being marked paid
  const [paymentMode, setPaymentMode] = useState(PAYMENT_MODES[0]);
  const [remarks,     setRemarks]     = useState('');
  const [marking,     setMarking]     = useState(false);

  useEffect(() => { load(); }, []);

  useEffect(() => {
    let result = [...bills];
    if (statusFilter !== 'ALL') {
      result = result.filter(b => b.paymentStatus === statusFilter);
    }
    const q = search.toLowerCase().trim();
    if (q) {
      result = result.filter(b =>
        b.flatNumber?.toLowerCase().includes(q) ||
        b.month?.toLowerCase().includes(q)
      );
    }
    setFiltered(result);
  }, [bills, statusFilter, search]);

  function load() {
    setLoading(true);
    Promise.all([getAllBills(), getTotalCollected()])
      .then(([billsRes, totalRes]) => {
        setBills(billsRes.data ?? []);
        setTotalCollected(totalRes.data ?? 0);
      })
      .catch(() => setError('Failed to load bills.'))
      .finally(() => setLoading(false));
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleGenerate(e) {
    e.preventDefault();
    setGenerating(true);
    setError(''); setSuccess('');
    try {
      await generateBill({
        ...form,
        amount: Number(form.amount),
        year:   Number(form.year),
      });
      setSuccess(`Bill generated for flat ${form.flatNumber} — ${form.month} ${form.year}.`);
      setForm(EMPTY_FORM);
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to generate bill.');
    } finally {
      setGenerating(false);
    }
  }

  // ── Mark paid modal ───────────────────────────────────────────────────────
  function openPayModal(bill) {
    setPayModal(bill);
    setPaymentMode(PAYMENT_MODES[0]);
    setRemarks('');
    setError('');
  }

  async function handleMarkPaidConfirm() {
    setMarking(true);
    setError(''); setSuccess('');
    try {
      await markBillPaid(payModal.id, paymentMode, remarks.trim() || undefined);
      setSuccess(
        `Bill for flat ${payModal.flatNumber} (${payModal.month} ${payModal.year}) marked as PAID via ${paymentMode}.`
      );
      setPayModal(null);
      load();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to mark bill as paid.');
    } finally {
      setMarking(false);
    }
  }

  const pendingCount = bills.filter(b => b.paymentStatus === 'PENDING').length;
  const paidCount    = bills.filter(b => b.paymentStatus === 'PAID').length;

  return (
    <Layout>

      {/* ── Header ──────────────────────────────────────────────── */}
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>Maintenance Billing</h2>
          <p style={s.sub}>Generate bills and track payment status.</p>
        </div>
        <button style={s.newBtn} onClick={() => { setShowForm(!showForm); setError(''); }}>
          {showForm ? '✕ Cancel' : '+ Generate Bill'}
        </button>
      </div>

      {error   && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      {/* ── Summary row ─────────────────────────────────────────── */}
      <div style={s.summaryRow}>
        <div style={s.summaryCard}>
          <div style={{ ...s.summaryVal, color: '#2563eb' }}>{bills.length}</div>
          <div style={s.summaryLabel}>Total Bills</div>
        </div>
        <div style={s.summaryCard}>
          <div style={{ ...s.summaryVal, color: '#d97706' }}>{pendingCount}</div>
          <div style={s.summaryLabel}>Pending</div>
        </div>
        <div style={s.summaryCard}>
          <div style={{ ...s.summaryVal, color: '#059669' }}>{paidCount}</div>
          <div style={s.summaryLabel}>Paid</div>
        </div>
        <div style={s.summaryCard}>
          <div style={{ ...s.summaryVal, color: '#059669' }}>
            ₹{Number(totalCollected).toLocaleString('en-IN')}
          </div>
          <div style={s.summaryLabel}>Total Collected</div>
        </div>
      </div>

      {/* ── Generate bill form ───────────────────────────────────── */}
      {showForm && (
        <form onSubmit={handleGenerate} style={s.form}>
          <h3 style={s.formTitle}>New Maintenance Bill</h3>
          <div style={s.formGrid}>
            <div style={s.field}>
              <label style={s.label}>Flat Number *</label>
              <input name="flatNumber" required value={form.flatNumber}
                onChange={handleChange} style={s.input} placeholder="B-204" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Amount (₹) *</label>
              <input name="amount" type="number" required min="1"
                value={form.amount} onChange={handleChange}
                style={s.input} placeholder="2500" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Month *</label>
              <select name="month" value={form.month}
                onChange={handleChange} style={s.select}>
                {MONTHS.map(m => <option key={m} value={m}>{m}</option>)}
              </select>
            </div>
            <div style={s.field}>
              <label style={s.label}>Year *</label>
              <input name="year" type="number" required
                value={form.year} onChange={handleChange}
                style={s.input} placeholder="2026" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Due Date *</label>
              <input name="dueDate" type="date" required
                value={form.dueDate} onChange={handleChange} style={s.input} />
            </div>
          </div>
          <button type="submit" disabled={generating} style={s.submitBtn}>
            {generating ? 'Generating…' : 'Generate Bill'}
          </button>
        </form>
      )}

      {/* ── Filters ─────────────────────────────────────────────── */}
      <div style={s.filtersRow}>
        <input
          style={s.search}
          placeholder="🔍  Search by flat or month…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div style={s.tabs}>
          {['ALL', 'PENDING', 'PAID'].map(f => (
            <button key={f} onClick={() => setStatusFilter(f)}
              style={{ ...s.tab, ...(statusFilter === f ? s.tabActive : {}) }}>
              {f}
            </button>
          ))}
        </div>
      </div>

      {loading && <p style={s.info}>Loading bills…</p>}

      {!loading && filtered.length === 0 && (
        <div style={s.empty}>
          {search || statusFilter !== 'ALL'
            ? 'No bills match your filter.'
            : 'No bills generated yet. Click "+ Generate Bill" to start.'}
        </div>
      )}

      {/* ── Bills table ─────────────────────────────────────────── */}
      {filtered.length > 0 && (
        <div style={s.tableWrap}>
          <table style={s.table}>
            <thead>
              <tr style={s.thead}>
                <th style={s.th}>Flat</th>
                <th style={s.th}>Period</th>
                <th style={s.th}>Amount</th>
                <th style={s.th}>Due Date</th>
                <th style={s.th}>Status</th>
                <th style={s.th}>Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(bill => (
                <tr key={bill.id} style={s.tr}>
                  <td style={{ ...s.td, fontWeight: 600 }}>{bill.flatNumber}</td>
                  <td style={s.td}>{bill.month} {bill.year}</td>
                  <td style={{ ...s.td, fontWeight: 600, color: '#0f172a' }}>
                    ₹{Number(bill.amount).toLocaleString('en-IN')}
                  </td>
                  <td style={s.td}>
                    {bill.dueDate
                      ? new Date(bill.dueDate).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
                      : '—'}
                  </td>
                  <td style={s.td}>
                    <span style={{
                      ...s.statusBadge,
                      background: STATUS_STYLE[bill.paymentStatus]?.bg,
                      color:      STATUS_STYLE[bill.paymentStatus]?.fg,
                    }}>
                      {bill.paymentStatus}
                    </span>
                  </td>
                  <td style={s.td}>
                    {bill.paymentStatus === 'PENDING' ? (
                      <button style={s.markPaidBtn} onClick={() => openPayModal(bill)}>
                        Mark Paid
                      </button>
                    ) : (
                      <span style={s.paidText}>✓ Paid</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Mark Paid Modal ───────────────────────────────────────── */}
      {payModal && (
        <div style={s.overlay} onClick={() => !marking && setPayModal(null)}>
          <div style={s.modal} onClick={e => e.stopPropagation()}>
            <h3 style={s.modalTitle}>Mark Bill as Paid</h3>
            <p style={s.modalSub}>
              Flat <strong>{payModal.flatNumber}</strong> · {payModal.month} {payModal.year} ·{' '}
              <strong>₹{Number(payModal.amount).toLocaleString('en-IN')}</strong>
            </p>

            <label style={s.label}>Payment Mode *</label>
            <select
              value={paymentMode}
              onChange={e => setPaymentMode(e.target.value)}
              style={s.select}
            >
              {PAYMENT_MODES.map(m => <option key={m} value={m}>{m}</option>)}
            </select>

            <label style={{ ...s.label, marginTop: 12 }}>Remarks (optional)</label>
            <textarea
              rows={3}
              value={remarks}
              onChange={e => setRemarks(e.target.value)}
              placeholder="e.g. Paid in person at society office"
              style={s.textarea}
            />

            {error && <div style={s.errorBox}>{error}</div>}

            <div style={s.modalActions}>
              <button
                style={s.cancelBtn}
                onClick={() => setPayModal(null)}
                disabled={marking}
              >
                Cancel
              </button>
              <button
                style={s.confirmPayBtn}
                onClick={handleMarkPaidConfirm}
                disabled={marking}
              >
                {marking ? 'Marking…' : 'Confirm Paid'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

const s = {
  topRow:      { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 },
  heading:     { margin: 0, fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:         { margin: '4px 0 0', color: '#64748b', fontSize: 14 },
  newBtn:      { padding: '9px 20px', borderRadius: 8, background: '#2563eb', color: '#fff', fontWeight: 600, fontSize: 13, border: 'none', cursor: 'pointer' },
  errorBox:    { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:  { background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  summaryRow:  { display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 12, marginBottom: 24 },
  summaryCard: { background: '#fff', borderRadius: 10, padding: '1rem', textAlign: 'center', boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  summaryVal:  { fontSize: 26, fontWeight: 700, marginBottom: 4 },
  summaryLabel:{ fontSize: 12, color: '#64748b' },
  form:        { background: '#fff', borderRadius: 10, padding: '1.5rem', marginBottom: 20, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  formTitle:   { margin: '0 0 16px', fontSize: 16, fontWeight: 600, color: '#1e293b' },
  formGrid:    { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px,1fr))', gap: 14, marginBottom: 16 },
  field:       { display: 'flex', flexDirection: 'column', gap: 5 },
  label:       { fontSize: 13, fontWeight: 500, color: '#374151', display: 'block', marginBottom: 6 },
  input:       { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, outline: 'none' },
  select:      { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, background: '#fff', width: '100%', boxSizing: 'border-box' },
  textarea:    { width: '100%', padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, fontFamily: 'inherit', resize: 'vertical', boxSizing: 'border-box', marginTop: 6 },
  submitBtn:   { padding: '10px 28px', borderRadius: 7, background: '#059669', color: '#fff', fontWeight: 600, fontSize: 14, border: 'none', cursor: 'pointer' },
  filtersRow:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, gap: 12 },
  search:      { padding: '9px 14px', borderRadius: 8, border: '1.5px solid #e2e8f0', fontSize: 14, width: 260, outline: 'none' },
  tabs:        { display: 'flex', gap: 6 },
  tab:         { padding: '7px 16px', borderRadius: 6, border: '1px solid #e2e8f0', background: '#fff', fontSize: 13, fontWeight: 500, color: '#64748b', cursor: 'pointer' },
  tabActive:   { background: '#1e293b', color: '#fff', borderColor: '#1e293b' },
  info:        { color: '#64748b' },
  empty:       { background: '#fff', borderRadius: 10, padding: '2.5rem', textAlign: 'center', color: '#94a3b8', border: '2px dashed #e2e8f0' },
  tableWrap:   { background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  table:       { width: '100%', borderCollapse: 'collapse' },
  thead:       { background: '#f8fafc' },
  th:          { padding: '11px 16px', textAlign: 'left', fontSize: 12, fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px', borderBottom: '1px solid #e2e8f0' },
  tr:          { borderBottom: '1px solid #f1f5f9' },
  td:          { padding: '13px 16px', fontSize: 14, color: '#374151' },
  statusBadge: { fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 20 },
  markPaidBtn: { padding: '6px 14px', borderRadius: 6, background: '#059669', color: '#fff', border: 'none', fontSize: 12, fontWeight: 600, cursor: 'pointer' },
  paidText:    { fontSize: 13, color: '#059669', fontWeight: 500 },
  // Modal
  overlay:     { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' },
  modal:       { background: '#fff', borderRadius: 12, padding: '1.75rem', width: '100%', maxWidth: 420, boxShadow: '0 20px 60px rgba(0,0,0,0.2)' },
  modalTitle:  { margin: '0 0 8px', fontSize: 18, fontWeight: 700, color: '#0f172a' },
  modalSub:    { margin: '0 0 18px', fontSize: 14, color: '#64748b', lineHeight: 1.5 },
  modalActions:{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 16 },
  cancelBtn:   { padding: '9px 20px', borderRadius: 7, background: '#f1f5f9', border: 'none', fontSize: 13, fontWeight: 500, color: '#64748b', cursor: 'pointer' },
  confirmPayBtn: { padding: '9px 20px', borderRadius: 7, background: '#059669', border: 'none', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' },
};
