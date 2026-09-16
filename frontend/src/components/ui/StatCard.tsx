'use client';

import React from 'react';
import styles from './StatCard.module.css';
import clsx from 'clsx';

interface StatCardProps {
  icon: React.ReactNode;
  label: string;
  value: string | number;
  trend?: { value: string; positive: boolean };
  color?: 'accent' | 'success' | 'warning' | 'danger' | 'info';
}

export function StatCard({ icon, label, value, trend, color = 'accent' }: StatCardProps) {
  return (
    <div className={clsx(styles.card, styles[color])}>
      <div className={styles.iconWrap}>{icon}</div>
      <div className={styles.content}>
        <span className={styles.label}>{label}</span>
        <span className={styles.value}>{value}</span>
        {trend && (
          <span className={clsx(styles.trend, trend.positive ? styles.up : styles.down)}>
            {trend.positive ? '↑' : '↓'} {trend.value}
          </span>
        )}
      </div>
    </div>
  );
}
