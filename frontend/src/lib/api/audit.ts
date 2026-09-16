import { apiFetch, SERVICE_URLS, isMockMode } from '../api';
import { mockAuditEntries } from '../mock-data';

export interface AuditEntryResponse {
  id: string; eventType: string; category: string; sourceService: string;
  actorId: string; targetId: string; targetType: string;
  payload: Record<string, unknown>; eventTimestamp: string; receivedAt: string;
}

const BASE = SERVICE_URLS.audit;

export async function getAuditEntries(): Promise<AuditEntryResponse[]> {
  if (isMockMode()) return mockAuditEntries;
  return apiFetch<AuditEntryResponse[]>(BASE, '/api/audit');
}

export async function getAuditEntry(id: string): Promise<AuditEntryResponse> {
  if (isMockMode()) {
    const e = mockAuditEntries.find(e => e.id === id);
    if (!e) throw new Error('Audit entry not found');
    return e;
  }
  return apiFetch<AuditEntryResponse>(BASE, `/api/audit/${id}`);
}

export async function getByCategory(category: string): Promise<AuditEntryResponse[]> {
  if (isMockMode()) return mockAuditEntries.filter(e => e.category === category);
  return apiFetch<AuditEntryResponse[]>(BASE, `/api/audit/category/${category}`);
}

export async function getByService(service: string): Promise<AuditEntryResponse[]> {
  if (isMockMode()) return mockAuditEntries.filter(e => e.sourceService === service);
  return apiFetch<AuditEntryResponse[]>(BASE, `/api/audit/service/${service}`);
}

export async function getByDateRange(from: string, to: string): Promise<AuditEntryResponse[]> {
  if (isMockMode()) return mockAuditEntries.filter(e => e.eventTimestamp >= from && e.eventTimestamp <= to);
  return apiFetch<AuditEntryResponse[]>(BASE, `/api/audit/range?from=${from}&to=${to}`);
}
