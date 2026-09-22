import { apiFetch, ApiError, SERVICE_URLS, isMockMode } from '../api';
import { mockAuthResponse, mockUsers } from '../mock-data';

export interface LoginData { email: string; password: string; }
export interface RegisterData { email: string; password: string; firstName: string; lastName: string; role: string; }
export interface AuthResponse {
  accessToken: string; refreshToken: string; tokenType: string; expiresIn: number;
  user: { id: string; email: string; firstName: string; lastName: string; role: string; };
}
export interface UserResponse {
  id: string; email: string; firstName: string; lastName: string; role: string;
  active: boolean; lastLoginAt: string; createdAt: string;
}

const BASE = SERVICE_URLS.auth;

export async function login(data: LoginData): Promise<AuthResponse> {
  if (isMockMode()) {
    localStorage.setItem('medtrust_access_token', mockAuthResponse.accessToken);
    localStorage.setItem('medtrust_refresh_token', mockAuthResponse.refreshToken);
    return mockAuthResponse;
  }
  const res = await apiFetch<AuthResponse>(BASE, '/api/auth/login', {
    method: 'POST', body: JSON.stringify(data),
  });
  localStorage.setItem('medtrust_access_token', res.accessToken);
  localStorage.setItem('medtrust_refresh_token', res.refreshToken);
  return res;
}

export async function register(data: RegisterData): Promise<AuthResponse> {
  if (isMockMode()) return mockAuthResponse;
  const res = await apiFetch<AuthResponse>(BASE, '/api/auth/register', {
    method: 'POST', body: JSON.stringify(data),
  });
  localStorage.setItem('medtrust_access_token', res.accessToken);
  localStorage.setItem('medtrust_refresh_token', res.refreshToken);
  return res;
}

export async function refreshAccessToken(): Promise<AuthResponse> {
  const refreshToken = localStorage.getItem('medtrust_refresh_token');
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }
  const res = await apiFetch<AuthResponse>(BASE, '/api/auth/refresh', {
    method: 'POST', body: JSON.stringify({ refreshToken }),
  });
  localStorage.setItem('medtrust_access_token', res.accessToken);
  localStorage.setItem('medtrust_refresh_token', res.refreshToken);
  return res;
}

export async function getUsers(): Promise<UserResponse[]> {
  if (isMockMode()) return mockUsers;
  return apiFetch<UserResponse[]>(BASE, '/api/users');
}

export async function getCurrentUser(): Promise<UserResponse> {
  if (isMockMode()) return mockUsers[3]; // admin
  return apiFetch<UserResponse>(BASE, '/api/users/me');
}

export function logout() {
  const refreshToken = localStorage.getItem('medtrust_refresh_token');
  // Best-effort server-side logout (revoke refresh token)
  if (refreshToken && !isMockMode()) {
    fetch(`${BASE}/api/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    }).catch(() => { /* ignore errors during logout */ });
  }
  localStorage.removeItem('medtrust_access_token');
  localStorage.removeItem('medtrust_refresh_token');
}

export function isAuthenticated(): boolean {
  if (typeof window === 'undefined') return false;
  return !!localStorage.getItem('medtrust_access_token');
}

export { ApiError };
