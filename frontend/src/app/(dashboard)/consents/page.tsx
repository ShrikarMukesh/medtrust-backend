'use client';

import React, { useEffect, useState } from 'react';
import styles from './consents.module.css';
import { Header } from '@/components/layout/Header';
import { Table } from '@/components/ui/Table';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { getConsents, revokeConsent, ConsentResponse } from '@/lib/api/consents';
import { getPatientName, getProviderName } from '@/lib/mock-data';
import { format } from 'date-fns';
import { Plus, ShieldOff } from 'lucide-react';

export default function ConsentsPage() {
  const [consents, setConsents] = useState<ConsentResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadConsents(); }, []);

  const loadConsents = async () => {
    setLoading(true);
    try { setConsents(await getConsents()); }
    catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleRevoke = async (id: string) => {
    try {
      await revokeConsent(id);
      await loadConsents();
    } catch (err) { console.error(err); }
  };

  const columns = [
    { key: 'patient', header: 'Patient', render: (c: ConsentResponse) => (
      <span className={styles.nameCell}>{getPatientName(c.patientId)}</span>
    )},
    { key: 'grantedTo', header: 'Granted To', render: (c: ConsentResponse) => getProviderName(c.grantedToUserId) },
    { key: 'scope', header: 'Scope', render: (c: ConsentResponse) => (
      <Badge variant="info" size="sm">{c.scope.replace('_', ' ')}</Badge>
    )},
    { key: 'status', header: 'Status', width: '110px', render: (c: ConsentResponse) => (
      <Badge variant={statusVariant(c.status)} size="sm" dot>{c.status}</Badge>
    )},
    { key: 'grantedAt', header: 'Granted At', render: (c: ConsentResponse) => c.grantedAt ? format(new Date(c.grantedAt), 'MMM d, yyyy') : '—' },
    { key: 'expiresAt', header: 'Expires At', render: (c: ConsentResponse) => c.expiresAt ? format(new Date(c.expiresAt), 'MMM d, yyyy') : '—' },
    { key: 'actions', header: '', width: '100px', render: (c: ConsentResponse) => (
      c.status === 'GRANTED' ? (
        <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); handleRevoke(c.id); }} icon={<ShieldOff size={14} />}>
          Revoke
        </Button>
      ) : null
    )},
  ];

  return (
    <>
      <Header title="Consent Management" subtitle="PHI access consent tracking" />
      <div className={styles.content}>
        <div className={styles.toolbar}>
          <div />
          <Button variant="primary" icon={<Plus size={16} />}>
            Grant Consent
          </Button>
        </div>

        {loading ? (
          <div className={styles.loading}>Loading consents...</div>
        ) : (
          <Table
            columns={columns}
            data={consents}
            emptyMessage="No consents found"
          />
        )}
      </div>
    </>
  );
}
