'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './login.module.css';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { login, ApiError } from '@/lib/api/auth';
import { isMockMode } from '@/lib/api';
import { Activity, Shield } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Email and password are required.');
      return;
    }

    setLoading(true);

    try {
      await login({ email, password });
      router.push('/dashboard');
    } catch (err) {
      if (err instanceof ApiError) {
        // Try to parse the JSON error body from the backend
        try {
          const parsed = JSON.parse(err.message);
          setError(parsed.message || 'Authentication failed');
        } catch {
          setError(err.message || 'Authentication failed');
        }
      } else {
        setError('Unable to connect to server. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`${styles.page} mesh-gradient`}>
      <div className={styles.card}>
        {/* Logo */}
        <div className={styles.logo}>
          <div className={styles.logoIcon}>
            <Activity size={28} />
          </div>
          <h1 className={styles.brand}>MedTrust</h1>
          <p className={styles.tagline}>Healthcare Administration Platform</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
            label="Email"
            type="email"
            placeholder="admin@medtrust.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <Input
            label="Password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          {error && <p className={styles.error}>{error}</p>}

          <Button type="submit" variant="primary" size="lg" loading={loading}>
            Sign In
          </Button>
        </form>

        {/* Footer */}
        <div className={styles.footer}>
          <Shield size={14} />
          <span>HIPAA Compliant • SOC 2 Certified</span>
        </div>

        {/* Mock mode notice — only shown in mock mode */}
        {isMockMode() && (
          <div className={styles.mockNotice}>
            <span>Mock Mode — any credentials will work</span>
          </div>
        )}
      </div>
    </div>
  );
}
