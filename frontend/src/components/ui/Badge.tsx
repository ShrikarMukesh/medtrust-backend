'use client';

import React from 'react';
import styles from './Badge.module.css';
import clsx from 'clsx';

type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info' |
  'scheduled' | 'confirmed' | 'cancelled' | 'completed' | 'no_show' | 'active' | 'inactive';

interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  size?: 'sm' | 'md';
  dot?: boolean;
}

export function Badge({ children, variant = 'default', size = 'md', dot = false }: BadgeProps) {
  return (
    <span className={clsx(styles.badge, styles[variant], styles[size])}>
      {dot && <span className={styles.dot} />}
      {children}
    </span>
  );
}

/** Helper to map appointment status string to badge variant */
export function statusVariant(status: string): BadgeVariant {
  const map: Record<string, BadgeVariant> = {
    SCHEDULED: 'scheduled',
    CONFIRMED: 'confirmed',
    CANCELLED: 'cancelled',
    COMPLETED: 'completed',
    NO_SHOW: 'no_show',
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    IN_PROGRESS: 'info',
    ADMITTED: 'info',
    DISCHARGED: 'completed',
    REGISTERED: 'scheduled',
    REVOKED: 'danger',
    GRANTED: 'success',
  };
  return map[status?.toUpperCase()] || 'default';
}
