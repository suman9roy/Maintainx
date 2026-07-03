import api from './axios';

// Step 1: create Razorpay order for a bill
export const createOrder = (maintenanceBillId) =>
  api.post('/payments/create-order', { maintenanceBillId });

// Step 2: verify Razorpay payment after the resident completes checkout
export const verifyPayment = (razorpayOrderId, razorpayPaymentId, razorpaySignature) =>
  api.post('/payments/verify', {
    razorpayOrderId,
    razorpayPaymentId,
    razorpaySignature,
  });
