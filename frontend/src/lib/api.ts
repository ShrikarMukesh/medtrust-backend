/* ─── Central API Client ──────────────────────────────────────────────── */

const USE_MOCK = process.env.NEXT_PUBLIC_USE_MOCK === 'true';

export const SERVICE_URLS = {
  patient: process.env.NEXT_PUBLIC_PATIENT_SERVICE_URL || 'http://localhost:8081',
  appointment: process.env.NEXT_PUBLIC_APPOINTMENT_SERVICE_URL || 'http://localhost:8082',
  auth: process.env.NEXT_PUBLIC_AUTH_SERVICE_URL || 'http://localhost:8083',
  consent: process.env.NEXT_PUBLIC_CONSENT_SERVICE_URL || 'http://localhost:8084',
  audit: process.env.NEXT_PUBLIC_AUDIT_SERVICE_URL || 'http://localhost:8085',
  clinical: process.env.NEXT_PUBLIC_CLINICAL_SERVICE_URL || 'http://localhost:8080',
} as const;

export function isMockMode(): boolean {
  return USE_MOCK;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  count?: number;
}

function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('medtrust_access_token');
}

export async function apiFetch<T>(
  baseUrl: string,
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    const body = await res.text();
    throw new ApiError(res.status, body || `HTTP ${res.status}`);
  }

  const json: ApiResponse<T> = await res.json();

  if (!json.success) {
    throw new ApiError(400, json.message || 'Request failed');
  }

  return json.data;
}
