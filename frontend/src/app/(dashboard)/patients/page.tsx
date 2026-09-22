'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './patients.module.css';
import { Header } from '@/components/layout/Header';
import { Table } from '@/components/ui/Table';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { getPatients, PatientResponse } from '@/lib/api/patients';
import { Search, UserPlus } from 'lucide-react';

export default function PatientsPage() {
  const router = useRouter();
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setLoading(true);
    try {
      const data = await getPatients();
      setPatients(data);
    } catch (err) {
      console.error('Failed to load patients', err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = patients.filter(p =>
    p.fullName.toLowerCase().includes(search.toLowerCase()) ||
    p.mrn.toLowerCase().includes(search.toLowerCase())
  );

  const columns = [
    { key: 'mrn', header: 'MRN', width: '120px' },
    { key: 'fullName', header: 'Name', render: (p: PatientResponse) => (
      <span className={styles.nameCell}>{p.fullName}</span>
    )},
    { key: 'dateOfBirth', header: 'Date of Birth', width: '130px' },
    { key: 'gender', header: 'Gender', width: '100px' },
    { key: 'phone', header: 'Phone', render: (p: PatientResponse) => p.contactInfo?.phone || '—' },
    { key: 'active', header: 'Status', width: '100px', render: (p: PatientResponse) => (
      <Badge variant={statusVariant(p.active ? 'ACTIVE' : 'INACTIVE')} size="sm" dot>
        {p.active ? 'Active' : 'Inactive'}
      </Badge>
    )},
    { key: 'allergies', header: 'Allergies', render: (p: PatientResponse) => (
      <span className={styles.allergies}>
        {p.allergies && p.allergies.length > 0 ? p.allergies.join(', ') : '—'}
      </span>
    )},
  ];

  return (
    <>
      <Header title="Patients" subtitle={`${patients.length} registered patients`} />
      <div className={styles.content}>
        <div className={styles.toolbar}>
          <div className={styles.searchWrap}>
            <Search size={16} className={styles.searchIcon} />
            <input
              className={styles.search}
              placeholder="Search by name or MRN..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <Button variant="primary" icon={<UserPlus size={16} />}>
            Register Patient
          </Button>
        </div>

        {loading ? (
          <div className={styles.loading}>Loading patients...</div>
        ) : (
          <Table
            columns={columns}
            data={filtered}
            onRowClick={(p) => router.push(`/patients/${p.id}`)}
            emptyMessage="No patients found"
          />
        )}
      </div>
    </>
  );
}
