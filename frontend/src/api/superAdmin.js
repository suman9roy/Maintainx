import api from './axios';

// Onboard a brand-new apartment + its first admin account, in one step
export const onboardApartment = (data) =>
  api.post('/super-admin/apartments', data);

// List every onboarded apartment
export const listApartments = () =>
  api.get('/super-admin/apartments');

export const suspendApartment = (id) =>
  api.patch(`/super-admin/apartments/${id}/suspend`);

export const reactivateApartment = (id) =>
  api.patch(`/super-admin/apartments/${id}/reactivate`);