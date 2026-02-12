import { Entity, PrimaryColumn, Column, CreateDateColumn, UpdateDateColumn, OneToMany } from 'typeorm';
import { ClinicalNoteEntity } from './ClinicalNoteEntity';

@Entity('patients')
export class PatientEntity {
    @PrimaryColumn('uuid')
    id: string;

    @Column({ unique: true })
    mrn: string;

    @Column({ name: 'first_name' })
    firstName: string;

    @Column({ name: 'last_name' })
    lastName: string;

    @Column({ name: 'date_of_birth', type: 'date' })
    dateOfBirth: Date;

    @Column()
    gender: string;

    @Column({ type: 'jsonb' })
    contactInfo: {
        phone: string;
        email: string;
        address: string;
    };

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;
}
