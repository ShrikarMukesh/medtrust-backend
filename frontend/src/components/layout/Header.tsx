'use client';

import React from 'react';
import styles from './Header.module.css';
import { Bell, Search, LogOut, User } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
}

export function Header({ title, subtitle }: HeaderProps) {
  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <div>
          <h1 className={styles.title}>{title}</h1>
          {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
        </div>
      </div>

      <div className={styles.right}>
        {/* Search */}
        <div className={styles.searchWrap}>
          <Search size={16} className={styles.searchIcon} />
          <input
            type="text"
            className={styles.search}
            placeholder="Search..."
          />
        </div>

        {/* Notifications */}
        <button className={styles.iconBtn} aria-label="Notifications">
          <Bell size={20} />
          <span className={styles.notifDot} />
        </button>

        {/* Profile */}
        <div className={styles.profile}>
          <div className={styles.avatar}>
            <User size={16} />
          </div>
          <button className={styles.iconBtn} aria-label="Logout">
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  );
}
