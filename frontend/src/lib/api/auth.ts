import { apiFetch, SERVICE_URLS, isMockMode } from '../api';
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
    return mockAuthResponse;
  }
  const res = await apiFetch<AuthResponse>(BASE, '/api/auth/login', {
    method: 'POST', body: JSON.stringify(data),
  });
  localStorage.setItem('medtrust_access_token', res.accessToken);
  return res;
}

export async function register(data: RegisterData): Promise<AuthResponse> {
  if (isMockMode()) return mockAuthResponse;
  return apiFetch<AuthResponse>(BASE, '/api/auth/register', {
    method: 'POST', body: JSON.stringify(data),
  });
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
  localStorage.removeItem('medtrust_access_token');
}
