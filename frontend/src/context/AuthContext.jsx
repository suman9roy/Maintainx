import { createContext, useContext, useState, useEffect } from 'react';
import { login as loginApi, register as registerApi, refreshToken as refreshTokenApi } from '../api/auth';

const AuthContext = createContext(null);

// Decode JWT payload without a library — JWTs are just base64 JSON
function decodeToken(token) {
  try {
    const payload = token.split('.')[1];
    // atob doesn't handle URL-safe base64, fix padding first
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken]   = useState(() => sessionStorage.getItem('token'));
  const [user, setUser]     = useState(() => {
    const t = sessionStorage.getItem('token');
    const decoded = t ? decodeToken(t) : null;
    const storedEmail = sessionStorage.getItem('email');
    return decoded ? { ...decoded, email: storedEmail || decoded.email } : null;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState(null);

  // Derive role + userId from JWT payload
  // JWT payload: { sub: "uuid", role: "ADMIN"|"RESIDENT", exp: ... }
  const role   = user?.role   ?? null;
  const userId = user?.sub    ?? null;
  const isAdmin    = role === 'ADMIN';
  const isResident = role === 'RESIDENT';
  const isSuperAdmin = role === 'SUPER_ADMIN';

  // Auto-logout when token expires
  useEffect(() => {
    if (!user) return;
    const msUntilExpiry = user.exp * 1000 - Date.now();
    if (msUntilExpiry <= 0) {
      logout();
      return;
    }
    const timer = setTimeout(logout, msUntilExpiry);
    return () => clearTimeout(timer);
  }, [user]);

  async function login(email, password) {
    setLoading(true);
    setError(null);
    try {
      const res = await loginApi({ email, password });
      // Backend returns the JWT string directly as response.data
      const jwt = res.data;
      sessionStorage.setItem('token', jwt);
      // persist the email used to login so we can autofill across reloads
      sessionStorage.setItem('email', email);
      const decoded = decodeToken(jwt);
      const withEmail = { ...decoded, email };
      setToken(jwt);
      setUser(withEmail);
      return withEmail; // so the caller can redirect based on role
    } catch (err) {
      // Extract error message from multiple possible formats
      let msg = 'Login failed';
      
      if (err.response?.data?.message) {
        msg = err.response.data.message;
      } else if (err.response?.data?.error) {
        msg = err.response.data.error;
      } else if (typeof err.response?.data === 'string') {
        msg = err.response.data;
      } else if (err.message) {
        msg = err.message;
      }
      
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  }

  async function register(data) {
    setLoading(true);
    setError(null);
    try {
      const res = await registerApi(data);
      return res.data; // { message, userId }
    } catch (err) {
      // Extract error message from multiple possible formats
      let msg = 'Registration failed';
      
      if (err.response?.data?.message) {
        msg = err.response.data.message;
      } else if (err.response?.data?.error) {
        msg = err.response.data.error;
      } else if (typeof err.response?.data === 'string') {
        msg = err.response.data;
      } else if (err.message) {
        msg = err.message;
      }
      
      setError(msg);
      throw new Error(msg);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Silently reissues the JWT from the user's current DB state (picks up
   * a newly-approved apartmentId, a changed role, etc.) without touching
   * loading/error state or requiring a password — safe to call from a
   * background poll. Swaps sessionStorage + context state in place so
   * every subsequent request carries the fresh token.
   *
   * Returns the newly decoded user, or null if the refresh failed (e.g.
   * the account no longer exists) — callers should treat null as
   * "nothing changed, try again later" rather than forcing a logout,
   * since the existing token may still be perfectly valid.
   */
  async function refreshToken() {
    try {
      const res = await refreshTokenApi();
      const jwt = res.data;
      const storedEmail = sessionStorage.getItem('email');
      const decoded = decodeToken(jwt);
      if (!decoded) return null;

      sessionStorage.setItem('token', jwt);
      const withEmail = { ...decoded, email: storedEmail || decoded.email };
      setToken(jwt);
      setUser(withEmail);
      return withEmail;
    } catch (err) {
      console.error('Token refresh failed:', err);
      return null;
    }
  }

  function logout() {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('email');
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  }

  return (
    <AuthContext.Provider value={{
      token, user, role, userId,
      isAdmin, isResident, isSuperAdmin,
      loading, error,
      login, register, logout, refreshToken,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
