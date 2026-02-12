import { Encounter } from './Encounter';

export interface IEncounterRepository {
    save(encounter: Encounter): Promise<Encounter>;
    findById(id: string): Promise<Encounter | null>;
    findByPatientId(patientId: string): Promise<Encounter[]>;
    findAll(): Promise<Encounter[]>;
}
