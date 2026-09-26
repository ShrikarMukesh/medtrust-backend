'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { isAuthenticated } from '@/lib/api/auth';

interface AuthGuardProps {
  children: React.ReactNode;
}

/** Decode the JWT payload (base64url) without verifying signature — client-side only */
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    // exp is in seconds, Date.now() is in ms
    return payload.exp * 1000 < Date.now();
  } catch {
    return true; // treat malformed token as expired
  }
}

export function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('medtrust_access_token');
    const refreshToken = localStorage.getItem('medtrust_refresh_token');

    if (!token || !isAuthenticated()) {
      router.replace('/login');
      return;
    }

    if (isTokenExpired(token)) {
      // Access token expired — api.ts will auto-refresh on the next API call,
      // but if there's no refresh token either, kick to login now
      if (!refreshToken) {
        localStorage.removeItem('medtrust_access_token');
        localStorage.removeItem('medtrust_user_role');
        localStorage.removeItem('medtrust_user');
        router.replace('/login');
        return;
      }
      // Has refresh token — let api.ts handle the refresh transparently on next call
    }

    setChecked(true);
  }, [router]);

  if (!checked) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        color: 'var(--text-muted)',
        fontSize: 'var(--text-sm)',
      }}>
        Verifying authentication…
      </div>
    );
  }

  return <>{children}</>;
}
