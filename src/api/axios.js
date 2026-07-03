import axios from 'axios';

// All requests go to the gateway — frontend never talks to individual services
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// ── Request interceptor — attach JWT to every request ────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor — handle auth failures globally ─────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Enhance error object with better message extraction
    if (error.response) {
      // Ensure error.response.data has a consistent structure
      const responseData = error.response.data;
      
      // If response is a string (like plain text error), convert to object
      if (typeof responseData === 'string') {
        error.response.data = { message: responseData };
      }
      
      // Extract message from various possible error response formats
      if (!error.response.data.message) {
        if (error.response.data.error) {
          error.response.data.message = error.response.data.error;
        } else if (error.response.data.detail) {
          error.response.data.message = error.response.data.detail;
        } else {
          error.response.data.message = error.response.statusText || 'Request failed';
        }
      }
    }
    
    // Only auto-redirect on 401 if user already had a token (token expired/invalidated)
    // Don't redirect on login/register pages when credentials are invalid
    if (error.response?.status === 401) {
      const hasToken = !!sessionStorage.getItem('token');
      if (hasToken) {
        // Token existed but became invalid — user session expired
        sessionStorage.removeItem('token');
        window.location.href = '/login';
      }
      // If no token, it's probably a login/register error — let the page handle it
    }
    // Re-throw so individual API calls can still handle their own errors
    return Promise.reject(error);
  }
);

export default api;
