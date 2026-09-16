'use client';

import React, { useEffect, useState } from 'react';
import styles from './dashboard.module.css';
import { Header } from '@/components/layout/Header';
import { StatCard } from '@/components/ui/StatCard';
import { Card } from '@/components/ui/Card';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Users, CalendarDays, Stethoscope, ScrollText, Plus } from 'lucide-react';
import { mockPatients, mockAppointments, mockEncounters, mockAuditEntries, getPatientName, getProviderName } from '@/lib/mock-data';
import { format } from 'date-fns';
import Link from 'next/link';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
} from 'recharts';

const statusColors: Record<string, string> = {
  SCHEDULED: '#3b82f6',
  CONFIRMED: '#10b981',
  CANCELLED: '#ef4444',
  COMPLETED: '#64748b',
  NO_SHOW: '#f59e0b',
};

export default function DashboardPage() {
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  const todayAppts = mockAppointments.filter(a =>
    a.startTime.startsWith(new Date().toISOString().slice(0, 10))
  ).length;

  const chartData = Object.entries(
    mockAppointments.reduce<Record<string, number>>((acc, a) => {
      acc[a.status] = (acc[a.status] || 0) + 1;
      return acc;
    }, {})
  ).map(([status, count]) => ({ status, count }));

  const recentAppointments = [...mockAppointments]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5);

  const recentAudit = [...mockAuditEntries]
    .sort((a, b) => new Date(b.eventTimestamp).getTime() - new Date(a.eventTimestamp).getTime())
    .slice(0, 6);

  return (
    <>
      <Header title="Dashboard" subtitle="Welcome back — here's your overview" />
      <div className={styles.content}>
        {/* Stat Cards */}
        <div className={styles.stats}>
          <StatCard
            icon={<Users size={22} />}
            label="Total Patients"
            value={mockPatients.length}
            trend={{ value: '+3 this month', positive: true }}
            color="accent"
          />
          <StatCard
            icon={<CalendarDays size={22} />}
            label="Today's Appointments"
            value={todayAppts || mockAppointments.filter(a => a.status === 'SCHEDULED').length}
            color="info"
          />
          <StatCard
            icon={<Stethoscope size={22} />}
            label="Active Encounters"
            value={mockEncounters.filter(e => e.status !== 'DISCHARGED').length}
            color="success"
          />
          <StatCard
            icon={<ScrollText size={22} />}
            label="Audit Events (24h)"
            value={mockAuditEntries.length}
            color="warning"
          />
        </div>

        {/* Charts + Quick Actions */}
        <div className={styles.grid}>
          {/* Chart */}
          <Card className={styles.chartCard}>
            <h3 className={styles.sectionTitle}>Appointments by Status</h3>
            {mounted && (
              <div className={styles.chart}>
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={chartData}>
                    <XAxis dataKey="status" tick={{ fill: '#94a3b8', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} axisLine={false} tickLine={false} />
                    <Tooltip
                      contentStyle={{ background: '#1a2235', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '10px', color: '#f1f5f9' }}
                    />
                    <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                      {chartData.map((entry) => (
                        <Cell key={entry.status} fill={statusColors[entry.status] || '#64748b'} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </Card>

          {/* Quick Actions */}
          <Card className={styles.actionsCard}>
            <h3 className={styles.sectionTitle}>Quick Actions</h3>
            <div className={styles.actions}>
              <Link href="/appointments">
                <Button variant="primary" icon={<Plus size={16} />}>
                  New Appointment
                </Button>
              </Link>
              <Link href="/patients">
                <Button variant="secondary" icon={<Plus size={16} />}>
                  Register Patient
                </Button>
              </Link>
              <Link href="/clinical">
                <Button variant="secondary" icon={<Plus size={16} />}>
                  New Encounter
                </Button>
              </Link>
            </div>
          </Card>
        </div>

        {/* Recent Tables */}
        <div className={styles.grid}>
          {/* Recent Appointments */}
          <Card className={styles.tableCard} padding="none">
            <div className={styles.tableHeader}>
              <h3 className={styles.sectionTitle}>Recent Appointments</h3>
              <Link href="/appointments" className={styles.viewAll}>View all →</Link>
            </div>
            <table className={styles.miniTable}>
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Provider</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentAppointments.map((a) => (
                  <tr key={a.id}>
                    <td>{getPatientName(a.patientId)}</td>
                    <td>{getProviderName(a.providerId)}</td>
                    <td>{format(new Date(a.startTime), 'MMM d, h:mm a')}</td>
                    <td><Badge variant={statusVariant(a.status)} size="sm">{a.status}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>

          {/* Audit Feed */}
          <Card className={styles.auditCard}>
            <div className={styles.tableHeader}>
              <h3 className={styles.sectionTitle}>Recent Activity</h3>
              <Link href="/audit" className={styles.viewAll}>View all →</Link>
            </div>
            <div className={styles.auditFeed}>
              {recentAudit.map((entry) => (
                <div key={entry.id} className={styles.auditItem}>
                  <div className={styles.auditDot} />
                  <div className={styles.auditContent}>
                    <span className={styles.auditEvent}>{entry.eventType.replace(/\./g, ' → ')}</span>
                    <span className={styles.auditMeta}>
                      {entry.sourceService} • {format(new Date(entry.eventTimestamp), 'MMM d, h:mm a')}
                    </span>
                  </div>
                  <Badge variant={statusVariant(entry.category)} size="sm">{entry.category}</Badge>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </>
  );
}
