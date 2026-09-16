'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import styles from './Sidebar.module.css';
import clsx from 'clsx';
import {
  LayoutDashboard,
  Users,
  CalendarDays,
  Stethoscope,
  ScrollText,
  ShieldCheck,
  ChevronLeft,
  ChevronRight,
  Activity,
} from 'lucide-react';

const navItems = [
  { href: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { href: '/patients', icon: Users, label: 'Patients' },
  { href: '/appointments', icon: CalendarDays, label: 'Appointments' },
  { href: '/clinical', icon: Stethoscope, label: 'Clinical' },
  { href: '/audit', icon: ScrollText, label: 'Audit Log' },
  { href: '/consents', icon: ShieldCheck, label: 'Consents' },
];

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
}

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const pathname = usePathname();

  return (
    <aside className={clsx(styles.sidebar, collapsed && styles.collapsed)}>
      {/* Brand */}
      <div className={styles.brand}>
        <div className={styles.logo}>
          <Activity size={24} />
        </div>
        {!collapsed && <span className={styles.brandText}>MedTrust</span>}
      </div>

      {/* Navigation */}
      <nav className={styles.nav}>
        {navItems.map((item) => {
          const isActive = pathname.startsWith(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={clsx(styles.navItem, isActive && styles.active)}
              title={collapsed ? item.label : undefined}
            >
              <item.icon size={20} />
              {!collapsed && <span>{item.label}</span>}
              {isActive && <div className={styles.activeIndicator} />}
            </Link>
          );
        })}
      </nav>

      {/* Collapse Toggle */}
      <button className={styles.toggle} onClick={onToggle} aria-label="Toggle sidebar">
        {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
      </button>
    </aside>
  );
}
