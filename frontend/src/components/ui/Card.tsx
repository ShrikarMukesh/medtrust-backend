'use client';

import React from 'react';
import styles from './Card.module.css';
import clsx from 'clsx';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  variant?: 'default' | 'glass' | 'outlined';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hover?: boolean;
  onClick?: () => void;
}

export function Card({
  children,
  className,
  variant = 'default',
  padding = 'md',
  hover = false,
  onClick,
}: CardProps) {
  return (
    <div
      className={clsx(
        styles.card,
        styles[variant],
        styles[`padding-${padding}`],
        hover && styles.hoverable,
        onClick && styles.clickable,
        className
      )}
      onClick={onClick}
    >
      {children}
    </div>
  );
}
