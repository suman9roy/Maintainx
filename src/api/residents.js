import api from './axios';

// Admin: all approved residents
export const getAllResidents = () =>
  api.get('/residents');

// Any: get own resident profile by DB id
export const getResidentById = (id) =>
  api.get(`/residents/${id}`);

// Resident: get all my approved flat registrations
export const getMyResidents = () =>
  api.get('/residents/byUserId');

// Admin: remove a resident
export const deleteResident = (id) =>
  api.delete(`/residents/${id}`);
