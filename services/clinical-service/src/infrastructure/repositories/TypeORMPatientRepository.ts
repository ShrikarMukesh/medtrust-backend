import { Repository } from 'typeorm';
import { Patient, Gender } from '@domain/entities/Patient';
import { IPatientRepository } from '@domain/entities/IPatientRepository';
import { PatientEntity } from '@infrastructure/persistence/PatientEntity';
import { AppDataSource } from '@infrastructure/persistence/database';

export class TypeORMPatientRepository implements IPatientRepository {
    private readonly repo: Repository<PatientEntity>;

    constructor() {
        this.repo = AppDataSource.getRepository(PatientEntity);
    }

    async save(patient: Patient): Promise<Patient> {
        const entity = this.toEntity(patient);
        const saved = await this.repo.save(entity);
        return this.toDomain(saved);
    }

    async findById(id: string): Promise<Patient | null> {
        const entity = await this.repo.findOneBy({ id });
        return entity ? this.toDomain(entity) : null;
    }

    async findByMrn(mrn: string): Promise<Patient | null> {
        const entity = await this.repo.findOneBy({ mrn });
        return entity ? this.toDomain(entity) : null;
    }

    async findAll(): Promise<Patient[]> {
        const entities = await this.repo.find();
        return entities.map((e) => this.toDomain(e));
    }

    private toEntity(patient: Patient): PatientEntity {
        const entity = new PatientEntity();
        entity.id = patient.id;
        entity.mrn = patient.mrn;
        entity.firstName = patient.firstName;
        entity.lastName = patient.lastName;
        entity.dateOfBirth = patient.dateOfBirth;
        entity.gender = patient.gender;
        entity.contactInfo = patient.contactInfo;
        return entity;
    }

    private toDomain(entity: PatientEntity): Patient {
        return Patient.create({
            id: entity.id,
            mrn: entity.mrn,
            firstName: entity.firstName,
            lastName: entity.lastName,
            dateOfBirth: entity.dateOfBirth,
            gender: entity.gender as Gender,
            contactInfo: entity.contactInfo,
            createdAt: entity.createdAt,
            updatedAt: entity.updatedAt,
        });
    }
}
