import api from './axios';

// Resident: raise a complaint
export const createComplaint = (data) =>
  api.post('/complaints', data);

// Admin: all complaints
export const getAllComplaints = () =>
  api.get('/complaints');

// Any: complaints for a resident email (service checks ownership)
export const getComplaintsByResident = (email) =>
  api.get(`/complaints/resident/${email}`);

// Admin: update complaint status
export const updateComplaintStatus = (id, status) =>
  api.put(`/complaints/${id}/status`, { status });
