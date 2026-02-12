import { Entity, PrimaryColumn, Column, CreateDateColumn, UpdateDateColumn, OneToMany } from 'typeorm';
import { ClinicalNoteEntity } from './ClinicalNoteEntity';

@Entity('encounters')
export class EncounterEntity {
    @PrimaryColumn('uuid')
    id: string;

    @Column({ name: 'patient_id', type: 'uuid' })
    patientId: string;

    @Column({ default: 'PLANNED' })
    status: string;

    @Column({ name: 'start_date', type: 'timestamp' })
    startDate: Date;

    @Column({ name: 'end_date', type: 'timestamp', nullable: true })
    endDate: Date | null;

    @Column({ type: 'jsonb', default: '[]' })
    diagnoses: Array<{ code: string; description: string }>;

    @OneToMany(() => ClinicalNoteEntity, (note) => note.encounter, { cascade: true, eager: true })
    clinicalNotes: ClinicalNoteEntity[];

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;
}
