import { useEffect, useMemo, useState } from 'react';
import Layout from '../../components/Layout';
import { deleteDocument, getDocuments, uploadDocument } from '../../api/aiChat';

const initialState = {
  uploadError: '',
  uploadSuccess: '',
  loading: true,
  documents: [],
  uploading: false,
};

export default function KnowledgeBase() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);

  const loadDocuments = async () => {
    try {
      const { data } = await getDocuments();
      setDocuments(Array.isArray(data) ? data : data?.documents ?? []);
      setUploadError('');
    } catch (error) {
      setUploadError(error?.response?.data?.message || 'Unable to load documents.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const canUpload = useMemo(() => Boolean(selectedFile), [selectedFile]);

  const handleUpload = async (event) => {
    event.preventDefault();
    if (!selectedFile) return;

    setUploading(true);
    setUploadError('');
    setUploadSuccess('');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      await uploadDocument(formData);
      setSelectedFile(null);
      setUploadSuccess('Document uploaded successfully.');
      await loadDocuments();
    } catch (error) {
      setUploadError(error?.response?.data?.message || 'Upload failed.');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (documentId) => {
    if (!window.confirm('Delete this document from the knowledge base?')) return;

    try {
      await deleteDocument(documentId);
      setDocuments((prev) => prev.filter((doc) => doc.id !== documentId));
      setUploadSuccess('Document deleted.');
    } catch (error) {
      setUploadError(error?.response?.data?.message || 'Unable to delete document.');
    }
  };

  return (
    <Layout>
      <div style={styles.page}>
        <div style={styles.header}>
          <div>
            <h2 style={styles.title}>AI Knowledge Base</h2>
            <p style={styles.subtitle}>Upload PDFs so the assistant can answer questions using your society documents.</p>
          </div>
        </div>

        <form onSubmit={handleUpload} style={styles.card}>
          <h3 style={styles.cardTitle}>Upload document</h3>
          <input
            type="file"
            accept="application/pdf"
            onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
            style={styles.input}
          />
          <button type="submit" disabled={!canUpload || uploading} style={styles.primaryButton}>
            {uploading ? 'Uploading…' : 'Upload PDF'}
          </button>
          {uploadError && <p style={styles.error}>{uploadError}</p>}
          {uploadSuccess && <p style={styles.success}>{uploadSuccess}</p>}
        </form>

        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <h3 style={styles.cardTitle}>Knowledge base documents</h3>
            <span style={styles.count}>{documents.length} item{documents.length === 1 ? '' : 's'}</span>
          </div>

          {loading ? (
            <p style={styles.empty}>Loading documents…</p>
          ) : documents.length === 0 ? (
            <p style={styles.empty}>No documents uploaded yet.</p>
          ) : (
            <div style={styles.list}>
              {documents.map((doc) => (
                <div key={doc.id} style={styles.row}>
                  <div>
                    <div style={styles.rowTitle}>{doc.name || doc.title || 'Untitled document'}</div>
                    <div style={styles.rowMeta}>{doc.status || 'Ready'}</div>
                  </div>
                  <button style={styles.deleteButton} onClick={() => handleDelete(doc.id)}>
                    Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

const styles = {
  page: { display: 'flex', flexDirection: 'column', gap: 20 },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  title: { margin: '0 0 4px', fontSize: 26, fontWeight: 700, color: '#0f172a' },
  subtitle: { margin: 0, color: '#64748b', fontSize: 15 },
  card: { background: '#fff', padding: 20, borderRadius: 12, boxShadow: '0 1px 4px rgba(0,0,0,0.07)', border: '1px solid #e2e8f0' },
  cardTitle: { margin: '0 0 12px', fontSize: 17, fontWeight: 600, color: '#1e293b' },
  input: { display: 'block', width: '100%', marginBottom: 12 },
  primaryButton: { background: '#2563eb', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 14px', cursor: 'pointer' },
  error: { color: '#b91c1c', marginTop: 8 },
  success: { color: '#047857', marginTop: 8 },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  count: { fontSize: 12, color: '#64748b' },
  empty: { color: '#64748b', margin: 0 },
  list: { display: 'flex', flexDirection: 'column', gap: 10 },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 14px', border: '1px solid #e2e8f0', borderRadius: 10, background: '#f8fafc' },
  rowTitle: { fontWeight: 600, color: '#111827' },
  rowMeta: { fontSize: 12, color: '#64748b', marginTop: 2 },
  deleteButton: { background: '#fee2e2', color: '#b91c1c', border: 'none', borderRadius: 8, padding: '8px 10px', cursor: 'pointer' },
};
