import { Entity, PrimaryColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { EncounterEntity } from './EncounterEntity';

@Entity('clinical_notes')
export class ClinicalNoteEntity {
    @PrimaryColumn('uuid')
    id: string;

    @Column({ name: 'encounter_id', type: 'uuid' })
    encounterId: string;

    @Column({ type: 'text' })
    content: string;

    @Column({ name: 'author_id' })
    authorId: string;

    @Column({ name: 'note_type' })
    noteType: string;

    @ManyToOne(() => EncounterEntity, (encounter) => encounter.clinicalNotes)
    @JoinColumn({ name: 'encounter_id' })
    encounter: EncounterEntity;

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;
}
