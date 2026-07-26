import { useState } from 'react';
import Layout from '../../components/Layout';
import { submitJoinRequest } from '../../api/joinRequests';

const RESIDENT_TYPES = ['OWNER', 'TENANT', 'FAMILY_MEMBER'];

export default function JoinRequest() {
  const [form, setForm] = useState({
    fullName: '', phoneNumber: '', residentEmail: '',
    flatNumber: '', blockName: '', floorNumber: '',
    residentType: 'OWNER',
  });
  const [document, setDocument] = useState(null);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState('');
  const [success,  setSuccess]  = useState('');

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  function handleFile(e) {
    const file = e.target.files[0];
    if (file && file.type !== 'application/pdf') {
      setError('Only PDF files are accepted.');
      e.target.value = '';
      return;
    }
    if (file && file.size > 5 * 1024 * 1024) {
      setError('File must be under 5MB.');
      e.target.value = '';
      return;
    }
    setDocument(file);
    setError('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (form.residentType !== 'FAMILY_MEMBER' && !document) {
      setError(`A document (flat deed or rental agreement) is required for ${form.residentType}.`);
      return;
    }

    setLoading(true);
    try {
      await submitJoinRequest({ ...form, floorNumber: Number(form.floorNumber) }, document);
      setSuccess('Join request submitted successfully! The admin will review your request.');
      setForm({
        fullName: '', phoneNumber: '', residentEmail: '',
        flatNumber: '', blockName: '', floorNumber: '', residentType: 'OWNER',
      });
      setDocument(null);
    } catch (err) {
      setError(err.response?.data?.message ?? 'Submission failed. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  const needsDoc = form.residentType !== 'FAMILY_MEMBER';

  return (
    <Layout>
      <div style={s.wrap}>
        <h2 style={s.heading}>Submit Join Request</h2>
        <p style={s.sub}>
          Fill in your details and upload the required document.
          The admin will verify and approve your request.
        </p>

        <form onSubmit={handleSubmit} style={s.form}>

          <div style={s.row}>
            <div style={s.field}>
              <label style={s.label}>Full Name *</label>
              <input name="fullName" required value={form.fullName}
                onChange={handleChange} style={s.input} placeholder="Rahul Sharma" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Phone Number *</label>
              <input name="phoneNumber" required value={form.phoneNumber}
                onChange={handleChange} style={s.input} placeholder="9876543210" />
            </div>
          </div>

          <div style={s.field}>
            <label style={s.label}>Resident Email *</label>
            <input name="residentEmail" type="email" required value={form.residentEmail}
              onChange={handleChange} style={s.input} placeholder="rahul@example.com" />
          </div>

          <div style={s.row}>
            <div style={s.field}>
              <label style={s.label}>Flat Number *</label>
              <input name="flatNumber" required value={form.flatNumber}
                onChange={handleChange} style={s.input} placeholder="B-204" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Block Name *</label>
              <input name="blockName" required value={form.blockName}
                onChange={handleChange} style={s.input} placeholder="B" />
            </div>
            <div style={s.field}>
              <label style={s.label}>Floor Number *</label>
              <input name="floorNumber" type="number" required value={form.floorNumber}
                onChange={handleChange} style={s.input} placeholder="2" />
            </div>
          </div>

          <div style={s.field}>
            <label style={s.label}>Resident Type *</label>
            <select name="residentType" value={form.residentType}
              onChange={handleChange} style={s.select}>
              {RESIDENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>

          {/* Document upload — only shown when needed */}
          <div style={{ ...s.docBox, borderColor: needsDoc ? '#2563eb' : '#e2e8f0' }}>
            <label style={s.label}>
              {needsDoc
                ? `Document required for ${form.residentType} *`
                : 'Document (not required for FAMILY_MEMBER)'}
            </label>
            <input
              type="file" accept="application/pdf"
              disabled={!needsDoc}
              onChange={handleFile}
              style={s.fileInput}
            />
            <p style={s.docHint}>
              {form.residentType === 'OWNER'   && 'Upload flat deed / sale agreement (PDF, max 5MB)'}
              {form.residentType === 'TENANT'  && 'Upload rental / lease agreement (PDF, max 5MB)'}
              {form.residentType === 'FAMILY_MEMBER' && 'No document required for family members.'}
            </p>
            {document && <p style={s.fileChosen}>📎 {document.name}</p>}
          </div>

          {error   && <div style={s.error}>{error}</div>}
          {success && <div style={s.successBox}>{success}</div>}

          <button type="submit" disabled={loading} style={s.btn}>
            {loading ? 'Submitting…' : 'Submit Join Request'}
          </button>
        </form>
      </div>
    </Layout>
  );
}

const s = {
  wrap:    { maxWidth: 680 },
  heading: { margin: '0 0 4px', fontSize: 24, fontWeight: 700, color: '#0f172a' },
  sub:     { margin: '0 0 28px', color: '#64748b', fontSize: 14, lineHeight: 1.5 },
  form:    { background: '#fff', borderRadius: 12, padding: '2rem', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  row:     { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px,1fr))', gap: 14, marginBottom: 16 },
  field:   { display: 'flex', flexDirection: 'column', gap: 5, marginBottom: 16 },
  label:   { fontSize: 13, fontWeight: 500, color: '#374151' },
  input:   { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, outline: 'none' },
  select:  { padding: '9px 12px', borderRadius: 7, border: '1.5px solid #ddd', fontSize: 14, background: '#fff' },
  docBox:  { border: '1.5px dashed', borderRadius: 8, padding: '1.25rem', marginBottom: 16 },
  docHint: { margin: '6px 0 0', fontSize: 12, color: '#64748b' },
  fileInput: { marginTop: 8, fontSize: 13 },
  fileChosen: { margin: '8px 0 0', fontSize: 13, color: '#059669', fontWeight: 500 },
  btn:     { padding: '11px 0', width: '100%', borderRadius: 8, background: '#2563eb', color: '#fff', fontWeight: 600, fontSize: 15, border: 'none', cursor: 'pointer' },
  error:   { background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
  successBox: { background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '10px 14px', borderRadius: 7, fontSize: 13, marginBottom: 14 },
};
