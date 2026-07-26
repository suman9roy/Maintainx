import api from './axios';

// Submit join request with optional PDF document
// data: JoinRequestDto fields, document: File (PDF) or null
export const submitJoinRequest = (data, document) => {
  const formData = new FormData();
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  if (document) {
    formData.append('document', document);
  }
  return api.post('/join-requests', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

// Resident: view own requests
export const getMyRequests = () =>
  api.get('/join-requests/my');

// Admin: list all requests, optionally filtered by status
export const getAllRequests = (status) =>
  api.get('/join-requests', { params: status ? { status } : {} });

// Admin: download the PDF document for a request
// Returns a blob so the browser can open/save it
export const downloadDocument = (id) =>
  api.get(`/join-requests/${id}/document`, { responseType: 'blob' });

// Admin: approve a request
export const approveRequest = (id) =>
  api.put(`/join-requests/${id}/approve`);

// Admin: reject a request with a reason
export const rejectRequest = (id, reason) =>
  api.put(`/join-requests/${id}/reject`, { reason });
