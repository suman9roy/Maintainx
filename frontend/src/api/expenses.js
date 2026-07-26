import api from './axios';

// Admin: add an expense
export const addExpense = (data) =>
  api.post('/expenses', data);

// Any: all expenses
export const getAllExpenses = () =>
  api.get('/expenses');

// Any: expenses filtered by category
export const getExpensesByCategory = (category) =>
  api.get(`/expenses/category/${category}`);

// Any: fund summary (total collected vs total spent)
export const getFundSummary = () =>
  api.get('/expenses/fund-summary');
