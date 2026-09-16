'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './login.module.css';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { login } from '@/lib/api/auth';
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
    setLoading(true);

    try {
      await login({ email: email || 'admin@medtrust.com', password: password || 'password' });
      router.push('/dashboard');
    } catch {
      setError('Invalid credentials. Please try again.');
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

        {/* Mock mode notice */}
        <div className={styles.mockNotice}>
          <span>Mock Mode — any credentials will work</span>
        </div>
      </div>
    </div>
  );
}
