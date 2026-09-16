'use client';

import React, { useEffect, useState } from 'react';
import styles from './clinical.module.css';
import { Header } from '@/components/layout/Header';
import { Table } from '@/components/ui/Table';
import { Badge, statusVariant } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Modal } from '@/components/ui/Modal';
import { getEncounters, EncounterResponse } from '@/lib/api/clinical';
import { getPatientName } from '@/lib/mock-data';
import { format } from 'date-fns';
import { Plus, FileText } from 'lucide-react';

export default function ClinicalPage() {
  const [encounters, setEncounters] = useState<EncounterResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedEncounter, setSelectedEncounter] = useState<EncounterResponse | null>(null);

  useEffect(() => { loadEncounters(); }, []);

  const loadEncounters = async () => {
    setLoading(true);
    try {
      const data = await getEncounters();
      setEncounters(data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const columns = [
    { key: 'patient', header: 'Patient', render: (e: EncounterResponse) => (
      <span className={styles.nameCell}>{getPatientName(e.patientId)}</span>
    )},
    { key: 'status', header: 'Status', width: '120px', render: (e: EncounterResponse) => (
      <Badge variant={statusVariant(e.status)} size="sm" dot>{e.status}</Badge>
    )},
    { key: 'startDate', header: 'Start Date', render: (e: EncounterResponse) => format(new Date(e.startDate), 'MMM d, yyyy h:mm a') },
    { key: 'endDate', header: 'End Date', render: (e: EncounterResponse) => e.endDate ? format(new Date(e.endDate), 'MMM d, yyyy h:mm a') : '—' },
    { key: 'notes', header: 'Notes', width: '80px', render: (e: EncounterResponse) => (
      <Badge variant="default" size="sm">{e.clinicalNotes.length}</Badge>
    )},
    { key: 'diagnoses', header: 'Diagnoses', render: (e: EncounterResponse) => (
      <span className={styles.diagnoses}>
        {e.diagnoses.length > 0 ? e.diagnoses.map(d => d.code).join(', ') : '—'}
      </span>
    )},
    { key: 'actions', header: '', width: '100px', render: (e: EncounterResponse) => (
      <Button variant="ghost" size="sm" onClick={(ev) => { ev.stopPropagation(); setSelectedEncounter(e); }} icon={<FileText size={14} />}>
        Notes
      </Button>
    )},
  ];

  return (
    <>
      <Header title="Clinical Encounters" subtitle={`${encounters.length} encounters`} />
      <div className={styles.content}>
        <div className={styles.toolbar}>
          <div />
          <Button variant="primary" icon={<Plus size={16} />}>
            New Encounter
          </Button>
        </div>

        {loading ? (
          <div className={styles.loading}>Loading encounters...</div>
        ) : (
          <Table
            columns={columns}
            data={encounters}
            onRowClick={(e) => setSelectedEncounter(e)}
            emptyMessage="No encounters found"
          />
        )}
      </div>

      {/* Notes Modal */}
      <Modal
        open={!!selectedEncounter}
        onClose={() => setSelectedEncounter(null)}
        title={`Encounter Notes — ${selectedEncounter ? getPatientName(selectedEncounter.patientId) : ''}`}
        size="lg"
      >
        {selectedEncounter && (
          <div className={styles.notesModal}>
            <div className={styles.encounterInfo}>
              <Badge variant={statusVariant(selectedEncounter.status)} dot>{selectedEncounter.status}</Badge>
              <span className={styles.encounterDate}>
                {format(new Date(selectedEncounter.startDate), 'MMM d, yyyy h:mm a')}
              </span>
            </div>

            {selectedEncounter.diagnoses.length > 0 && (
              <div className={styles.diagnosesSection}>
                <h4 className={styles.subsectionTitle}>Diagnoses</h4>
                {selectedEncounter.diagnoses.map((d, i) => (
                  <div key={i} className={styles.diagnosisItem}>
                    <Badge variant="info" size="sm">{d.code}</Badge>
                    <span>{d.description}</span>
                  </div>
                ))}
              </div>
            )}

            <div className={styles.notesTimeline}>
              <h4 className={styles.subsectionTitle}>Clinical Notes</h4>
              {selectedEncounter.clinicalNotes.length === 0 ? (
                <p className={styles.noNotes}>No notes recorded yet</p>
              ) : (
                selectedEncounter.clinicalNotes.map((note) => (
                  <Card key={note.id} variant="outlined" padding="sm" className={styles.noteCard}>
                    <div className={styles.noteHeader}>
                      <Badge variant="default" size="sm">{note.noteType.replace('_', ' ')}</Badge>
                      <span className={styles.noteDate}>{format(new Date(note.createdAt), 'MMM d, h:mm a')}</span>
                    </div>
                    <p className={styles.noteContent}>{note.content}</p>
                    <span className={styles.noteAuthor}>Author: {note.authorId}</span>
                  </Card>
                ))
              )}
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}
