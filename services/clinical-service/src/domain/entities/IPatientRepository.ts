import { Patient } from '@domain/entities/Patient';

export interface IPatientRepository {
    save(patient: Patient): Promise<Patient>;
    findById(id: string): Promise<Patient | null>;
    findByMrn(mrn: string): Promise<Patient | null>;
    findAll(): Promise<Patient[]>;
}
