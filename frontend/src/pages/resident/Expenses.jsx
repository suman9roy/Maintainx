import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { getAllExpenses, getFundSummary } from '../../api/expenses';

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [summary,  setSummary]  = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');

  useEffect(() => {
    Promise.all([getAllExpenses(), getFundSummary()])
      .then(([e, s]) => {
        setExpenses(e.data ?? []);
        setSummary(s.data ?? null);
      })
      .catch(() => setError('Failed to load expenses.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <h2 style={s.heading}>Society Expenses</h2>
      <p style={s.sub}>Transparent view of how society funds are spent.</p>

      {error   && <div style={s.error}>{error}</div>}
      {loading && <p style={s.info}>Loading…</p>}

      {/* Fund summary */}
      {summary && (
        <div style={s.summaryRow}>
          {[
            { label: 'Total Collected', value: summary.totalCollection, color: '#059669' },
            { label: 'Total Expenses',  value: summary.totalExpenses,   color: '#dc2626' },
            { label: 'Remaining Fund',  value: summary.remainingFund,   color: '#2563eb' },
          ].map(item => (
            <div key={item.label} style={s.summaryCard}>
              <div style={{ ...s.summaryVal, color: item.color }}>
                ₹{Number(item.value ?? 0).toLocaleString('en-IN')}
              </div>
              <div style={s.summaryLabel}>{item.label}</div>
            </div>
          ))}
        </div>
      )}

      {/* Expense list */}
      {expenses.map(e => (
        <div key={e.id} style={s.card}>
          <div style={s.cardLeft}>
            <div style={s.expTitle}>{e.title}</div>
            <div style={s.expMeta}>{e.category} · {e.expenseDate ? new Date(e.expenseDate).toLocaleDateString() : ''}</div>
            {e.description && <div style={s.expDesc}>{e.description}</div>}
          </div>
          <div style={s.amount}>₹{Number(e.amount).toLocaleString('en-IN')}</div>
        </div>
      ))}

      {!loading && expenses.length === 0 && (
        <div style={s.empty}><p>No expenses recorded yet.</p></div>
      )}
    </Layout>
  );
}

const s = {
  heading:     { margin: '0 0 4px', fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:         { margin: '0 0 24px', color: '#64748b', fontSize: 14 },
  info:        { color: '#64748b' },
  error:       { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 16 },
  summaryRow:  { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 14, marginBottom: 24 },
  summaryCard: { background: '#fff', borderRadius: 10, padding: '1.2rem', textAlign: 'center', boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  summaryVal:  { fontSize: 28, fontWeight: 700, marginBottom: 4 },
  summaryLabel:{ fontSize: 12, color: '#64748b' },
  card:        { background: '#fff', borderRadius: 10, padding: '1rem 1.25rem', marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 4px rgba(0,0,0,0.07)' },
  cardLeft:    { flex: 1 },
  expTitle:    { fontSize: 15, fontWeight: 600, color: '#0f172a', marginBottom: 2 },
  expMeta:     { fontSize: 12, color: '#64748b' },
  expDesc:     { fontSize: 13, color: '#374151', marginTop: 4 },
  amount:      { fontSize: 18, fontWeight: 700, color: '#dc2626', marginLeft: 16 },
  empty:       { background: '#fff', borderRadius: 10, padding: '2rem', textAlign: 'center', color: '#64748b', border: '2px dashed #e2e8f0' },
};
