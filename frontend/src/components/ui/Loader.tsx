'use client';

import React from 'react';
import styles from './Loader.module.css';

export function Spinner({ size = 24 }: { size?: number }) {
  return (
    <div
      className={styles.spinner}
      style={{ width: size, height: size }}
    />
  );
}

export function Skeleton({ width, height = 20 }: { width?: string | number; height?: string | number }) {
  return (
    <div
      className={styles.skeleton}
      style={{ width: width || '100%', height }}
    />
  );
}

export function PageLoader() {
  return (
    <div className={styles.pageLoader}>
      <Spinner size={36} />
    </div>
  );
}
