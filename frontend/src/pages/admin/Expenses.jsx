import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAllExpenses, addExpense, getFundSummary } from '../../api/expenses';

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [adding, setAdding] = useState(false);
  const [form, setForm] = useState({ title: '', category: 'STAFF_SALARY', amount: '', description: '', expenseDate: '' });
  const [fund, setFund] = useState(null);

  useEffect(() => { load(); loadFund(); }, []);

  function load() {
    setLoading(true); setError('');
    getAllExpenses()
      .then(res => setExpenses(res.data ?? []))
      .catch(() => setError('Failed to load expenses.'))
      .finally(() => setLoading(false));
  }

  function loadFund() {
    getFundSummary().then(res => setFund(res.data)).catch(() => {});
  }

  async function handleAdd(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    setAdding(true);
    try {
      // basic validation
      if (!form.title || !form.category || !form.amount || !form.expenseDate) {
        setError('Please fill title, category, amount and date.');
        setAdding(false);
        return;
      }
      const payload = {
        title: form.title,
        category: form.category,
        amount: Number(form.amount),
        description: form.description,
        expenseDate: form.expenseDate,
      };
      await addExpense(payload);
      setSuccess('Expense added.');
      setForm({ title: '', category: form.category, amount: '', description: '', expenseDate: '' });
      load(); loadFund();
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to add expense.');
    } finally { setAdding(false); }
  }

  return (
    <Layout>
      <div style={s.topRow}>
        <div>
          <h2 style={s.heading}>Expenses</h2>
          <p style={s.sub}>Add and review expenses, and view fund summary.</p>
        </div>
      </div>

      {error && <div style={s.errorBox}>{error}</div>}
      {success && <div style={s.successBox}>{success}</div>}

      <div style={s.form}>
        <h3 style={s.formTitle}>Add Expense</h3>
        <div style={s.row}>
          <div style={s.field}>
            <label style={s.label}>Title *</label>
            <input value={form.title} onChange={e => setForm({...form, title: e.target.value})} style={s.input} />
          </div>
          <div style={s.field}>
            <label style={s.label}>Category *</label>
            <select value={form.category} onChange={e => setForm({...form, category: e.target.value})} style={s.select}>
              <option value="STAFF_SALARY">STAFF_SALARY</option>
              <option value="WATER_BILL">WATER_BILL</option>
              <option value="ELECTRICITY">ELECTRICITY</option>
              <option value="LIFT_MAINTENANCE">LIFT_MAINTENANCE</option>
              <option value="SECURITY">SECURITY</option>
              <option value="REPAIR">REPAIR</option>
              <option value="CLEANING">CLEANING</option>
              <option value="FESTIVAL">FESTIVAL</option>
              <option value="EMERGENCY_FUND">EMERGENCY_FUND</option>
            </select>
          </div>
        </div>
        <div style={s.row}>
          <div style={s.field}>
            <label style={s.label}>Amount *</label>
            <input type="number" value={form.amount} onChange={e => setForm({...form, amount: e.target.value})} style={s.input} />
          </div>
          <div style={s.field}>
            <label style={s.label}>Expense Date *</label>
            <input type="date" value={form.expenseDate} onChange={e => setForm({...form, expenseDate: e.target.value})} style={s.input} />
          </div>
        </div>
        <div style={s.field}>
          <label style={s.label}>Description</label>
          <textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})} style={s.textarea} rows={3} />
        </div>
        <button onClick={handleAdd} disabled={adding} style={s.submitBtn}>{adding ? 'Adding…' : 'Add Expense'}</button>
      </div>

      <div style={{ marginTop: 20 }}>
        <h3 style={{ marginBottom: 8 }}>Fund Summary</h3>
        {fund ? (
          <div style={s.fundRow}>
            <div>Total Collection: ₹{fund.totalCollection?.toFixed(2)}</div>
            <div>Total Expenses: ₹{fund.totalExpenses?.toFixed(2)}</div>
            <div>Remaining: ₹{fund.remainingFund?.toFixed(2)}</div>
          </div>
        ) : <div style={s.info}>Unable to load fund summary.</div>}
      </div>

      <div style={{ marginTop: 20 }}>
        <h3 style={{ marginBottom: 8 }}>All Expenses</h3>
        {loading && <div style={s.info}>Loading…</div>}
        {!loading && expenses.length === 0 && <div style={s.empty}>No expenses yet.</div>}
        {expenses.map(x => (
          <div key={x.id} style={s.card}>
            <div style={s.cardTop}>
              <div>
                <div style={s.title}>{x.title}</div>
                <div style={s.meta}>{x.category} · ₹{x.amount}</div>
              </div>
              <div style={s.date}>{x.expenseDate}</div>
            </div>
            <p style={s.description}>{x.description}</p>
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
  fundRow:  { display: 'flex', gap: 24, background: '#fff', padding: 12, borderRadius: 8 },
  info:     { color: '#64748b' },
  empty:    { background: '#fff', borderRadius: 8, padding: 12, color: '#64748b' },
  card:     { background: '#fff', borderRadius: 8, padding: 12, marginBottom: 10, boxShadow: '0 1px 3px rgba(0,0,0,0.04)' },
  cardTop:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  title:    { fontSize: 14, fontWeight: 700 },
  meta:     { fontSize: 12, color: '#64748b' },
  description: { fontSize: 13, color: '#374151' },
  date:     { fontSize: 12, color: '#94a3b8' },
  errorBox: { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox:{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
};
