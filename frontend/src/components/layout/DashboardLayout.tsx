'use client';

import React, { useState } from 'react';
import { Sidebar } from './Sidebar';
import styles from './DashboardLayout.module.css';
import clsx from 'clsx';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className={styles.layout}>
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(!collapsed)} />
      <main className={clsx(styles.main, collapsed && styles.mainCollapsed)}>
        {children}
      </main>
    </div>
  );
}
