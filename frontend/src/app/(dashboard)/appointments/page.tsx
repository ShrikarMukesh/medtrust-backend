'use client';

import React, { useEffect, useState } from 'react';
import styles from './appointments.module.css';
import { Header } from '@/components/layout/Header';
import { Table } from '@/components/ui/Table';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { getAppointments, cancelAppointment, confirmAppointment, completeAppointment, AppointmentResponse } from '@/lib/api/appointments';
import { getPatientName, getProviderName } from '@/lib/mock-data';
import { format } from 'date-fns';
import { Plus, Check, X, CheckCircle } from 'lucide-react';

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadAppointments(); }, []);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const data = await getAppointments();
      setAppointments(data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const filtered = filter === 'ALL' ? appointments : appointments.filter(a => a.status === filter);

  const handleAction = async (id: string, action: string) => {
    try {
      if (action === 'confirm') await confirmAppointment(id);
      if (action === 'cancel') await cancelAppointment(id, 'Cancelled via dashboard');
      if (action === 'complete') await completeAppointment(id);
      await loadAppointments();
    } catch (err) { console.error(err); }
  };

  const columns = [
    { key: 'patient', header: 'Patient', render: (a: AppointmentResponse) => (
      <span className={styles.nameCell}>{getPatientName(a.patientId)}</span>
    )},
    { key: 'provider', header: 'Provider', render: (a: AppointmentResponse) => getProviderName(a.providerId) },
    { key: 'dateTime', header: 'Date & Time', render: (a: AppointmentResponse) => (
      <span>{format(new Date(a.startTime), 'MMM d, yyyy')} <span className={styles.time}>{format(new Date(a.startTime), 'h:mm a')}</span></span>
    )},
    { key: 'type', header: 'Type', width: '120px', render: (a: AppointmentResponse) => (
      <Badge variant="default" size="sm">{a.type.replace('_', ' ')}</Badge>
    )},
    { key: 'status', header: 'Status', width: '120px', render: (a: AppointmentResponse) => (
      <Badge variant={statusVariant(a.status)} size="sm" dot>{a.status}</Badge>
    )},
    { key: 'reason', header: 'Reason', render: (a: AppointmentResponse) => (
      <span className={styles.reason}>{a.reason}</span>
    )},
    { key: 'actions', header: 'Actions', width: '160px', render: (a: AppointmentResponse) => (
      <div className={styles.actionBtns}>
        {a.status === 'SCHEDULED' && (
          <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); handleAction(a.id, 'confirm'); }} icon={<Check size={14} />}>
            Confirm
          </Button>
        )}
        {(a.status === 'SCHEDULED' || a.status === 'CONFIRMED') && (
          <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); handleAction(a.id, 'cancel'); }} icon={<X size={14} />}>
            Cancel
          </Button>
        )}
        {a.status === 'CONFIRMED' && (
          <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); handleAction(a.id, 'complete'); }} icon={<CheckCircle size={14} />}>
            Complete
          </Button>
        )}
      </div>
    )},
  ];

  const statuses = ['ALL', 'SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'];

  return (
    <>
      <Header title="Appointments" subtitle={`${appointments.length} total appointments`} />
      <div className={styles.content}>
        <div className={styles.toolbar}>
          <div className={styles.filters}>
            {statuses.map(s => (
              <button
                key={s}
                className={`${styles.filterBtn} ${filter === s ? styles.filterActive : ''}`}
                onClick={() => setFilter(s)}
              >
                {s === 'ALL' ? 'All' : s.charAt(0) + s.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
          <Button variant="primary" icon={<Plus size={16} />}>
            New Appointment
          </Button>
        </div>

        {loading ? (
          <div className={styles.loading}>Loading appointments...</div>
        ) : (
          <Table
            columns={columns}
            data={filtered}
            emptyMessage="No appointments found"
          />
        )}
      </div>
    </>
  );
}
