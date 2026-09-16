'use client';

import React, { useEffect, useState } from 'react';
import styles from './audit.module.css';
import { Header } from '@/components/layout/Header';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Card } from '@/components/ui/Card';
import { getAuditEntries, AuditEntryResponse } from '@/lib/api/audit';
import { format } from 'date-fns';
import { ChevronDown, ChevronRight } from 'lucide-react';

export default function AuditPage() {
  const [entries, setEntries] = useState<AuditEntryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [expandedId, setExpandedId] = useState<string | null>(null);

  useEffect(() => { loadEntries(); }, []);

  const loadEntries = async () => {
    setLoading(true);
    try { setEntries(await getAuditEntries()); }
    catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const categories = ['ALL', ...Array.from(new Set(entries.map(e => e.category)))];
  const filtered = categoryFilter === 'ALL' ? entries : entries.filter(e => e.category === categoryFilter);
  const sorted = [...filtered].sort((a, b) => new Date(b.eventTimestamp).getTime() - new Date(a.eventTimestamp).getTime());

  return (
    <>
      <Header title="Audit Log" subtitle="Immutable compliance trail — HIPAA §164.530(j)" />
      <div className={styles.content}>
        {/* Filters */}
        <div className={styles.filters}>
          {categories.map(c => (
            <button
              key={c}
              className={`${styles.filterBtn} ${categoryFilter === c ? styles.filterActive : ''}`}
              onClick={() => setCategoryFilter(c)}
            >
              {c === 'ALL' ? 'All' : c.charAt(0) + c.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        {loading ? (
          <div className={styles.loading}>Loading audit log...</div>
        ) : (
          <div className={styles.logList}>
            {sorted.map((entry) => (
              <Card
                key={entry.id}
                className={styles.logEntry}
                padding="none"
                hover
                onClick={() => setExpandedId(expandedId === entry.id ? null : entry.id)}
              >
                <div className={styles.logRow}>
                  <span className={styles.expandIcon}>
                    {expandedId === entry.id ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                  </span>
                  <span className={styles.timestamp}>
                    {format(new Date(entry.eventTimestamp), 'MMM d, h:mm:ss a')}
                  </span>
                  <span className={styles.eventType}>{entry.eventType}</span>
                  <Badge variant={statusVariant(entry.category)} size="sm">{entry.category}</Badge>
                  <span className={styles.source}>{entry.sourceService}</span>
                  <span className={styles.actor}>{entry.actorId || '—'}</span>
                  <span className={styles.target}>{entry.targetId || '—'}</span>
                </div>
                {expandedId === entry.id && (
                  <div className={styles.expandedContent}>
                    <div className={styles.payloadGrid}>
                      <div className={styles.payloadItem}>
                        <span className={styles.payloadLabel}>Event Type</span>
                        <span>{entry.eventType}</span>
                      </div>
                      <div className={styles.payloadItem}>
                        <span className={styles.payloadLabel}>Target Type</span>
                        <span>{entry.targetType}</span>
                      </div>
                      <div className={styles.payloadItem}>
                        <span className={styles.payloadLabel}>Received At</span>
                        <span>{format(new Date(entry.receivedAt), 'MMM d, yyyy h:mm:ss a')}</span>
                      </div>
                    </div>
                    <div className={styles.payloadSection}>
                      <span className={styles.payloadLabel}>Payload</span>
                      <pre className={styles.payload}>{JSON.stringify(entry.payload, null, 2)}</pre>
                    </div>
                  </div>
                )}
              </Card>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
