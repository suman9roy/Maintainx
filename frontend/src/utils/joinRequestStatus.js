const STATUS_ALIASES = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  PENDING_APPROVAL: 'PENDING',
  PENDING_REVIEW: 'PENDING',
  ACCEPTED: 'APPROVED',
  DECLINED: 'REJECTED',
  CANCELLED: 'REJECTED',
};

export function normalizeJoinRequestStatus(value) {
  if (value === null || value === undefined || value === '') {
    return 'UNKNOWN';
  }

  const normalized = String(value).trim().toUpperCase();
  return STATUS_ALIASES[normalized] ?? normalized;
}

export function normalizeJoinRequests(payload) {
  if (Array.isArray(payload)) return payload;
  if (payload && typeof payload === 'object') {
    if (Array.isArray(payload.data)) return payload.data;
    if (Array.isArray(payload.requests)) return payload.requests;
    if (Array.isArray(payload.items)) return payload.items;
  }
  return [];
}

export function getJoinRequestStatus(request) {
  return normalizeJoinRequestStatus(request?.status ?? request?.requestStatus);
}

export function getApprovedJoinRequests(requests = []) {
  return normalizeJoinRequests(requests).filter(
    (request) => getJoinRequestStatus(request) === 'APPROVED'
  );
}

export function getRequestApartmentIds(requests = []) {
  return normalizeJoinRequests(requests)
    .map((request) => request.apartmentId ?? request.apartmentID ?? request.apartment_id)
    .filter(Boolean);
}

export function getResidentApartmentIds(residents = []) {
  return (residents ?? [])
    .map((resident) => resident.apartmentId ?? resident.apartmentID ?? resident.apartment_id)
    .filter(Boolean);
}

export function getUserApartmentIds(residents = [], requests = []) {
  return [
    ...new Set([
      ...getResidentApartmentIds(residents),
      ...getRequestApartmentIds(getApprovedJoinRequests(requests)),
    ]),
  ];
}

export function getJoinRequestCounts(requests = []) {
  return requests.reduce(
    (acc, request) => {
      const status = normalizeJoinRequestStatus(request?.status ?? request?.requestStatus);
      if (status === 'PENDING') acc.pending += 1;
      else if (status === 'APPROVED') acc.approved += 1;
      else if (status === 'REJECTED') acc.rejected += 1;
      return acc;
    },
    { pending: 0, approved: 0, rejected: 0 }
  );
}
