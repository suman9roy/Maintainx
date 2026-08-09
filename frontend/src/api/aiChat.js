import api from './axios';

export async function sendChatMessage(message, sessionId, { signal } = {}) {
  const payload = { message };
  if (sessionId) payload.sessionId = sessionId;

  const { data } = await api.post('/ai/chat', payload, { signal });
  return data;
}

export async function uploadDocument(formData, { signal } = {}) {
  // Let axios set the Content-Type (with boundary) for FormData
  return api.post('/ai/documents', formData, { signal });
}

export async function getDocuments({ signal } = {}) {
  return api.get('/ai/documents', { signal });
}

export async function getDocument(id, { signal } = {}) {
  return api.get(`/ai/documents/${id}`, { signal });
}

export async function deleteDocument(id, { signal } = {}) {
  return api.delete(`/ai/documents/${id}`, { signal });
}
