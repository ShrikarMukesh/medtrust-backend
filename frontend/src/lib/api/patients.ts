import { apiFetch, SERVICE_URLS, isMockMode } from '../api';
import { mockPatients } from '../mock-data';

export interface PatientResponse {
  id: string; mrn: string; firstName: string; middleName: string | null; lastName: string;
  fullName: string; dateOfBirth: string; gender: string; bloodType: string;
  contactInfo: { phone: string; email: string; address: string; city: string; state: string; zipCode: string; };
  emergencyContact: { name: string; relationship: string; phone: string; };
  insuranceInfo: { provider: string; policyNumber: string; groupNumber: string; expirationDate: string; };
  allergies: string[]; active: boolean; createdAt: string; updatedAt: string;
}

const BASE = SERVICE_URLS.patient;

export async function getPatients(lastName?: string): Promise<PatientResponse[]> {
  if (isMockMode()) {
    if (lastName) return mockPatients.filter(p => p.lastName.toLowerCase().includes(lastName.toLowerCase()));
    return mockPatients;
  }
  const query = lastName ? `?lastName=${encodeURIComponent(lastName)}` : '';
  return apiFetch<PatientResponse[]>(BASE, `/api/patients${query}`);
}

export async function getPatient(id: string): Promise<PatientResponse> {
  if (isMockMode()) {
    const p = mockPatients.find(p => p.id === id);
    if (!p) throw new Error('Patient not found');
    return p;
  }
  return apiFetch<PatientResponse>(BASE, `/api/patients/${id}`);
}

export async function registerPatient(data: Record<string, unknown>): Promise<PatientResponse> {
  if (isMockMode()) return mockPatients[0];
  return apiFetch<PatientResponse>(BASE, '/api/patients', {
    method: 'POST', body: JSON.stringify(data),
  });
}

export async function deactivatePatient(id: string): Promise<PatientResponse> {
  if (isMockMode()) {
    const p = mockPatients.find(p => p.id === id);
    if (!p) throw new Error('Patient not found');
    return { ...p, active: false };
  }
  return apiFetch<PatientResponse>(BASE, `/api/patients/${id}`, { method: 'DELETE' });
}
