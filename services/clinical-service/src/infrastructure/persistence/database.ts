import { DataSource } from 'typeorm';
import { PatientEntity } from './PatientEntity';
import { EncounterEntity } from './EncounterEntity';
import { ClinicalNoteEntity } from './ClinicalNoteEntity';

export const AppDataSource = new DataSource({
    type: 'postgres',
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432', 10),
    username: process.env.DB_USERNAME || 'medtrust',
    password: process.env.DB_PASSWORD || 'changeme',
    database: process.env.DB_DATABASE || 'clinical_service',
    entities: [PatientEntity, EncounterEntity, ClinicalNoteEntity],
    synchronize: process.env.NODE_ENV !== 'production',
    logging: process.env.NODE_ENV !== 'production',
});
