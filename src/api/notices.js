import api from './axios';

// Admin: post a notice
export const createNotice = (data) =>
  api.post('/notices', data);

// Any: all notices
export const getAllNotices = () =>
  api.get('/notices');

// Any: notices filtered by type
export const getNoticesByType = (type) =>
  api.get(`/notices/type/${type}`);
