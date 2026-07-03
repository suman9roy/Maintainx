import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';

// Auth
import Login    from './pages/auth/Login';
import Register from './pages/auth/Register';

// Resident
import ResidentDashboard from './pages/resident/Dashboard';
import JoinRequest       from './pages/resident/JoinRequest';
import MyRequests        from './pages/resident/MyRequests';
import MyBills           from './pages/resident/MyBills';
import MyComplaints      from './pages/resident/MyComplaints';
import ExpensesPage      from './pages/resident/Expenses';
import NoticesPage       from './pages/resident/Notices';

// Admin — uncomment as each page is built
import AdminDashboard    from './pages/admin/Dashboard';
import AdminJoinRequests from './pages/admin/JoinRequests.jsx';
import AdminResidents    from './pages/admin/AdminResidents';
import AdminBills        from './pages/admin/Bills';
import AdminComplaints   from './pages/admin/Complaints';
import AdminExpenses     from './pages/admin/Expenses';
import AdminNotices      from './pages/admin/Notices';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>

          {/* Public */}
          <Route path="/login"    element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Authenticated */}
          <Route element={<ProtectedRoute />}>

            {/* Resident routes */}
            <Route path="/dashboard"     element={<ResidentDashboard />} />
            <Route path="/join-request"  element={<JoinRequest />} />
            <Route path="/my-requests"   element={<MyRequests />} />
            <Route path="/my-bills"      element={<MyBills />} />
            <Route path="/my-complaints" element={<MyComplaints />} />
            <Route path="/expenses"      element={<ExpensesPage />} />
            <Route path="/notices"       element={<NoticesPage />} />

            {/* Admin routes */}
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminDashboard />} />
              {/* Uncomment as each page is built: */}
              <Route path="/admin/join-requests" element={<AdminJoinRequests />} />
              <Route path="/admin/residents" element={<AdminResidents />} />
              {<Route path="/admin/bills"         element={<AdminBills />} /> }
              <Route path="/admin/complaints"    element={<AdminComplaints />} />
              <Route path="/admin/expenses"      element={<AdminExpenses />} />
              {<Route path="/admin/notices"       element={<AdminNotices />} /> }
            </Route>

          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
