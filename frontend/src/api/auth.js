import api from './axios';

const register = (data) => api.post('/auth/register', data);

const login = (data) => api.post('/auth/login', data);

// Reissues a JWT for the current user from their latest DB state
// (role, apartmentId) — no password needed. Auth comes from the
// existing token via the request interceptor, same as any other call.
const refreshToken = () => api.post('/auth/refresh');

// ── Email verification (registration) ─────────────────────────────────────
const verifyEmail = (email, otp) => api.post('/auth/verify-email', { email, otp });
const resendOtp    = (email) => api.post('/auth/resend-otp', { email });

// ── Forgot password ─────────────────────────────────────────────────────────
const forgotPassword = (email) => api.post('/auth/forgot-password', { email });
const resetPassword  = (email, otp, newPassword) =>
  api.post('/auth/reset-password', { email, otp, newPassword });

export {
  login, register, refreshToken,
  verifyEmail, resendOtp,
  forgotPassword, resetPassword,
};
