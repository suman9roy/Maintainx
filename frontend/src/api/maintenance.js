import api from './axios';

// Admin: generate a new bill
export const generateBill = (data) =>
  api.post('/maintenance', data);

// Admin: all bills
export const getAllBills = () =>
  api.get('/maintenance');

// Any: bills for a specific flat (service checks ownership)
export const getBillsByFlat = (flatNumber) =>
  api.get(`/maintenance/${flatNumber}`);

// Any: single bill by UUID (used before payment)
export const getBillById = (id) =>
  api.get(`/maintenance/bill/${id}`);

// Admin: total amount collected
export const getTotalCollected = () =>
  api.get('/maintenance/total-collected');

// Admin: manually mark a bill as paid
// Backend requires a MarkBillPaidRequest body: { paymentMode, remarks }
// X-User-Id (admin) is injected by the gateway from the JWT — not sent manually
export const markBillPaid = (id, paymentMode, remarks) =>
  api.put(`/maintenance/mark-paid/${id}`, { paymentMode, remarks });