import { apiFetch, SERVICE_URLS, isMockMode } from '../api';
import { mockConsents } from '../mock-data';

export interface ConsentResponse {
  id: string; patientId: string; grantedToUserId: string; scope: string;
  status: string; grantedAt: string; expiresAt: string;
}

const BASE = SERVICE_URLS.consent;

export async function getConsents(): Promise<ConsentResponse[]> {
  if (isMockMode()) return mockConsents;
  return apiFetch<ConsentResponse[]>(BASE, '/api/consents');
}

export async function grantConsent(data: { patientId: string; grantedToUserId: string; scope: string }): Promise<ConsentResponse> {
  if (isMockMode()) return { id: 'c-new', ...data, status: 'GRANTED', grantedAt: new Date().toISOString(), expiresAt: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString() };
  return apiFetch<ConsentResponse>(BASE, '/api/consents', {
    method: 'POST', body: JSON.stringify(data),
  });
}

export async function revokeConsent(id: string): Promise<ConsentResponse> {
  if (isMockMode()) {
    const c = mockConsents.find(c => c.id === id);
    if (!c) throw new Error('Consent not found');
    return { ...c, status: 'REVOKED' };
  }
  return apiFetch<ConsentResponse>(BASE, `/api/consents/${id}`, { method: 'DELETE' });
}

export async function verifyConsent(patientId: string, userId: string, scope: string): Promise<{ hasConsent: boolean }> {
  if (isMockMode()) {
    const found = mockConsents.some(c => c.patientId === patientId && c.grantedToUserId === userId && c.scope === scope && c.status === 'GRANTED');
    return { hasConsent: found };
  }
  return apiFetch<{ hasConsent: boolean }>(BASE, `/api/consents/verify?patientId=${patientId}&userId=${userId}&scope=${scope}`);
}
