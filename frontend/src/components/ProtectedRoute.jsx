import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ChatWidget from './ai/ChatWidget';

export default function ProtectedRoute() {
  const { token } = useAuth();
  return token ? (
    <>
      <Outlet />
      <ChatWidget />
    </>
  ) : (
    <Navigate to="/login" replace />
  );
}
