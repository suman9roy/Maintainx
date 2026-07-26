import api from './axios';

const register = (data) => api.post('/auth/register', data);

const login = (data) => api.post('/auth/login', data);

export { login, register };
